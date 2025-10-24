package com.woltaxi.smartvehicle.repository;

import com.woltaxi.smartvehicle.entity.PathPlanningData;
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
 * Path Planning Data Repository
 * Rota planlama ve navigasyon verilerini yönetir
 */
@Repository
public interface PathPlanningDataRepository extends JpaRepository<PathPlanningData, Long> {
    
    // Araç bazlı sorgular
    List<PathPlanningData> findByVehicleIdOrderByTimestampDesc(String vehicleId);
    
    Page<PathPlanningData> findByVehicleIdOrderByTimestampDesc(String vehicleId, Pageable pageable);
    
    // Zaman bazlı sorgular
    List<PathPlanningData> findByVehicleIdAndTimestampBetween(
        String vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.timestamp >= :startTime ORDER BY pp.timestamp DESC")
    List<PathPlanningData> findRecentPlanningData(
        @Param("vehicleId") String vehicleId, 
        @Param("startTime") LocalDateTime startTime);
    
    // Rota durumu bazlı
    List<PathPlanningData> findByVehicleIdAndRouteStatusOrderByTimestampDesc(
        String vehicleId, PathPlanningData.RouteStatus status);
    
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.routeStatus IN :statuses ORDER BY pp.timestamp DESC")
    List<PathPlanningData> findByRouteStatuses(
        @Param("vehicleId") String vehicleId,
        @Param("statuses") List<PathPlanningData.RouteStatus> statuses);
    
    // Aktif rotalar
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.routeStatus = 'ACTIVE' ORDER BY pp.timestamp DESC LIMIT 1")
    Optional<PathPlanningData> findActiveRoute(@Param("vehicleId") String vehicleId);
    
    // Algoritma performans analizi
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.planningAlgorithm = :algorithm " +
           "AND pp.timestamp >= :startTime ORDER BY pp.planningTimeMs ASC")
    List<PathPlanningData> findByAlgorithmPerformance(
        @Param("algorithm") PathPlanningData.PlanningAlgorithm algorithm,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT pp.planningAlgorithm, AVG(pp.planningTimeMs), AVG(pp.safetyScore) " +
           "FROM PathPlanningData pp WHERE pp.timestamp >= :startTime " +
           "GROUP BY pp.planningAlgorithm")
    List<Object[]> getAlgorithmPerformanceStats(@Param("startTime") LocalDateTime startTime);
    
    // Güvenlik skorları
    @Query("SELECT AVG(pp.safetyScore) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAverageSafetyScore(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.safetyScore < :minScore ORDER BY pp.safetyScore ASC")
    List<PathPlanningData> findLowSafetyRoutes(
        @Param("vehicleId") String vehicleId,
        @Param("minScore") Double minScore);
    
    // Verimlilik skorları
    @Query("SELECT AVG(pp.efficiencyScore) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAverageEfficiencyScore(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.efficiencyScore >= :minScore ORDER BY pp.efficiencyScore DESC")
    List<PathPlanningData> findHighEfficiencyRoutes(
        @Param("vehicleId") String vehicleId,
        @Param("minScore") Double minScore);
    
    // Yeniden hesaplama analizi
    @Query("SELECT COUNT(pp) FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.recalculationCount > 0 AND pp.timestamp >= :startTime")
    Long countRecalculations(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.recalculationCount >= :minCount ORDER BY pp.recalculationCount DESC")
    List<PathPlanningData> findFrequentRecalculations(
        @Param("vehicleId") String vehicleId,
        @Param("minCount") Integer minCount);
    
    // Trafik faktörü analizi
    @Query("SELECT AVG(pp.trafficFactor) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAverageTrafficFactor(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.trafficFactor >= :minFactor ORDER BY pp.trafficFactor DESC")
    List<PathPlanningData> findHighTrafficRoutes(
        @Param("vehicleId") String vehicleId,
        @Param("minFactor") Double minFactor);
    
    // Hava durumu etkisi
    @Query("SELECT AVG(pp.weatherFactor) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAverageWeatherFactor(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Engel analizi
    @Query("SELECT SUM(pp.obstaclesDetectedCount) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Long getTotalObstaclesDetected(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT SUM(pp.avoidanceManeuversCount) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Long getTotalAvoidanceManeuvers(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Acil durumlar
    @Query("SELECT SUM(pp.emergencyStopsCount) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Long getTotalEmergencyStops(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Şerit değiştirme analizi
    @Query("SELECT SUM(pp.laneChangesCount) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Long getTotalLaneChanges(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Yakıt tüketimi analizi
    @Query("SELECT AVG(pp.estimatedFuelConsumption) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAverageEstimatedFuelConsumption(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(pp.actualFuelConsumption) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.actualFuelConsumption IS NOT NULL " +
           "AND pp.timestamp >= :startTime")
    Double getAverageActualFuelConsumption(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Enerji verimliliği
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.energyEfficiencyRating IN ('A+', 'A') ORDER BY pp.timestamp DESC")
    List<PathPlanningData> findEcoFriendlyRoutes(@Param("vehicleId") String vehicleId);
    
    // Hız analizi
    @Query("SELECT AVG(pp.averageSpeedKmh) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAverageSpeed(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT MAX(pp.maxSpeedKmh) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getMaxSpeedReached(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Sürüş profili analizi
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.accelerationProfile = :profile ORDER BY pp.timestamp DESC")
    List<PathPlanningData> findByAccelerationProfile(
        @Param("vehicleId") String vehicleId,
        @Param("profile") String profile);
    
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.brakingProfile = :profile ORDER BY pp.timestamp DESC")
    List<PathPlanningData> findByBrakingProfile(
        @Param("vehicleId") String vehicleId,
        @Param("profile") String profile);
    
    // ML performansı
    @Query("SELECT AVG(pp.mlPredictionConfidence) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAverageMLConfidence(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(pp) FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.routeOptimizationApplied = true AND pp.timestamp >= :startTime")
    Long countOptimizedRoutes(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Rota tamamlama analizi
    @Query("SELECT AVG(pp.routeCompletionPercentage) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAverageCompletionPercentage(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.routeCompletionPercentage >= 95.0 ORDER BY pp.timestamp DESC")
    List<PathPlanningData> findCompletedRoutes(@Param("vehicleId") String vehicleId);
    
    // Yol sapması analizi
    @Query("SELECT AVG(pp.pathDeviationMeters) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAveragePathDeviation(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Varış zamanı doğruluğu
    @Query("SELECT AVG(ABS(pp.arrivalTimeAccuracySeconds)) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.arrivalTimeAccuracySeconds IS NOT NULL " +
           "AND pp.timestamp >= :startTime")
    Double getAverageArrivalAccuracy(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // İnşaat ve okul bölgesi analizi
    @Query("SELECT SUM(pp.constructionZonesCount) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Long getTotalConstructionZones(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT SUM(pp.schoolZonesCount) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Long getTotalSchoolZones(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Konfor skoru analizi
    @Query("SELECT AVG(pp.comfortScore) FROM PathPlanningData pp " +
           "WHERE pp.vehicleId = :vehicleId AND pp.timestamp >= :startTime")
    Double getAverageComfortScore(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Optimal rota performansı
    @Query("SELECT COUNT(pp) FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.safetyScore >= 0.8 AND pp.efficiencyScore >= 0.8 " +
           "AND pp.comfortScore >= 0.7 AND pp.timestamp >= :startTime")
    Long countOptimalRoutes(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // En son aktif planlama verisi
    @Query("SELECT pp FROM PathPlanningData pp WHERE pp.vehicleId = :vehicleId " +
           "AND pp.routeStatus IN ('PLANNING', 'ACTIVE', 'RECALCULATING') " +
           "ORDER BY pp.timestamp DESC LIMIT 1")
    Optional<PathPlanningData> findLatestActivePlanning(@Param("vehicleId") String vehicleId);
}