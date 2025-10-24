package com.woltaxi.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Sensor Fusion entity'si
 * Farklı sensörlerden gelen verileri birleştirip analiz eder
 */
@Entity
@Table(name = "sensor_fusion_data", indexes = {
    @Index(name = "idx_vehicle_fusion_timestamp", columnList = "vehicle_id, timestamp"),
    @Index(name = "idx_fusion_confidence", columnList = "fusion_confidence_score"),
    @Index(name = "idx_decision_type", columnList = "ai_decision_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorFusionData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    // LIDAR verisi
    @Column(name = "lidar_range_data", columnDefinition = "TEXT")
    private String lidarRangeData; // JSON format: point cloud data
    
    @Column(name = "lidar_accuracy")
    private Double lidarAccuracy;
    
    @Column(name = "lidar_obstacles_detected")
    private Integer lidarObstaclesDetected;
    
    // Radar verisi
    @Column(name = "radar_speed_data", columnDefinition = "TEXT")
    private String radarSpeedData; // JSON format: speed vectors
    
    @Column(name = "radar_range_meters")
    private Double radarRangeMeters;
    
    @Column(name = "radar_objects_tracked")
    private Integer radarObjectsTracked;
    
    // Kamera verisi (Computer Vision'dan gelen)
    @Column(name = "camera_confidence")
    private Double cameraConfidence;
    
    @Column(name = "camera_objects_detected")
    private Integer cameraObjectsDetected;
    
    @Column(name = "visual_recognition_score")
    private Double visualRecognitionScore;
    
    // GPS ve IMU verisi
    @Column(name = "gps_accuracy_meters")
    private Double gpsAccuracyMeters;
    
    @Column(name = "imu_acceleration_x")
    private Double imuAccelerationX;
    
    @Column(name = "imu_acceleration_y")
    private Double imuAccelerationY;
    
    @Column(name = "imu_acceleration_z")
    private Double imuAccelerationZ;
    
    @Column(name = "gyroscope_x")
    private Double gyroscopeX;
    
    @Column(name = "gyroscope_y")
    private Double gyroscopeY;
    
    @Column(name = "gyroscope_z")
    private Double gyroscopeZ;
    
    // Fusion sonuçları
    @Column(name = "fusion_confidence_score")
    private Double fusionConfidenceScore;
    
    @Column(name = "sensor_agreement_level")
    private Double sensorAgreementLevel; // Sensörler arası uyum
    
    @Column(name = "data_consistency_score")
    private Double dataConsistencyScore;
    
    // AI karar verme
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_decision_type")
    private AIDecisionType aiDecisionType;
    
    @Column(name = "decision_confidence")
    private Double decisionConfidence;
    
    @Column(name = "recommended_action")
    private String recommendedAction;
    
    @Column(name = "action_priority_level")
    private Integer actionPriorityLevel; // 1-10 arası
    
    // Çevresel durum analizi
    @Column(name = "environmental_risk_score")
    private Double environmentalRiskScore;
    
    @Column(name = "weather_impact_factor")
    private Double weatherImpactFactor;
    
    @Column(name = "road_condition_score")
    private Double roadConditionScore;
    
    @Column(name = "traffic_density_level")
    private Integer trafficDensityLevel; // 1-5 arası
    
    // Performans metrikleri
    @Column(name = "fusion_processing_time_ms")
    private Long fusionProcessingTimeMs;
    
    @Column(name = "sensor_latency_ms")
    private Long sensorLatencyMs;
    
    @Column(name = "ai_inference_time_ms")
    private Long aiInferenceTimeMs;
    
    @Column(name = "total_pipeline_time_ms")
    private Long totalPipelineTimeMs;
    
    // Kalibrasyon ve hata düzeltme
    @Column(name = "calibration_status")
    private String calibrationStatus;
    
    @Column(name = "sensor_health_score")
    private Double sensorHealthScore;
    
    @Column(name = "error_correction_applied")
    private Boolean errorCorrectionApplied;
    
    @Column(name = "outlier_detection_triggered")
    private Boolean outlierDetectionTriggered;
    
    // Machine Learning model bilgileri
    @Column(name = "ml_model_version")
    private String mlModelVersion;
    
    @Column(name = "feature_vector", columnDefinition = "TEXT")
    private String featureVector; // JSON format
    
    @Column(name = "prediction_accuracy")
    private Double predictionAccuracy;
    
    @Column(name = "learning_rate_applied")
    private Double learningRateApplied;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum tanımları
    public enum AIDecisionType {
        CONTINUE_STRAIGHT,
        TURN_LEFT,
        TURN_RIGHT,
        BRAKE_GRADUALLY,
        EMERGENCY_BRAKE,
        ACCELERATE,
        CHANGE_LANE_LEFT,
        CHANGE_LANE_RIGHT,
        STOP_AND_WAIT,
        REVERSE,
        PARK,
        EMERGENCY_STOP,
        REQUEST_HUMAN_INTERVENTION
    }
    
    // Business methods
    public boolean isHighConfidenceFusion() {
        return fusionConfidenceScore != null && fusionConfidenceScore >= 0.85;
    }
    
    public boolean isSensorDataConsistent() {
        return sensorAgreementLevel != null && sensorAgreementLevel >= 0.8 &&
               dataConsistencyScore != null && dataConsistencyScore >= 0.75;
    }
    
    public boolean requiresImmediateAction() {
        return actionPriorityLevel != null && actionPriorityLevel >= 8 ||
               aiDecisionType == AIDecisionType.EMERGENCY_BRAKE ||
               aiDecisionType == AIDecisionType.EMERGENCY_STOP ||
               aiDecisionType == AIDecisionType.REQUEST_HUMAN_INTERVENTION;
    }
    
    public boolean isPerformanceOptimal() {
        return totalPipelineTimeMs != null && totalPipelineTimeMs <= 100 && // 100ms altında
               sensorHealthScore != null && sensorHealthScore >= 0.9;
    }
    
    public String getRiskLevel() {
        if (environmentalRiskScore == null) return "UNKNOWN";
        
        if (environmentalRiskScore >= 0.8) return "CRITICAL";
        else if (environmentalRiskScore >= 0.6) return "HIGH";
        else if (environmentalRiskScore >= 0.4) return "MEDIUM";
        else if (environmentalRiskScore >= 0.2) return "LOW";
        else return "MINIMAL";
    }
    
    public boolean isCalibrationRequired() {
        return !"OPTIMAL".equals(calibrationStatus) ||
               (sensorHealthScore != null && sensorHealthScore < 0.8) ||
               Boolean.TRUE.equals(outlierDetectionTriggered);
    }
    
    public double getOverallSystemHealth() {
        double health = 0.0;
        int factors = 0;
        
        if (fusionConfidenceScore != null) {
            health += fusionConfidenceScore;
            factors++;
        }
        
        if (sensorHealthScore != null) {
            health += sensorHealthScore;
            factors++;
        }
        
        if (dataConsistencyScore != null) {
            health += dataConsistencyScore;
            factors++;
        }
        
        if (predictionAccuracy != null) {
            health += predictionAccuracy;
            factors++;
        }
        
        return factors > 0 ? health / factors : 0.0;
    }
    
    public String getPerformanceMetrics() {
        return String.format(
            "Fusion: %dms, Sensor: %dms, AI: %dms, Total: %dms, Health: %.2f",
            fusionProcessingTimeMs != null ? fusionProcessingTimeMs : 0,
            sensorLatencyMs != null ? sensorLatencyMs : 0,
            aiInferenceTimeMs != null ? aiInferenceTimeMs : 0,
            totalPipelineTimeMs != null ? totalPipelineTimeMs : 0,
            getOverallSystemHealth()
        );
    }
}