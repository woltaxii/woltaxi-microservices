package com.woltaxi.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WOLTAXI Subscription Service Application
 * 
 * Bu mikroservis sürücü abonelik paketleri, müşteri portföyü yönetimi
 * ve subscription-based ödeme sistemlerini yönetir.
 * 
 * Özellikler:
 * - Aylık/Yıllık sürücü paketleri (Basic, Premium, Gold, Diamond)
 * - Müşteri portföyü ve CRM sistemi
 * - Otomatik ödeme ve yenileme
 * - Pazarlama kampanyaları
 * - Sürücü performans analitiği
 * - Müşteri etkileşim takibi
 * 
 * @author WOLTAXI Development Team
 * @version 2.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableScheduling
public class SubscriptionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionServiceApplication.class, args);
    }
}