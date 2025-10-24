package com.woltaxi.smartvehicle.repository;

import com.woltaxi.smartvehicle.entity.AutonomousDrivingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Otonom Sürüş Oturumu Repository
 */
@Repository
public interface AutonomousDrivingSessionRepository extends JpaRepository<AutonomousDrivingSession, Long> {
    
    // Temel Sorgular
    List<AutonomousDrivingSession> findByVehicleId(Long vehicleId);
    List<AutonomousDrivingSession> findByRideId(Long rideId);
    
    // Aktif Oturum
    Optional<AutonomousDrivingSession> findByVehicleIdAndSessionEndTimeIsNull(Long vehicleId);
    
    // Zaman Bazlı Sorgular
    List<AutonomousDrivingSession> findByVehicleIdAndCreatedAtAfter(Long vehicleId, LocalDateTime startDate);
    List<AutonomousDrivingSession> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Performans Analizi
    @Query("SELECT s FROM AutonomousDrivingSession s WHERE s.vehicle.id = :vehicleId AND " +
           "s.safetyScore IS NOT NULL ORDER BY s.safetyScore DESC")
    List<AutonomousDrivingSession> findByVehicleIdOrderBySafetyScore(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT s FROM AutonomousDrivingSession s WHERE s.vehicle.id = :vehicleId AND " +
           "s.efficiencyScore IS NOT NULL ORDER BY s.efficiencyScore DESC")
    List<AutonomousDrivingSession> findByVehicleIdOrderByEfficiencyScore(@Param("vehicleId") Long vehicleId);
    
    // Kritik Olaylar
    List<AutonomousDrivingSession> findByCriticalIncidentTrue();
    List<AutonomousDrivingSession> findByVehicleIdAndCriticalIncidentTrue(Long vehicleId);
    
    // İnsan Müdahalesi Analizi
    @Query("SELECT s FROM AutonomousDrivingSession s WHERE s.humanInterventionCount > :threshold")
    List<AutonomousDrivingSession> findSessionsWithHighInterventions(@Param("threshold") Integer threshold);
    
    // Otonom Mod Analizi
    List<AutonomousDrivingSession> findByAutonomyMode(AutonomousDrivingSession.AutonomyMode autonomyMode);
    
    @Query("SELECT s FROM AutonomousDrivingSession s WHERE s.autonomyMode IN :modes AND " +
           "s.createdAt >= :startDate")
    List<AutonomousDrivingSession> findByAutonomyModeInAndCreatedAtAfter(
        @Param("modes") List<AutonomousDrivingSession.AutonomyMode> modes,
        @Param("startDate") LocalDateTime startDate
    );
    
    // İstatistiksel Sorgular
    @Query("SELECT COUNT(s) FROM AutonomousDrivingSession s WHERE s.vehicle.id = :vehicleId")
    Long countSessionsByVehicleId(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT AVG(s.safetyScore) FROM AutonomousDrivingSession s WHERE s.vehicle.id = :vehicleId")
    Double getAverageSafetyScoreByVehicleId(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT AVG(s.efficiencyScore) FROM AutonomousDrivingSession s WHERE s.vehicle.id = :vehicleId")
    Double getAverageEfficiencyScoreByVehicleId(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT SUM(s.totalDistance) FROM AutonomousDrivingSession s WHERE s.vehicle.id = :vehicleId")
    Double getTotalDistanceByVehicleId(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT AVG(s.averageSpeed) FROM AutonomousDrivingSession s WHERE s.vehicle.id = :vehicleId")
    Double getAverageSpeedByVehicleId(@Param("vehicleId") Long vehicleId);
    
    // Hava Durumu Analizi
    List<AutonomousDrivingSession> findByWeatherCondition(String weatherCondition);
    List<AutonomousDrivingSession> findByRoadCondition(String roadCondition);
    
    @Query("SELECT s FROM AutonomousDrivingSession s WHERE s.weatherCondition IN :conditions AND " +
           "s.createdAt >= :startDate")
    List<AutonomousDrivingSession> findByWeatherConditionsAndDate(
        @Param("conditions") List<String> conditions,
        @Param("startDate") LocalDateTime startDate
    );
    
    // Makine Öğrenmesi Verileri
    List<AutonomousDrivingSession> findByDataUsedForTrainingTrue();
    List<AutonomousDrivingSession> findByDataUsedForTrainingFalse();
    
    // Complex Analytics Queries
    @Query("SELECT s.weatherCondition, COUNT(s), AVG(s.safetyScore), AVG(s.humanInterventionCount) " +
           "FROM AutonomousDrivingSession s WHERE s.createdAt >= :startDate " +
           "GROUP BY s.weatherCondition")
    List<Object[]> getPerformanceByWeatherCondition(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT s.autonomyMode, COUNT(s), AVG(s.safetyScore), AVG(s.aiConfidenceAverage) " +
           "FROM AutonomousDrivingSession s WHERE s.createdAt >= :startDate " +
           "GROUP BY s.autonomyMode")
    List<Object[]> getPerformanceByAutonomyMode(@Param("startDate") LocalDateTime startDate);
    
    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as sessions, " +
           "AVG(safety_score) as avg_safety, AVG(ai_confidence_average) as avg_confidence " +
           "FROM autonomous_driving_sessions WHERE created_at >= :startDate " +
           "GROUP BY DATE(created_at) ORDER BY date",
           nativeQuery = true)
    List<Object[]> getDailyPerformanceStats(@Param("startDate") LocalDateTime startDate);
    
    // Yolcu Memnuniyeti Analizi
    @Query("SELECT AVG(s.passengerRating) FROM AutonomousDrivingSession s WHERE " +
           "s.vehicle.id = :vehicleId AND s.passengerRating IS NOT NULL")
    Double getAveragePassengerRatingByVehicleId(@Param("vehicleId") Long vehicleId);
    
    List<AutonomousDrivingSession> findByPassengerRatingGreaterThanEqual(Integer rating);
    List<AutonomousDrivingSession> findByPassengerRatingLessThan(Integer rating);
}