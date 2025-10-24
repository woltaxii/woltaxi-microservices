package com.woltaxi.globalperformance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WOLTAXI Global Performance Analytics & Rewards System
 * 
 * Bu mikroservis şu özellikleri sağlar:
 * - Ülkeler arası performans analizi ve karşılaştırması
 * - Kapasite optimizasyonu ve verimlilik çalışmaları
 * - Kar-zarar analizi ve finansal performans takibi
 * - En iyi ülke ve sürücü belirleme algoritmaları
 * - Küresel ödüllendirme sistemi yönetimi
 * - Gerçek zamanlı performans metrikleri
 * - AI destekli performans öngörüleri
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableCaching
@EnableKafka
@EnableAsync
@EnableScheduling
public class GlobalPerformanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlobalPerformanceServiceApplication.class, args);
    }
}