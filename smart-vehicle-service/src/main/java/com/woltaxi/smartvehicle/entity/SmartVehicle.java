package com.woltaxi.smartvehicle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Akıllı Araç Entity - WOLTAXI Smart Vehicle Integration
 * Gelecekteki akıllı araçlar için tüm özellikler ve AI entegrasyonu
 */
@Entity
@Table(name = "smart_vehicles")
public class SmartVehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "VIN numarası gereklidir")
    private String vin; // Vehicle Identification Number
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Plaka numarası gereklidir")
    private String licensePlate;
    
    @Column(nullable = false)
    @NotBlank(message = "Marka gereklidir")
    private String make; // Tesla, BMW, Mercedes, Toyota vs.
    
    @Column(nullable = false)
    @NotBlank(message = "Model gereklidir")
    private String model;
    
    @Column(nullable = false)
    @Min(value = 2020, message = "Akıllı araç en az 2020 model olmalıdır")
    private Integer year;
    
    @Column(nullable = false)
    @NotBlank(message = "Araç tipi gereklidir")
    private String vehicleType; // SEDAN, SUV, HATCHBACK, ELECTRIC, HYBRID
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleStatus status;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AutonomyLevel autonomyLevel; // L0, L1, L2, L3, L4, L5
    
    // Sürücü Bilgileri
    @Column(nullable = false)
    private Long driverId;
    
    @Column
    private String driverName;
    
    // Konum Bilgileri
    @Column(precision = 10, scale = 7)
    private Double currentLatitude;
    
    @Column(precision = 10, scale = 7)
    private Double currentLongitude;
    
    @Column
    private Double currentSpeed; // km/h
    
    @Column
    private Double currentHeading; // derece (0-360)
    
    @Column
    private Double currentAltitude; // metre
    
    // Araç Sağlık Durumu
    @Column
    private Double engineTemperature; // Celsius
    
    @Column
    private Double batteryLevel; // Yüzde (0-100)
    
    @Column
    private Double fuelLevel; // Yüzde (0-100)
    
    @Column
    private Integer odometer; // Toplam kilometre
    
    @Column
    private Double tirePressureFrontLeft; // PSI
    
    @Column
    private Double tirePressureFrontRight; // PSI
    
    @Column
    private Double tirePressureRearLeft; // PSI
    
    @Column
    private Double tirePressureRearRight; // PSI
    
    // AI & Sensör Durumu
    @Column(columnDefinition = "TEXT")
    private String aiModelVersions; // JSON format
    
    @Column(columnDefinition = "TEXT")
    private String sensorStatus; // JSON format - LIDAR, Camera, Radar vs.
    
    @Column(columnDefinition = "TEXT")
    private String activeSafetyFeatures; // JSON format
    
    // İletişim Özellikleri
    @Column
    private Boolean v2vEnabled; // Vehicle to Vehicle
    
    @Column
    private Boolean v2iEnabled; // Vehicle to Infrastructure
    
    @Column
    private Boolean v2pEnabled; // Vehicle to Pedestrian
    
    @Column
    private String connectivityType; // 4G, 5G, WiFi, Satellite
    
    // Güvenlik ve Şifreleme
    @Column
    private String securityCertificate;
    
    @Column
    private String encryptionLevel;
    
    @Column
    private Boolean intrusionDetectionActive;
    
    // Rota ve Navigasyon
    @Column(columnDefinition = "TEXT")
    private String currentRoute; // JSON format - waypoints
    
    @Column(columnDefinition = "TEXT")
    private String trafficData; // JSON format - gerçek zamanlı trafik
    
    @Column
    private Boolean ecoModeActive;
    
    @Column
    private Boolean predictiveRoutingActive;
    
    // Yolcu ve Kargo Bilgileri
    @Column
    private Integer currentPassengerCount;
    
    @Column
    private Integer maxPassengerCapacity;
    
    @Column
    private Double cargoWeight; // kg
    
    @Column
    private Double maxCargoCapacity; // kg
    
    // İstatistikler ve Analitik
    @Column
    private Double totalDistanceDriven; // km
    
    @Column
    private Double averageFuelConsumption; // L/100km
    
    @Column
    private Double co2Emission; // g/km
    
    @Column
    private Integer totalTrips;
    
    @Column
    private Double averageRating;
    
    // Bakım ve Servis
    @Column
    private LocalDateTime lastMaintenanceDate;
    
    @Column
    private LocalDateTime nextMaintenanceDate;
    
    @Column
    private Integer maintenanceAlertDistance; // km kala uyarı
    
    @Column(columnDefinition = "TEXT")
    private String diagnosticCodes; // JSON format - OBD codes
    
    // Zaman Damgaları
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime lastActiveAt;
    
    // Constructors
    public SmartVehicle() {}
    
    public SmartVehicle(String vin, String licensePlate, String make, String model, 
                       Integer year, String vehicleType, Long driverId) {
        this.vin = vin;
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
        this.year = year;
        this.vehicleType = vehicleType;
        this.driverId = driverId;
        this.status = VehicleStatus.OFFLINE;
        this.autonomyLevel = AutonomyLevel.L0;
        this.batteryLevel = 100.0;
        this.currentPassengerCount = 0;
        this.totalTrips = 0;
        this.averageRating = 5.0;
    }
    
    // Enums
    public enum VehicleStatus {
        OFFLINE,        // Çevrimdışı
        ONLINE,         // Çevrimiçi - Beklemede
        IN_RIDE,        // Yolculukta
        MAINTENANCE,    // Bakımda
        CHARGING,       // Şarj oluyor
        EMERGENCY,      // Acil durum
        AUTONOMOUS,     // Otonom modda
        MANUAL          // Manuel sürüş
    }
    
    public enum AutonomyLevel {
        L0,  // No Automation - Manuel sürüş
        L1,  // Driver Assistance - Sürücü yardımı
        L2,  // Partial Automation - Kısmi otomasyon
        L3,  // Conditional Automation - Koşullu otomasyon
        L4,  // High Automation - Yüksek otomasyon
        L5   // Full Automation - Tam otomasyon
    }
    
    // Business Methods
    public boolean isAutonomousCapable() {
        return autonomyLevel.ordinal() >= AutonomyLevel.L3.ordinal();
    }
    
    public boolean isFullyAutonomous() {
        return autonomyLevel == AutonomyLevel.L5;
    }
    
    public boolean needsMaintenance() {
        return nextMaintenanceDate != null && 
               nextMaintenanceDate.isBefore(LocalDateTime.now().plusDays(7));
    }
    
    public boolean isLowBattery() {
        return batteryLevel != null && batteryLevel < 20.0;
    }
    
    public boolean isAvailableForRide() {
        return status == VehicleStatus.ONLINE && !needsMaintenance() && 
               !isLowBattery() && currentPassengerCount < maxPassengerCapacity;
    }
    
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
    
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    
    public AutonomyLevel getAutonomyLevel() { return autonomyLevel; }
    public void setAutonomyLevel(AutonomyLevel autonomyLevel) { this.autonomyLevel = autonomyLevel; }
    
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
    
    public Double getEngineTemperature() { return engineTemperature; }
    public void setEngineTemperature(Double engineTemperature) { this.engineTemperature = engineTemperature; }
    
    public Double getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Double batteryLevel) { this.batteryLevel = batteryLevel; }
    
    public Double getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(Double fuelLevel) { this.fuelLevel = fuelLevel; }
    
    public Integer getOdometer() { return odometer; }
    public void setOdometer(Integer odometer) { this.odometer = odometer; }
    
    public Double getTirePressureFrontLeft() { return tirePressureFrontLeft; }
    public void setTirePressureFrontLeft(Double tirePressureFrontLeft) { this.tirePressureFrontLeft = tirePressureFrontLeft; }
    
    public Double getTirePressureFrontRight() { return tirePressureFrontRight; }
    public void setTirePressureFrontRight(Double tirePressureFrontRight) { this.tirePressureFrontRight = tirePressureFrontRight; }
    
    public Double getTirePressureRearLeft() { return tirePressureRearLeft; }
    public void setTirePressureRearLeft(Double tirePressureRearLeft) { this.tirePressureRearLeft = tirePressureRearLeft; }
    
    public Double getTirePressureRearRight() { return tirePressureRearRight; }
    public void setTirePressureRearRight(Double tirePressureRearRight) { this.tirePressureRearRight = tirePressureRearRight; }
    
    public String getAiModelVersions() { return aiModelVersions; }
    public void setAiModelVersions(String aiModelVersions) { this.aiModelVersions = aiModelVersions; }
    
    public String getSensorStatus() { return sensorStatus; }
    public void setSensorStatus(String sensorStatus) { this.sensorStatus = sensorStatus; }
    
    public String getActiveSafetyFeatures() { return activeSafetyFeatures; }
    public void setActiveSafetyFeatures(String activeSafetyFeatures) { this.activeSafetyFeatures = activeSafetyFeatures; }
    
    public Boolean getV2vEnabled() { return v2vEnabled; }
    public void setV2vEnabled(Boolean v2vEnabled) { this.v2vEnabled = v2vEnabled; }
    
    public Boolean getV2iEnabled() { return v2iEnabled; }
    public void setV2iEnabled(Boolean v2iEnabled) { this.v2iEnabled = v2iEnabled; }
    
    public Boolean getV2pEnabled() { return v2pEnabled; }
    public void setV2pEnabled(Boolean v2pEnabled) { this.v2pEnabled = v2pEnabled; }
    
    public String getConnectivityType() { return connectivityType; }
    public void setConnectivityType(String connectivityType) { this.connectivityType = connectivityType; }
    
    public String getSecurityCertificate() { return securityCertificate; }
    public void setSecurityCertificate(String securityCertificate) { this.securityCertificate = securityCertificate; }
    
    public String getEncryptionLevel() { return encryptionLevel; }
    public void setEncryptionLevel(String encryptionLevel) { this.encryptionLevel = encryptionLevel; }
    
    public Boolean getIntrusionDetectionActive() { return intrusionDetectionActive; }
    public void setIntrusionDetectionActive(Boolean intrusionDetectionActive) { this.intrusionDetectionActive = intrusionDetectionActive; }
    
    public String getCurrentRoute() { return currentRoute; }
    public void setCurrentRoute(String currentRoute) { this.currentRoute = currentRoute; }
    
    public String getTrafficData() { return trafficData; }
    public void setTrafficData(String trafficData) { this.trafficData = trafficData; }
    
    public Boolean getEcoModeActive() { return ecoModeActive; }
    public void setEcoModeActive(Boolean ecoModeActive) { this.ecoModeActive = ecoModeActive; }
    
    public Boolean getPredictiveRoutingActive() { return predictiveRoutingActive; }
    public void setPredictiveRoutingActive(Boolean predictiveRoutingActive) { this.predictiveRoutingActive = predictiveRoutingActive; }
    
    public Integer getCurrentPassengerCount() { return currentPassengerCount; }
    public void setCurrentPassengerCount(Integer currentPassengerCount) { this.currentPassengerCount = currentPassengerCount; }
    
    public Integer getMaxPassengerCapacity() { return maxPassengerCapacity; }
    public void setMaxPassengerCapacity(Integer maxPassengerCapacity) { this.maxPassengerCapacity = maxPassengerCapacity; }
    
    public Double getCargoWeight() { return cargoWeight; }
    public void setCargoWeight(Double cargoWeight) { this.cargoWeight = cargoWeight; }
    
    public Double getMaxCargoCapacity() { return maxCargoCapacity; }
    public void setMaxCargoCapacity(Double maxCargoCapacity) { this.maxCargoCapacity = maxCargoCapacity; }
    
    public Double getTotalDistanceDriven() { return totalDistanceDriven; }
    public void setTotalDistanceDriven(Double totalDistanceDriven) { this.totalDistanceDriven = totalDistanceDriven; }
    
    public Double getAverageFuelConsumption() { return averageFuelConsumption; }
    public void setAverageFuelConsumption(Double averageFuelConsumption) { this.averageFuelConsumption = averageFuelConsumption; }
    
    public Double getCo2Emission() { return co2Emission; }
    public void setCo2Emission(Double co2Emission) { this.co2Emission = co2Emission; }
    
    public Integer getTotalTrips() { return totalTrips; }
    public void setTotalTrips(Integer totalTrips) { this.totalTrips = totalTrips; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public LocalDateTime getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    
    public LocalDateTime getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }
    
    public Integer getMaintenanceAlertDistance() { return maintenanceAlertDistance; }
    public void setMaintenanceAlertDistance(Integer maintenanceAlertDistance) { this.maintenanceAlertDistance = maintenanceAlertDistance; }
    
    public String getDiagnosticCodes() { return diagnosticCodes; }
    public void setDiagnosticCodes(String diagnosticCodes) { this.diagnosticCodes = diagnosticCodes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}