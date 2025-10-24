package com.woltaxi.smartvehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * WOLTAXI Smart Vehicle Service - Akıllı Araç Entegrasyon Servisi
 * 
 * Gelecekteki akıllı araçlar için:
 * - Otonom sürüş algoritmaları (L0-L5)
 * - AI ve makine öğrenmesi entegrasyonu
 * - Gerçek zamanlı sensör veri analizi
 * - V2X (Vehicle-to-Everything) iletişim
 * - Predictive maintenance
 * - IoT ve cloud connectivity
 * - Advanced safety features
 * - Eco-routing ve energy optimization
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 * @since 2025
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class SmartVehicleServiceApplication {
    
    public static void main(String[] args) {
        System.out.println("🚗🤖 WOLTAXI Smart Vehicle Service Starting...");
        System.out.println("🔮 Future-Ready Smart Vehicle Integration");
        System.out.println("🧠 AI-Powered Autonomous Driving");
        System.out.println("🌐 IoT & V2X Communication");
        System.out.println("⚡ Real-time Sensor Analytics");
        System.out.println("🔧 Predictive Maintenance");
        System.out.println("🌱 Eco-friendly Routing");
        System.out.println("🛡️ Advanced Safety Systems");
        
        SpringApplication.run(SmartVehicleServiceApplication.class, args);
        
        System.out.println("✅ WOLTAXI Smart Vehicle Service Successfully Started!");
        System.out.println("🚀 Ready for Smart Vehicle Integration on Port 8095");
        System.out.println("🔗 Eureka Discovery: http://localhost:8761/eureka/");
        System.out.println("📊 H2 Console: http://localhost:8095/h2-console");
        System.out.println("🔧 Health Check: http://localhost:8095/actuator/health");
        System.out.println("📈 Metrics: http://localhost:8095/actuator/metrics");
        System.out.println("🎯 Smart Vehicle API: http://localhost:8095/api/v1/smart-vehicles");
    }
}