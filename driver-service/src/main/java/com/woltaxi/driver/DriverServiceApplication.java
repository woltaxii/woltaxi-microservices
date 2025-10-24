package com.woltaxi.driver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * WOLTAXI Driver Service Application
 * 
 * Bu mikroservis sürücü kayıt, yönetim, konum takibi ve performans 
 * değerlendirmesi işlemlerini yönetir.
 * 
 * Özellikler:
 * - Sürücü kayıt ve onay süreci
 * - Gerçek zamanlı konum takibi
 * - Performans ve rating sistemi
 * - Kazanç ve komisyon hesaplaması
 * - Araç yönetimi
 * 
 * @author WOLTAXI Development Team
 * @version 2.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class DriverServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriverServiceApplication.class, args);
    }
}