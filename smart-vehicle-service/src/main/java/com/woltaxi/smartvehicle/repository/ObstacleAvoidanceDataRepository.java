package com.woltaxi.smartvehicle.repository;

import com.woltaxi.smartvehicle.entity.ObstacleAvoidanceData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Obstacle Avoidance Data Repository
 * Engel algılama ve kaçınma stratejilerini yönetir
 */
@Repository
public interface ObstacleAvoidanceDataRepository extends JpaRepository<ObstacleAvoidanceData, Long> {
    
    // Araç bazlı sorgular
    List<ObstacleAvoidanceData> findByVehicleIdOrderByTimestampDesc(String vehicleId);
    
    Page<ObstacleAvoidanceData> findByVehicleIdOrderByTimestampDesc(String vehicleId, Pageable pageable);
    
    // Zaman bazlı sorgular
    List<ObstacleAvoidanceData> findByVehicleIdAndTimestampBetween(
        String vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.timestamp >= :startTime ORDER BY oa.timestamp DESC")
    List<ObstacleAvoidanceData> findRecentAvoidanceData(
        @Param("vehicleId") String vehicleId, 
        @Param("startTime") LocalDateTime startTime);
    
    // Engel tipi bazlı
    List<ObstacleAvoidanceData> findByVehicleIdAndObstacleTypeOrderByTimestampDesc(
        String vehicleId, ObstacleAvoidanceData.ObstacleType obstacleType);
    
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.obstacleType IN :obstacleTypes ORDER BY oa.timestamp DESC")
    List<ObstacleAvoidanceData> findByObstacleTypes(
        @Param("vehicleId") String vehicleId,
        @Param("obstacleTypes") List<ObstacleAvoidanceData.ObstacleType> obstacleTypes);
    
    // Tehlike seviyesi bazlı
    List<ObstacleAvoidanceData> findByVehicleIdAndObstacleSeverityOrderByTimestampDesc(
        String vehicleId, ObstacleAvoidanceData.ObstacleSeverity severity);
    
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.obstacleSeverity IN ('HIGH', 'CRITICAL', 'EMERGENCY') " +
           "ORDER BY oa.timestamp DESC")
    List<ObstacleAvoidanceData> findHighSeverityObstacles(@Param("vehicleId") String vehicleId);
    
    // Acil durum eylemleri
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.avoidanceAction IN ('EMERGENCY_BRAKE', 'EMERGENCY_STOP', 'REQUEST_HUMAN_CONTROL') " +
           "ORDER BY oa.timestamp DESC")
    List<ObstacleAvoidanceData> findEmergencyActions(@Param("vehicleId") String vehicleId);
    
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.avoidanceAction IN " +
           "('EMERGENCY_BRAKE', 'EMERGENCY_STOP', 'REQUEST_HUMAN_CONTROL') " +
           "AND oa.timestamp >= :startTime ORDER BY oa.timestamp DESC")
    List<ObstacleAvoidanceData> findAllEmergencyActions(@Param("startTime") LocalDateTime startTime);
    
    // Çarpışma riski analizi
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.collisionProbability >= :minProbability ORDER BY oa.collisionProbability DESC")
    List<ObstacleAvoidanceData> findHighCollisionRisk(
        @Param("vehicleId") String vehicleId,
        @Param("minProbability") Double minProbability);
    
    @Query("SELECT AVG(oa.collisionProbability) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.timestamp >= :startTime")
    Double getAverageCollisionProbability(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Çarpışmaya kalan süre
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.timeToCollisionSeconds <= :maxTime ORDER BY oa.timeToCollisionSeconds ASC")
    List<ObstacleAvoidanceData> findImmediateCollisionRisk(
        @Param("vehicleId") String vehicleId,
        @Param("maxTime") Double maxTime);
    
    @Query("SELECT AVG(oa.timeToCollisionSeconds) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.timeToCollisionSeconds IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageTimeToCollision(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Eylem türü analizi
    @Query("SELECT oa.avoidanceAction, COUNT(oa) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.timestamp >= :startTime " +
           "GROUP BY oa.avoidanceAction ORDER BY COUNT(oa) DESC")
    List<Object[]> getActionFrequencyStats(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Başarı oranı analizi
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.actionExecutedSuccessfully = true AND oa.timestamp >= :startTime")
    Long countSuccessfulActions(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.collisionAvoided = true AND oa.timestamp >= :startTime")
    Long countAvoidedCollisions(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN oa.actionExecutedSuccessfully = true THEN 1 END) AS DOUBLE) / " +
           "CAST(COUNT(oa) AS DOUBLE) * 100.0 " +
           "FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.timestamp >= :startTime")
    Double getSuccessRate(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Tepki süresi analizi
    @Query("SELECT AVG(oa.totalResponseTimeMs) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.totalResponseTimeMs IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageResponseTime(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.totalResponseTimeMs <= :maxTime ORDER BY oa.totalResponseTimeMs ASC")
    List<ObstacleAvoidanceData> findFastResponseTimes(
        @Param("vehicleId") String vehicleId,
        @Param("maxTime") Long maxTime);
    
    // AI işlem süresi
    @Query("SELECT AVG(oa.aiProcessingTimeMs) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.aiProcessingTimeMs IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageAIProcessingTime(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Sensör algılama analizi
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.detectedByLidar = true AND oa.timestamp >= :startTime")
    Long countLidarDetections(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.detectedByRadar = true AND oa.timestamp >= :startTime")
    Long countRadarDetections(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.detectedByCamera = true AND oa.timestamp >= :startTime")
    Long countCameraDetections(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND ((oa.detectedByLidar = true AND oa.detectedByRadar = true) OR " +
           "(oa.detectedByLidar = true AND oa.detectedByCamera = true) OR " +
           "(oa.detectedByRadar = true AND oa.detectedByCamera = true)) " +
           "AND oa.timestamp >= :startTime")
    Long countMultiSensorDetections(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Sensör füzyon güvenilirliği
    @Query("SELECT AVG(oa.sensorFusionConfidence) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.sensorFusionConfidence IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageFusionConfidence(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Karar güvenilirliği
    @Query("SELECT AVG(oa.decisionConfidenceScore) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.decisionConfidenceScore IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageDecisionConfidence(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Mesafe analizi
    @Query("SELECT AVG(oa.distanceToObstacle) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.distanceToObstacle IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageObstacleDistance(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.distanceToObstacle <= :maxDistance ORDER BY oa.distanceToObstacle ASC")
    List<ObstacleAvoidanceData> findCloseObstacles(
        @Param("vehicleId") String vehicleId,
        @Param("maxDistance") Double maxDistance);
    
    // Güvenlik marjı
    @Query("SELECT AVG(oa.safetyMarginMeters) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.safetyMarginMeters IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageSafetyMargin(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Yol sapması
    @Query("SELECT AVG(oa.pathDeviationMeters) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.pathDeviationMeters IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAveragePathDeviation(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Yaya algılamaları
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.obstacleType = 'PEDESTRIAN' ORDER BY oa.timestamp DESC")
    List<ObstacleAvoidanceData> findPedestrianDetections(@Param("vehicleId") String vehicleId);
    
    // Bisikletli algılamaları
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.obstacleType = 'CYCLIST' ORDER BY oa.timestamp DESC")
    List<ObstacleAvoidanceData> findCyclistDetections(@Param("vehicleId") String vehicleId);
    
    // Hava durumu etkisi
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.weatherCondition = :weather ORDER BY oa.timestamp DESC")
    List<ObstacleAvoidanceData> findByWeatherCondition(
        @Param("vehicleId") String vehicleId,
        @Param("weather") String weather);
    
    // Görüş mesafesi etkisi
    @Query("SELECT AVG(oa.visibilityMeters) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.visibilityMeters IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageVisibility(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // İnsan müdahalesi
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.humanInterventionRequired = true AND oa.timestamp >= :startTime")
    Long countHumanInterventions(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.humanTakeoverRequested = true AND oa.timestamp >= :startTime")
    Long countTakeoverRequests(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Acil uyarılar
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.emergencyAlertSent = true AND oa.timestamp >= :startTime")
    Long countEmergencyAlerts(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Konfor etkisi
    @Query("SELECT oa.passengerComfortImpact, COUNT(oa) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.timestamp >= :startTime " +
           "GROUP BY oa.passengerComfortImpact")
    List<Object[]> getComfortImpactStats(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Yakıt tüketimi etkisi
    @Query("SELECT AVG(oa.fuelConsumptionImpact) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.fuelConsumptionImpact IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageFuelImpact(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // ML model performansı
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.mlModelVersion = :modelVersion " +
           "AND oa.timestamp >= :startTime ORDER BY oa.decisionConfidenceScore DESC")
    List<ObstacleAvoidanceData> findByMLModelVersion(
        @Param("modelVersion") String modelVersion,
        @Param("startTime") LocalDateTime startTime);
    
    // Alternatif eylemler
    @Query("SELECT AVG(oa.alternativeActionsConsidered) FROM ObstacleAvoidanceData oa " +
           "WHERE oa.vehicleId = :vehicleId AND oa.alternativeActionsConsidered IS NOT NULL " +
           "AND oa.timestamp >= :startTime")
    Double getAverageAlternativeActions(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // En son güvenilir algılama
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.sensorFusionConfidence >= 0.8 AND oa.decisionConfidenceScore >= 0.8 " +
           "ORDER BY oa.timestamp DESC LIMIT 1")
    Optional<ObstacleAvoidanceData> findLatestReliableDetection(@Param("vehicleId") String vehicleId);
    
    // Performans özeti
    @Query("SELECT COUNT(oa) FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.actionExecutedSuccessfully = true AND oa.collisionAvoided = true " +
           "AND oa.totalResponseTimeMs <= 500 AND oa.timestamp >= :startTime")
    Long countOptimalPerformance(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Trafik yoğunluğu etkisi
    @Query("SELECT oa FROM ObstacleAvoidanceData oa WHERE oa.vehicleId = :vehicleId " +
           "AND oa.trafficDensity = :density ORDER BY oa.timestamp DESC")
    List<ObstacleAvoidanceData> findByTrafficDensity(
        @Param("vehicleId") String vehicleId,
        @Param("density") String density);
}