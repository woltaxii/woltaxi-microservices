package com.woltaxi.aiml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Machine Learning Model Entity
 * 
 * AI/ML sisteminde kullanılan modellerin meta bilgilerini ve durumlarını saklar.
 * Her model için version kontrolü, performans metrikleri ve deployment bilgileri tutar.
 */
@Entity
@Table(name = "ml_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String modelName;

    @Column(nullable = false)
    private String modelType; // CLASSIFICATION, REGRESSION, CLUSTERING, NLP, COMPUTER_VISION

    @Column(nullable = false)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String filePath; // Model dosyasının yolu

    @Column(nullable = false)
    private String framework; // TENSORFLOW, PYTORCH, SKLEARN, WEKA, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModelStatus status = ModelStatus.TRAINING;

    // Performance Metrics
    @Column(precision = 10, scale = 4)
    private BigDecimal accuracy;

    @Column(precision = 10, scale = 4)
    private BigDecimal precision;

    @Column(precision = 10, scale = 4)
    private BigDecimal recall;

    @Column(precision = 10, scale = 4)
    private BigDecimal f1Score;

    @Column(precision = 10, scale = 4)
    private BigDecimal mse; // Mean Squared Error for regression

    @Column(precision = 10, scale = 4)
    private BigDecimal mae; // Mean Absolute Error

    // Training Information
    @Column
    private LocalDateTime trainingStartDate;

    @Column
    private LocalDateTime trainingEndDate;

    @Column
    private Integer epochs;

    @Column
    private Integer batchSize;

    @Column(precision = 10, scale = 6)
    private BigDecimal learningRate;

    // Deployment Information
    @Column
    private LocalDateTime deploymentDate;

    @Column
    private String deploymentEnvironment; // DEV, STAGING, PRODUCTION

    @Column
    private Boolean isActive = false;

    @Column
    private String servingEndpoint; // REST API endpoint for model serving

    // Monitoring
    @Column
    private LocalDateTime lastPredictionDate;

    @Column
    private Long totalPredictions = 0L;

    @Column(precision = 10, scale = 4)
    private BigDecimal avgResponseTime; // milliseconds

    @Column(columnDefinition = "TEXT")
    private String hyperparameters; // JSON format

    @Column(columnDefinition = "TEXT")
    private String features; // JSON array of feature names

    @Column(columnDefinition = "TEXT")
    private String labels; // JSON array for classification labels

    // Model Drift Detection
    @Column(precision = 10, scale = 4)
    private BigDecimal driftScore;

    @Column
    private LocalDateTime lastDriftCheck;

    @Column
    private Boolean driftDetected = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String createdBy;

    @Column
    private String updatedBy;

    public enum ModelStatus {
        TRAINING,
        TRAINED,
        VALIDATING,
        VALIDATED,
        DEPLOYING,
        DEPLOYED,
        SERVING,
        DEPRECATED,
        FAILED,
        ARCHIVED
    }
}