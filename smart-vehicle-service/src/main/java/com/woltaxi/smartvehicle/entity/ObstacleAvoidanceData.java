package com.woltaxi.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Obstacle Avoidance entity'si
 * Otonom araçlar için engel algılama ve kaçınma stratejilerini saklar
 */
@Entity
@Table(name = "obstacle_avoidance_data", indexes = {
    @Index(name = "idx_vehicle_obstacle_timestamp", columnList = "vehicle_id, timestamp"),
    @Index(name = "idx_obstacle_severity", columnList = "obstacle_severity"),
    @Index(name = "idx_avoidance_action", columnList = "avoidance_action")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObstacleAvoidanceData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    // Engel bilgileri
    @Enumerated(EnumType.STRING)
    @Column(name = "obstacle_type")
    private ObstacleType obstacleType;
    
    @Column(name = "obstacle_size_length")
    private Double obstacleSizeLength; // metre
    
    @Column(name = "obstacle_size_width")
    private Double obstacleSizeWidth; // metre
    
    @Column(name = "obstacle_size_height")
    private Double obstacleSizeHeight; // metre
    
    // Konum ve mesafe bilgileri
    @Column(name = "obstacle_latitude")
    private Double obstacleLatitude;
    
    @Column(name = "obstacle_longitude")
    private Double obstacleLongitude;
    
    @Column(name = "distance_to_obstacle")
    private Double distanceToObstacle; // metre
    
    @Column(name = "relative_bearing_degrees")
    private Double relativeBearingDegrees; // -180 to +180
    
    @Column(name = "obstacle_speed_kmh")
    private Double obstacleSpeedKmh;
    
    @Column(name = "obstacle_direction_degrees")
    private Double obstacleDirectionDegrees;
    
    // Araç durumu
    @Column(name = "vehicle_speed_at_detection")
    private Double vehicleSpeedAtDetection; // km/h
    
    @Column(name = "vehicle_acceleration")
    private Double vehicleAcceleration; // m/s²
    
    @Column(name = "vehicle_heading_degrees")
    private Double vehicleHeadingDegrees;
    
    @Column(name = "lane_position")
    private String lanePosition; // CENTER, LEFT, RIGHT
    
    // Risk analizi
    @Enumerated(EnumType.STRING)
    @Column(name = "obstacle_severity")
    private ObstacleSeverity obstacleSeverity;
    
    @Column(name = "collision_probability")
    private Double collisionProbability; // 0-1 arası
    
    @Column(name = "time_to_collision_seconds")
    private Double timeToCollisionSeconds;
    
    @Column(name = "impact_risk_score")
    private Double impactRiskScore; // 0-10 arası
    
    // Kaçınma stratejisi
    @Enumerated(EnumType.STRING)
    @Column(name = "avoidance_action")
    private AvoidanceAction avoidanceAction;
    
    @Column(name = "avoidance_direction")
    private String avoidanceDirection; // LEFT, RIGHT, REVERSE, STOP
    
    @Column(name = "avoidance_distance_meters")
    private Double avoidanceDistanceMeters;
    
    @Column(name = "avoidance_speed_change_kmh")
    private Double avoidanceSpeedChangeKmh;
    
    // Sensör verileri
    @Column(name = "detected_by_lidar")
    private Boolean detectedByLidar;
    
    @Column(name = "detected_by_radar")
    private Boolean detectedByRadar;
    
    @Column(name = "detected_by_camera")
    private Boolean detectedByCamera;
    
    @Column(name = "detected_by_ultrasonic")
    private Boolean detectedByUltrasonic;
    
    @Column(name = "sensor_fusion_confidence")
    private Double sensorFusionConfidence;
    
    // AI karar verme
    @Column(name = "ai_processing_time_ms")
    private Long aiProcessingTimeMs;
    
    @Column(name = "decision_confidence_score")
    private Double decisionConfidenceScore;
    
    @Column(name = "alternative_actions_considered")
    private Integer alternativeActionsConsidered;
    
    @Column(name = "ml_model_version")
    private String mlModelVersion;
    
    // Eylem sonuçları
    @Column(name = "action_executed_successfully")
    private Boolean actionExecutedSuccessfully;
    
    @Column(name = "action_execution_time_ms")
    private Long actionExecutionTimeMs;
    
    @Column(name = "collision_avoided")
    private Boolean collisionAvoided;
    
    @Column(name = "passenger_comfort_impact")
    private String passengerComfortImpact; // MINIMAL, MODERATE, SIGNIFICANT
    
    // Performans metrikleri
    @Column(name = "total_response_time_ms")
    private Long totalResponseTimeMs;
    
    @Column(name = "safety_margin_meters")
    private Double safetyMarginMeters;
    
    @Column(name = "path_deviation_meters")
    private Double pathDeviationMeters;
    
    @Column(name = "fuel_consumption_impact")
    private Double fuelConsumptionImpact; // %
    
    // Çevresel faktörler
    @Column(name = "weather_condition")
    private String weatherCondition;
    
    @Column(name = "road_surface_condition")
    private String roadSurfaceCondition;
    
    @Column(name = "visibility_meters")
    private Double visibilityMeters;
    
    @Column(name = "traffic_density")
    private String trafficDensity; // LOW, MEDIUM, HIGH
    
    // İnsan müdahalesi
    @Column(name = "human_intervention_required")
    private Boolean humanInterventionRequired;
    
    @Column(name = "human_takeover_requested")
    private Boolean humanTakeoverRequested;
    
    @Column(name = "emergency_alert_sent")
    private Boolean emergencyAlertSent;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum tanımları
    public enum ObstacleType {
        VEHICLE,              // Araç
        PEDESTRIAN,          // Yaya
        CYCLIST,             // Bisikletli
        MOTORCYCLIST,        // Motosikletli
        ANIMAL,              // Hayvan
        CONSTRUCTION_BARRIER, // İnşaat bariyeri
        TRAFFIC_CONE,        // Trafik konisi
        POTHOLE,             // Çukur
        DEBRIS,              // Enkaz/moloz
        TREE_BRANCH,         // Ağaç dalı
        PARKED_VEHICLE,      // Park halindeki araç
        EMERGENCY_VEHICLE,   // Acil durum aracı
        ROAD_SIGN,           // Yol tabelası
        BRIDGE_PILLAR,       // Köprü direği
        UNKNOWN              // Bilinmeyen
    }
    
    public enum ObstacleSeverity {
        MINIMAL,    // Minimal risk
        LOW,        // Düşük risk
        MEDIUM,     // Orta risk
        HIGH,       // Yüksek risk
        CRITICAL,   // Kritik risk
        EMERGENCY   // Acil durum
    }
    
    public enum AvoidanceAction {
        NO_ACTION,              // Herhangi bir eylem gerekmez
        REDUCE_SPEED,           // Hız azaltma
        EMERGENCY_BRAKE,        // Acil fren
        CHANGE_LANE_LEFT,       // Sola şerit değiştirme
        CHANGE_LANE_RIGHT,      // Sağa şerit değiştirme
        SWERVE_LEFT,           // Sola ani manevra
        SWERVE_RIGHT,          // Sağa ani manevra
        REVERSE,               // Geri gitme
        COMPLETE_STOP,         // Tam durma
        INCREASE_SPEED,        // Hız artırma (aşma için)
        WAIT_AND_PROCEED,      // Bekle ve devam et
        REQUEST_HUMAN_CONTROL, // İnsan kontrolü iste
        PULL_OVER,             // Kenara çekil
        FOLLOW_AT_DISTANCE     // Mesafeli takip et
    }
    
    // Business methods
    public boolean isEmergencySituation() {
        return obstacleSeverity == ObstacleSeverity.CRITICAL ||
               obstacleSeverity == ObstacleSeverity.EMERGENCY ||
               (collisionProbability != null && collisionProbability >= 0.8) ||
               (timeToCollisionSeconds != null && timeToCollisionSeconds <= 2.0);
    }
    
    public boolean isHighRiskObstacle() {
        return obstacleType == ObstacleType.PEDESTRIAN ||
               obstacleType == ObstacleType.CYCLIST ||
               obstacleType == ObstacleType.EMERGENCY_VEHICLE ||
               isEmergencySituation();
    }
    
    public boolean wasActionSuccessful() {
        return Boolean.TRUE.equals(actionExecutedSuccessfully) &&
               Boolean.TRUE.equals(collisionAvoided) &&
               !Boolean.TRUE.equals(humanInterventionRequired);
    }
    
    public String getResponseTimeCategory() {
        if (totalResponseTimeMs == null) return "UNKNOWN";
        
        if (totalResponseTimeMs <= 100) return "EXCELLENT";
        else if (totalResponseTimeMs <= 250) return "VERY_GOOD";
        else if (totalResponseTimeMs <= 500) return "GOOD";
        else if (totalResponseTimeMs <= 1000) return "ACCEPTABLE";
        else return "SLOW";
    }
    
    public boolean requiresImmediateAction() {
        return isEmergencySituation() ||
               avoidanceAction == AvoidanceAction.EMERGENCY_BRAKE ||
               avoidanceAction == AvoidanceAction.REQUEST_HUMAN_CONTROL ||
               Boolean.TRUE.equals(humanTakeoverRequested);
    }
    
    public double getSafetyScore() {
        double score = 0.0;
        int factors = 0;
        
        // Collision avoided
        if (Boolean.TRUE.equals(collisionAvoided)) {
            score += 30.0;
        }
        factors++;
        
        // Safety margin
        if (safetyMarginMeters != null) {
            score += Math.min(20.0, safetyMarginMeters * 2.0);
        }
        factors++;
        
        // Response time
        if (totalResponseTimeMs != null) {
            score += Math.max(0, 25.0 - (totalResponseTimeMs / 40.0));
        }
        factors++;
        
        // Decision confidence
        if (decisionConfidenceScore != null) {
            score += decisionConfidenceScore * 25.0;
        }
        factors++;
        
        return Math.min(100.0, score);
    }
    
    public String getComfortImpactAssessment() {
        if (passengerComfortImpact != null) {
            return passengerComfortImpact;
        }
        
        if (avoidanceAction == AvoidanceAction.EMERGENCY_BRAKE ||
            avoidanceAction == AvoidanceAction.SWERVE_LEFT ||
            avoidanceAction == AvoidanceAction.SWERVE_RIGHT) {
            return "SIGNIFICANT";
        } else if (avoidanceAction == AvoidanceAction.CHANGE_LANE_LEFT ||
                   avoidanceAction == AvoidanceAction.CHANGE_LANE_RIGHT ||
                   avoidanceAction == AvoidanceAction.REDUCE_SPEED) {
            return "MODERATE";
        } else {
            return "MINIMAL";
        }
    }
    
    public boolean isMultiSensorDetection() {
        int sensorCount = 0;
        if (Boolean.TRUE.equals(detectedByLidar)) sensorCount++;
        if (Boolean.TRUE.equals(detectedByRadar)) sensorCount++;
        if (Boolean.TRUE.equals(detectedByCamera)) sensorCount++;
        if (Boolean.TRUE.equals(detectedByUltrasonic)) sensorCount++;
        
        return sensorCount >= 2;
    }
    
    public String getPerformanceSummary() {
        return String.format(
            "Obstacle: %s, Severity: %s, Action: %s, Success: %s, Response: %s, Safety: %.1f%%",
            obstacleType != null ? obstacleType.name() : "UNKNOWN",
            obstacleSeverity != null ? obstacleSeverity.name() : "UNKNOWN",
            avoidanceAction != null ? avoidanceAction.name() : "NONE",
            wasActionSuccessful() ? "YES" : "NO",
            getResponseTimeCategory(),
            getSafetyScore()
        );
    }
}