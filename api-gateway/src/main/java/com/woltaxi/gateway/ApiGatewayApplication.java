package com.woltaxi.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * WOLTAXI API Gateway Application
 * 
 * Bu sınıf WOLTAXI mikroservis mimarisinin merkezi giriş noktasıdır.
 * Tüm istemci istekleri bu gateway üzerinden ilgili servislere yönlendirilir.
 * 
 * @author WOLTAXI Development Team
 * @version 2.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Gateway Route Yapılandırması
     * 
     * Bu metod tüm mikroservislere yönlendirme kurallarını tanımlar.
     * Her servis için özel yollar ve filtreler uygulanır.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User Service Routes
            .route("user-service", r -> r.path("/api/users/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway-Service", "user-service")
                    .circuitBreaker(c -> c
                        .setName("user-service-cb")
                        .setFallbackUri("forward:/fallback/user")))
                .uri("lb://user-service"))
                
            // Ride Service Routes
            .route("ride-service", r -> r.path("/api/rides/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway-Service", "ride-service")
                    .circuitBreaker(c -> c
                        .setName("ride-service-cb")
                        .setFallbackUri("forward:/fallback/ride")))
                .uri("lb://ride-service"))
                
            // Driver Service Routes
            .route("driver-service", r -> r.path("/api/drivers/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway-Service", "driver-service")
                    .circuitBreaker(c -> c
                        .setName("driver-service-cb")
                        .setFallbackUri("forward:/fallback/driver")))
                .uri("lb://driver-service"))
                
            // Payment Service Routes
            .route("payment-service", r -> r.path("/api/payments/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway-Service", "payment-service")
                    .circuitBreaker(c -> c
                        .setName("payment-service-cb")
                        .setFallbackUri("forward:/fallback/payment")))
                .uri("lb://payment-service"))
                
            // Location Service Routes
            .route("location-service", r -> r.path("/api/locations/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway-Service", "location-service")
                    .circuitBreaker(c -> c
                        .setName("location-service-cb")
                        .setFallbackUri("forward:/fallback/location")))
                .uri("lb://location-service"))
                
            // AI Matching Service Routes
            .route("ai-matching-service", r -> r.path("/api/matching/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway-Service", "ai-matching-service")
                    .circuitBreaker(c -> c
                        .setName("ai-matching-service-cb")
                        .setFallbackUri("forward:/fallback/matching")))
                .uri("lb://ai-matching-service"))
                
            // Notification Service Routes
            .route("notification-service", r -> r.path("/api/notifications/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway-Service", "notification-service")
                    .circuitBreaker(c -> c
                        .setName("notification-service-cb")
                        .setFallbackUri("forward:/fallback/notification")))
                .uri("lb://notification-service"))
                
            // Admin Panel Routes
            .route("admin-service", r -> r.path("/api/admin/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway-Service", "admin-service")
                    .addRequestHeader("X-Require-Admin", "true"))
                .uri("lb://admin-service"))
                
            .build();
    }
}