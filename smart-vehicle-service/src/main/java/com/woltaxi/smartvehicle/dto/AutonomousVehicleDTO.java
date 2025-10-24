package com.woltaxi.smartvehicle.dto;

import com.woltaxi.smartvehicle.entity.AutonomousDrivingSession;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Otonom Mod Aktivasyon DTO
 */
public class AutonomousModeActivationDTO {
    
    @NotNull(message = "Otonom mod gereklidir")
    private AutonomousDrivingSession.AutonomyMode autonomyMode;
    
    @NotNull(message = "Ride ID gereklidir")
    private Long rideId;
    
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    
    private String weatherCondition;
    private String roadCondition;
    private String trafficDensity;
    
    private Map<String, Object> routePreferences;
    private List<String> safetyOverrides;
    
    // Constructors
    public AutonomousModeActivationDTO() {}
    
    // Getters and Setters
    public AutonomousDrivingSession.AutonomyMode getAutonomyMode() { return autonomyMode; }
    public void setAutonomyMode(AutonomousDrivingSession.AutonomyMode autonomyMode) { this.autonomyMode = autonomyMode; }
    
    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    
    public Double getStartLatitude() { return startLatitude; }
    public void setStartLatitude(Double startLatitude) { this.startLatitude = startLatitude; }
    
    public Double getStartLongitude() { return startLongitude; }
    public void setStartLongitude(Double startLongitude) { this.startLongitude = startLongitude; }
    
    public Double getEndLatitude() { return endLatitude; }
    public void setEndLatitude(Double endLatitude) { this.endLatitude = endLatitude; }
    
    public Double getEndLongitude() { return endLongitude; }
    public void setEndLongitude(Double endLongitude) { this.endLongitude = endLongitude; }
    
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    
    public String getRoadCondition() { return roadCondition; }
    public void setRoadCondition(String roadCondition) { this.roadCondition = roadCondition; }
    
    public String getTrafficDensity() { return trafficDensity; }
    public void setTrafficDensity(String trafficDensity) { this.trafficDensity = trafficDensity; }
    
    public Map<String, Object> getRoutePreferences() { return routePreferences; }
    public void setRoutePreferences(Map<String, Object> routePreferences) { this.routePreferences = routePreferences; }
    
    public List<String> getSafetyOverrides() { return safetyOverrides; }
    public void setSafetyOverrides(List<String> safetyOverrides) { this.safetyOverrides = safetyOverrides; }
}

/**
 * Otonom Mod Response DTO
 */
public class AutonomousModeResponseDTO {
    
    private Long sessionId;
    private AutonomousDrivingSession.AutonomyMode autonomyMode;
    private Boolean activated;
    private String activationMessage;
    private LocalDateTime activationTime;
    
    private Double aiConfidenceScore;
    private List<String> activeSafetyFeatures;
    private List<String> systemChecks;
    private Map<String, Object> sensorStatus;
    
    private String estimatedArrivalTime;
    private String recommendedRoute;
    private List<String> warnings;
    
    // Constructors
    public AutonomousModeResponseDTO() {}
    
    // Getters and Setters
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    
    public AutonomousDrivingSession.AutonomyMode getAutonomyMode() { return autonomyMode; }
    public void setAutonomyMode(AutonomousDrivingSession.AutonomyMode autonomyMode) { this.autonomyMode = autonomyMode; }
    
    public Boolean getActivated() { return activated; }
    public void setActivated(Boolean activated) { this.activated = activated; }
    
    public String getActivationMessage() { return activationMessage; }
    public void setActivationMessage(String activationMessage) { this.activationMessage = activationMessage; }
    
    public LocalDateTime getActivationTime() { return activationTime; }
    public void setActivationTime(LocalDateTime activationTime) { this.activationTime = activationTime; }
    
    public Double getAiConfidenceScore() { return aiConfidenceScore; }
    public void setAiConfidenceScore(Double aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }
    
    public List<String> getActiveSafetyFeatures() { return activeSafetyFeatures; }
    public void setActiveSafetyFeatures(List<String> activeSafetyFeatures) { this.activeSafetyFeatures = activeSafetyFeatures; }
    
    public List<String> getSystemChecks() { return systemChecks; }
    public void setSystemChecks(List<String> systemChecks) { this.systemChecks = systemChecks; }
    
    public Map<String, Object> getSensorStatus() { return sensorStatus; }
    public void setSensorStatus(Map<String, Object> sensorStatus) { this.sensorStatus = sensorStatus; }
    
    public String getEstimatedArrivalTime() { return estimatedArrivalTime; }
    public void setEstimatedArrivalTime(String estimatedArrivalTime) { this.estimatedArrivalTime = estimatedArrivalTime; }
    
    public String getRecommendedRoute() { return recommendedRoute; }
    public void setRecommendedRoute(String recommendedRoute) { this.recommendedRoute = recommendedRoute; }
    
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}

/**
 * Araç Sağlık Durumu DTO
 */
public class VehicleHealthResponseDTO {
    
    private Long vehicleId;
    private String overallHealthStatus; // EXCELLENT, GOOD, FAIR, POOR, CRITICAL
    private Double overallHealthScore; // 0-100
    
    // Motor ve Sistem Sağlığı
    private Double engineHealth;
    private Double engineTemperature;
    private Double batteryHealth;
    private Double batteryLevel;
    private Double fuelLevel;
    
    // Lastik Durumu
    private Map<String, Double> tirePressures;
    private Map<String, String> tireConditions;
    
    // Sensör Sağlığı
    private Map<String, String> sensorHealth; // LIDAR, Camera, Radar, GPS
    private List<String> faultySensors;
    private List<String> calibrationNeeded;
    
    // AI ve Yazılım
    private Map<String, String> aiModelStatus;
    private List<String> softwareUpdatesAvailable;
    private String lastDiagnosticDate;
    
    // Bakım Durumu
    private Boolean maintenanceRequired;
    private LocalDateTime nextMaintenanceDate;
    private List<String> maintenanceItems;
    private List<String> warnings;
    private List<String> criticalIssues;
    
    // Constructors
    public VehicleHealthResponseDTO() {}
    
    // Getters and Setters
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    
    public String getOverallHealthStatus() { return overallHealthStatus; }
    public void setOverallHealthStatus(String overallHealthStatus) { this.overallHealthStatus = overallHealthStatus; }
    
    public Double getOverallHealthScore() { return overallHealthScore; }
    public void setOverallHealthScore(Double overallHealthScore) { this.overallHealthScore = overallHealthScore; }
    
    public Double getEngineHealth() { return engineHealth; }
    public void setEngineHealth(Double engineHealth) { this.engineHealth = engineHealth; }
    
    public Double getEngineTemperature() { return engineTemperature; }
    public void setEngineTemperature(Double engineTemperature) { this.engineTemperature = engineTemperature; }
    
    public Double getBatteryHealth() { return batteryHealth; }
    public void setBatteryHealth(Double batteryHealth) { this.batteryHealth = batteryHealth; }
    
    public Double getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Double batteryLevel) { this.batteryLevel = batteryLevel; }
    
    public Double getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(Double fuelLevel) { this.fuelLevel = fuelLevel; }
    
    public Map<String, Double> getTirePressures() { return tirePressures; }
    public void setTirePressures(Map<String, Double> tirePressures) { this.tirePressures = tirePressures; }
    
    public Map<String, String> getTireConditions() { return tireConditions; }
    public void setTireConditions(Map<String, String> tireConditions) { this.tireConditions = tireConditions; }
    
    public Map<String, String> getSensorHealth() { return sensorHealth; }
    public void setSensorHealth(Map<String, String> sensorHealth) { this.sensorHealth = sensorHealth; }
    
    public List<String> getFaultySensors() { return faultySensors; }
    public void setFaultySensors(List<String> faultySensors) { this.faultySensors = faultySensors; }
    
    public List<String> getCalibrationNeeded() { return calibrationNeeded; }
    public void setCalibrationNeeded(List<String> calibrationNeeded) { this.calibrationNeeded = calibrationNeeded; }
    
    public Map<String, String> getAiModelStatus() { return aiModelStatus; }
    public void setAiModelStatus(Map<String, String> aiModelStatus) { this.aiModelStatus = aiModelStatus; }
    
    public List<String> getSoftwareUpdatesAvailable() { return softwareUpdatesAvailable; }
    public void setSoftwareUpdatesAvailable(List<String> softwareUpdatesAvailable) { this.softwareUpdatesAvailable = softwareUpdatesAvailable; }
    
    public String getLastDiagnosticDate() { return lastDiagnosticDate; }
    public void setLastDiagnosticDate(String lastDiagnosticDate) { this.lastDiagnosticDate = lastDiagnosticDate; }
    
    public Boolean getMaintenanceRequired() { return maintenanceRequired; }
    public void setMaintenanceRequired(Boolean maintenanceRequired) { this.maintenanceRequired = maintenanceRequired; }
    
    public LocalDateTime getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }
    
    public List<String> getMaintenanceItems() { return maintenanceItems; }
    public void setMaintenanceItems(List<String> maintenanceItems) { this.maintenanceItems = maintenanceItems; }
    
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    
    public List<String> getCriticalIssues() { return criticalIssues; }
    public void setCriticalIssues(List<String> criticalIssues) { this.criticalIssues = criticalIssues; }
}

/**
 * Araç Performans DTO
 */
public class VehiclePerformanceDTO {
    
    private Long vehicleId;
    private Integer analysisPeriodDays;
    
    // Genel Performans
    private Double overallPerformanceScore;
    private Integer totalTrips;
    private Double totalDistanceDriven;
    private Double averageSpeed;
    private Double maxSpeed;
    
    // Güvenlik Performansı
    private Double safetyScore;
    private Integer emergencyBrakeCount;
    private Integer nearMissCount;
    private Integer accidentCount;
    private Double aiReliabilityScore;
    
    // Verimlilik
    private Double efficiencyScore;
    private Double averageFuelConsumption;
    private Double co2EmissionTotal;
    private Double energyConsumptionTotal;
    
    // Yolcu Memnuniyeti
    private Double averagePassengerRating;
    private List<String> commonComplaints;
    private List<String> positiveComments;
    private Double comfortScore;
    
    // Otonom Performans
    private Integer autonomousTrips;
    private Double autonomousModeSuccessRate;
    private Integer humanInterventionCount;
    private Double averageAiConfidence;
    
    // Sistem Performansı
    private Map<String, Double> sensorPerformance;
    private List<String> systemErrors;
    private Double uptimePercentage;
    private Integer maintenanceCount;
    
    // Constructors
    public VehiclePerformanceDTO() {}
    
    // Getters and Setters
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    
    public Integer getAnalysisPeriodDays() { return analysisPeriodDays; }
    public void setAnalysisPeriodDays(Integer analysisPeriodDays) { this.analysisPeriodDays = analysisPeriodDays; }
    
    public Double getOverallPerformanceScore() { return overallPerformanceScore; }
    public void setOverallPerformanceScore(Double overallPerformanceScore) { this.overallPerformanceScore = overallPerformanceScore; }
    
    public Integer getTotalTrips() { return totalTrips; }
    public void setTotalTrips(Integer totalTrips) { this.totalTrips = totalTrips; }
    
    public Double getTotalDistanceDriven() { return totalDistanceDriven; }
    public void setTotalDistanceDriven(Double totalDistanceDriven) { this.totalDistanceDriven = totalDistanceDriven; }
    
    public Double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(Double averageSpeed) { this.averageSpeed = averageSpeed; }
    
    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }
    
    public Double getSafetyScore() { return safetyScore; }
    public void setSafetyScore(Double safetyScore) { this.safetyScore = safetyScore; }
    
    public Integer getEmergencyBrakeCount() { return emergencyBrakeCount; }
    public void setEmergencyBrakeCount(Integer emergencyBrakeCount) { this.emergencyBrakeCount = emergencyBrakeCount; }
    
    public Integer getNearMissCount() { return nearMissCount; }
    public void setNearMissCount(Integer nearMissCount) { this.nearMissCount = nearMissCount; }
    
    public Integer getAccidentCount() { return accidentCount; }
    public void setAccidentCount(Integer accidentCount) { this.accidentCount = accidentCount; }
    
    public Double getAiReliabilityScore() { return aiReliabilityScore; }
    public void setAiReliabilityScore(Double aiReliabilityScore) { this.aiReliabilityScore = aiReliabilityScore; }
    
    public Double getEfficiencyScore() { return efficiencyScore; }
    public void setEfficiencyScore(Double efficiencyScore) { this.efficiencyScore = efficiencyScore; }
    
    public Double getAverageFuelConsumption() { return averageFuelConsumption; }
    public void setAverageFuelConsumption(Double averageFuelConsumption) { this.averageFuelConsumption = averageFuelConsumption; }
    
    public Double getCo2EmissionTotal() { return co2EmissionTotal; }
    public void setCo2EmissionTotal(Double co2EmissionTotal) { this.co2EmissionTotal = co2EmissionTotal; }
    
    public Double getEnergyConsumptionTotal() { return energyConsumptionTotal; }
    public void setEnergyConsumptionTotal(Double energyConsumptionTotal) { this.energyConsumptionTotal = energyConsumptionTotal; }
    
    public Double getAveragePassengerRating() { return averagePassengerRating; }
    public void setAveragePassengerRating(Double averagePassengerRating) { this.averagePassengerRating = averagePassengerRating; }
    
    public List<String> getCommonComplaints() { return commonComplaints; }
    public void setCommonComplaints(List<String> commonComplaints) { this.commonComplaints = commonComplaints; }
    
    public List<String> getPositiveComments() { return positiveComments; }
    public void setPositiveComments(List<String> positiveComments) { this.positiveComments = positiveComments; }
    
    public Double getComfortScore() { return comfortScore; }
    public void setComfortScore(Double comfortScore) { this.comfortScore = comfortScore; }
    
    public Integer getAutonomousTrips() { return autonomousTrips; }
    public void setAutonomousTrips(Integer autonomousTrips) { this.autonomousTrips = autonomousTrips; }
    
    public Double getAutonomousModeSuccessRate() { return autonomousModeSuccessRate; }
    public void setAutonomousModeSuccessRate(Double autonomousModeSuccessRate) { this.autonomousModeSuccessRate = autonomousModeSuccessRate; }
    
    public Integer getHumanInterventionCount() { return humanInterventionCount; }
    public void setHumanInterventionCount(Integer humanInterventionCount) { this.humanInterventionCount = humanInterventionCount; }
    
    public Double getAverageAiConfidence() { return averageAiConfidence; }
    public void setAverageAiConfidence(Double averageAiConfidence) { this.averageAiConfidence = averageAiConfidence; }
    
    public Map<String, Double> getSensorPerformance() { return sensorPerformance; }
    public void setSensorPerformance(Map<String, Double> sensorPerformance) { this.sensorPerformance = sensorPerformance; }
    
    public List<String> getSystemErrors() { return systemErrors; }
    public void setSystemErrors(List<String> systemErrors) { this.systemErrors = systemErrors; }
    
    public Double getUptimePercentage() { return uptimePercentage; }
    public void setUptimePercentage(Double uptimePercentage) { this.uptimePercentage = uptimePercentage; }
    
    public Integer getMaintenanceCount() { return maintenanceCount; }
    public void setMaintenanceCount(Integer maintenanceCount) { this.maintenanceCount = maintenanceCount; }
}