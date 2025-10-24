package com.woltaxi.aiml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.cache.annotation.EnableCaching;

/**
 * WOLTAXI AI/ML Core Service Application
 * 
 * Bu mikroservis, WOLTAXI ekosisteminin merkezi yapay zeka ve makine öğrenmesi altyapısını sağlar.
 * Tüm sistemde kullanılan AI/ML algoritmalarını, modellerini ve akıllı automasyonu yönetir.
 * 
 * Ana Özellikler:
 * - Makine Öğrenmesi Model Yönetimi (TensorFlow, PyTorch, Scikit-learn)
 * - Doğal Dil İşleme (NLP) - Türkçe, İngilizce, Arapça
 * - Bilgisayarlı Görü (Computer Vision) - Yüz tanıma, nesne tespiti
 * - Tahmine Dayalı Analitik - Talep tahmini, fiyat optimizasyonu
 * - Gerçek Zamanlı Karar Verme - Akıllı rota optimizasyonu
 * - Fraud Detection - Sahtecilik tespiti
 * - Müşteri Segmentasyonu ve Kişiselleştirme
 * - A/B Testing ve Performans Optimizasyonu
 * - Chatbot ve Sanal Asistan
 * - Sesli Komut İşleme
 * - Görüntü ve Video Analizi
 * - Sentiment Analysis - Müşteri memnuniyeti analizi
 * - Churn Prediction - Müşteri kaybı tahmini
 * - Dynamic Pricing - Dinamik fiyatlandırma
 * - Traffic Pattern Analysis - Trafik deseni analizi
 * - Driver Behavior Analysis - Sürücü davranış analizi
 * 
 * Teknoloji Stack:
 * - TensorFlow Java API
 * - DeepLearning4J
 * - OpenCV
 * - WEKA
 * - SMILE
 * - Stanford CoreNLP
 * - OpenAI GPT Integration
 * - Google Cloud AI
 * - Azure Cognitive Services
 * - Hugging Face Transformers
 * 
 * @author WOLTAXI AI/ML Team
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
@EnableCaching
public class AiMlServiceApplication {

    /**
     * Ana uygulama giriş noktası
     * 
     * Bu servis başlatıldığında:
     * 1. ML modellerini yükler ve cache'e alır
     * 2. TensorFlow ve diğer AI frameworklerini başlatır
     * 3. Real-time analytics pipeline'ını aktive eder
     * 4. Scheduled batch processing görevlerini başlatır
     * 5. Model monitoring ve drift detection'ı devreye alır
     */
    public static void main(String[] args) {
        // JVM optimizasyonları AI/ML işleri için
        System.setProperty("java.awt.headless", "true");
        System.setProperty("org.bytedeco.javacpp.maxbytes", "8G");
        System.setProperty("org.bytedeco.javacpp.maxphysicalbytes", "12G");
        
        SpringApplication.run(AiMlServiceApplication.class, args);
    }
}