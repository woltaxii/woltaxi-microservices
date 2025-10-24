package com.woltaxi.smartvehicle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Araç Sensör Verisi Entity - Gerçek zamanlı sensör okumalarını depolar
 * LIDAR, Kamera, Radar, GPS ve diğer IoT sensörleri için
 */
@Entity
@Table(name = "vehicle_sensor_data")
@Index(name = "idx_vehicle_timestamp", columnList = "vehicle_id,timestamp")
@Index(name = "idx_sensor_type", columnList = "sensor_type,timestamp")
public class VehicleSensorData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private SmartVehicle vehicle;
    
    @Column(nullable = false)
    @NotBlank(message = "Sensör tipi gereklidir")
    private String sensorType; // LIDAR, CAMERA, RADAR, GPS, ACCELEROMETER, GYROSCOPE
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(precision = 10, scale = 7)
    private Double latitude;
    
    @Column(precision = 10, scale = 7)
    private Double longitude;
    
    @Column
    private Double altitude; // metre
    
    @Column
    private Double speed; // km/h
    
    @Column
    private Double heading; // derece (0-360)
    
    @Column
    private Double acceleration; // m/s²
    
    // LIDAR Sensör Verileri
    @Column(columnDefinition = "TEXT")
    private String lidarPointCloud; // JSON - 3D nokta bulutu verisi
    
    @Column
    private Double lidarRange; // metre - algılama menzili
    
    @Column
    private Integer objectsDetected; // tespit edilen nesne sayısı
    
    // Kamera Sensör Verileri
    @Column(columnDefinition = "TEXT")
    private String imageAnalysis; // JSON - görüntü analiz sonuçları
    
    @Column(columnDefinition = "TEXT")
    private String detectedObjects; // JSON - tespit edilen nesneler
    
    @Column(columnDefinition = "TEXT")
    private String trafficSigns; // JSON - trafik levhaları
    
    @Column(columnDefinition = "TEXT")
    private String laneDetection; // JSON - şerit tespit sonuçları
    
    @Column
    private Double imageQuality; // 0-1 görüntü kalitesi skoru
    
    @Column
    private Integer lightCondition; // 0-100 ışık durumu
    
    // Radar Sensör Verileri
    @Column(columnDefinition = "TEXT")
    private String radarTargets; // JSON - radar hedefleri
    
    @Column
    private Double radarRange; // metre
    
    @Column
    private Double closestObjectDistance; // en yakın nesne mesafesi (metre)
    
    @Column
    private Double relativeVelocity; // göreceli hız (km/h)
    
    // GPS ve Navigasyon
    @Column
    private Double gpsAccuracy; // metre - GPS doğruluk
    
    @Column
    private Integer satelliteCount; // bağlı uydu sayısı
    
    @Column
    private String gpsStatus; // FIX, NO_FIX, DGPS
    
    // İvmeölçer ve Jiroskop
    @Column
    private Double accelerometerX; // X ekseni ivme
    
    @Column
    private Double accelerometerY; // Y ekseni ivme
    
    @Column
    private Double accelerometerZ; // Z ekseni ivme
    
    @Column
    private Double gyroscopeX; // X ekseni açısal hız
    
    @Column
    private Double gyroscopeY; // Y ekseni açısal hız
    
    @Column
    private Double gyroscopeZ; // Z ekseni açısal hız
    
    // Araç İç Sensörleri
    @Column
    private Double engineRpm; // motor devri
    
    @Column
    private Double engineTemperature; // motor sıcaklığı
    
    @Column
    private Double batteryVoltage; // batarya voltajı
    
    @Column
    private Double fuelLevel; // yakıt seviyesi (%)
    
    @Column
    private Double tirePressureFrontLeft;
    
    @Column
    private Double tirePressureFrontRight;
    
    @Column
    private Double tirePressureRearLeft;
    
    @Column
    private Double tirePressureRearRight;
    
    // Çevre Sensörleri
    @Column
    private Double ambientTemperature; // dış sıcaklık
    
    @Column
    private Double humidity; // nem oranı (%)
    
    @Column
    private Double atmosphericPressure; // atmosfer basıncı (hPa)
    
    @Column
    private Integer visibilityRange; // görüş mesafesi (metre)
    
    @Column
    private String weatherCondition; // hava durumu
    
    // AI ve Makine Öğrenmesi
    @Column(columnDefinition = "TEXT")
    private String aiPredictions; // JSON - AI tahminleri
    
    @Column
    private Double predictionConfidence; // tahmin güven skoru (0-1)
    
    @Column(columnDefinition = "TEXT")
    private String anomaliesDetected; // JSON - tespit edilen anomaliler
    
    @Column
    private Boolean requiresAttention; // dikkat gerektiriyor mu?
    
    // V2X İletişim Verileri
    @Column(columnDefinition = "TEXT")
    private String v2vMessages; // JSON - araçtan araca mesajlar
    
    @Column(columnDefinition = "TEXT")
    private String v2iMessages; // JSON - araçtan altyapıya mesajlar
    
    @Column(columnDefinition = "TEXT")
    private String v2pMessages; // JSON - araçtan yayaya mesajlar
    
    // Veri Kalitesi ve Güvenilirlik
    @Column
    private Double dataQualityScore; // veri kalite skoru (0-1)
    
    @Column
    private Boolean dataValidated; // veri doğrulandı mı?
    
    @Column
    private String validationStatus; // VALID, INVALID, SUSPICIOUS
    
    @Column(columnDefinition = "TEXT")
    private String rawData; // ham sensör verisi (JSON)
    
    @Column(columnDefinition = "TEXT")
    private String processedData; // işlenmiş veri (JSON)
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public VehicleSensorData() {}
    
    public VehicleSensorData(SmartVehicle vehicle, String sensorType) {
        this.vehicle = vehicle;
        this.sensorType = sensorType;
        this.timestamp = LocalDateTime.now();
        this.dataValidated = false;
        this.requiresAttention = false;
        this.validationStatus = "PENDING";
    }
    
    // Business Methods
    public boolean isCriticalData() {
        return requiresAttention || "INVALID".equals(validationStatus) || 
               (dataQualityScore != null && dataQualityScore < 0.5);
    }
    
    public boolean isRealtimeData() {
        return timestamp != null && 
               java.time.Duration.between(timestamp, LocalDateTime.now()).getSeconds() < 5;
    }
    
    public boolean hasGoodGpsSignal() {
        return satelliteCount != null && satelliteCount >= 4 && 
               gpsAccuracy != null && gpsAccuracy <= 10.0;
    }
    
    public boolean isVehicleMoving() {
        return speed != null && speed > 1.0; // 1 km/h üzeri hareket kabul edilir
    }
    
    public double calculateOverallDataQuality() {
        double qualityScore = 0.0;
        int factors = 0;
        
        if (dataQualityScore != null) {
            qualityScore += dataQualityScore;
            factors++;
        }
        
        if (hasGoodGpsSignal()) {
            qualityScore += 1.0;
            factors++;
        }
        
        if (imageQuality != null) {
            qualityScore += imageQuality;
            factors++;
        }
        
        if (predictionConfidence != null) {
            qualityScore += predictionConfidence;
            factors++;
        }
        
        return factors > 0 ? qualityScore / factors : 0.0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SmartVehicle getVehicle() { return vehicle; }
    public void setVehicle(SmartVehicle vehicle) { this.vehicle = vehicle; }
    
    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }
    
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    
    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }
    
    public Double getAcceleration() { return acceleration; }
    public void setAcceleration(Double acceleration) { this.acceleration = acceleration; }
    
    public String getLidarPointCloud() { return lidarPointCloud; }
    public void setLidarPointCloud(String lidarPointCloud) { this.lidarPointCloud = lidarPointCloud; }
    
    public Double getLidarRange() { return lidarRange; }
    public void setLidarRange(Double lidarRange) { this.lidarRange = lidarRange; }
    
    public Integer getObjectsDetected() { return objectsDetected; }
    public void setObjectsDetected(Integer objectsDetected) { this.objectsDetected = objectsDetected; }
    
    public String getImageAnalysis() { return imageAnalysis; }
    public void setImageAnalysis(String imageAnalysis) { this.imageAnalysis = imageAnalysis; }
    
    public String getDetectedObjects() { return detectedObjects; }
    public void setDetectedObjects(String detectedObjects) { this.detectedObjects = detectedObjects; }
    
    public String getTrafficSigns() { return trafficSigns; }
    public void setTrafficSigns(String trafficSigns) { this.trafficSigns = trafficSigns; }
    
    public String getLaneDetection() { return laneDetection; }
    public void setLaneDetection(String laneDetection) { this.laneDetection = laneDetection; }
    
    public Double getImageQuality() { return imageQuality; }
    public void setImageQuality(Double imageQuality) { this.imageQuality = imageQuality; }
    
    public Integer getLightCondition() { return lightCondition; }
    public void setLightCondition(Integer lightCondition) { this.lightCondition = lightCondition; }
    
    public String getRadarTargets() { return radarTargets; }
    public void setRadarTargets(String radarTargets) { this.radarTargets = radarTargets; }
    
    public Double getRadarRange() { return radarRange; }
    public void setRadarRange(Double radarRange) { this.radarRange = radarRange; }
    
    public Double getClosestObjectDistance() { return closestObjectDistance; }
    public void setClosestObjectDistance(Double closestObjectDistance) { this.closestObjectDistance = closestObjectDistance; }
    
    public Double getRelativeVelocity() { return relativeVelocity; }
    public void setRelativeVelocity(Double relativeVelocity) { this.relativeVelocity = relativeVelocity; }
    
    public Double getGpsAccuracy() { return gpsAccuracy; }
    public void setGpsAccuracy(Double gpsAccuracy) { this.gpsAccuracy = gpsAccuracy; }
    
    public Integer getSatelliteCount() { return satelliteCount; }
    public void setSatelliteCount(Integer satelliteCount) { this.satelliteCount = satelliteCount; }
    
    public String getGpsStatus() { return gpsStatus; }
    public void setGpsStatus(String gpsStatus) { this.gpsStatus = gpsStatus; }
    
    public Double getAccelerometerX() { return accelerometerX; }
    public void setAccelerometerX(Double accelerometerX) { this.accelerometerX = accelerometerX; }
    
    public Double getAccelerometerY() { return accelerometerY; }
    public void setAccelerometerY(Double accelerometerY) { this.accelerometerY = accelerometerY; }
    
    public Double getAccelerometerZ() { return accelerometerZ; }
    public void setAccelerometerZ(Double accelerometerZ) { this.accelerometerZ = accelerometerZ; }
    
    public Double getGyroscopeX() { return gyroscopeX; }
    public void setGyroscopeX(Double gyroscopeX) { this.gyroscopeX = gyroscopeX; }
    
    public Double getGyroscopeY() { return gyroscopeY; }
    public void setGyroscopeY(Double gyroscopeY) { this.gyroscopeY = gyroscopeY; }
    
    public Double getGyroscopeZ() { return gyroscopeZ; }
    public void setGyroscopeZ(Double gyroscopeZ) { this.gyroscopeZ = gyroscopeZ; }
    
    public Double getEngineRpm() { return engineRpm; }
    public void setEngineRpm(Double engineRpm) { this.engineRpm = engineRpm; }
    
    public Double getEngineTemperature() { return engineTemperature; }
    public void setEngineTemperature(Double engineTemperature) { this.engineTemperature = engineTemperature; }
    
    public Double getBatteryVoltage() { return batteryVoltage; }
    public void setBatteryVoltage(Double batteryVoltage) { this.batteryVoltage = batteryVoltage; }
    
    public Double getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(Double fuelLevel) { this.fuelLevel = fuelLevel; }
    
    public Double getTirePressureFrontLeft() { return tirePressureFrontLeft; }
    public void setTirePressureFrontLeft(Double tirePressureFrontLeft) { this.tirePressureFrontLeft = tirePressureFrontLeft; }
    
    public Double getTirePressureFrontRight() { return tirePressureFrontRight; }
    public void setTirePressureFrontRight(Double tirePressureFrontRight) { this.tirePressureFrontRight = tirePressureFrontRight; }
    
    public Double getTirePressureRearLeft() { return tirePressureRearLeft; }
    public void setTirePressureRearLeft(Double tirePressureRearLeft) { this.tirePressureRearLeft = tirePressureRearLeft; }
    
    public Double getTirePressureRearRight() { return tirePressureRearRight; }
    public void setTirePressureRearRight(Double tirePressureRearRight) { this.tirePressureRearRight = tirePressureRearRight; }
    
    public Double getAmbientTemperature() { return ambientTemperature; }
    public void setAmbientTemperature(Double ambientTemperature) { this.ambientTemperature = ambientTemperature; }
    
    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
    
    public Double getAtmosphericPressure() { return atmosphericPressure; }
    public void setAtmosphericPressure(Double atmosphericPressure) { this.atmosphericPressure = atmosphericPressure; }
    
    public Integer getVisibilityRange() { return visibilityRange; }
    public void setVisibilityRange(Integer visibilityRange) { this.visibilityRange = visibilityRange; }
    
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    
    public String getAiPredictions() { return aiPredictions; }
    public void setAiPredictions(String aiPredictions) { this.aiPredictions = aiPredictions; }
    
    public Double getPredictionConfidence() { return predictionConfidence; }
    public void setPredictionConfidence(Double predictionConfidence) { this.predictionConfidence = predictionConfidence; }
    
    public String getAnomaliesDetected() { return anomaliesDetected; }
    public void setAnomaliesDetected(String anomaliesDetected) { this.anomaliesDetected = anomaliesDetected; }
    
    public Boolean getRequiresAttention() { return requiresAttention; }
    public void setRequiresAttention(Boolean requiresAttention) { this.requiresAttention = requiresAttention; }
    
    public String getV2vMessages() { return v2vMessages; }
    public void setV2vMessages(String v2vMessages) { this.v2vMessages = v2vMessages; }
    
    public String getV2iMessages() { return v2iMessages; }
    public void setV2iMessages(String v2iMessages) { this.v2iMessages = v2iMessages; }
    
    public String getV2pMessages() { return v2pMessages; }
    public void setV2pMessages(String v2pMessages) { this.v2pMessages = v2pMessages; }
    
    public Double getDataQualityScore() { return dataQualityScore; }
    public void setDataQualityScore(Double dataQualityScore) { this.dataQualityScore = dataQualityScore; }
    
    public Boolean getDataValidated() { return dataValidated; }
    public void setDataValidated(Boolean dataValidated) { this.dataValidated = dataValidated; }
    
    public String getValidationStatus() { return validationStatus; }
    public void setValidationStatus(String validationStatus) { this.validationStatus = validationStatus; }
    
    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
    
    public String getProcessedData() { return processedData; }
    public void setProcessedData(String processedData) { this.processedData = processedData; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}