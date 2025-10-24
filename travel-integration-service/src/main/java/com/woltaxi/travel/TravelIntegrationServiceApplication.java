package com.woltaxi.travel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * WOLTAXI Travel Integration Service
 * 
 * Comprehensive Tourism & Travel Integration Platform with:
 * - Airline API Integrations (Amadeus, Turkish Airlines, Pegasus)
 * - Bus Company Direct Connections (Metro Turizm, Kamil Koç, Pamukkale, Varan)
 * - Hotel Booking Systems (Booking.com, Expedia, Hotels.com)
 * - Car Rental Services (Avis, Hertz)
 * - Commission-based Monthly Payment Model
 * - Real-time Ticket Confirmation & Sales
 * - Multi-currency Support & Payment Integration
 * - PDF Ticket Generation with QR Codes
 * - SMS & Email Notifications
 * - Advanced Analytics & Reporting
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication(scanBasePackages = "com.woltaxi.travel")
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaRepositories
@EnableJpaAuditing
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class TravelIntegrationServiceApplication {

    public static void main(String[] args) {
        // Set system properties for travel service security
        System.setProperty("spring.jpa.show-sql", "false");
        System.setProperty("spring.jpa.properties.hibernate.format_sql", "false");
        
        // Enhanced security for travel data processing
        System.setProperty("server.ssl.enabled", "true");
        System.setProperty("management.security.enabled", "true");
        
        // Travel-specific configurations
        System.setProperty("travel.api.timeout", "30000");
        System.setProperty("travel.booking.confirmation.timeout", "60000");
        
        SpringApplication.run(TravelIntegrationServiceApplication.class, args);
    }
}