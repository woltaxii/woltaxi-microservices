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
 * Vehicle IoT Sensor Data Entity
 * Akıllı araçlardan gelen IoT sensor verilerini saklar
 */
@Entity
@Table(name = "vehicle_iot_sensor_data", indexes = {
    @Index(name = "idx_vehicle_sensor_timestamp", columnList = "vehicle_id, sensor_type, timestamp"),
    @Index(name = "idx_sensor_health", columnList = "sensor_health_status"),
    @Index(name = "idx_critical_alerts", columnList = "critical_alert")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleIoTSensorData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    // Sensor bilgileri
    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false)
    private SensorType sensorType;
    
    @Column(name = "sensor_id")
    private String sensorId;
    
    @Column(name = "sensor_location")
    private String sensorLocation;
    
    @Column(name = "sensor_manufacturer")
    private String sensorManufacturer;
    
    @Column(name = "sensor_model")
    private String sensorModel;
    
    @Column(name = "firmware_version")
    private String firmwareVersion;
    
    // Sensor değerleri
    @Column(name = "raw_value")
    private Double rawValue;
    
    @Column(name = "processed_value")
    private Double processedValue;
    
    @Column(name = "unit_of_measurement")
    private String unitOfMeasurement;
    
    @Column(name = "value_range")
    private String valueRange; // JSON: {"min": 0, "max": 100}
    
    // Kalite ve güvenilirlik
    @Column(name = "data_quality_score")
    private Double dataQualityScore; // 0-1 arası
    
    @Column(name = "signal_strength")
    private Double signalStrength; // dBm
    
    @Column(name = "noise_level")
    private Double noiseLevel;
    
    @Column(name = "calibration_status")
    private String calibrationStatus; // CALIBRATED, NEEDS_CALIBRATION, FAILED
    
    @Column(name = "last_calibration_date")
    private LocalDateTime lastCalibrationDate;
    
    // Sensor sağlığı
    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_health_status")
    private SensorHealthStatus sensorHealthStatus;
    
    @Column(name = "operating_temperature")
    private Double operatingTemperature; // Celsius
    
    @Column(name = "operating_humidity")
    private Double operatingHumidity; // %
    
    @Column(name = "power_consumption")
    private Double powerConsumption; // Watts
    
    @Column(name = "battery_level")
    private Double batteryLevel; // % (wireless sensors için)
    
    // Ağ ve iletişim
    @Column(name = "connection_type")
    private String connectionType; // WIRED, WIFI, BLUETOOTH, CELLULAR, LORA
    
    @Column(name = "network_latency_ms")
    private Long networkLatencyMs;
    
    @Column(name = "packet_loss_rate")
    private Double packetLossRate; // %
    
    @Column(name = "bandwidth_usage")
    private Double bandwidthUsage; // Kbps
    
    // Alertler ve uyarılar
    @Column(name = "critical_alert")
    private Boolean criticalAlert;
    
    @Column(name = "warning_alert")
    private Boolean warningAlert;
    
    @Column(name = "alert_message")
    private String alertMessage;
    
    @Column(name = "alert_severity")
    private Integer alertSeverity; // 1-5 arası
    
    // Predictive maintenance
    @Column(name = "predicted_failure_risk")
    private Double predictedFailureRisk; // 0-1 arası
    
    @Column(name = "maintenance_due_date")
    private LocalDateTime maintenanceDueDate;
    
    @Column(name = "replacement_recommended")
    private Boolean replacementRecommended;
    
    @Column(name = "estimated_remaining_life")
    private Integer estimatedRemainingLife; // gün
    
    // Geolocation (mobil sensörler için)
    @Column(name = "sensor_latitude")
    private Double sensorLatitude;
    
    @Column(name = "sensor_longitude")
    private Double sensorLongitude;
    
    @Column(name = "sensor_altitude")
    private Double sensorAltitude;
    
    // Data pipeline
    @Column(name = "processing_pipeline")
    private String processingPipeline;
    
    @Column(name = "data_source")
    private String dataSource; // DIRECT, GATEWAY, CLOUD
    
    @Column(name = "sync_status")
    private String syncStatus; // SYNCED, PENDING, FAILED
    
    @Column(name = "cloud_sync_timestamp")
    private LocalDateTime cloudSyncTimestamp;
    
    // Analytics ve ML
    @Column(name = "anomaly_detected")
    private Boolean anomalyDetected;
    
    @Column(name = "anomaly_score")
    private Double anomalyScore; // 0-1 arası
    
    @Column(name = "trend_analysis")
    private String trendAnalysis; // INCREASING, DECREASING, STABLE, VOLATILE
    
    @Column(name = "correlation_factors", columnDefinition = "TEXT")
    private String correlationFactors; // JSON format
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum tanımları
    public enum SensorType {
        // Engine sensors
        ENGINE_TEMPERATURE,
        ENGINE_RPM,
        ENGINE_OIL_PRESSURE,
        ENGINE_OIL_TEMPERATURE,
        ENGINE_COOLANT_LEVEL,
        FUEL_LEVEL,
        FUEL_PRESSURE,
        AIR_INTAKE_TEMPERATURE,
        EXHAUST_TEMPERATURE,
        
        // Transmission sensors
        TRANSMISSION_TEMPERATURE,
        TRANSMISSION_PRESSURE,
        GEAR_POSITION,
        
        // Electrical sensors
        BATTERY_VOLTAGE,
        BATTERY_CURRENT,
        ALTERNATOR_OUTPUT,
        STARTER_CURRENT,
        
        // Brake system
        BRAKE_FLUID_LEVEL,
        BRAKE_TEMPERATURE,
        BRAKE_PRESSURE,
        ABS_SENSOR,
        
        // Tire sensors
        TIRE_PRESSURE,
        TIRE_TEMPERATURE,
        TIRE_WEAR,
        TIRE_TREAD_DEPTH,
        
        // Environmental sensors
        AMBIENT_TEMPERATURE,
        AMBIENT_HUMIDITY,
        AMBIENT_LIGHT,
        RAIN_SENSOR,
        AIR_QUALITY,
        
        // Safety sensors
        AIRBAG_STATUS,
        SEATBELT_STATUS,
        DOOR_STATUS,
        WINDOW_STATUS,
        
        // Performance sensors
        ACCELERATION_X,
        ACCELERATION_Y,
        ACCELERATION_Z,
        GYROSCOPE_X,
        GYROSCOPE_Y,
        GYROSCOPE_Z,
        SPEED_SENSOR,
        ODOMETER,
        
        // Autonomous driving sensors
        LIDAR,
        RADAR,
        CAMERA,
        ULTRASONIC,
        GPS,
        
        // Cabin sensors
        CABIN_TEMPERATURE,
        CABIN_HUMIDITY,
        CABIN_CO2,
        CABIN_PRESSURE,
        SEAT_OCCUPANCY,
        
        // Maintenance sensors
        AIR_FILTER_STATUS,
        OIL_FILTER_STATUS,
        SPARK_PLUG_STATUS,
        BELT_TENSION,
        
        // Custom sensors
        CUSTOM_ANALOG,
        CUSTOM_DIGITAL,
        CUSTOM_SMART
    }
    
    public enum SensorHealthStatus {
        EXCELLENT,      // 90-100%
        GOOD,          // 80-89%
        FAIR,          // 70-79%
        POOR,          // 50-69%
        CRITICAL,      // 30-49%
        FAILING,       // 10-29%
        FAILED         // 0-9%
    }
    
    // Business methods
    public boolean isHealthy() {
        return sensorHealthStatus == SensorHealthStatus.EXCELLENT ||
               sensorHealthStatus == SensorHealthStatus.GOOD;
    }
    
    public boolean needsMaintenance() {
        return Boolean.TRUE.equals(replacementRecommended) ||
               sensorHealthStatus == SensorHealthStatus.POOR ||
               sensorHealthStatus == SensorHealthStatus.CRITICAL ||
               (maintenanceDueDate != null && maintenanceDueDate.isBefore(LocalDateTime.now()));
    }
    
    public boolean isConnected() {
        return networkLatencyMs != null && networkLatencyMs < 5000 && // 5 saniyeden az latency
               (packetLossRate == null || packetLossRate < 5.0); // %5'den az packet loss
    }
    
    public boolean requiresImmedateAttention() {
        return Boolean.TRUE.equals(criticalAlert) ||
               sensorHealthStatus == SensorHealthStatus.FAILING ||
               sensorHealthStatus == SensorHealthStatus.FAILED ||
               (predictedFailureRisk != null && predictedFailureRisk >= 0.8);
    }
    
    public double getOverallScore() {
        double score = 0.0;
        int factors = 0;
        
        // Data quality
        if (dataQualityScore != null) {
            score += dataQualityScore * 30;
            factors++;
        }
        
        // Sensor health
        if (sensorHealthStatus != null) {
            double healthScore = switch (sensorHealthStatus) {
                case EXCELLENT -> 1.0;
                case GOOD -> 0.85;
                case FAIR -> 0.7;
                case POOR -> 0.5;
                case CRITICAL -> 0.3;
                case FAILING -> 0.15;
                case FAILED -> 0.0;
            };
            score += healthScore * 40;
            factors++;
        }
        
        // Network connectivity
        if (isConnected()) {
            score += 30;
        }
        factors++;
        
        return factors > 0 ? score / factors : 0.0;
    }
    
    public String getRiskLevel() {
        if (requiresImmedateAttention()) return "CRITICAL";
        else if (needsMaintenance()) return "HIGH";
        else if (!isHealthy()) return "MEDIUM";
        else if (!isConnected()) return "LOW";
        else return "MINIMAL";
    }
    
    public boolean isAnomalous() {
        return Boolean.TRUE.equals(anomalyDetected) ||
               (anomalyScore != null && anomalyScore >= 0.7);
    }
    
    public String getMaintenanceRecommendation() {
        if (sensorHealthStatus == SensorHealthStatus.FAILED) {
            return "IMMEDIATE_REPLACEMENT_REQUIRED";
        } else if (sensorHealthStatus == SensorHealthStatus.FAILING) {
            return "REPLACE_WITHIN_24_HOURS";
        } else if (Boolean.TRUE.equals(replacementRecommended)) {
            return "SCHEDULE_REPLACEMENT";
        } else if (needsMaintenance()) {
            return "SCHEDULE_MAINTENANCE";
        } else if (!"CALIBRATED".equals(calibrationStatus)) {
            return "CALIBRATION_REQUIRED";
        } else {
            return "NO_ACTION_NEEDED";
        }
    }
}