package com.woltaxi.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Manager for WOLTAXI API Gateway
 * 
 * Handles JWT token validation and user authentication
 * 
 * @author WOLTAXI Development Team
 * @version 2.0.0
 * @since 2024
 */
@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    @Value("${woltaxi.gateway.security.jwt-secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                
                // Check token expiration
                if (claims.getExpiration().before(new Date())) {
                    throw new RuntimeException("Token expired");
                }
                
                // Extract user information
                String username = claims.getSubject();
                String userId = claims.get("userId", String.class);
                String userType = claims.get("userType", String.class); // USER, DRIVER, ADMIN
                
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                
                List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
                
                // Create authentication token with user details
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                // Add custom details
                auth.setDetails(new WoltaxiUserDetails(userId, userType, roles));
                
                return auth;
                
            } catch (Exception e) {
                throw new RuntimeException("Invalid JWT token: " + e.getMessage());
            }
        })
        .onErrorMap(ex -> new RuntimeException("Authentication failed: " + ex.getMessage()));
    }

    /**
     * Custom user details for WOLTAXI users
     */
    public static class WoltaxiUserDetails {
        private final String userId;
        private final String userType;
        private final List<String> roles;

        public WoltaxiUserDetails(String userId, String userType, List<String> roles) {
            this.userId = userId;
            this.userType = userType;
            this.roles = roles;
        }

        public String getUserId() { return userId; }
        public String getUserType() { return userType; }
        public List<String> getRoles() { return roles; }
    }
}