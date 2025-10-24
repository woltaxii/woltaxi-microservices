package com.woltaxi.emergency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WOLTAXI Emergency and Safety System
 * 
 * Bu mikroservis şu kritik güvenlik özelliklerini sağlar:
 * - SOS/Panic Button sistemi
 * - Acil durum otomatik bildirimi
 * - Gerçek zamanlı konum paylaşımı
 * - Aile ve arkadaş bilgilendirme sistemi
 * - Yerel acil servis entegrasyonu
 * - Kadın ve aile güvenlik modu
 * - Araç içi kayıt sistemi
 * - 24/7 acil durum merkezi
 * - Çok dilli acil durum iletişimi
 * - Otomatik polis/ambulans çağırma
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
public class EmergencyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmergencyServiceApplication.class, args);
    }
}