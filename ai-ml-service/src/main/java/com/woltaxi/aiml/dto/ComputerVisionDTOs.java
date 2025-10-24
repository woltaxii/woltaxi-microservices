package com.woltaxi.aiml.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Computer Vision DTOs
 * Bilgisayarlı görü işlemleri için data transfer objects
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceRecognitionResponse {
    private Long taskId;
    private String status;
    private Integer facesDetected;
    private List<DetectedFace> recognizedFaces;
    private BigDecimal confidence;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    private String errorMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectedFace {
        private String personId;
        private String personName;
        private BigDecimal confidence;
        private BoundingBox boundingBox;
        private Map<String, Object> faceAttributes;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectDetectionResponse {
    private Long taskId;
    private String status;
    private Integer objectsDetected;
    private List<DetectedObject> objects;
    private BigDecimal confidence;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    private String errorMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectedObject {
        private String objectType;
        private String label;
        private BigDecimal confidence;
        private BoundingBox boundingBox;
        private Map<String, Object> attributes;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoundingBox {
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OCRResponse {
    private Long taskId;
    private String status;
    private String extractedText;
    private String detectedLanguage;
    private BigDecimal textConfidence;
    private List<TextBlock> textBlocks;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    private String errorMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextBlock {
        private String text;
        private BigDecimal confidence;
        private BoundingBox boundingBox;
        private String language;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicensePlateResponse {
    private Long taskId;
    private String status;
    private String licensePlate;
    private BigDecimal plateConfidence;
    private BoundingBox plateBoundingBox;
    private String country;
    private String region;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    private String errorMessage;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetectionResponse {
    private Long taskId;
    private String status;
    private List<DetectedVehicle> vehicles;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    private String errorMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectedVehicle {
        private String vehicleType; // CAR, MOTORCYCLE, TRUCK, BUS
        private String brand;
        private String model;
        private String color;
        private BigDecimal confidence;
        private BoundingBox boundingBox;
        private String licensePlate;
        private Map<String, Object> attributes;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyAnalysisResponse {
    private Long taskId;
    private String status;
    private Boolean safetyViolationDetected;
    private List<SafetyViolation> violations;
    private Boolean emergencyDetected;
    private String emergencyType;
    private BigDecimal riskScore;
    private List<String> recommendations;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    private String errorMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyViolation {
        private String violationType;
        private String description;
        private BigDecimal severity; // 1-10
        private BoundingBox location;
        private BigDecimal confidence;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageQualityResponse {
    private Long taskId;
    private String status;
    private BigDecimal qualityScore; // 0-100
    private String qualityLevel; // POOR, FAIR, GOOD, EXCELLENT
    private Boolean isBlurry;
    private Boolean isDarkImage;
    private Boolean hasGoodLighting;
    private Map<String, BigDecimal> qualityMetrics;
    private List<String> improvements;
    private String enhancedImagePath;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAnalysisRequest {
    @NotNull(message = "Analiz tipleri belirtilmelidir")
    private List<String> analysisTypes; // FACE_RECOGNITION, OBJECT_DETECTION, OCR, etc.
    
    private Long userId;
    private String serviceName;
    private Map<String, Object> analysisConfig;
    private Boolean generateReport;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAnalysisResponse {
    private String batchId;
    private String status;
    private Integer totalImages;
    private Integer processedImages;
    private Integer failedImages;
    private List<ImageAnalysisResult> results;
    private BigDecimal totalProcessingTimeMs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reportUrl;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageAnalysisResult {
        private String imageName;
        private String status;
        private Map<String, Object> analysisResults;
        private String errorMessage;
        private BigDecimal processingTimeMs;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComputerVisionTaskResponse {
    private Long id;
    private String taskType;
    private Long userId;
    private String serviceName;
    private String inputImagePath;
    private String outputImagePath;
    private Map<String, Object> analysisResults;
    private String status;
    private BigDecimal confidence;
    private BigDecimal processingTimeMs;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    
    // Image Metadata
    private Integer imageWidth;
    private Integer imageHeight;
    private String imageFormat;
    private Long imageSizeBytes;
    
    // Analysis Results Summary
    private Integer facesDetected;
    private Integer objectsDetected;
    private String extractedText;
    private String licensePlate;
    private String vehicleType;
    private Boolean safetyViolationDetected;
    private Boolean emergencyDetected;
    private BigDecimal imageQualityScore;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultsResponse {
    private Long taskId;
    private String taskType;
    private String status;
    private Map<String, Object> detailedResults;
    private List<String> outputFiles;
    private Map<String, BigDecimal> metrics;
    private LocalDateTime analysisDate;
    private String downloadUrl;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRegistrationRequest {
    @NotBlank(message = "Kişi adı boş olamaz")
    private String personName;
    
    private String personId;
    private String department;
    private String role;
    private Map<String, String> metadata;
    private Long userId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRegistrationResponse {
    private String personId;
    private String personName;
    private String status;
    private Boolean faceDetected;
    private BigDecimal faceQuality;
    private String faceEmbeddingId;
    private LocalDateTime registrationDate;
    private String errorMessage;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComputerVisionStatsResponse {
    private LocalDateTime reportDate;
    private String timeframe;
    
    // Task Statistics
    private Long totalTasks;
    private Long completedTasks;
    private Long failedTasks;
    private Map<String, Long> tasksByType;
    
    // Performance Metrics
    private BigDecimal avgProcessingTime;
    private BigDecimal avgConfidence;
    private BigDecimal successRate;
    
    // Analysis Results
    private Long totalFacesDetected;
    private Long totalObjectsDetected;
    private Long totalTextExtractions;
    private Long totalLicensePlates;
    private Long totalSafetyViolations;
    private Long totalEmergencies;
    
    // Usage by Service
    private Map<String, Long> usageByService;
    private Map<String, BigDecimal> performanceByService;
    
    // Trends
    private List<Map<String, Object>> dailyUsageTrend;
    private List<Map<String, Object>> performanceTrend;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamAnalysisRequest {
    @NotBlank(message = "Stream URL boş olamaz")
    private String streamUrl;
    
    @NotNull(message = "Analiz tipleri belirtilmelidir")
    private List<String> analysisTypes;
    
    private Integer framesPerSecond;
    private Integer maxDurationMinutes;
    private Map<String, Object> analysisConfig;
    private String callbackUrl;
    private Long userId;
    private String serviceName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamAnalysisResponse {
    private String sessionId;
    private String status;
    private String streamUrl;
    private List<String> analysisTypes;
    private LocalDateTime startTime;
    private String statusUrl;
    private String resultsUrl;
    private Map<String, Object> config;
}