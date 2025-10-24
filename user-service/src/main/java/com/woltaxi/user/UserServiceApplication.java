package com.woltaxi.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * WOLTAXI User Service Application
 * 
 * Main entry point for the WOLTAXI User Management Service
 * 
 * Features:
 * - User registration and authentication
 * - Profile management
 * - JWT token generation
 * - Email verification
 * - SMS verification
 * - Password management
 * - User preferences
 * 
 * @author WOLTAXI Development Team
 * @version 2.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("👥 WOLTAXI User Service started successfully!");
        System.out.println("🔐 JWT Authentication: Enabled");
        System.out.println("📧 Email Verification: Enabled");
        System.out.println("📱 SMS Verification: Enabled");
        System.out.println("🔄 Redis Caching: Enabled");
        System.out.println("📊 Monitoring: Enabled");
    }
}
