package com.woltaxi.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

/**
 * WOLTAXI Driver Analytics & Performance Service
 * 
 * Sürücü Performans ve Kar-Zarar Analiz Sistemi
 * - Günlük, aylık, yıllık performans takibi
 * - Kar-zarar analizi ve mali raporlama
 * - Sürücü hedef belirleme ve takibi
 * - Gider yönetimi ve vergi hesaplamaları
 * - Performans karşılaştırmaları ve sıralamalar
 * - Otomatik raporlama ve dashboard
 * 
 * @author WOLTAXI Development Team
 * @version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableCaching
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}