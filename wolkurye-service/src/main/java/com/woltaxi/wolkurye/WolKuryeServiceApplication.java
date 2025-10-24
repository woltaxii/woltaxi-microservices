package com.woltaxi.wolkurye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * WolKurye Motor Kurye & Paket Teslimat Mikroservisi
 * 
 * Bu mikroservis, WOLTAXI ekosisteminin bir parçası olarak motor kurye hizmetleri sağlar.
 * Restoran, market, pastane, çiçekçi gibi işletmelerden paket teslimatı yapar.
 * Aynı sürücü ağını kullanarak hem yolcu taşımacılığı hem de paket teslimatı hizmeti verir.
 * 
 * Özellikler:
 * - Gerçek zamanlı paket takibi
 * - Çoklu paket teslimatı
 * - Fotoğraflı teslimat onayı
 * - Otomatik QR kod oluşturma
 * - E-ticaret entegrasyonları
 * - Komisyon yönetimi
 * - Push notification & SMS
 * - Coğrafi konum tabanlı atama
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableJpaAuditing
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class WolKuryeServiceApplication {

    /**
     * Ana uygulama giriş noktası
     */
    public static void main(String[] args) {
        SpringApplication.run(WolKuryeServiceApplication.class, args);
    }
}