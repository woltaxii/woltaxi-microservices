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
 * Path Planning entity'si
 * Otonom araçlar için rota planlama ve navigasyon verilerini saklar
 */
@Entity
@Table(name = "path_planning_data", indexes = {
    @Index(name = "idx_vehicle_planning_timestamp", columnList = "vehicle_id, timestamp"),
    @Index(name = "idx_route_status", columnList = "route_status"),
    @Index(name = "idx_planning_algorithm", columnList = "planning_algorithm")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathPlanningData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    // Rota bilgileri
    @Column(name = "origin_latitude")
    private Double originLatitude;
    
    @Column(name = "origin_longitude")
    private Double originLongitude;
    
    @Column(name = "destination_latitude")
    private Double destinationLatitude;
    
    @Column(name = "destination_longitude")
    private Double destinationLongitude;
    
    @Column(name = "current_latitude")
    private Double currentLatitude;
    
    @Column(name = "current_longitude")
    private Double currentLongitude;
    
    // Yol planlama algoritması
    @Enumerated(EnumType.STRING)
    @Column(name = "planning_algorithm")
    private PlanningAlgorithm planningAlgorithm;
    
    @Column(name = "algorithm_version")
    private String algorithmVersion;
    
    @Column(name = "planning_time_ms")
    private Long planningTimeMs;
    
    // Rota detayları
    @Column(name = "route_waypoints", columnDefinition = "TEXT")
    private String routeWaypoints; // JSON format: [[lat, lng], [lat, lng], ...]
    
    @Column(name = "total_distance_meters")
    private Double totalDistanceMeters;
    
    @Column(name = "estimated_duration_seconds")
    private Long estimatedDurationSeconds;
    
    @Column(name = "remaining_distance_meters")
    private Double remainingDistanceMeters;
    
    @Column(name = "remaining_time_seconds")
    private Long remainingTimeSeconds;
    
    // Trafik ve koşullar
    @Column(name = "traffic_factor")
    private Double trafficFactor; // 1.0 = normal, >1.0 = yoğun trafik
    
    @Column(name = "weather_factor")
    private Double weatherFactor; // 1.0 = iyi hava, >1.0 = kötü hava
    
    @Column(name = "road_condition_factor")
    private Double roadConditionFactor;
    
    @Column(name = "construction_zones_count")
    private Integer constructionZonesCount;
    
    @Column(name = "school_zones_count")
    private Integer schoolZonesCount;
    
    // Dinamik rota güncellemeleri
    @Enumerated(EnumType.STRING)
    @Column(name = "route_status")
    private RouteStatus routeStatus;
    
    @Column(name = "recalculation_count")
    private Integer recalculationCount;
    
    @Column(name = "last_recalculation_reason")
    private String lastRecalculationReason;
    
    @Column(name = "alternative_routes_count")
    private Integer alternativeRoutesCount;
    
    // Güvenlik ve optimizasyon
    @Column(name = "safety_score")
    private Double safetyScore; // 0-1 arası
    
    @Column(name = "efficiency_score")
    private Double efficiencyScore; // 0-1 arası
    
    @Column(name = "comfort_score")
    private Double comfortScore; // 0-1 arası
    
    @Column(name = "eco_friendliness_score")
    private Double ecoFriendlinessScore; // 0-1 arası
    
    // Sürüş davranışı parametreleri
    @Column(name = "max_speed_kmh")
    private Double maxSpeedKmh;
    
    @Column(name = "average_speed_kmh")
    private Double averageSpeedKmh;
    
    @Column(name = "acceleration_profile")
    private String accelerationProfile; // GENTLE, NORMAL, AGGRESSIVE
    
    @Column(name = "braking_profile")
    private String brakingProfile; // GENTLE, NORMAL, AGGRESSIVE
    
    @Column(name = "cornering_profile")
    private String corneringProfile; // CONSERVATIVE, NORMAL, DYNAMIC
    
    // Engel kaçınma
    @Column(name = "obstacles_detected_count")
    private Integer obstaclesDetectedCount;
    
    @Column(name = "avoidance_maneuvers_count")
    private Integer avoidanceManeuversCount;
    
    @Column(name = "emergency_stops_count")
    private Integer emergencyStopsCount;
    
    @Column(name = "lane_changes_count")
    private Integer laneChangesCount;
    
    // AI ve makine öğrenmesi
    @Column(name = "ml_prediction_confidence")
    private Double mlPredictionConfidence;
    
    @Column(name = "learning_data_collected")
    private Boolean learningDataCollected;
    
    @Column(name = "route_optimization_applied")
    private Boolean routeOptimizationApplied;
    
    @Column(name = "predictive_adjustments_made")
    private Integer predictiveAdjustmentsMade;
    
    // Yakıt/enerji tüketimi
    @Column(name = "estimated_fuel_consumption")
    private Double estimatedFuelConsumption; // litre veya kWh
    
    @Column(name = "actual_fuel_consumption")
    private Double actualFuelConsumption;
    
    @Column(name = "energy_efficiency_rating")
    private String energyEfficiencyRating; // A+, A, B, C, D
    
    // Performans metrikleri
    @Column(name = "path_deviation_meters")
    private Double pathDeviationMeters;
    
    @Column(name = "arrival_time_accuracy_seconds")
    private Long arrivalTimeAccuracySeconds;
    
    @Column(name = "route_completion_percentage")
    private Double routeCompletionPercentage;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum tanımları
    public enum PlanningAlgorithm {
        A_STAR,           // A* algoritması
        DIJKSTRA,         // Dijkstra algoritması
        RRT,              // Rapidly-exploring Random Tree
        RRT_STAR,         // RRT* (optimal)
        HYBRID_A_STAR,    // Hibrit A*
        DYNAMIC_PROGRAMMING,
        GENETIC_ALGORITHM,
        NEURAL_NETWORK,
        REINFORCEMENT_LEARNING,
        MULTI_OBJECTIVE_OPTIMIZATION
    }
    
    public enum RouteStatus {
        PLANNING,
        ACTIVE,
        RECALCULATING,
        PAUSED,
        COMPLETED,
        CANCELLED,
        ERROR,
        WAITING_FOR_CONDITIONS,
        HUMAN_INTERVENTION_REQUIRED
    }
    
    // Business methods
    public boolean isRouteOptimal() {
        return safetyScore != null && safetyScore >= 0.8 &&
               efficiencyScore != null && efficiencyScore >= 0.8 &&
               pathDeviationMeters != null && pathDeviationMeters <= 5.0;
    }
    
    public boolean requiresRecalculation() {
        return routeStatus == RouteStatus.RECALCULATING ||
               (obstaclesDetectedCount != null && obstaclesDetectedCount > 5) ||
               (pathDeviationMeters != null && pathDeviationMeters > 50.0);
    }
    
    public String getOverallPerformanceRating() {
        if (safetyScore == null || efficiencyScore == null || comfortScore == null) {
            return "INSUFFICIENT_DATA";
        }
        
        double avgScore = (safetyScore + efficiencyScore + comfortScore) / 3.0;
        
        if (avgScore >= 0.9) return "EXCELLENT";
        else if (avgScore >= 0.8) return "VERY_GOOD";
        else if (avgScore >= 0.7) return "GOOD";
        else if (avgScore >= 0.6) return "FAIR";
        else return "NEEDS_IMPROVEMENT";
    }
    
    public boolean isEcoFriendly() {
        return ecoFriendlinessScore != null && ecoFriendlinessScore >= 0.7 &&
               "A+".equals(energyEfficiencyRating) || "A".equals(energyEfficiencyRating);
    }
    
    public double getRouteProgress() {
        if (totalDistanceMeters == null || remainingDistanceMeters == null) {
            return 0.0;
        }
        
        double completedDistance = totalDistanceMeters - remainingDistanceMeters;
        return Math.max(0.0, Math.min(100.0, (completedDistance / totalDistanceMeters) * 100.0));
    }
    
    public boolean isRunningLate() {
        if (estimatedDurationSeconds == null || remainingTimeSeconds == null) {
            return false;
        }
        
        long elapsedTime = estimatedDurationSeconds - remainingTimeSeconds;
        double progressRatio = getRouteProgress() / 100.0;
        long expectedElapsedTime = (long) (estimatedDurationSeconds * progressRatio);
        
        return elapsedTime > expectedElapsedTime * 1.2; // %20 fazla zaman geçmişse
    }
    
    public String getDrivingStyleAnalysis() {
        StringBuilder analysis = new StringBuilder();
        
        if ("GENTLE".equals(accelerationProfile) && "GENTLE".equals(brakingProfile)) {
            analysis.append("Smooth driving style, ");
        } else if ("AGGRESSIVE".equals(accelerationProfile) || "AGGRESSIVE".equals(brakingProfile)) {
            analysis.append("Aggressive driving style, ");
        } else {
            analysis.append("Balanced driving style, ");
        }
        
        if (avoidanceManeuversCount != null && avoidanceManeuversCount > 10) {
            analysis.append("high avoidance activity, ");
        }
        
        if (emergencyStopsCount != null && emergencyStopsCount > 3) {
            analysis.append("multiple emergency stops, ");
        }
        
        if (laneChangesCount != null && laneChangesCount > 20) {
            analysis.append("frequent lane changes");
        } else {
            analysis.append("stable lane keeping");
        }
        
        return analysis.toString();
    }
    
    public double getFuelEfficiencyRating() {
        if (estimatedFuelConsumption == null || actualFuelConsumption == null) {
            return 0.0;
        }
        
        return Math.max(0.0, Math.min(5.0, 
            5.0 * (estimatedFuelConsumption / Math.max(actualFuelConsumption, 0.1))
        ));
    }
}