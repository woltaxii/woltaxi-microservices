package com.woltaxi.payment;

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
 * WOLTAXI Advanced Payment Hub Service
 * 
 * Multi-Platform Payment Processing Hub with:
 * - Global Payment Provider Integration (Stripe, PayPal, Square, Adyen, Braintree)
 * - Turkish Payment Support (Iyzico, PayTR)
 * - Apple Pay, Google Pay, Samsung Pay Integration
 * - Multi-Currency Wallet System
 * - Automatic Currency Conversion
 * - Real-time Payment Processing
 * - Subscription Payment Management
 * - Fraud Detection & Prevention
 * - PCI DSS Compliance
 * - Advanced Analytics & Reporting
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication(scanBasePackages = "com.woltaxi.payment")
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaRepositories
@EnableJpaAuditing
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class PaymentHubServiceApplication {

    public static void main(String[] args) {
        // Set system properties for payment security
        System.setProperty("spring.jpa.show-sql", "false");
        System.setProperty("spring.jpa.properties.hibernate.format_sql", "false");
        
        // Enhanced security for payment processing
        System.setProperty("server.ssl.enabled", "true");
        System.setProperty("management.security.enabled", "true");
        
        SpringApplication.run(PaymentHubServiceApplication.class, args);
    }
}