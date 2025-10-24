package com.woltaxi.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Computer Vision veri entity'si
 * Akıllı araçlar için görüntü işleme ve nesne tanıma verilerini saklar
 */
@Entity
@Table(name = "computer_vision_data", indexes = {
    @Index(name = "idx_vehicle_timestamp", columnList = "vehicle_id, timestamp"),
    @Index(name = "idx_confidence_score", columnList = "confidence_score"),
    @Index(name = "idx_object_type", columnList = "detected_object_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComputerVisionData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    // Kamera bilgileri
    @Enumerated(EnumType.STRING)
    @Column(name = "camera_position", nullable = false)
    private CameraPosition cameraPosition;
    
    @Column(name = "camera_resolution")
    private String cameraResolution;
    
    @Column(name = "frame_rate")
    private Integer frameRate;
    
    // Algılanan nesne bilgileri
    @Column(name = "detected_object_type")
    private String detectedObjectType;
    
    @Column(name = "object_count")
    private Integer objectCount;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "bounding_box_coordinates", columnDefinition = "TEXT")
    private String boundingBoxCoordinates; // JSON format: [x, y, width, height]
    
    // Trafik analizi
    @Column(name = "traffic_light_status")
    private String trafficLightStatus;
    
    @Column(name = "road_sign_detected")
    private String roadSignDetected;
    
    @Column(name = "lane_detection_active")
    private Boolean laneDetectionActive;
    
    @Column(name = "pedestrian_count")
    private Integer pedestrianCount;
    
    @Column(name = "vehicle_count")
    private Integer vehicleCount;
    
    // Mesafe ve hız bilgileri
    @Column(name = "distance_to_object")
    private Double distanceToObject; // metre
    
    @Column(name = "relative_speed")
    private Double relativeSpeed; // km/h
    
    @Column(name = "collision_risk_level")
    private String collisionRiskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    // Hava durumu ve görüş koşulları
    @Column(name = "visibility_condition")
    private String visibilityCondition; // CLEAR, FOG, RAIN, SNOW, NIGHT
    
    @Column(name = "light_condition")
    private String lightCondition; // DAY, NIGHT, DAWN, DUSK
    
    // AI model bilgileri
    @Column(name = "ai_model_version")
    private String aiModelVersion;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Column(name = "gpu_usage_percent")
    private Double gpuUsagePercent;
    
    // Özel durumlar
    @Column(name = "emergency_detected")
    private Boolean emergencyDetected;
    
    @Column(name = "obstacle_avoidance_triggered")
    private Boolean obstacleAvoidanceTriggered;
    
    @Column(name = "automatic_braking_applied")
    private Boolean automaticBrakingApplied;
    
    // Metadata
    @Column(name = "image_hash")
    private String imageHash;
    
    @Column(name = "image_size_bytes")
    private Long imageSizeBytes;
    
    @Column(name = "compression_ratio")
    private Double compressionRatio;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum tanımları
    public enum CameraPosition {
        FRONT,
        REAR,
        LEFT_SIDE,
        RIGHT_SIDE,
        TOP_360,
        DASHBOARD,
        INTERIOR
    }
    
    // Business methods
    public boolean isHighRiskSituation() {
        return "HIGH".equals(collisionRiskLevel) || 
               "CRITICAL".equals(collisionRiskLevel) ||
               Boolean.TRUE.equals(emergencyDetected);
    }
    
    public boolean isGoodVisibility() {
        return "CLEAR".equals(visibilityCondition) && 
               ("DAY".equals(lightCondition) || "DAWN".equals(lightCondition));
    }
    
    public boolean isReliableDetection() {
        return confidenceScore != null && confidenceScore >= 0.8;
    }
    
    public boolean requiresImmediateAction() {
        return Boolean.TRUE.equals(emergencyDetected) ||
               Boolean.TRUE.equals(obstacleAvoidanceTriggered) ||
               "CRITICAL".equals(collisionRiskLevel);
    }
    
    public double getProcessingEfficiency() {
        if (processingTimeMs == null || processingTimeMs == 0) {
            return 0.0;
        }
        return Math.min(1000.0 / processingTimeMs, 10.0); // Saniyede işlenen frame sayısı
    }
    
    public String getRiskAssessment() {
        if (isHighRiskSituation()) {
            return "IMMEDIATE_ACTION_REQUIRED";
        } else if (distanceToObject != null && distanceToObject < 10.0) {
            return "CAUTION_ADVISED";
        } else if (isReliableDetection() && isGoodVisibility()) {
            return "NORMAL_CONDITIONS";
        } else {
            return "MONITORING_REQUIRED";
        }
    }
}