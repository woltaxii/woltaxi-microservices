package com.woltaxi.marketing;

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
 * WOLTAXI Global Marketing & Advertisement Service
 * 
 * Küresel Pazarlama ve Reklam Yönetim Sistemi
 * - Çok ülkeli reklam kampanya yönetimi
 * - Multi-platform API entegrasyonları (Facebook, Google, TikTok, Twitter)
 * - AI destekli optimizasyon ve hedefleme
 * - Gerçek zamanlı performans analizi
 * - Otomatik yerelleştirme ve çeviri
 * - Akıllı bütçe dağıtımı ve optimizasyonu
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
public class MarketingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketingServiceApplication.class, args);
    }
}