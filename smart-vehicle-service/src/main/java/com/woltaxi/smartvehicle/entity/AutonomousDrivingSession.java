package com.woltaxi.smartvehicle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Otonom Sürüş Oturumu Entity - Akıllı araçlar için otonom sürüş kayıtları
 * AI algoritmaları ve sensör verilerinin analizi
 */
@Entity
@Table(name = "autonomous_driving_sessions")
public class AutonomousDrivingSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private SmartVehicle vehicle;
    
    @Column(nullable = false)
    private Long rideId; // WOLTAXI ride referansı
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AutonomyMode autonomyMode;
    
    @Column(nullable = false)
    private LocalDateTime sessionStartTime;
    
    @Column
    private LocalDateTime sessionEndTime;
    
    @Column
    private Double startLatitude;
    
    @Column
    private Double startLongitude;
    
    @Column
    private Double endLatitude;
    
    @Column
    private Double endLongitude;
    
    @Column
    private Double totalDistance; // km
    
    @Column
    private Integer sessionDuration; // saniye
    
    @Column
    private Double averageSpeed; // km/h
    
    @Column
    private Double maxSpeed; // km/h
    
    // AI Performans Metrikleri
    @Column
    private Integer aiDecisionCount; // AI kararları sayısı
    
    @Column
    private Integer humanInterventionCount; // İnsan müdahalesi sayısı
    
    @Column
    private Double aiConfidenceAverage; // Ortalama AI güven skoru (0-1)
    
    @Column
    private Integer obstacleDetectionCount; // Engel tespit sayısı
    
    @Column
    private Integer laneChangeCount; // Şerit değişimi sayısı
    
    @Column
    private Integer trafficLightStopCount; // Trafik ışığında durma sayısı
    
    @Column
    private Integer emergencyBrakeCount; // Acil fren sayısı
    
    // Sensör Performansı
    @Column(columnDefinition = "TEXT")
    private String lidarData; // JSON - LIDAR sensör verileri
    
    @Column(columnDefinition = "TEXT")
    private String cameraData; // JSON - Kamera görüntü analizi
    
    @Column(columnDefinition = "TEXT")
    private String radarData; // JSON - Radar sensör verileri
    
    @Column(columnDefinition = "TEXT")
    private String gpsAccuracy; // JSON - GPS doğruluk verileri
    
    // Hava Durumu ve Çevre Koşulları
    @Column
    private String weatherCondition; // SUNNY, RAINY, FOGGY, SNOWY
    
    @Column
    private String roadCondition; // DRY, WET, ICY, CONSTRUCTION
    
    @Column
    private String trafficDensity; // LOW, MEDIUM, HIGH, CONGESTED
    
    @Column
    private Integer visibilityRange; // metre
    
    @Column
    private Double ambientTemperature; // Celsius
    
    // Güvenlik ve Performans Skorları
    @Column
    private Double safetyScore; // 0-100 güvenlik skoru
    
    @Column
    private Double efficiencyScore; // 0-100 verimlilik skoru
    
    @Column
    private Double comfortScore; // 0-100 konfor skoru
    
    @Column
    private Double overallPerformance; // 0-100 genel performans
    
    // Enerji Tüketimi
    @Column
    private Double energyConsumed; // kWh veya L
    
    @Column
    private Double fuelEfficiency; // km/L veya kWh/100km
    
    @Column
    private Double co2Emissions; // gram
    
    // Yolcu Memnuniyeti
    @Column
    private Integer passengerRating; // 1-5 yolcu değerlendirmesi
    
    @Column(columnDefinition = "TEXT")
    private String passengerFeedback; // Yolcu geri bildirimi
    
    // Hata ve Anomaliler
    @Column(columnDefinition = "TEXT")
    private String systemErrors; // JSON - sistem hataları
    
    @Column(columnDefinition = "TEXT")
    private String anomaliesDetected; // JSON - tespit edilen anomaliler
    
    @Column
    private Boolean criticalIncident; // Kritik olay var mı?
    
    @Column(columnDefinition = "TEXT")
    private String incidentDescription; // Olay açıklaması
    
    // Makine Öğrenmesi Verileri
    @Column(columnDefinition = "TEXT")
    private String modelPredictions; // JSON - AI model tahminleri
    
    @Column(columnDefinition = "TEXT")
    private String learningOutcomes; // JSON - öğrenme sonuçları
    
    @Column
    private Boolean dataUsedForTraining; // Eğitim için kullanıldı mı?
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public AutonomousDrivingSession() {}
    
    public AutonomousDrivingSession(SmartVehicle vehicle, Long rideId, AutonomyMode autonomyMode) {
        this.vehicle = vehicle;
        this.rideId = rideId;
        this.autonomyMode = autonomyMode;
        this.sessionStartTime = LocalDateTime.now();
        this.aiDecisionCount = 0;
        this.humanInterventionCount = 0;
        this.obstacleDetectionCount = 0;
        this.laneChangeCount = 0;
        this.trafficLightStopCount = 0;
        this.emergencyBrakeCount = 0;
        this.criticalIncident = false;
        this.dataUsedForTraining = false;
    }
    
    // Enums
    public enum AutonomyMode {
        MANUAL,              // Manuel sürüş
        DRIVER_ASSISTANCE,   // Sürücü yardımı (L1)
        PARTIAL_AUTOMATION,  // Kısmi otomasyon (L2)
        CONDITIONAL_AUTO,    // Koşullu otomasyon (L3)
        HIGH_AUTOMATION,     // Yüksek otomasyon (L4)
        FULL_AUTOMATION      // Tam otomasyon (L5)
    }
    
    // Business Methods
    public void endSession() {
        this.sessionEndTime = LocalDateTime.now();
        if (sessionStartTime != null) {
            this.sessionDuration = (int) java.time.Duration.between(sessionStartTime, sessionEndTime).getSeconds();
        }
    }
    
    public double calculateSafetyScore() {
        double baseScore = 100.0;
        
        // Acil fren sayısına göre puan düşür
        baseScore -= (emergencyBrakeCount * 10);
        
        // İnsan müdahalesi sayısına göre puan düşür
        baseScore -= (humanInterventionCount * 5);
        
        // Kritik olay varsa ciddi puan düşür
        if (criticalIncident) {
            baseScore -= 30;
        }
        
        // AI güven skoruna göre ayarla
        if (aiConfidenceAverage != null) {
            baseScore = baseScore * aiConfidenceAverage;
        }
        
        return Math.max(0, Math.min(100, baseScore));
    }
    
    public double calculateEfficiencyScore() {
        double baseScore = 100.0;
        
        // Yakıt verimliliği (varsayılan değerlerle karşılaştır)
        if (fuelEfficiency != null) {
            double expectedEfficiency = 15.0; // km/L veya kWh/100km
            double efficiencyRatio = fuelEfficiency / expectedEfficiency;
            baseScore = Math.min(100, baseScore * efficiencyRatio);
        }
        
        return Math.max(0, Math.min(100, baseScore));
    }
    
    public boolean isAutonomousSession() {
        return autonomyMode.ordinal() >= AutonomyMode.CONDITIONAL_AUTO.ordinal();
    }
    
    public boolean requiresReview() {
        return criticalIncident || humanInterventionCount > 5 || emergencyBrakeCount > 2;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SmartVehicle getVehicle() { return vehicle; }
    public void setVehicle(SmartVehicle vehicle) { this.vehicle = vehicle; }
    
    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    
    public AutonomyMode getAutonomyMode() { return autonomyMode; }
    public void setAutonomyMode(AutonomyMode autonomyMode) { this.autonomyMode = autonomyMode; }
    
    public LocalDateTime getSessionStartTime() { return sessionStartTime; }
    public void setSessionStartTime(LocalDateTime sessionStartTime) { this.sessionStartTime = sessionStartTime; }
    
    public LocalDateTime getSessionEndTime() { return sessionEndTime; }
    public void setSessionEndTime(LocalDateTime sessionEndTime) { this.sessionEndTime = sessionEndTime; }
    
    public Double getStartLatitude() { return startLatitude; }
    public void setStartLatitude(Double startLatitude) { this.startLatitude = startLatitude; }
    
    public Double getStartLongitude() { return startLongitude; }
    public void setStartLongitude(Double startLongitude) { this.startLongitude = startLongitude; }
    
    public Double getEndLatitude() { return endLatitude; }
    public void setEndLatitude(Double endLatitude) { this.endLatitude = endLatitude; }
    
    public Double getEndLongitude() { return endLongitude; }
    public void setEndLongitude(Double endLongitude) { this.endLongitude = endLongitude; }
    
    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }
    
    public Integer getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(Integer sessionDuration) { this.sessionDuration = sessionDuration; }
    
    public Double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(Double averageSpeed) { this.averageSpeed = averageSpeed; }
    
    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }
    
    public Integer getAiDecisionCount() { return aiDecisionCount; }
    public void setAiDecisionCount(Integer aiDecisionCount) { this.aiDecisionCount = aiDecisionCount; }
    
    public Integer getHumanInterventionCount() { return humanInterventionCount; }
    public void setHumanInterventionCount(Integer humanInterventionCount) { this.humanInterventionCount = humanInterventionCount; }
    
    public Double getAiConfidenceAverage() { return aiConfidenceAverage; }
    public void setAiConfidenceAverage(Double aiConfidenceAverage) { this.aiConfidenceAverage = aiConfidenceAverage; }
    
    public Integer getObstacleDetectionCount() { return obstacleDetectionCount; }
    public void setObstacleDetectionCount(Integer obstacleDetectionCount) { this.obstacleDetectionCount = obstacleDetectionCount; }
    
    public Integer getLaneChangeCount() { return laneChangeCount; }
    public void setLaneChangeCount(Integer laneChangeCount) { this.laneChangeCount = laneChangeCount; }
    
    public Integer getTrafficLightStopCount() { return trafficLightStopCount; }
    public void setTrafficLightStopCount(Integer trafficLightStopCount) { this.trafficLightStopCount = trafficLightStopCount; }
    
    public Integer getEmergencyBrakeCount() { return emergencyBrakeCount; }
    public void setEmergencyBrakeCount(Integer emergencyBrakeCount) { this.emergencyBrakeCount = emergencyBrakeCount; }
    
    public String getLidarData() { return lidarData; }
    public void setLidarData(String lidarData) { this.lidarData = lidarData; }
    
    public String getCameraData() { return cameraData; }
    public void setCameraData(String cameraData) { this.cameraData = cameraData; }
    
    public String getRadarData() { return radarData; }
    public void setRadarData(String radarData) { this.radarData = radarData; }
    
    public String getGpsAccuracy() { return gpsAccuracy; }
    public void setGpsAccuracy(String gpsAccuracy) { this.gpsAccuracy = gpsAccuracy; }
    
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    
    public String getRoadCondition() { return roadCondition; }
    public void setRoadCondition(String roadCondition) { this.roadCondition = roadCondition; }
    
    public String getTrafficDensity() { return trafficDensity; }
    public void setTrafficDensity(String trafficDensity) { this.trafficDensity = trafficDensity; }
    
    public Integer getVisibilityRange() { return visibilityRange; }
    public void setVisibilityRange(Integer visibilityRange) { this.visibilityRange = visibilityRange; }
    
    public Double getAmbientTemperature() { return ambientTemperature; }
    public void setAmbientTemperature(Double ambientTemperature) { this.ambientTemperature = ambientTemperature; }
    
    public Double getSafetyScore() { return safetyScore; }
    public void setSafetyScore(Double safetyScore) { this.safetyScore = safetyScore; }
    
    public Double getEfficiencyScore() { return efficiencyScore; }
    public void setEfficiencyScore(Double efficiencyScore) { this.efficiencyScore = efficiencyScore; }
    
    public Double getComfortScore() { return comfortScore; }
    public void setComfortScore(Double comfortScore) { this.comfortScore = comfortScore; }
    
    public Double getOverallPerformance() { return overallPerformance; }
    public void setOverallPerformance(Double overallPerformance) { this.overallPerformance = overallPerformance; }
    
    public Double getEnergyConsumed() { return energyConsumed; }
    public void setEnergyConsumed(Double energyConsumed) { this.energyConsumed = energyConsumed; }
    
    public Double getFuelEfficiency() { return fuelEfficiency; }
    public void setFuelEfficiency(Double fuelEfficiency) { this.fuelEfficiency = fuelEfficiency; }
    
    public Double getCo2Emissions() { return co2Emissions; }
    public void setCo2Emissions(Double co2Emissions) { this.co2Emissions = co2Emissions; }
    
    public Integer getPassengerRating() { return passengerRating; }
    public void setPassengerRating(Integer passengerRating) { this.passengerRating = passengerRating; }
    
    public String getPassengerFeedback() { return passengerFeedback; }
    public void setPassengerFeedback(String passengerFeedback) { this.passengerFeedback = passengerFeedback; }
    
    public String getSystemErrors() { return systemErrors; }
    public void setSystemErrors(String systemErrors) { this.systemErrors = systemErrors; }
    
    public String getAnomaliesDetected() { return anomaliesDetected; }
    public void setAnomaliesDetected(String anomaliesDetected) { this.anomaliesDetected = anomaliesDetected; }
    
    public Boolean getCriticalIncident() { return criticalIncident; }
    public void setCriticalIncident(Boolean criticalIncident) { this.criticalIncident = criticalIncident; }
    
    public String getIncidentDescription() { return incidentDescription; }
    public void setIncidentDescription(String incidentDescription) { this.incidentDescription = incidentDescription; }
    
    public String getModelPredictions() { return modelPredictions; }
    public void setModelPredictions(String modelPredictions) { this.modelPredictions = modelPredictions; }
    
    public String getLearningOutcomes() { return learningOutcomes; }
    public void setLearningOutcomes(String learningOutcomes) { this.learningOutcomes = learningOutcomes; }
    
    public Boolean getDataUsedForTraining() { return dataUsedForTraining; }
    public void setDataUsedForTraining(Boolean dataUsedForTraining) { this.dataUsedForTraining = dataUsedForTraining; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}