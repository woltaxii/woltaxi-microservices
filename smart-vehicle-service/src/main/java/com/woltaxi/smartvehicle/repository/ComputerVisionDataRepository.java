package com.woltaxi.smartvehicle.repository;

import com.woltaxi.smartvehicle.entity.ComputerVisionData;
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
 * Computer Vision Data Repository
 * Görüntü işleme ve nesne tanıma verilerini yönetir
 */
@Repository
public interface ComputerVisionDataRepository extends JpaRepository<ComputerVisionData, Long> {
    
    // Araç bazlı sorgular
    List<ComputerVisionData> findByVehicleIdOrderByTimestampDesc(String vehicleId);
    
    Page<ComputerVisionData> findByVehicleIdOrderByTimestampDesc(String vehicleId, Pageable pageable);
    
    // Zaman bazlı sorgular
    List<ComputerVisionData> findByVehicleIdAndTimestampBetween(
        String vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.timestamp >= :startTime ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findRecentDataByVehicle(
        @Param("vehicleId") String vehicleId, 
        @Param("startTime") LocalDateTime startTime);
    
    // Güven skoru bazlı sorgular
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.confidenceScore >= :minConfidence ORDER BY cv.confidenceScore DESC")
    List<ComputerVisionData> findHighConfidenceDetections(
        @Param("vehicleId") String vehicleId, 
        @Param("minConfidence") Double minConfidence);
    
    // Nesne tipi bazlı sorgular
    List<ComputerVisionData> findByVehicleIdAndDetectedObjectTypeOrderByTimestampDesc(
        String vehicleId, String objectType);
    
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.detectedObjectType IN :objectTypes " +
           "AND cv.timestamp >= :startTime ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findByObjectTypes(
        @Param("vehicleId") String vehicleId,
        @Param("objectTypes") List<String> objectTypes,
        @Param("startTime") LocalDateTime startTime);
    
    // Acil durum sorguları
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND (cv.emergencyDetected = true OR cv.collisionRiskLevel IN ('HIGH', 'CRITICAL')) " +
           "ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findEmergencySituations(@Param("vehicleId") String vehicleId);
    
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.emergencyDetected = true " +
           "AND cv.timestamp >= :startTime ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findAllEmergencySituations(@Param("startTime") LocalDateTime startTime);
    
    // Risk seviyesi sorguları
    List<ComputerVisionData> findByVehicleIdAndCollisionRiskLevelOrderByTimestampDesc(
        String vehicleId, String riskLevel);
    
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.collisionRiskLevel IN ('HIGH', 'CRITICAL') " +
           "AND cv.timestamp >= :startTime ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findHighRiskSituations(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Performans analizi
    @Query("SELECT AVG(cv.confidenceScore) FROM ComputerVisionData cv " +
           "WHERE cv.vehicleId = :vehicleId AND cv.timestamp >= :startTime")
    Double getAverageConfidenceScore(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(cv.processingTimeMs) FROM ComputerVisionData cv " +
           "WHERE cv.vehicleId = :vehicleId AND cv.timestamp >= :startTime")
    Double getAverageProcessingTime(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Kamera pozisyonu bazlı
    List<ComputerVisionData> findByVehicleIdAndCameraPositionOrderByTimestampDesc(
        String vehicleId, ComputerVisionData.CameraPosition cameraPosition);
    
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.cameraPosition = :position AND cv.timestamp >= :startTime " +
           "ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findByCameraPositionAndTimeRange(
        @Param("vehicleId") String vehicleId,
        @Param("position") ComputerVisionData.CameraPosition position,
        @Param("startTime") LocalDateTime startTime);
    
    // Trafik analizi
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.trafficLightStatus IS NOT NULL " +
           "AND cv.timestamp >= :startTime ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findTrafficLightDetections(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.roadSignDetected IS NOT NULL " +
           "AND cv.timestamp >= :startTime ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findRoadSignDetections(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Yaya ve araç sayısı
    @Query("SELECT SUM(cv.pedestrianCount) FROM ComputerVisionData cv " +
           "WHERE cv.vehicleId = :vehicleId AND cv.timestamp >= :startTime")
    Long getTotalPedestrianCount(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT SUM(cv.vehicleCount) FROM ComputerVisionData cv " +
           "WHERE cv.vehicleId = :vehicleId AND cv.timestamp >= :startTime")
    Long getTotalVehicleCount(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Hava durumu ve görüş koşulları
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.visibilityCondition = :condition " +
           "AND cv.timestamp >= :startTime ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findByVisibilityCondition(
        @Param("vehicleId") String vehicleId,
        @Param("condition") String condition,
        @Param("startTime") LocalDateTime startTime);
    
    // Otomatik fren durumları
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.automaticBrakingApplied = true " +
           "ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findAutomaticBrakingEvents(@Param("vehicleId") String vehicleId);
    
    // AI model performansı
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.aiModelVersion = :modelVersion " +
           "AND cv.timestamp >= :startTime ORDER BY cv.confidenceScore DESC")
    List<ComputerVisionData> findByAIModelVersion(
        @Param("modelVersion") String modelVersion,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT cv.aiModelVersion, AVG(cv.confidenceScore), AVG(cv.processingTimeMs) " +
           "FROM ComputerVisionData cv WHERE cv.timestamp >= :startTime " +
           "GROUP BY cv.aiModelVersion")
    List<Object[]> getModelPerformanceStats(@Param("startTime") LocalDateTime startTime);
    
    // Engel kaçınma tetiklemeleri
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.obstacleAvoidanceTriggered = true " +
           "ORDER BY cv.timestamp DESC")
    List<ComputerVisionData> findObstacleAvoidanceEvents(@Param("vehicleId") String vehicleId);
    
    // GPU kullanım analizi
    @Query("SELECT AVG(cv.gpuUsagePercent) FROM ComputerVisionData cv " +
           "WHERE cv.vehicleId = :vehicleId AND cv.timestamp >= :startTime")
    Double getAverageGpuUsage(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // En son güvenilir veri
    @Query("SELECT cv FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.confidenceScore >= 0.8 ORDER BY cv.timestamp DESC LIMIT 1")
    Optional<ComputerVisionData> findLatestReliableData(@Param("vehicleId") String vehicleId);
    
    // Zaman aralığında toplam işlem sayısı
    @Query("SELECT COUNT(cv) FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.timestamp BETWEEN :startTime AND :endTime")
    Long countProcessedFrames(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    // Şerit algılama performansı
    @Query("SELECT COUNT(cv) FROM ComputerVisionData cv WHERE cv.vehicleId = :vehicleId " +
           "AND cv.laneDetectionActive = true AND cv.timestamp >= :startTime")
    Long countLaneDetectionActive(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
}