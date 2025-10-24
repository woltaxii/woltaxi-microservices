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
 * Computer Vision Task Entity
 * 
 * Görüntü işleme ve bilgisayarlı görü görevlerini takip eder.
 * Yüz tanıma, nesne tespiti, OCR, görüntü analizi gibi işlemleri saklar.
 */
@Entity
@Table(name = "computer_vision_tasks", indexes = {
    @Index(name = "idx_cv_task_type", columnList = "taskType"),
    @Index(name = "idx_cv_user_id", columnList = "userId"),
    @Index(name = "idx_cv_date", columnList = "createdAt"),
    @Index(name = "idx_cv_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComputerVisionTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String taskType; // FACE_RECOGNITION, OBJECT_DETECTION, OCR, IMAGE_CLASSIFICATION, etc.

    @Column
    private Long userId;

    @Column
    private String serviceName; // WOLTAXI, WOLKURYE, EMERGENCY

    @Column(nullable = false)
    private String inputImagePath; // Giriş görüntüsünün yolu

    @Column
    private String outputImagePath; // İşlenmiş görüntünün yolu (eğer varsa)

    @Column(columnDefinition = "TEXT")
    private String analysisResults; // JSON format - tespit edilen nesneler, yüzler, metin vb.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(precision = 10, scale = 4)
    private BigDecimal confidence; // Genel güven skoru

    @Column(precision = 10, scale = 2)
    private BigDecimal processingTimeMs; // İşlem süresi

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // Image Metadata
    @Column
    private Integer imageWidth;

    @Column
    private Integer imageHeight;

    @Column
    private String imageFormat; // JPEG, PNG, WEBP, etc.

    @Column
    private Long imageSizeBytes;

    // Face Recognition Specific
    @Column
    private Integer facesDetected;

    @Column(columnDefinition = "TEXT")
    private String recognizedFaces; // JSON array of recognized person IDs

    // Object Detection Specific
    @Column
    private Integer objectsDetected;

    @Column(columnDefinition = "TEXT")
    private String detectedObjects; // JSON array of objects with bounding boxes

    // OCR Specific
    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column
    private String textLanguage; // TR, EN, AR, etc.

    @Column(precision = 10, scale = 4)
    private BigDecimal textConfidence;

    // License Plate Recognition
    @Column
    private String licensePlate;

    @Column(precision = 10, scale = 4)
    private BigDecimal plateConfidence;

    // Vehicle Recognition
    @Column
    private String vehicleType; // CAR, MOTORCYCLE, TRUCK, BUS

    @Column
    private String vehicleBrand;

    @Column
    private String vehicleModel;

    @Column
    private String vehicleColor;

    // Safety & Security
    @Column
    private Boolean safetyViolationDetected = false;

    @Column(columnDefinition = "TEXT")
    private String safetyViolations; // JSON array

    @Column
    private Boolean emergencyDetected = false;

    @Column
    private String emergencyType; // ACCIDENT, MEDICAL, FIRE, etc.

    // Quality Metrics
    @Column(precision = 10, scale = 4)
    private BigDecimal imageQualityScore;

    @Column
    private Boolean isBlurry = false;

    @Column
    private Boolean isDarkImage = false;

    @Column
    private Boolean hasGoodLighting = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private String processedBy; // Model veya service name

    public enum TaskStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        TIMEOUT,
        CANCELLED
    }
}