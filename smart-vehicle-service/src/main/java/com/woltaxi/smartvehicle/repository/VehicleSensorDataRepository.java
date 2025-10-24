package com.woltaxi.smartvehicle.repository;

import com.woltaxi.smartvehicle.entity.VehicleSensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Araç Sensör Verisi Repository - Gerçek zamanlı IoT veri yönetimi
 */
@Repository
public interface VehicleSensorDataRepository extends JpaRepository<VehicleSensorData, Long> {
    
    // Temel Sorgular
    List<VehicleSensorData> findByVehicleId(Long vehicleId);
    List<VehicleSensorData> findBySensorType(String sensorType);
    List<VehicleSensorData> findByVehicleIdAndSensorType(Long vehicleId, String sensorType);
    
    // Zaman Bazlı Sorgular
    List<VehicleSensorData> findByVehicleIdAndTimestampAfter(Long vehicleId, LocalDateTime timestamp);
    List<VehicleSensorData> findByVehicleIdAndTimestampBetween(Long vehicleId, LocalDateTime start, LocalDateTime end);
    
    // Son N dakikadaki veriler
    @Query("SELECT s FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId AND " +
           "s.timestamp >= :since ORDER BY s.timestamp DESC")
    List<VehicleSensorData> findRecentDataByVehicleId(
        @Param("vehicleId") Long vehicleId, 
        @Param("since") LocalDateTime since
    );
    
    // Belirli sensör tipinin son verileri
    @Query("SELECT s FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId AND " +
           "s.sensorType = :sensorType ORDER BY s.timestamp DESC")
    List<VehicleSensorData> findLatestBySensorType(
        @Param("vehicleId") Long vehicleId,
        @Param("sensorType") String sensorType
    );
    
    // Veri Kalitesi Analizi
    List<VehicleSensorData> findByDataValidatedFalse();
    List<VehicleSensorData> findByRequiresAttentionTrue();
    List<VehicleSensorData> findByValidationStatus(String status);
    
    @Query("SELECT s FROM VehicleSensorData s WHERE s.dataQualityScore < :threshold")
    List<VehicleSensorData> findLowQualityData(@Param("threshold") Double threshold);
    
    // GPS Verisi Analizi
    @Query("SELECT s FROM VehicleSensorData s WHERE s.sensorType = 'GPS' AND " +
           "s.vehicle.id = :vehicleId AND s.gpsAccuracy <= :maxAccuracy " +
           "ORDER BY s.timestamp DESC")
    List<VehicleSensorData> findAccurateGPSData(
        @Param("vehicleId") Long vehicleId,
        @Param("maxAccuracy") Double maxAccuracy
    );
    
    // Hareket Halindeki Araç Verileri
    @Query("SELECT s FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId AND " +
           "s.speed > :minSpeed ORDER BY s.timestamp DESC")
    List<VehicleSensorData> findMovingVehicleData(
        @Param("vehicleId") Long vehicleId,
        @Param("minSpeed") Double minSpeed
    );
    
    // Kritik Sensör Alarmları
    @Query("SELECT s FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId AND " +
           "(s.engineTemperature > 90 OR s.batteryVoltage < 11.5 OR " +
           "s.tirePressureFrontLeft < 25 OR s.tirePressureFrontRight < 25 OR " +
           "s.tirePressureRearLeft < 25 OR s.tirePressureRearRight < 25)")
    List<VehicleSensorData> findCriticalSensorAlerts(@Param("vehicleId") Long vehicleId);
    
    // LIDAR Analizi
    @Query("SELECT s FROM VehicleSensorData s WHERE s.sensorType = 'LIDAR' AND " +
           "s.vehicle.id = :vehicleId AND s.objectsDetected > :minObjects " +
           "ORDER BY s.timestamp DESC")
    List<VehicleSensorData> findLidarDetections(
        @Param("vehicleId") Long vehicleId,
        @Param("minObjects") Integer minObjects
    );
    
    // Yakın Nesne Tespit
    @Query("SELECT s FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId AND " +
           "s.closestObjectDistance IS NOT NULL AND s.closestObjectDistance <= :maxDistance " +
           "ORDER BY s.timestamp DESC")
    List<VehicleSensorData> findNearbyObjectDetections(
        @Param("vehicleId") Long vehicleId,
        @Param("maxDistance") Double maxDistance
    );
    
    // Hava Durumu Korelasyonu
    List<VehicleSensorData> findByWeatherCondition(String weatherCondition);
    
    @Query("SELECT s FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId AND " +
           "s.weatherCondition = :weather AND s.timestamp >= :since")
    List<VehicleSensorData> findByVehicleAndWeatherSince(
        @Param("vehicleId") Long vehicleId,
        @Param("weather") String weather,
        @Param("since") LocalDateTime since
    );
    
    // AI Tahmin Analizi
    @Query("SELECT s FROM VehicleSensorData s WHERE s.predictionConfidence >= :minConfidence AND " +
           "s.aiPredictions IS NOT NULL ORDER BY s.timestamp DESC")
    List<VehicleSensorData> findHighConfidenceAIPredictions(@Param("minConfidence") Double minConfidence);
    
    // V2X İletişim Verileri
    @Query("SELECT s FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId AND " +
           "(s.v2vMessages IS NOT NULL OR s.v2iMessages IS NOT NULL OR s.v2pMessages IS NOT NULL)")
    List<VehicleSensorData> findV2XCommunicationData(@Param("vehicleId") Long vehicleId);
    
    // Anomali Tespiti
    @Query("SELECT s FROM VehicleSensorData s WHERE s.anomaliesDetected IS NOT NULL AND " +
           "s.vehicle.id = :vehicleId ORDER BY s.timestamp DESC")
    List<VehicleSensorData> findAnomalies(@Param("vehicleId") Long vehicleId);
    
    // İstatistiksel Analiz
    @Query("SELECT COUNT(s) FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId AND " +
           "s.sensorType = :sensorType AND s.timestamp >= :since")
    Long countBySensorTypeAndTimestamp(
        @Param("vehicleId") Long vehicleId,
        @Param("sensorType") String sensorType,
        @Param("since") LocalDateTime since
    );
    
    @Query("SELECT AVG(s.dataQualityScore) FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId")
    Double getAverageDataQualityByVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT s.sensorType, COUNT(s), AVG(s.dataQualityScore) FROM VehicleSensorData s " +
           "WHERE s.vehicle.id = :vehicleId AND s.timestamp >= :since " +
           "GROUP BY s.sensorType")
    List<Object[]> getSensorStatsForVehicle(
        @Param("vehicleId") Long vehicleId,
        @Param("since") LocalDateTime since
    );
    
    // Performans Optimizasyonu için Indeksli Sorgular
    @Query("SELECT s FROM VehicleSensorData s WHERE s.vehicle.id = :vehicleId AND " +
           "s.sensorType = 'GPS' AND s.timestamp >= :since " +
           "ORDER BY s.timestamp DESC LIMIT 100")
    List<VehicleSensorData> findRecentGPSDataOptimized(
        @Param("vehicleId") Long vehicleId,
        @Param("since") LocalDateTime since
    );
    
    // Batch İşlemler için
    @Query("SELECT s FROM VehicleSensorData s WHERE s.dataValidated = false AND " +
           "s.createdAt <= :threshold ORDER BY s.createdAt ASC")
    List<VehicleSensorData> findUnvalidatedDataBefore(@Param("threshold") LocalDateTime threshold);
    
    // Real-time Monitoring
    @Query(value = "SELECT * FROM vehicle_sensor_data WHERE vehicle_id = :vehicleId AND " +
           "timestamp >= NOW() - INTERVAL '5 MINUTE' ORDER BY timestamp DESC LIMIT 1000",
           nativeQuery = true)
    List<VehicleSensorData> findRealtimeData(@Param("vehicleId") Long vehicleId);
    
    // Makine Öğrenmesi için Temiz Veri
    @Query("SELECT s FROM VehicleSensorData s WHERE s.dataValidated = true AND " +
           "s.dataQualityScore >= :minQuality AND s.timestamp >= :since")
    List<VehicleSensorData> findCleanDataForML(
        @Param("minQuality") Double minQuality,
        @Param("since") LocalDateTime since
    );
}