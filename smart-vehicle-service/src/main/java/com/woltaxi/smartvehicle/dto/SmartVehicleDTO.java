package com.woltaxi.smartvehicle.dto;

import com.woltaxi.smartvehicle.entity.SmartVehicle;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Akıllı Araç Kayıt DTO - Yeni araç kaydı için
 */
public class SmartVehicleRegisterDTO {
    
    @NotBlank(message = "VIN numarası gereklidir")
    @Size(min = 17, max = 17, message = "VIN numarası 17 karakter olmalıdır")
    private String vin;
    
    @NotBlank(message = "Plaka numarası gereklidir")
    private String licensePlate;
    
    @NotBlank(message = "Marka gereklidir")
    private String make;
    
    @NotBlank(message = "Model gereklidir")
    private String model;
    
    @NotNull(message = "Yıl gereklidir")
    @Min(value = 2020, message = "Akıllı araç en az 2020 model olmalıdır")
    private Integer year;
    
    @NotBlank(message = "Araç tipi gereklidir")
    private String vehicleType;
    
    @NotNull(message = "Sürücü ID gereklidir")
    private Long driverId;
    
    private String driverName;
    
    @NotNull(message = "Otonom seviye gereklidir")
    private SmartVehicle.AutonomyLevel autonomyLevel;
    
    @Min(value = 1, message = "Yolcu kapasitesi en az 1 olmalıdır")
    @Max(value = 8, message = "Yolcu kapasitesi en fazla 8 olabilir")
    private Integer maxPassengerCapacity;
    
    @Min(value = 0, message = "Kargo kapasitesi negatif olamaz")
    private Double maxCargoCapacity;
    
    private String connectivityType; // 4G, 5G, WiFi, Satellite
    
    private Boolean v2vEnabled = true;
    private Boolean v2iEnabled = true;
    private Boolean v2pEnabled = true;
    
    private String encryptionLevel = "AES-256";
    private Boolean intrusionDetectionActive = true;
    
    private Map<String, Object> aiModelVersions;
    private Map<String, Object> sensorConfiguration;
    
    // Constructors
    public SmartVehicleRegisterDTO() {}
    
    // Getters and Setters
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public SmartVehicle.AutonomyLevel getAutonomyLevel() { return autonomyLevel; }
    public void setAutonomyLevel(SmartVehicle.AutonomyLevel autonomyLevel) { this.autonomyLevel = autonomyLevel; }
    
    public Integer getMaxPassengerCapacity() { return maxPassengerCapacity; }
    public void setMaxPassengerCapacity(Integer maxPassengerCapacity) { this.maxPassengerCapacity = maxPassengerCapacity; }
    
    public Double getMaxCargoCapacity() { return maxCargoCapacity; }
    public void setMaxCargoCapacity(Double maxCargoCapacity) { this.maxCargoCapacity = maxCargoCapacity; }
    
    public String getConnectivityType() { return connectivityType; }
    public void setConnectivityType(String connectivityType) { this.connectivityType = connectivityType; }
    
    public Boolean getV2vEnabled() { return v2vEnabled; }
    public void setV2vEnabled(Boolean v2vEnabled) { this.v2vEnabled = v2vEnabled; }
    
    public Boolean getV2iEnabled() { return v2iEnabled; }
    public void setV2iEnabled(Boolean v2iEnabled) { this.v2iEnabled = v2iEnabled; }
    
    public Boolean getV2pEnabled() { return v2pEnabled; }
    public void setV2pEnabled(Boolean v2pEnabled) { this.v2pEnabled = v2pEnabled; }
    
    public String getEncryptionLevel() { return encryptionLevel; }
    public void setEncryptionLevel(String encryptionLevel) { this.encryptionLevel = encryptionLevel; }
    
    public Boolean getIntrusionDetectionActive() { return intrusionDetectionActive; }
    public void setIntrusionDetectionActive(Boolean intrusionDetectionActive) { this.intrusionDetectionActive = intrusionDetectionActive; }
    
    public Map<String, Object> getAiModelVersions() { return aiModelVersions; }
    public void setAiModelVersions(Map<String, Object> aiModelVersions) { this.aiModelVersions = aiModelVersions; }
    
    public Map<String, Object> getSensorConfiguration() { return sensorConfiguration; }
    public void setSensorConfiguration(Map<String, Object> sensorConfiguration) { this.sensorConfiguration = sensorConfiguration; }
}

/**
 * Akıllı Araç Response DTO - API yanıtları için
 */
public class SmartVehicleResponseDTO {
    
    private Long id;
    private String vin;
    private String licensePlate;
    private String make;
    private String model;
    private Integer year;
    private String vehicleType;
    private SmartVehicle.VehicleStatus status;
    private SmartVehicle.AutonomyLevel autonomyLevel;
    private Long driverId;
    private String driverName;
    
    // Konum Bilgileri
    private Double currentLatitude;
    private Double currentLongitude;
    private Double currentSpeed;
    private Double currentHeading;
    private Double currentAltitude;
    
    // Araç Durumu
    private Double batteryLevel;
    private Double fuelLevel;
    private Integer currentPassengerCount;
    private Integer maxPassengerCapacity;
    private Double cargoWeight;
    private Double maxCargoCapacity;
    
    // İstatistikler
    private Integer totalTrips;
    private Double averageRating;
    private Double totalDistanceDriven;
    private Double averageFuelConsumption;
    
    // İletişim
    private Boolean v2vEnabled;
    private Boolean v2iEnabled;
    private Boolean v2pEnabled;
    private String connectivityType;
    
    // AI & Özellikler
    private Boolean ecoModeActive;
    private Boolean predictiveRoutingActive;
    private Map<String, Object> aiModelVersions;
    private Map<String, Object> activeSafetyFeatures;
    
    // Zaman Bilgileri
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActiveAt;
    
    // Constructors
    public SmartVehicleResponseDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    
    public SmartVehicle.VehicleStatus getStatus() { return status; }
    public void setStatus(SmartVehicle.VehicleStatus status) { this.status = status; }
    
    public SmartVehicle.AutonomyLevel getAutonomyLevel() { return autonomyLevel; }
    public void setAutonomyLevel(SmartVehicle.AutonomyLevel autonomyLevel) { this.autonomyLevel = autonomyLevel; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }
    
    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }
    
    public Double getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(Double currentSpeed) { this.currentSpeed = currentSpeed; }
    
    public Double getCurrentHeading() { return currentHeading; }
    public void setCurrentHeading(Double currentHeading) { this.currentHeading = currentHeading; }
    
    public Double getCurrentAltitude() { return currentAltitude; }
    public void setCurrentAltitude(Double currentAltitude) { this.currentAltitude = currentAltitude; }
    
    public Double getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Double batteryLevel) { this.batteryLevel = batteryLevel; }
    
    public Double getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(Double fuelLevel) { this.fuelLevel = fuelLevel; }
    
    public Integer getCurrentPassengerCount() { return currentPassengerCount; }
    public void setCurrentPassengerCount(Integer currentPassengerCount) { this.currentPassengerCount = currentPassengerCount; }
    
    public Integer getMaxPassengerCapacity() { return maxPassengerCapacity; }
    public void setMaxPassengerCapacity(Integer maxPassengerCapacity) { this.maxPassengerCapacity = maxPassengerCapacity; }
    
    public Double getCargoWeight() { return cargoWeight; }
    public void setCargoWeight(Double cargoWeight) { this.cargoWeight = cargoWeight; }
    
    public Double getMaxCargoCapacity() { return maxCargoCapacity; }
    public void setMaxCargoCapacity(Double maxCargoCapacity) { this.maxCargoCapacity = maxCargoCapacity; }
    
    public Integer getTotalTrips() { return totalTrips; }
    public void setTotalTrips(Integer totalTrips) { this.totalTrips = totalTrips; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Double getTotalDistanceDriven() { return totalDistanceDriven; }
    public void setTotalDistanceDriven(Double totalDistanceDriven) { this.totalDistanceDriven = totalDistanceDriven; }
    
    public Double getAverageFuelConsumption() { return averageFuelConsumption; }
    public void setAverageFuelConsumption(Double averageFuelConsumption) { this.averageFuelConsumption = averageFuelConsumption; }
    
    public Boolean getV2vEnabled() { return v2vEnabled; }
    public void setV2vEnabled(Boolean v2vEnabled) { this.v2vEnabled = v2vEnabled; }
    
    public Boolean getV2iEnabled() { return v2iEnabled; }
    public void setV2iEnabled(Boolean v2iEnabled) { this.v2iEnabled = v2iEnabled; }
    
    public Boolean getV2pEnabled() { return v2pEnabled; }
    public void setV2pEnabled(Boolean v2pEnabled) { this.v2pEnabled = v2pEnabled; }
    
    public String getConnectivityType() { return connectivityType; }
    public void setConnectivityType(String connectivityType) { this.connectivityType = connectivityType; }
    
    public Boolean getEcoModeActive() { return ecoModeActive; }
    public void setEcoModeActive(Boolean ecoModeActive) { this.ecoModeActive = ecoModeActive; }
    
    public Boolean getPredictiveRoutingActive() { return predictiveRoutingActive; }
    public void setPredictiveRoutingActive(Boolean predictiveRoutingActive) { this.predictiveRoutingActive = predictiveRoutingActive; }
    
    public Map<String, Object> getAiModelVersions() { return aiModelVersions; }
    public void setAiModelVersions(Map<String, Object> aiModelVersions) { this.aiModelVersions = aiModelVersions; }
    
    public Map<String, Object> getActiveSafetyFeatures() { return activeSafetyFeatures; }
    public void setActiveSafetyFeatures(Map<String, Object> activeSafetyFeatures) { this.activeSafetyFeatures = activeSafetyFeatures; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}

/**
 * Araç Durum Güncelleme DTO
 */
public class VehicleStatusUpdateDTO {
    
    @NotNull(message = "Araç durumu gereklidir")
    private SmartVehicle.VehicleStatus status;
    
    private Integer currentPassengerCount;
    private Double cargoWeight;
    private String statusReason;
    
    // Constructors
    public VehicleStatusUpdateDTO() {}
    
    // Getters and Setters
    public SmartVehicle.VehicleStatus getStatus() { return status; }
    public void setStatus(SmartVehicle.VehicleStatus status) { this.status = status; }
    
    public Integer getCurrentPassengerCount() { return currentPassengerCount; }
    public void setCurrentPassengerCount(Integer currentPassengerCount) { this.currentPassengerCount = currentPassengerCount; }
    
    public Double getCargoWeight() { return cargoWeight; }
    public void setCargoWeight(Double cargoWeight) { this.cargoWeight = cargoWeight; }
    
    public String getStatusReason() { return statusReason; }
    public void setStatusReason(String statusReason) { this.statusReason = statusReason; }
}

/**
 * Araç Konum Güncelleme DTO
 */
public class VehicleLocationUpdateDTO {
    
    @NotNull(message = "Enlem gereklidir")
    @DecimalMin(value = "-90.0", message = "Enlem -90 ile 90 arasında olmalıdır")
    @DecimalMax(value = "90.0", message = "Enlem -90 ile 90 arasında olmalıdır")
    private Double latitude;
    
    @NotNull(message = "Boylam gereklidir")
    @DecimalMin(value = "-180.0", message = "Boylam -180 ile 180 arasında olmalıdır")
    @DecimalMax(value = "180.0", message = "Boylam -180 ile 180 arasında olmalıdır")
    private Double longitude;
    
    private Double altitude;
    
    @DecimalMin(value = "0.0", message = "Hız negatif olamaz")
    @DecimalMax(value = "300.0", message = "Hız 300 km/h'yi aşamaz")
    private Double speed;
    
    @DecimalMin(value = "0.0", message = "Yön 0-360 derece arasında olmalıdır")
    @DecimalMax(value = "360.0", message = "Yön 0-360 derece arasında olmalıdır")
    private Double heading;
    
    private Double gpsAccuracy;
    private LocalDateTime timestamp;
    
    // Constructors
    public VehicleLocationUpdateDTO() {}
    
    // Getters and Setters
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
    
    public Double getGpsAccuracy() { return gpsAccuracy; }
    public void setGpsAccuracy(Double gpsAccuracy) { this.gpsAccuracy = gpsAccuracy; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}