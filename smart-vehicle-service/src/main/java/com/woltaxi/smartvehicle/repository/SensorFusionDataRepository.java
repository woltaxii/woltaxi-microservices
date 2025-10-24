package com.woltaxi.smartvehicle.repository;

import com.woltaxi.smartvehicle.entity.SensorFusionData;
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
 * Sensor Fusion Data Repository
 * Sensör verilerinin füzyonu ve AI karar verme süreçlerini yönetir
 */
@Repository
public interface SensorFusionDataRepository extends JpaRepository<SensorFusionData, Long> {
    
    // Araç bazlı sorgular
    List<SensorFusionData> findByVehicleIdOrderByTimestampDesc(String vehicleId);
    
    Page<SensorFusionData> findByVehicleIdOrderByTimestampDesc(String vehicleId, Pageable pageable);
    
    // Zaman bazlı sorgular
    List<SensorFusionData> findByVehicleIdAndTimestampBetween(
        String vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.timestamp >= :startTime ORDER BY sf.timestamp DESC")
    List<SensorFusionData> findRecentFusionData(
        @Param("vehicleId") String vehicleId, 
        @Param("startTime") LocalDateTime startTime);
    
    // Fusion güven skoru bazlı
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.fusionConfidenceScore >= :minConfidence ORDER BY sf.fusionConfidenceScore DESC")
    List<SensorFusionData> findHighConfidenceFusions(
        @Param("vehicleId") String vehicleId, 
        @Param("minConfidence") Double minConfidence);
    
    // AI karar tipi bazlı
    List<SensorFusionData> findByVehicleIdAndAiDecisionTypeOrderByTimestampDesc(
        String vehicleId, SensorFusionData.AIDecisionType decisionType);
    
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.aiDecisionType IN :decisionTypes " +
           "AND sf.timestamp >= :startTime ORDER BY sf.timestamp DESC")
    List<SensorFusionData> findByDecisionTypes(
        @Param("vehicleId") String vehicleId,
        @Param("decisionTypes") List<SensorFusionData.AIDecisionType> decisionTypes,
        @Param("startTime") LocalDateTime startTime);
    
    // Acil durum kararları
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.aiDecisionType IN ('EMERGENCY_BRAKE', 'EMERGENCY_STOP', 'REQUEST_HUMAN_INTERVENTION') " +
           "ORDER BY sf.timestamp DESC")
    List<SensorFusionData> findEmergencyDecisions(@Param("vehicleId") String vehicleId);
    
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.aiDecisionType IN " +
           "('EMERGENCY_BRAKE', 'EMERGENCY_STOP', 'REQUEST_HUMAN_INTERVENTION') " +
           "AND sf.timestamp >= :startTime ORDER BY sf.timestamp DESC")
    List<SensorFusionData> findAllEmergencyDecisions(@Param("startTime") LocalDateTime startTime);
    
    // Yüksek öncelikli eylemler
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.actionPriorityLevel >= :minPriority " +
           "ORDER BY sf.actionPriorityLevel DESC, sf.timestamp DESC")
    List<SensorFusionData> findHighPriorityActions(
        @Param("vehicleId") String vehicleId,
        @Param("minPriority") Integer minPriority);
    
    // Sensör performans analizi
    @Query("SELECT AVG(sf.sensorAgreementLevel) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageSensorAgreement(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(sf.dataConsistencyScore) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageDataConsistency(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Performans metrikleri
    @Query("SELECT AVG(sf.fusionProcessingTimeMs) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageFusionProcessingTime(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(sf.totalPipelineTimeMs) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageTotalPipelineTime(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // LIDAR performansı
    @Query("SELECT AVG(sf.lidarAccuracy) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.lidarAccuracy IS NOT NULL " +
           "AND sf.timestamp >= :startTime")
    Double getAverageLidarAccuracy(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT SUM(sf.lidarObstaclesDetected) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Long getTotalLidarObstaclesDetected(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Radar performansı
    @Query("SELECT AVG(sf.radarRangeMeters) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.radarRangeMeters IS NOT NULL " +
           "AND sf.timestamp >= :startTime")
    Double getAverageRadarRange(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT SUM(sf.radarObjectsTracked) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Long getTotalRadarObjectsTracked(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // GPS doğruluğu
    @Query("SELECT AVG(sf.gpsAccuracyMeters) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.gpsAccuracyMeters IS NOT NULL " +
           "AND sf.timestamp >= :startTime")
    Double getAverageGpsAccuracy(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Çevresel risk analizi
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.environmentalRiskScore >= :minRiskScore " +
           "ORDER BY sf.environmentalRiskScore DESC, sf.timestamp DESC")
    List<SensorFusionData> findHighEnvironmentalRisk(
        @Param("vehicleId") String vehicleId,
        @Param("minRiskScore") Double minRiskScore);
    
    @Query("SELECT AVG(sf.environmentalRiskScore) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageEnvironmentalRisk(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Trafik yoğunluğu analizi
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.trafficDensityLevel >= :minDensity " +
           "ORDER BY sf.timestamp DESC")
    List<SensorFusionData> findHighTrafficDensity(
        @Param("vehicleId") String vehicleId,
        @Param("minDensity") Integer minDensity);
    
    @Query("SELECT AVG(sf.trafficDensityLevel) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageTrafficDensity(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Kalibrasyon durumu
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.calibrationStatus != 'OPTIMAL' ORDER BY sf.timestamp DESC")
    List<SensorFusionData> findCalibrationIssues(@Param("vehicleId") String vehicleId);
    
    @Query("SELECT COUNT(sf) FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.errorCorrectionApplied = true AND sf.timestamp >= :startTime")
    Long countErrorCorrections(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Outlier detection
    @Query("SELECT COUNT(sf) FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.outlierDetectionTriggered = true AND sf.timestamp >= :startTime")
    Long countOutlierDetections(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Machine Learning model performansı
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.mlModelVersion = :modelVersion " +
           "AND sf.timestamp >= :startTime ORDER BY sf.predictionAccuracy DESC")
    List<SensorFusionData> findByMLModelVersion(
        @Param("modelVersion") String modelVersion,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT sf.mlModelVersion, AVG(sf.predictionAccuracy), AVG(sf.decisionConfidence) " +
           "FROM SensorFusionData sf WHERE sf.timestamp >= :startTime " +
           "GROUP BY sf.mlModelVersion")
    List<Object[]> getMLModelPerformanceStats(@Param("startTime") LocalDateTime startTime);
    
    // IMU verisi analizi
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND (ABS(sf.imuAccelerationX) > :threshold OR " +
           "ABS(sf.imuAccelerationY) > :threshold OR " +
           "ABS(sf.imuAccelerationZ) > :threshold) " +
           "ORDER BY sf.timestamp DESC")
    List<SensorFusionData> findHighAccelerationEvents(
        @Param("vehicleId") String vehicleId,
        @Param("threshold") Double threshold);
    
    // Sensör sağlığı
    @Query("SELECT AVG(sf.sensorHealthScore) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageSensorHealth(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // En son güvenilir fusion verisi
    @Query("SELECT sf FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.fusionConfidenceScore >= 0.8 AND sf.sensorAgreementLevel >= 0.8 " +
           "ORDER BY sf.timestamp DESC LIMIT 1")
    Optional<SensorFusionData> findLatestReliableFusion(@Param("vehicleId") String vehicleId);
    
    // Karar güvenilirliği
    @Query("SELECT AVG(sf.decisionConfidence) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageDecisionConfidence(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Sistem genel performansı
    @Query("SELECT COUNT(sf) FROM SensorFusionData sf WHERE sf.vehicleId = :vehicleId " +
           "AND sf.fusionConfidenceScore >= 0.8 AND sf.sensorAgreementLevel >= 0.8 " +
           "AND sf.dataConsistencyScore >= 0.75 AND sf.timestamp >= :startTime")
    Long countOptimalPerformance(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Hava durumu etkisi
    @Query("SELECT AVG(sf.weatherImpactFactor) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageWeatherImpact(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Yol durumu etkisi
    @Query("SELECT AVG(sf.roadConditionScore) FROM SensorFusionData sf " +
           "WHERE sf.vehicleId = :vehicleId AND sf.timestamp >= :startTime")
    Double getAverageRoadCondition(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
}