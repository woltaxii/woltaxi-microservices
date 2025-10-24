package com.woltaxi.smartvehicle.service;

import com.woltaxi.smartvehicle.entity.*;
import com.woltaxi.smartvehicle.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Autonomous Vehicle AI Service
 * Otonom araç AI sistemlerini yönetir ve koordine eder
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AutonomousVehicleAIService {
    
    private final ComputerVisionDataRepository computerVisionRepository;
    private final SensorFusionDataRepository sensorFusionRepository;
    private final PathPlanningDataRepository pathPlanningRepository;
    private final ObstacleAvoidanceDataRepository obstacleAvoidanceRepository;
    private final SmartVehicleRepository smartVehicleRepository;
    
    // Computer Vision İşlemleri
    public ComputerVisionData processComputerVisionFrame(String vehicleId, Map<String, Object> frameData) {
        log.info("Processing computer vision frame for vehicle: {}", vehicleId);
        
        try {
            ComputerVisionData visionData = ComputerVisionData.builder()
                .vehicleId(vehicleId)
                .timestamp(LocalDateTime.now())
                .cameraPosition(extractCameraPosition(frameData))
                .cameraResolution(extractString(frameData, "resolution"))
                .frameRate(extractInteger(frameData, "frameRate"))
                .detectedObjectType(extractString(frameData, "objectType"))
                .objectCount(extractInteger(frameData, "objectCount"))
                .confidenceScore(extractDouble(frameData, "confidence"))
                .boundingBoxCoordinates(extractString(frameData, "boundingBox"))
                .trafficLightStatus(extractString(frameData, "trafficLight"))
                .roadSignDetected(extractString(frameData, "roadSign"))
                .laneDetectionActive(extractBoolean(frameData, "laneDetection"))
                .pedestrianCount(extractInteger(frameData, "pedestrians"))
                .vehicleCount(extractInteger(frameData, "vehicles"))
                .distanceToObject(extractDouble(frameData, "distance"))
                .relativeSpeed(extractDouble(frameData, "relativeSpeed"))
                .collisionRiskLevel(calculateCollisionRisk(frameData))
                .visibilityCondition(extractString(frameData, "visibility"))
                .lightCondition(extractString(frameData, "lightCondition"))
                .aiModelVersion(extractString(frameData, "modelVersion"))
                .processingTimeMs(extractLong(frameData, "processingTime"))
                .gpuUsagePercent(extractDouble(frameData, "gpuUsage"))
                .emergencyDetected(detectEmergencyFromVision(frameData))
                .obstacleAvoidanceTriggered(shouldTriggerObstacleAvoidance(frameData))
                .automaticBrakingApplied(shouldApplyAutomaticBraking(frameData))
                .imageHash(extractString(frameData, "imageHash"))
                .imageSizeBytes(extractLong(frameData, "imageSize"))
                .compressionRatio(extractDouble(frameData, "compressionRatio"))
                .build();
            
            ComputerVisionData savedData = computerVisionRepository.save(visionData);
            
            // Acil durum tespit edilmişse sistem uyarısı
            if (savedData.isHighRiskSituation()) {
                handleEmergencyVisionDetection(savedData);
            }
            
            log.info("Computer vision data processed successfully for vehicle: {}", vehicleId);
            return savedData;
            
        } catch (Exception e) {
            log.error("Error processing computer vision frame for vehicle: {}", vehicleId, e);
            throw new RuntimeException("Computer vision processing failed", e);
        }
    }
    
    // Sensor Fusion İşlemleri  
    public SensorFusionData processSensorFusion(String vehicleId, Map<String, Object> sensorData) {
        log.info("Processing sensor fusion for vehicle: {}", vehicleId);
        
        try {
            SensorFusionData fusionData = SensorFusionData.builder()
                .vehicleId(vehicleId)
                .timestamp(LocalDateTime.now())
                .lidarRangeData(extractString(sensorData, "lidarData"))
                .lidarAccuracy(extractDouble(sensorData, "lidarAccuracy"))
                .lidarObstaclesDetected(extractInteger(sensorData, "lidarObstacles"))
                .radarSpeedData(extractString(sensorData, "radarData"))
                .radarRangeMeters(extractDouble(sensorData, "radarRange"))
                .radarObjectsTracked(extractInteger(sensorData, "radarObjects"))
                .cameraConfidence(extractDouble(sensorData, "cameraConfidence"))
                .cameraObjectsDetected(extractInteger(sensorData, "cameraObjects"))
                .visualRecognitionScore(extractDouble(sensorData, "visualScore"))
                .gpsAccuracyMeters(extractDouble(sensorData, "gpsAccuracy"))
                .imuAccelerationX(extractDouble(sensorData, "accelX"))
                .imuAccelerationY(extractDouble(sensorData, "accelY"))
                .imuAccelerationZ(extractDouble(sensorData, "accelZ"))
                .gyroscopeX(extractDouble(sensorData, "gyroX"))
                .gyroscopeY(extractDouble(sensorData, "gyroY"))
                .gyroscopeZ(extractDouble(sensorData, "gyroZ"))
                .fusionConfidenceScore(calculateFusionConfidence(sensorData))
                .sensorAgreementLevel(calculateSensorAgreement(sensorData))
                .dataConsistencyScore(calculateDataConsistency(sensorData))
                .aiDecisionType(determineAIDecision(sensorData))
                .decisionConfidence(calculateDecisionConfidence(sensorData))
                .recommendedAction(determineRecommendedAction(sensorData))
                .actionPriorityLevel(calculateActionPriority(sensorData))
                .environmentalRiskScore(calculateEnvironmentalRisk(sensorData))
                .weatherImpactFactor(extractDouble(sensorData, "weatherImpact"))
                .roadConditionScore(extractDouble(sensorData, "roadCondition"))
                .trafficDensityLevel(extractInteger(sensorData, "trafficDensity"))
                .fusionProcessingTimeMs(extractLong(sensorData, "fusionTime"))
                .sensorLatencyMs(extractLong(sensorData, "sensorLatency"))
                .aiInferenceTimeMs(extractLong(sensorData, "aiInferenceTime"))
                .totalPipelineTimeMs(extractLong(sensorData, "totalTime"))
                .calibrationStatus(extractString(sensorData, "calibrationStatus"))
                .sensorHealthScore(calculateSensorHealth(sensorData))
                .errorCorrectionApplied(extractBoolean(sensorData, "errorCorrection"))
                .outlierDetectionTriggered(extractBoolean(sensorData, "outlierDetection"))
                .mlModelVersion(extractString(sensorData, "mlModelVersion"))
                .featureVector(extractString(sensorData, "featureVector"))
                .predictionAccuracy(extractDouble(sensorData, "predictionAccuracy"))
                .learningRateApplied(extractDouble(sensorData, "learningRate"))
                .build();
            
            SensorFusionData savedData = sensorFusionRepository.save(fusionData);
            
            // Acil durum kararı varsa sistem uyarısı
            if (savedData.requiresImmediateAction()) {
                handleEmergencyFusionDecision(savedData);
            }
            
            log.info("Sensor fusion data processed successfully for vehicle: {}", vehicleId);
            return savedData;
            
        } catch (Exception e) {
            log.error("Error processing sensor fusion for vehicle: {}", vehicleId, e);
            throw new RuntimeException("Sensor fusion processing failed", e);
        }
    }
    
    // Path Planning İşlemleri
    public PathPlanningData processPathPlanning(String vehicleId, Map<String, Object> routeData) {
        log.info("Processing path planning for vehicle: {}", vehicleId);
        
        try {
            PathPlanningData planningData = PathPlanningData.builder()
                .vehicleId(vehicleId)
                .timestamp(LocalDateTime.now())
                .originLatitude(extractDouble(routeData, "originLat"))
                .originLongitude(extractDouble(routeData, "originLng"))
                .destinationLatitude(extractDouble(routeData, "destLat"))
                .destinationLongitude(extractDouble(routeData, "destLng"))
                .currentLatitude(extractDouble(routeData, "currentLat"))
                .currentLongitude(extractDouble(routeData, "currentLng"))
                .planningAlgorithm(extractPlanningAlgorithm(routeData))
                .algorithmVersion(extractString(routeData, "algorithmVersion"))
                .planningTimeMs(extractLong(routeData, "planningTime"))
                .routeWaypoints(extractString(routeData, "waypoints"))
                .totalDistanceMeters(extractDouble(routeData, "totalDistance"))
                .estimatedDurationSeconds(extractLong(routeData, "estimatedDuration"))
                .remainingDistanceMeters(extractDouble(routeData, "remainingDistance"))
                .remainingTimeSeconds(extractLong(routeData, "remainingTime"))
                .trafficFactor(extractDouble(routeData, "trafficFactor"))
                .weatherFactor(extractDouble(routeData, "weatherFactor"))
                .roadConditionFactor(extractDouble(routeData, "roadFactor"))
                .constructionZonesCount(extractInteger(routeData, "constructionZones"))
                .schoolZonesCount(extractInteger(routeData, "schoolZones"))
                .routeStatus(extractRouteStatus(routeData))
                .recalculationCount(extractInteger(routeData, "recalculations"))
                .lastRecalculationReason(extractString(routeData, "recalculationReason"))
                .alternativeRoutesCount(extractInteger(routeData, "alternatives"))
                .safetyScore(calculateRouteSafety(routeData))
                .efficiencyScore(calculateRouteEfficiency(routeData))
                .comfortScore(calculateRouteComfort(routeData))
                .ecoFriendlinessScore(calculateEcoFriendliness(routeData))
                .maxSpeedKmh(extractDouble(routeData, "maxSpeed"))
                .averageSpeedKmh(extractDouble(routeData, "avgSpeed"))
                .accelerationProfile(extractString(routeData, "accelProfile"))
                .brakingProfile(extractString(routeData, "brakeProfile"))
                .corneringProfile(extractString(routeData, "cornerProfile"))
                .obstaclesDetectedCount(extractInteger(routeData, "obstacles"))
                .avoidanceManeuversCount(extractInteger(routeData, "avoidanceManeuvers"))
                .emergencyStopsCount(extractInteger(routeData, "emergencyStops"))
                .laneChangesCount(extractInteger(routeData, "laneChanges"))
                .mlPredictionConfidence(extractDouble(routeData, "mlConfidence"))
                .learningDataCollected(extractBoolean(routeData, "learningData"))
                .routeOptimizationApplied(extractBoolean(routeData, "optimization"))
                .predictiveAdjustmentsMade(extractInteger(routeData, "adjustments"))
                .estimatedFuelConsumption(extractDouble(routeData, "estimatedFuel"))
                .actualFuelConsumption(extractDouble(routeData, "actualFuel"))
                .energyEfficiencyRating(calculateEnergyRating(routeData))
                .pathDeviationMeters(extractDouble(routeData, "pathDeviation"))
                .arrivalTimeAccuracySeconds(extractLong(routeData, "arrivalAccuracy"))
                .routeCompletionPercentage(extractDouble(routeData, "completion"))
                .build();
            
            PathPlanningData savedData = pathPlanningRepository.save(planningData);
            
            // Rota yeniden hesaplanması gerekiyorsa
            if (savedData.requiresRecalculation()) {
                handleRouteRecalculation(savedData);
            }
            
            log.info("Path planning data processed successfully for vehicle: {}", vehicleId);
            return savedData;
            
        } catch (Exception e) {
            log.error("Error processing path planning for vehicle: {}", vehicleId, e);
            throw new RuntimeException("Path planning processing failed", e);
        }
    }
    
    // Obstacle Avoidance İşlemleri
    public ObstacleAvoidanceData processObstacleAvoidance(String vehicleId, Map<String, Object> obstacleData) {
        log.info("Processing obstacle avoidance for vehicle: {}", vehicleId);
        
        try {
            ObstacleAvoidanceData avoidanceData = ObstacleAvoidanceData.builder()
                .vehicleId(vehicleId)
                .timestamp(LocalDateTime.now())
                .obstacleType(extractObstacleType(obstacleData))
                .obstacleSizeLength(extractDouble(obstacleData, "length"))
                .obstacleSizeWidth(extractDouble(obstacleData, "width"))
                .obstacleSizeHeight(extractDouble(obstacleData, "height"))
                .obstacleLatitude(extractDouble(obstacleData, "obstacleLat"))
                .obstacleLongitude(extractDouble(obstacleData, "obstacleLng"))
                .distanceToObstacle(extractDouble(obstacleData, "distance"))
                .relativeBearingDegrees(extractDouble(obstacleData, "bearing"))
                .obstacleSpeedKmh(extractDouble(obstacleData, "obstacleSpeed"))
                .obstacleDirectionDegrees(extractDouble(obstacleData, "obstacleDirection"))
                .vehicleSpeedAtDetection(extractDouble(obstacleData, "vehicleSpeed"))
                .vehicleAcceleration(extractDouble(obstacleData, "acceleration"))
                .vehicleHeadingDegrees(extractDouble(obstacleData, "heading"))
                .lanePosition(extractString(obstacleData, "lanePosition"))
                .obstacleSeverity(calculateObstacleSeverity(obstacleData))
                .collisionProbability(calculateCollisionProbability(obstacleData))
                .timeToCollisionSeconds(calculateTimeToCollision(obstacleData))
                .impactRiskScore(calculateImpactRisk(obstacleData))
                .avoidanceAction(determineAvoidanceAction(obstacleData))
                .avoidanceDirection(extractString(obstacleData, "avoidanceDirection"))
                .avoidanceDistanceMeters(extractDouble(obstacleData, "avoidanceDistance"))
                .avoidanceSpeedChangeKmh(extractDouble(obstacleData, "speedChange"))
                .detectedByLidar(extractBoolean(obstacleData, "lidarDetection"))
                .detectedByRadar(extractBoolean(obstacleData, "radarDetection"))
                .detectedByCamera(extractBoolean(obstacleData, "cameraDetection"))
                .detectedByUltrasonic(extractBoolean(obstacleData, "ultrasonicDetection"))
                .sensorFusionConfidence(extractDouble(obstacleData, "fusionConfidence"))
                .aiProcessingTimeMs(extractLong(obstacleData, "aiProcessingTime"))
                .decisionConfidenceScore(extractDouble(obstacleData, "decisionConfidence"))
                .alternativeActionsConsidered(extractInteger(obstacleData, "alternatives"))
                .mlModelVersion(extractString(obstacleData, "mlModelVersion"))
                .actionExecutedSuccessfully(extractBoolean(obstacleData, "actionSuccess"))
                .actionExecutionTimeMs(extractLong(obstacleData, "executionTime"))
                .collisionAvoided(extractBoolean(obstacleData, "collisionAvoided"))
                .passengerComfortImpact(extractString(obstacleData, "comfortImpact"))
                .totalResponseTimeMs(extractLong(obstacleData, "responseTime"))
                .safetyMarginMeters(extractDouble(obstacleData, "safetyMargin"))
                .pathDeviationMeters(extractDouble(obstacleData, "pathDeviation"))
                .fuelConsumptionImpact(extractDouble(obstacleData, "fuelImpact"))
                .weatherCondition(extractString(obstacleData, "weather"))
                .roadSurfaceCondition(extractString(obstacleData, "roadSurface"))
                .visibilityMeters(extractDouble(obstacleData, "visibility"))
                .trafficDensity(extractString(obstacleData, "trafficDensity"))
                .humanInterventionRequired(extractBoolean(obstacleData, "humanIntervention"))
                .humanTakeoverRequested(extractBoolean(obstacleData, "takeover"))
                .emergencyAlertSent(extractBoolean(obstacleData, "emergencyAlert"))
                .build();
            
            ObstacleAvoidanceData savedData = obstacleAvoidanceRepository.save(avoidanceData);
            
            // Acil durum tespit edilmişse
            if (savedData.isEmergencySituation()) {
                handleEmergencyObstacleAvoidance(savedData);
            }
            
            log.info("Obstacle avoidance data processed successfully for vehicle: {}", vehicleId);
            return savedData;
            
        } catch (Exception e) {
            log.error("Error processing obstacle avoidance for vehicle: {}", vehicleId, e);
            throw new RuntimeException("Obstacle avoidance processing failed", e);
        }
    }
    
    // AI Sistem Durumu Analizi
    public Map<String, Object> getAISystemStatus(String vehicleId) {
        log.info("Getting AI system status for vehicle: {}", vehicleId);
        
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Computer Vision Status
            List<ComputerVisionData> recentVisionData = computerVisionRepository
                .findRecentDataByVehicle(vehicleId, oneHourAgo);
            
            status.put("computerVision", Map.of(
                "active", !recentVisionData.isEmpty(),
                "frameCount", recentVisionData.size(),
                "averageConfidence", computerVisionRepository.getAverageConfidenceScore(vehicleId, oneHourAgo),
                "averageProcessingTime", computerVisionRepository.getAverageProcessingTime(vehicleId, oneHourAgo),
                "emergencyDetections", computerVisionRepository.findEmergencySituations(vehicleId).size()
            ));
            
            // Sensor Fusion Status
            List<SensorFusionData> recentFusionData = sensorFusionRepository
                .findRecentFusionData(vehicleId, oneHourAgo);
            
            status.put("sensorFusion", Map.of(
                "active", !recentFusionData.isEmpty(),
                "fusionCount", recentFusionData.size(),
                "averageConfidence", sensorFusionRepository.getAverageSensorAgreement(vehicleId, oneHourAgo),
                "averageProcessingTime", sensorFusionRepository.getAverageTotalPipelineTime(vehicleId, oneHourAgo),
                "emergencyDecisions", sensorFusionRepository.findEmergencyDecisions(vehicleId).size()
            ));
            
            // Path Planning Status
            List<PathPlanningData> recentPlanningData = pathPlanningRepository
                .findRecentPlanningData(vehicleId, oneHourAgo);
            
            status.put("pathPlanning", Map.of(
                "active", !recentPlanningData.isEmpty(),
                "planningCount", recentPlanningData.size(),
                "averageSafetyScore", pathPlanningRepository.getAverageSafetyScore(vehicleId, oneHourAgo),
                "averageEfficiencyScore", pathPlanningRepository.getAverageEfficiencyScore(vehicleId, oneHourAgo),
                "recalculationCount", pathPlanningRepository.countRecalculations(vehicleId, oneHourAgo)
            ));
            
            // Obstacle Avoidance Status
            List<ObstacleAvoidanceData> recentAvoidanceData = obstacleAvoidanceRepository
                .findRecentAvoidanceData(vehicleId, oneHourAgo);
            
            status.put("obstacleAvoidance", Map.of(
                "active", !recentAvoidanceData.isEmpty(),
                "detectionCount", recentAvoidanceData.size(),
                "averageResponseTime", obstacleAvoidanceRepository.getAverageResponseTime(vehicleId, oneHourAgo),
                "successRate", obstacleAvoidanceRepository.getSuccessRate(vehicleId, oneHourAgo),
                "emergencyActions", obstacleAvoidanceRepository.findEmergencyActions(vehicleId).size()
            ));
            
            // Overall System Health
            status.put("overallHealth", calculateOverallAIHealth(status));
            status.put("lastUpdate", LocalDateTime.now());
            
            log.info("AI system status retrieved successfully for vehicle: {}", vehicleId);
            return status;
            
        } catch (Exception e) {
            log.error("Error getting AI system status for vehicle: {}", vehicleId, e);
            throw new RuntimeException("Failed to get AI system status", e);
        }
    }
    
    // Yardımcı metodlar
    private String extractString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Integer extractInteger(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
    
    private Long extractLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    private Double extractDouble(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
    
    private Boolean extractBoolean(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }
    
    // Enum extraction metodları ve hesaplama metodları burada devam edecek...
    // (Implementation details için specific business logic gerekli)
    
    private void handleEmergencyVisionDetection(ComputerVisionData data) {
        log.warn("Emergency vision detection for vehicle: {}", data.getVehicleId());
        // Emergency handling logic
    }
    
    private void handleEmergencyFusionDecision(SensorFusionData data) {
        log.warn("Emergency fusion decision for vehicle: {}", data.getVehicleId());
        // Emergency handling logic
    }
    
    private void handleRouteRecalculation(PathPlanningData data) {
        log.info("Route recalculation needed for vehicle: {}", data.getVehicleId());
        // Route recalculation logic
    }
    
    private void handleEmergencyObstacleAvoidance(ObstacleAvoidanceData data) {
        log.warn("Emergency obstacle avoidance for vehicle: {}", data.getVehicleId());
        // Emergency handling logic
    }
    
    private double calculateOverallAIHealth(Map<String, Object> status) {
        // Calculate overall AI system health score
        return 0.95; // Placeholder
    }
    
    // Placeholder implementations for extraction methods
    private ComputerVisionData.CameraPosition extractCameraPosition(Map<String, Object> data) {
        return ComputerVisionData.CameraPosition.FRONT;
    }
    
    private String calculateCollisionRisk(Map<String, Object> data) {
        return "LOW";
    }
    
    private Boolean detectEmergencyFromVision(Map<String, Object> data) {
        return false;
    }
    
    private Boolean shouldTriggerObstacleAvoidance(Map<String, Object> data) {
        return false;
    }
    
    private Boolean shouldApplyAutomaticBraking(Map<String, Object> data) {
        return false;
    }
    
    // Sensor fusion helper methods
    private Double calculateFusionConfidence(Map<String, Object> data) {
        return 0.85;
    }
    
    private Double calculateSensorAgreement(Map<String, Object> data) {
        return 0.90;
    }
    
    private Double calculateDataConsistency(Map<String, Object> data) {
        return 0.88;
    }
    
    private SensorFusionData.AIDecisionType determineAIDecision(Map<String, Object> data) {
        return SensorFusionData.AIDecisionType.CONTINUE_STRAIGHT;
    }
    
    private Double calculateDecisionConfidence(Map<String, Object> data) {
        return 0.92;
    }
    
    private String determineRecommendedAction(Map<String, Object> data) {
        return "MAINTAIN_COURSE";
    }
    
    private Integer calculateActionPriority(Map<String, Object> data) {
        return 5;
    }
    
    private Double calculateEnvironmentalRisk(Map<String, Object> data) {
        return 0.3;
    }
    
    private Double calculateSensorHealth(Map<String, Object> data) {
        return 0.95;
    }
    
    // Path planning helper methods
    private PathPlanningData.PlanningAlgorithm extractPlanningAlgorithm(Map<String, Object> data) {
        return PathPlanningData.PlanningAlgorithm.A_STAR;
    }
    
    private PathPlanningData.RouteStatus extractRouteStatus(Map<String, Object> data) {
        return PathPlanningData.RouteStatus.ACTIVE;
    }
    
    private Double calculateRouteSafety(Map<String, Object> data) {
        return 0.9;
    }
    
    private Double calculateRouteEfficiency(Map<String, Object> data) {
        return 0.85;
    }
    
    private Double calculateRouteComfort(Map<String, Object> data) {
        return 0.8;
    }
    
    private Double calculateEcoFriendliness(Map<String, Object> data) {
        return 0.75;
    }
    
    private String calculateEnergyRating(Map<String, Object> data) {
        return "A";
    }
    
    // Obstacle avoidance helper methods
    private ObstacleAvoidanceData.ObstacleType extractObstacleType(Map<String, Object> data) {
        return ObstacleAvoidanceData.ObstacleType.VEHICLE;
    }
    
    private ObstacleAvoidanceData.ObstacleSeverity calculateObstacleSeverity(Map<String, Object> data) {
        return ObstacleAvoidanceData.ObstacleSeverity.MEDIUM;
    }
    
    private Double calculateCollisionProbability(Map<String, Object> data) {
        return 0.2;
    }
    
    private Double calculateTimeToCollision(Map<String, Object> data) {
        return 5.0;
    }
    
    private Double calculateImpactRisk(Map<String, Object> data) {
        return 3.0;
    }
    
    private ObstacleAvoidanceData.AvoidanceAction determineAvoidanceAction(Map<String, Object> data) {
        return ObstacleAvoidanceData.AvoidanceAction.REDUCE_SPEED;
    }
}