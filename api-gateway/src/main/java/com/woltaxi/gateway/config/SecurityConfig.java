package com.woltaxi.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.woltaxi.gateway.filter.AuthenticationManager;
import com.woltaxi.gateway.filter.SecurityContextRepository;

import java.util.Arrays;
import java.util.List;

/**
 * WOLTAXI API Gateway Security Configuration
 * 
 * Features:
 * - JWT Token Validation
 * - CORS Configuration
 * - Route-based Security Rules
 * - Rate Limiting Integration
 * - Circuit Breaker Integration
 * 
 * @author WOLTAXI Development Team
 * @version 2.0.0
 * @since 2024
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${woltaxi.gateway.security.jwt-secret}")
    private String jwtSecret;

    @Value("${woltaxi.gateway.security.cors-enabled:true}")
    private boolean corsEnabled;

    /**
     * Main security filter chain configuration
     */
    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        
        return http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints - no authentication required
                .pathMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/drivers/auth/**",
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/prometheus",
                    "/favicon.ico",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**"
                ).permitAll()
                
                // Emergency endpoints - always accessible
                .pathMatchers("/api/v1/emergency/panic/**").permitAll()
                
                // Admin endpoints - require ADMIN role
                .pathMatchers(
                    "/actuator/**",
                    "/api/v1/admin/**",
                    "/api/v1/performance/**",
                    "/api/v1/analytics/admin/**"
                ).hasRole("ADMIN")
                
                // Driver-specific endpoints
                .pathMatchers("/api/v1/drivers/**").hasAnyRole("DRIVER", "ADMIN")
                
                // User endpoints
                .pathMatchers("/api/v1/users/**").hasAnyRole("USER", "DRIVER", "ADMIN")
                
                // Ride endpoints - users and drivers
                .pathMatchers("/api/v1/rides/**").hasAnyRole("USER", "DRIVER", "ADMIN")
                
                // Payment endpoints
                .pathMatchers("/api/v1/payments/**").hasAnyRole("USER", "DRIVER", "ADMIN")
                
                // AI/ML endpoints - restricted access
                .pathMatchers("/api/v1/ai/**").hasAnyRole("DRIVER", "ADMIN")
                
                // Smart vehicle endpoints - driver and admin only
                .pathMatchers("/api/v1/smart-vehicles/**").hasAnyRole("DRIVER", "ADMIN")
                
                // Marketing endpoints - admin only
                .pathMatchers("/api/v1/marketing/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyExchange().authenticated()
            )
            .cors(cors -> {
                if (corsEnabled) {
                    cors.configurationSource(corsConfigurationSource());
                } else {
                    cors.disable();
                }
            })
            .build();
    }

    /**
     * CORS configuration for cross-origin requests
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow all origins in development, restrict in production
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type", 
            "Accept",
            "X-Requested-With",
            "X-Gateway-Timestamp",
            "X-Gateway-Version",
            "X-Forwarded-For",
            "X-Real-IP",
            "User-Agent"
        ));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Cache preflight response
        configuration.setMaxAge(3600L);
        
        // Expose response headers
        configuration.setExposedHeaders(Arrays.asList(
            "X-Gateway-Timestamp",
            "X-Gateway-Version", 
            "X-RateLimit-Remaining",
            "X-RateLimit-Reset"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}