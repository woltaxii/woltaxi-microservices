package com.woltaxi.smartvehicle.repository;

import com.woltaxi.smartvehicle.entity.VehicleIoTSensorData;
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
 * Vehicle IoT Sensor Data Repository
 * IoT sensor verilerini yönetir
 */
@Repository
public interface VehicleIoTSensorDataRepository extends JpaRepository<VehicleIoTSensorData, Long> {
    
    // Araç bazlı sorgular
    List<VehicleIoTSensorData> findByVehicleIdOrderByTimestampDesc(String vehicleId);
    
    Page<VehicleIoTSensorData> findByVehicleIdOrderByTimestampDesc(String vehicleId, Pageable pageable);
    
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.timestamp >= :startTime ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findRecentSensorData(
        @Param("vehicleId") String vehicleId, 
        @Param("startTime") LocalDateTime startTime);
    
    // Sensor tipi bazlı
    List<VehicleIoTSensorData> findByVehicleIdAndSensorTypeOrderByTimestampDesc(
        String vehicleId, VehicleIoTSensorData.SensorType sensorType);
    
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType IN :sensorTypes ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findBySensorTypes(
        @Param("vehicleId") String vehicleId,
        @Param("sensorTypes") List<VehicleIoTSensorData.SensorType> sensorTypes);
    
    // Sensor sağlığı bazlı
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorHealthStatus IN ('CRITICAL', 'FAILING', 'FAILED') " +
           "ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findUnhealthySensors(@Param("vehicleId") String vehicleId);
    
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorHealthStatus IN ('EXCELLENT', 'GOOD') " +
           "ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findHealthySensors(@Param("vehicleId") String vehicleId);
    
    // Kritik uyarılar
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.criticalAlert = true ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findCriticalAlerts(@Param("vehicleId") String vehicleId);
    
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.criticalAlert = true " +
           "AND iot.timestamp >= :startTime ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findAllCriticalAlerts(@Param("startTime") LocalDateTime startTime);
    
    // Bakım gereksinimleri
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND (iot.replacementRecommended = true OR iot.maintenanceDueDate <= :dueDate) " +
           "ORDER BY iot.maintenanceDueDate ASC")
    List<VehicleIoTSensorData> findMaintenanceRequired(
        @Param("vehicleId") String vehicleId,
        @Param("dueDate") LocalDateTime dueDate);
    
    // Tahmin edilen arıza riski
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.predictedFailureRisk >= :riskThreshold " +
           "ORDER BY iot.predictedFailureRisk DESC")
    List<VehicleIoTSensorData> findHighFailureRisk(
        @Param("vehicleId") String vehicleId,
        @Param("riskThreshold") Double riskThreshold);
    
    // Anomali tespiti
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.anomalyDetected = true ORDER BY iot.anomalyScore DESC")
    List<VehicleIoTSensorData> findAnomalousReadings(@Param("vehicleId") String vehicleId);
    
    // Bağlantı durumu
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND (iot.networkLatencyMs > 5000 OR iot.packetLossRate > 5.0) " +
           "ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findConnectivityIssues(@Param("vehicleId") String vehicleId);
    
    // Performans metrikleri
    @Query("SELECT AVG(iot.dataQualityScore) FROM VehicleIoTSensorData iot " +
           "WHERE iot.vehicleId = :vehicleId AND iot.timestamp >= :startTime")
    Double getAverageDataQuality(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(iot.signalStrength) FROM VehicleIoTSensorData iot " +
           "WHERE iot.vehicleId = :vehicleId AND iot.timestamp >= :startTime")
    Double getAverageSignalStrength(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(iot.networkLatencyMs) FROM VehicleIoTSensorData iot " +
           "WHERE iot.vehicleId = :vehicleId AND iot.timestamp >= :startTime")
    Double getAverageNetworkLatency(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Sensor sayıları
    @Query("SELECT COUNT(DISTINCT iot.sensorId) FROM VehicleIoTSensorData iot " +
           "WHERE iot.vehicleId = :vehicleId AND iot.timestamp >= :startTime")
    Long countActiveSensors(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT iot.sensorType, COUNT(iot) FROM VehicleIoTSensorData iot " +
           "WHERE iot.vehicleId = :vehicleId AND iot.timestamp >= :startTime " +
           "GROUP BY iot.sensorType")
    List<Object[]> getSensorTypeStats(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Batarya durumu (wireless sensors)
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.batteryLevel IS NOT NULL AND iot.batteryLevel < :lowBatteryThreshold " +
           "ORDER BY iot.batteryLevel ASC")
    List<VehicleIoTSensorData> findLowBatterySensors(
        @Param("vehicleId") String vehicleId,
        @Param("lowBatteryThreshold") Double lowBatteryThreshold);
    
    // Güç tüketimi analizi
    @Query("SELECT AVG(iot.powerConsumption) FROM VehicleIoTSensorData iot " +
           "WHERE iot.vehicleId = :vehicleId AND iot.powerConsumption IS NOT NULL " +
           "AND iot.timestamp >= :startTime")
    Double getAveragePowerConsumption(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Kalibrasyon durumu
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.calibrationStatus != 'CALIBRATED' ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findCalibrationIssues(@Param("vehicleId") String vehicleId);
    
    // Çevresel koşullar
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType IN ('AMBIENT_TEMPERATURE', 'AMBIENT_HUMIDITY', 'AIR_QUALITY') " +
           "AND iot.timestamp >= :startTime ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findEnvironmentalSensors(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Engine sensörleri
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType IN ('ENGINE_TEMPERATURE', 'ENGINE_RPM', 'ENGINE_OIL_PRESSURE') " +
           "AND iot.timestamp >= :startTime ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findEngineSensors(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Fren sistemi sensörleri
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType IN ('BRAKE_FLUID_LEVEL', 'BRAKE_TEMPERATURE', 'BRAKE_PRESSURE') " +
           "AND iot.timestamp >= :startTime ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findBrakeSensors(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Lastik sensörleri
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType IN ('TIRE_PRESSURE', 'TIRE_TEMPERATURE', 'TIRE_WEAR') " +
           "AND iot.timestamp >= :startTime ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findTireSensors(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Elektriksel sistem sensörleri
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType IN ('BATTERY_VOLTAGE', 'BATTERY_CURRENT', 'ALTERNATOR_OUTPUT') " +
           "AND iot.timestamp >= :startTime ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findElectricalSensors(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Kabin sensörleri
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType IN ('CABIN_TEMPERATURE', 'CABIN_HUMIDITY', 'CABIN_CO2') " +
           "AND iot.timestamp >= :startTime ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findCabinSensors(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Otonom sürüş sensörleri
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType IN ('LIDAR', 'RADAR', 'CAMERA', 'GPS', 'ULTRASONIC') " +
           "AND iot.timestamp >= :startTime ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findAutonomousDrivingSensors(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Trend analizi
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType = :sensorType AND iot.trendAnalysis = :trend " +
           "ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findByTrendAnalysis(
        @Param("vehicleId") String vehicleId,
        @Param("sensorType") VehicleIoTSensorData.SensorType sensorType,
        @Param("trend") String trend);
    
    // En son sensor verileri
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.sensorType = :sensorType ORDER BY iot.timestamp DESC LIMIT 1")
    Optional<VehicleIoTSensorData> findLatestBySensorType(
        @Param("vehicleId") String vehicleId,
        @Param("sensorType") VehicleIoTSensorData.SensorType sensorType);
    
    // Veri kalitesi sorunları
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.dataQualityScore < :qualityThreshold " +
           "ORDER BY iot.dataQualityScore ASC")
    List<VehicleIoTSensorData> findPoorQualityData(
        @Param("vehicleId") String vehicleId,
        @Param("qualityThreshold") Double qualityThreshold);
    
    // Sync durumu
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.syncStatus = 'FAILED' ORDER BY iot.timestamp DESC")
    List<VehicleIoTSensorData> findSyncFailures(@Param("vehicleId") String vehicleId);
    
    // Genel sistem sağlığı
    @Query("SELECT " +
           "COUNT(CASE WHEN iot.sensorHealthStatus IN ('EXCELLENT', 'GOOD') THEN 1 END) as healthy, " +
           "COUNT(CASE WHEN iot.sensorHealthStatus IN ('CRITICAL', 'FAILING', 'FAILED') THEN 1 END) as unhealthy, " +
           "COUNT(iot) as total " +
           "FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.timestamp >= :startTime")
    List<Object[]> getSystemHealthSummary(
        @Param("vehicleId") String vehicleId,
        @Param("startTime") LocalDateTime startTime);
    
    // Kalan yaşam süresi analizi
    @Query("SELECT iot FROM VehicleIoTSensorData iot WHERE iot.vehicleId = :vehicleId " +
           "AND iot.estimatedRemainingLife IS NOT NULL AND iot.estimatedRemainingLife < :daysThreshold " +
           "ORDER BY iot.estimatedRemainingLife ASC")
    List<VehicleIoTSensorData> findSensorsNearEndOfLife(
        @Param("vehicleId") String vehicleId,
        @Param("daysThreshold") Integer daysThreshold);
}