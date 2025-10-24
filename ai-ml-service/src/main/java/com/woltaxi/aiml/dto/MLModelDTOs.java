package com.woltaxi.aiml.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Machine Learning Model DTOs
 * ML model işlemleri için data transfer objects
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMLModelRequest {
    @NotBlank(message = "Model adı boş olamaz")
    private String modelName;
    
    @NotBlank(message = "Model tipi belirtilmelidir")
    private String modelType; // CLASSIFICATION, REGRESSION, CLUSTERING, NLP, COMPUTER_VISION
    
    @NotBlank(message = "Framework belirtilmelidir")
    private String framework; // TENSORFLOW, PYTORCH, SKLEARN, WEKA
    
    private String description;
    private String version;
    private Map<String, Object> hyperparameters;
    private List<String> features;
    private List<String> labels;
    private String createdBy;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelUploadRequest {
    @NotBlank(message = "Model adı boş olamaz")
    private String modelName;
    
    @NotBlank(message = "Model tipi belirtilmelidir")
    private String modelType;
    
    @NotBlank(message = "Framework belirtilmelidir")
    private String framework;
    
    private String version;
    private String description;
    private Map<String, Object> hyperparameters;
    private List<String> features;
    private List<String> labels;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMLModelRequest {
    private String description;
    private String version;
    private BigDecimal accuracy;
    private BigDecimal precision;
    private BigDecimal recall;
    private BigDecimal f1Score;
    private Map<String, Object> hyperparameters;
    private List<String> features;
    private List<String> labels;
    private String updatedBy;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLModelResponse {
    private Long id;
    private String modelName;
    private String modelType;
    private String version;
    private String description;
    private String framework;
    private String status;
    private Boolean isActive;
    private String servingEndpoint;
    
    // Performance Metrics
    private BigDecimal accuracy;
    private BigDecimal precision;
    private BigDecimal recall;
    private BigDecimal f1Score;
    private BigDecimal mse;
    private BigDecimal mae;
    
    // Training Information
    private LocalDateTime trainingStartDate;
    private LocalDateTime trainingEndDate;
    private Integer epochs;
    private Integer batchSize;
    private BigDecimal learningRate;
    
    // Deployment Information
    private LocalDateTime deploymentDate;
    private String deploymentEnvironment;
    
    // Monitoring
    private LocalDateTime lastPredictionDate;
    private Long totalPredictions;
    private BigDecimal avgResponseTime;
    
    // Model Drift
    private BigDecimal driftScore;
    private LocalDateTime lastDriftCheck;
    private Boolean driftDetected;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    private Map<String, Object> hyperparameters;
    private List<String> features;
    private List<String> labels;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDeploymentRequest {
    @NotBlank(message = "Deployment ortamı belirtilmelidir")
    private String environment; // DEV, STAGING, PRODUCTION
    
    private String servingEndpoint;
    private Map<String, String> deploymentConfig;
    private Integer replicas;
    private String resourceRequirements;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentResponse {
    private Long modelId;
    private String modelName;
    private String deploymentEnvironment;
    private String servingEndpoint;
    private String status;
    private LocalDateTime deploymentDate;
    private Map<String, Object> deploymentDetails;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequestDto {
    @NotNull(message = "Giriş verisi boş olamaz")
    private Map<String, Object> inputData;
    
    private Long userId;
    private String serviceName;
    private String sessionId;
    private String requestSource;
    private Map<String, String> context;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    private Long requestId;
    private Long modelId;
    private String modelName;
    private Map<String, Object> inputData;
    private Map<String, Object> predictions;
    private BigDecimal confidence;
    private BigDecimal responseTimeMs;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime completedDate;
    private String errorMessage;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPredictionRequest {
    @NotNull(message = "Model ID boş olamaz")
    private Long modelId;
    
    @NotNull(message = "Veri noktaları boş olamaz")
    private List<Map<String, Object>> dataPoints;
    
    private Long userId;
    private String serviceName;
    private String sessionId;
    private Boolean asyncProcessing;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelPerformanceResponse {
    private Long modelId;
    private String modelName;
    private String timeframe;
    
    // Performance Metrics
    private BigDecimal accuracy;
    private BigDecimal precision;
    private BigDecimal recall;
    private BigDecimal f1Score;
    private BigDecimal auc;
    
    // Usage Statistics
    private Long totalPredictions;
    private Long successfulPredictions;
    private Long failedPredictions;
    private BigDecimal avgResponseTime;
    private BigDecimal maxResponseTime;
    
    // Drift Metrics
    private BigDecimal driftScore;
    private Boolean driftDetected;
    private LocalDateTime lastDriftCheck;
    
    // Time Series Data
    private List<Map<String, Object>> performanceTimeSeries;
    private List<Map<String, Object>> usageTimeSeries;
    
    private LocalDateTime analysisDate;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelRetrainingRequest {
    private String trainingDataPath;
    private String validationDataPath;
    private Map<String, Object> trainingConfig;
    private Map<String, Object> hyperparameters;
    private Boolean useIncrementalLearning;
    private String retrainingReason;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResponse {
    private Long modelId;
    private String trainingJobId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime estimatedCompletionTime;
    private Map<String, Object> trainingConfig;
    private String progressUrl;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionHistoryResponse {
    private Long id;
    private Long modelId;
    private String modelName;
    private Map<String, Object> inputData;
    private Map<String, Object> outputData;
    private BigDecimal confidence;
    private BigDecimal responseTimeMs;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime completedDate;
    private Long userId;
    private String serviceName;
    private String sessionId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelValidationRequest {
    private String testDataPath;
    private List<String> validationMetrics;
    private Map<String, Object> validationConfig;
    private Boolean generateReport;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
    private Long modelId;
    private String validationJobId;
    private Map<String, BigDecimal> metrics;
    private String status;
    private LocalDateTime validationDate;
    private String reportUrl;
    private List<String> recommendations;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriftDetectionRequest {
    private String referenceDataPath;
    private String currentDataPath;
    private List<String> features;
    private String driftMethod; // PSI, KS_TEST, WASSERSTEIN
    private BigDecimal threshold;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriftDetectionResponse {
    private Long modelId;
    private String modelName;
    private BigDecimal driftScore;
    private Boolean driftDetected;
    private Map<String, BigDecimal> featureDriftScores;
    private List<String> driftFeatures;
    private String driftMethod;
    private LocalDateTime detectionDate;
    private List<String> recommendations;
}