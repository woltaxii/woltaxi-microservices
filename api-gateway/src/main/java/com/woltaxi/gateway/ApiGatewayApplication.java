package com.woltaxi.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * WOLTAXI API Gateway Application
 * 
 * Main entry point for the WOLTAXI Enterprise API Gateway Service
 * 
 * Features:
 * - Centralized routing for all microservices
 * - JWT-based authentication and authorization
 * - Rate limiting and circuit breakers
 * - CORS handling
 * - Request/Response logging and monitoring
 * - Service discovery integration
 * 
 * @author WOLTAXI Development Team
 * @version 2.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("🌐 WOLTAXI API Gateway started successfully!");
        System.out.println("📡 Service Discovery: Enabled");
        System.out.println("🔐 JWT Authentication: Enabled");
        System.out.println("🚦 Rate Limiting: Enabled");
        System.out.println("🔄 Circuit Breakers: Enabled");
        System.out.println("📊 Monitoring: Enabled");
    }

    /**
     * Additional CORS configuration for global handling
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Collections.singletonList("*"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Collections.singletonList("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    /**
     * Custom route locator for programmatic route configuration
     * This complements the YAML-based route configuration
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Health check aggregation route
            .route("health-check", r -> r.path("/health/**")
                .filters(f -> f.stripPrefix(1))
                .uri("http://localhost:8761")) // Eureka server health
            
            // WebSocket routes for real-time features
            .route("websocket-rides", r -> r.path("/ws/rides/**")
                .and().header("Upgrade", "websocket")
                .uri("lb://ride-service"))
            
            .route("websocket-location", r -> r.path("/ws/location/**")
                .and().header("Upgrade", "websocket")
                .uri("lb://driver-service"))
                
            // File upload routes with size limits
            .route("file-upload", r -> r.path("/api/v1/files/**")
                .filters(f -> f.requestSize(50000000L)) // 50MB limit
                .uri("lb://user-service"))
                
            .build();
    }
}