package com.woltaxi.aiml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Prediction Request Entity
 * 
 * AI/ML modellerinden gelen tahmin isteklerini ve sonuçlarını saklar.
 * Audit trail ve performance monitoring için kullanılır.
 */
@Entity
@Table(name = "prediction_requests", indexes = {
    @Index(name = "idx_prediction_model_id", columnList = "modelId"),
    @Index(name = "idx_prediction_user_id", columnList = "userId"),
    @Index(name = "idx_prediction_date", columnList = "requestDate"),
    @Index(name = "idx_prediction_service", columnList = "serviceName")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long modelId;

    @Column
    private Long userId; // İsteği yapan kullanıcı

    @Column(nullable = false)
    private String serviceName; // WOLTAXI, WOLKURYE, EMERGENCY, PAYMENT, etc.

    @Column(nullable = false)
    private String predictionType; // DEMAND_FORECAST, PRICE_OPTIMIZATION, FRAUD_DETECTION, etc.

    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputData; // JSON format

    @Column(columnDefinition = "TEXT")
    private String outputData; // JSON format - prediction results

    @Column(precision = 10, scale = 4)
    private BigDecimal confidence; // Model confidence score

    @Column(precision = 10, scale = 2)
    private BigDecimal responseTimeMs; // Response time in milliseconds

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column
    private String sessionId; // For tracking related requests

    @Column
    private String requestSource; // API, BATCH, SCHEDULED, etc.

    @Column
    private String clientIP;

    @Column
    private String userAgent;

    // Business Context
    @Column
    private String city;

    @Column
    private String district;

    @Column
    private String vehicleType; // CAR, MOTORCYCLE, BICYCLE

    @Column
    private String requestCategory; // RIDE, DELIVERY, EMERGENCY, TOURISM

    // Feedback and Learning
    @Column
    private Boolean feedbackProvided = false;

    @Column(precision = 5, scale = 2)
    private BigDecimal actualValue; // For model performance evaluation

    @Column(precision = 10, scale = 4)
    private BigDecimal predictionError;

    @Column
    private Boolean wasCorrect;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestDate;

    @Column
    private LocalDateTime completedDate;

    public enum RequestStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        TIMEOUT,
        CANCELLED
    }
}