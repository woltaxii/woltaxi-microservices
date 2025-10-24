package com.woltaxi.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Security Context Repository for WOLTAXI API Gateway
 * 
 * Handles security context creation and JWT token extraction
 * 
 * @author WOLTAXI Development Team
 * @version 2.0.0
 * @since 2024
 */
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    @Autowired
    private AuthenticationManager authenticationManager;

    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // We don't save security context in stateless JWT authentication
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            String token = extractToken(exchange);
            if (token != null) {
                Authentication auth = new UsernamePasswordAuthenticationToken(token, token);
                return authenticationManager.authenticate(auth)
                    .map(SecurityContextImpl::new);
            }
            return Mono.<SecurityContext>empty();
        })
        .flatMap(mono -> mono)
        .onErrorResume(ex -> {
            // Log authentication errors for monitoring
            System.err.println("Authentication error: " + ex.getMessage());
            return Mono.empty();
        });
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
            .getHeaders()
            .getFirst(HEADER_NAME);
        
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        
        // Also check for token in query parameter (for WebSocket connections)
        String tokenParam = exchange.getRequest()
            .getQueryParams()
            .getFirst("token");
            
        return tokenParam;
    }
}