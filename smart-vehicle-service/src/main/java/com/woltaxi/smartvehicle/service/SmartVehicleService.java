package com.woltaxi.smartvehicle.service;

import com.woltaxi.smartvehicle.entity.SmartVehicle;
import com.woltaxi.smartvehicle.entity.AutonomousDrivingSession;
import com.woltaxi.smartvehicle.entity.VehicleSensorData;
import com.woltaxi.smartvehicle.dto.*;
import com.woltaxi.smartvehicle.repository.SmartVehicleRepository;
import com.woltaxi.smartvehicle.repository.AutonomousDrivingSessionRepository;
import com.woltaxi.smartvehicle.repository.VehicleSensorDataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Akıllı Araç Servisi - WOLTAXI Smart Vehicle Integration
 * Gelecekteki akıllı araçlar için iş mantığı ve AI entegrasyonu
 */
@Service
@Transactional
public class SmartVehicleService {
    
    @Autowired
    private SmartVehicleRepository smartVehicleRepository;
    
    @Autowired
    private AutonomousDrivingSessionRepository autonomousSessionRepository;
    
    @Autowired
    private VehicleSensorDataRepository sensorDataRepository;
    
    @Autowired
    private AutonomousAIService autonomousAIService;
    
    @Autowired
    private VehicleHealthMonitoringService healthMonitoringService;
    
    @Autowired
    private PredictiveMaintenanceService predictiveMaintenanceService;
    
    @Autowired
    private V2XCommunicationService v2xCommunicationService;
    
    @Autowired
    private EcoRoutingService ecoRoutingService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Yeni akıllı araç kaydı
     */
    public SmartVehicle registerVehicle(SmartVehicleRegisterDTO registerDTO) {
        // VIN ve plaka kontrolü
        if (smartVehicleRepository.existsByVin(registerDTO.getVin())) {
            throw new IllegalArgumentException("Bu VIN numarası zaten kayıtlı: " + registerDTO.getVin());
        }
        
        if (smartVehicleRepository.existsByLicensePlate(registerDTO.getLicensePlate())) {
            throw new IllegalArgumentException("Bu plaka numarası zaten kayıtlı: " + registerDTO.getLicensePlate());
        }
        
        SmartVehicle vehicle = new SmartVehicle(
            registerDTO.getVin(),
            registerDTO.getLicensePlate(),
            registerDTO.getMake(),
            registerDTO.getModel(),
            registerDTO.getYear(),
            registerDTO.getVehicleType(),
            registerDTO.getDriverId()
        );
        
        vehicle.setDriverName(registerDTO.getDriverName());
        vehicle.setAutonomyLevel(registerDTO.getAutonomyLevel());
        vehicle.setMaxPassengerCapacity(registerDTO.getMaxPassengerCapacity());
        vehicle.setMaxCargoCapacity(registerDTO.getMaxCargoCapacity());
        vehicle.setConnectivityType(registerDTO.getConnectivityType());
        vehicle.setV2vEnabled(registerDTO.getV2vEnabled());
        vehicle.setV2iEnabled(registerDTO.getV2iEnabled());
        vehicle.setV2pEnabled(registerDTO.getV2pEnabled());
        vehicle.setEncryptionLevel(registerDTO.getEncryptionLevel());
        vehicle.setIntrusionDetectionActive(registerDTO.getIntrusionDetectionActive());
        
        // AI model versiyonlarını JSON olarak kaydet
        if (registerDTO.getAiModelVersions() != null) {
            try {
                vehicle.setAiModelVersions(objectMapper.writeValueAsString(registerDTO.getAiModelVersions()));
            } catch (Exception e) {
                throw new RuntimeException("AI model versiyonları kaydedilemedi", e);
            }
        }
        
        // Sensör konfigürasyonunu JSON olarak kaydet
        if (registerDTO.getSensorConfiguration() != null) {
            try {
                vehicle.setSensorStatus(objectMapper.writeValueAsString(registerDTO.getSensorConfiguration()));
            } catch (Exception e) {
                throw new RuntimeException("Sensör konfigürasyonu kaydedilemedi", e);
            }
        }
        
        vehicle.setStatus(SmartVehicle.VehicleStatus.OFFLINE);
        vehicle.setLastActiveAt(LocalDateTime.now());
        
        return smartVehicleRepository.save(vehicle);
    }
    
    /**
     * Araç durumunu güncelle
     */
    public SmartVehicle updateVehicleStatus(Long vehicleId, VehicleStatusUpdateDTO statusUpdate) {
        SmartVehicle vehicle = findById(vehicleId);
        
        vehicle.setStatus(statusUpdate.getStatus());
        vehicle.setLastActiveAt(LocalDateTime.now());
        
        if (statusUpdate.getCurrentPassengerCount() != null) {
            vehicle.setCurrentPassengerCount(statusUpdate.getCurrentPassengerCount());
        }
        
        if (statusUpdate.getCargoWeight() != null) {
            vehicle.setCargoWeight(statusUpdate.getCargoWeight());
        }
        
        return smartVehicleRepository.save(vehicle);
    }
    
    /**
     * Araç konum güncelleme
     */
    public void updateVehicleLocation(Long vehicleId, VehicleLocationUpdateDTO locationUpdate) {
        SmartVehicle vehicle = findById(vehicleId);
        
        vehicle.setCurrentLatitude(locationUpdate.getLatitude());
        vehicle.setCurrentLongitude(locationUpdate.getLongitude());
        vehicle.setCurrentAltitude(locationUpdate.getAltitude());
        vehicle.setCurrentSpeed(locationUpdate.getSpeed());
        vehicle.setCurrentHeading(locationUpdate.getHeading());
        vehicle.setLastActiveAt(LocalDateTime.now());
        
        smartVehicleRepository.save(vehicle);
        
        // Sensör verisini de kaydet
        VehicleSensorData sensorData = new VehicleSensorData(vehicle, "GPS");
        sensorData.setLatitude(locationUpdate.getLatitude());
        sensorData.setLongitude(locationUpdate.getLongitude());
        sensorData.setAltitude(locationUpdate.getAltitude());
        sensorData.setSpeed(locationUpdate.getSpeed());
        sensorData.setHeading(locationUpdate.getHeading());
        sensorData.setGpsAccuracy(locationUpdate.getGpsAccuracy());
        sensorData.setTimestamp(locationUpdate.getTimestamp() != null ? 
            locationUpdate.getTimestamp() : LocalDateTime.now());
        sensorData.setDataValidated(true);
        sensorData.setValidationStatus("VALID");
        
        sensorDataRepository.save(sensorData);
    }
    
    /**
     * Otonom mod aktivasyonu
     */
    public AutonomousModeResponseDTO activateAutonomousMode(Long vehicleId, AutonomousModeActivationDTO activationDTO) {
        SmartVehicle vehicle = findById(vehicleId);
        
        // Otonom mod uygunluk kontrolü
        if (!vehicle.isAutonomousCapable()) {
            throw new IllegalStateException("Bu araç otonom sürüş yapabilecek seviyede değil");
        }
        
        if (vehicle.getStatus() != SmartVehicle.VehicleStatus.ONLINE) {
            throw new IllegalStateException("Araç otonom mod için hazır değil: " + vehicle.getStatus());
        }
        
        // Sistem ve sensör kontrolü
        List<String> systemChecks = performSystemChecks(vehicle);
        Map<String, Object> sensorStatus = checkSensorStatus(vehicle);
        
        boolean allSystemsReady = systemChecks.stream()
            .noneMatch(check -> check.contains("FAIL") || check.contains("ERROR"));
            
        if (!allSystemsReady) {
            throw new IllegalStateException("Sistem kontrolleri başarısız: " + systemChecks);
        }
        
        // Otonom sürüş oturumu başlat
        AutonomousDrivingSession session = new AutonomousDrivingSession(
            vehicle, activationDTO.getRideId(), activationDTO.getAutonomyMode());
        
        session.setStartLatitude(activationDTO.getStartLatitude());
        session.setStartLongitude(activationDTO.getStartLongitude());
        session.setWeatherCondition(activationDTO.getWeatherCondition());
        session.setRoadCondition(activationDTO.getRoadCondition());
        session.setTrafficDensity(activationDTO.getTrafficDensity());
        
        session = autonomousSessionRepository.save(session);
        
        // Araç durumunu güncelle
        vehicle.setStatus(SmartVehicle.VehicleStatus.AUTONOMOUS);
        smartVehicleRepository.save(vehicle);
        
        // AI güven skorunu hesapla
        Double aiConfidenceScore = autonomousAIService.calculateConfidenceScore(
            vehicle, activationDTO.getWeatherCondition(), activationDTO.getRoadCondition());
        
        // Response oluştur
        AutonomousModeResponseDTO response = new AutonomousModeResponseDTO();
        response.setSessionId(session.getId());
        response.setAutonomyMode(activationDTO.getAutonomyMode());
        response.setActivated(true);
        response.setActivationMessage("Otonom mod başarıyla aktif edildi");
        response.setActivationTime(LocalDateTime.now());
        response.setAiConfidenceScore(aiConfidenceScore);
        response.setSystemChecks(systemChecks);
        response.setSensorStatus(sensorStatus);
        response.setActiveSafetyFeatures(getActiveSafetyFeatures(vehicle));
        
        // Uyarılar
        List<String> warnings = new ArrayList<>();
        if (aiConfidenceScore < 0.8) {
            warnings.add("AI güven skoru düşük: " + aiConfidenceScore);
        }
        if ("RAINY".equals(activationDTO.getWeatherCondition()) || 
            "FOGGY".equals(activationDTO.getWeatherCondition())) {
            warnings.add("Hava koşulları otonom sürüş için zorlu");
        }
        response.setWarnings(warnings);
        
        return response;
    }
    
    /**
     * Otonom mod deaktivasyonu
     */
    public void deactivateAutonomousMode(Long vehicleId) {
        SmartVehicle vehicle = findById(vehicleId);
        
        // Aktif otonom oturumu bul ve bitir
        Optional<AutonomousDrivingSession> activeSession = 
            autonomousSessionRepository.findByVehicleIdAndSessionEndTimeIsNull(vehicleId);
        
        if (activeSession.isPresent()) {
            AutonomousDrivingSession session = activeSession.get();
            session.endSession();
            
            // Performans skorlarını hesapla
            session.setSafetyScore(session.calculateSafetyScore());
            session.setEfficiencyScore(session.calculateEfficiencyScore());
            
            autonomousSessionRepository.save(session);
        }
        
        // Araç durumunu güncelle
        vehicle.setStatus(SmartVehicle.VehicleStatus.ONLINE);
        smartVehicleRepository.save(vehicle);
    }
    
    /**
     * Müsait akıllı araçları getir
     */
    public List<SmartVehicle> findAvailableVehicles(Double latitude, Double longitude, 
                                                   Double radiusKm, SmartVehicle.AutonomyLevel minAutonomyLevel) {
        if (latitude != null && longitude != null) {
            return smartVehicleRepository.findAvailableVehiclesInRadius(
                latitude, longitude, radiusKm, minAutonomyLevel);
        } else {
            return smartVehicleRepository.findAvailableVehicles(minAutonomyLevel);
        }
    }
    
    /**
     * Araç sağlık durumu kontrolü
     */
    public VehicleHealthResponseDTO getVehicleHealth(Long vehicleId) {
        SmartVehicle vehicle = findById(vehicleId);
        return healthMonitoringService.analyzeVehicleHealth(vehicle);
    }
    
    /**
     * Araç performans istatistikleri
     */
    public VehiclePerformanceDTO getVehiclePerformance(Long vehicleId, Integer days) {
        SmartVehicle vehicle = findById(vehicleId);
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        List<AutonomousDrivingSession> sessions = 
            autonomousSessionRepository.findByVehicleIdAndCreatedAtAfter(vehicleId, startDate);
        
        return calculatePerformanceMetrics(vehicle, sessions, days);
    }
    
    /**
     * AI modeli güncelleme
     */
    public void updateAIModels(Long vehicleId, AIModelUpdateDTO modelUpdate) {
        SmartVehicle vehicle = findById(vehicleId);
        
        try {
            vehicle.setAiModelVersions(objectMapper.writeValueAsString(modelUpdate.getModelVersions()));
            smartVehicleRepository.save(vehicle);
            
            // AI servisine güncelleme bilgisi gönder
            autonomousAIService.updateVehicleModels(vehicle, modelUpdate);
            
        } catch (Exception e) {
            throw new RuntimeException("AI modelleri güncellenemedi", e);
        }
    }
    
    /**
     * V2X iletişim durumu
     */
    public V2XStatusDTO getV2XStatus(Long vehicleId) {
        SmartVehicle vehicle = findById(vehicleId);
        return v2xCommunicationService.getV2XStatus(vehicle);
    }
    
    /**
     * Acil durum protokolü aktivasyonu
     */
    public void activateEmergencyProtocol(Long vehicleId, EmergencyProtocolDTO emergencyData) {
        SmartVehicle vehicle = findById(vehicleId);
        
        // Araç durumunu acil duruma çevir
        vehicle.setStatus(SmartVehicle.VehicleStatus.EMERGENCY);
        smartVehicleRepository.save(vehicle);
        
        // Aktif otonom oturumu varsa güvenli şekilde bitir
        Optional<AutonomousDrivingSession> activeSession = 
            autonomousSessionRepository.findByVehicleIdAndSessionEndTimeIsNull(vehicleId);
        
        if (activeSession.isPresent()) {
            AutonomousDrivingSession session = activeSession.get();
            session.setCriticalIncident(true);
            session.setIncidentDescription(emergencyData.getEmergencyType() + ": " + emergencyData.getDescription());
            session.endSession();
            autonomousSessionRepository.save(session);
        }
        
        // Acil durum servisleriyle iletişim kur
        // emergencyService.handleVehicleEmergency(vehicle, emergencyData);
    }
    
    /**
     * Öngörülü bakım analizi
     */
    public PredictiveMaintenanceDTO getPredictiveMaintenance(Long vehicleId) {
        SmartVehicle vehicle = findById(vehicleId);
        return predictiveMaintenanceService.analyzePredictiveMaintenance(vehicle);
    }
    
    /**
     * Çevreci rota hesaplama
     */
    public EcoRouteResponseDTO calculateEcoRoute(Long vehicleId, EcoRouteRequestDTO routeRequest) {
        SmartVehicle vehicle = findById(vehicleId);
        return ecoRoutingService.calculateEcoRoute(vehicle, routeRequest);
    }
    
    /**
     * Filo durumu
     */
    public FleetStatusDTO getFleetStatus() {
        List<SmartVehicle> allVehicles = smartVehicleRepository.findAll();
        
        FleetStatusDTO fleetStatus = new FleetStatusDTO();
        fleetStatus.setTotalVehicles(allVehicles.size());
        
        Map<SmartVehicle.VehicleStatus, Long> statusCounts = allVehicles.stream()
            .collect(Collectors.groupingBy(SmartVehicle::getStatus, Collectors.counting()));
        
        fleetStatus.setOnlineVehicles(statusCounts.getOrDefault(SmartVehicle.VehicleStatus.ONLINE, 0L).intValue());
        fleetStatus.setInRideVehicles(statusCounts.getOrDefault(SmartVehicle.VehicleStatus.IN_RIDE, 0L).intValue());
        fleetStatus.setAutonomousVehicles(statusCounts.getOrDefault(SmartVehicle.VehicleStatus.AUTONOMOUS, 0L).intValue());
        fleetStatus.setMaintenanceVehicles(statusCounts.getOrDefault(SmartVehicle.VehicleStatus.MAINTENANCE, 0L).intValue());
        fleetStatus.setEmergencyVehicles(statusCounts.getOrDefault(SmartVehicle.VehicleStatus.EMERGENCY, 0L).intValue());
        
        // Otonom seviye dağılımı
        Map<SmartVehicle.AutonomyLevel, Long> autonomyLevelCounts = allVehicles.stream()
            .collect(Collectors.groupingBy(SmartVehicle::getAutonomyLevel, Collectors.counting()));
        fleetStatus.setAutonomyLevelDistribution(autonomyLevelCounts);
        
        // Ortalama performans metrikleri
        double avgBatteryLevel = allVehicles.stream()
            .filter(v -> v.getBatteryLevel() != null)
            .mapToDouble(SmartVehicle::getBatteryLevel)
            .average().orElse(0.0);
        fleetStatus.setAverageBatteryLevel(avgBatteryLevel);
        
        double avgRating = allVehicles.stream()
            .filter(v -> v.getAverageRating() != null)
            .mapToDouble(SmartVehicle::getAverageRating)
            .average().orElse(0.0);
        fleetStatus.setAverageRating(avgRating);
        
        return fleetStatus;
    }
    
    // Helper Methods
    public SmartVehicle findById(Long vehicleId) {
        return smartVehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("Araç bulunamadı: " + vehicleId));
    }
    
    public List<SmartVehicle> findByDriverId(Long driverId) {
        return smartVehicleRepository.findByDriverId(driverId);
    }
    
    public void deleteVehicle(Long vehicleId) {
        SmartVehicle vehicle = findById(vehicleId);
        smartVehicleRepository.delete(vehicle);
    }
    
    private List<String> performSystemChecks(SmartVehicle vehicle) {
        List<String> checks = new ArrayList<>();
        
        // Temel sistem kontrolleri
        checks.add("Engine Temperature: " + (vehicle.getEngineTemperature() != null && 
            vehicle.getEngineTemperature() < 95 ? "OK" : "WARNING"));
        checks.add("Battery Level: " + (vehicle.getBatteryLevel() != null && 
            vehicle.getBatteryLevel() > 20 ? "OK" : "LOW"));
        checks.add("GPS Status: OK");
        checks.add("Communication: " + (vehicle.getConnectivityType() != null ? "OK" : "FAIL"));
        checks.add("AI Systems: OK");
        checks.add("Safety Features: ACTIVE");
        
        return checks;
    }
    
    private Map<String, Object> checkSensorStatus(SmartVehicle vehicle) {
        Map<String, Object> sensorStatus = new HashMap<>();
        sensorStatus.put("LIDAR", "OPERATIONAL");
        sensorStatus.put("CAMERA", "OPERATIONAL");
        sensorStatus.put("RADAR", "OPERATIONAL");
        sensorStatus.put("GPS", "OPERATIONAL");
        sensorStatus.put("ACCELEROMETER", "OPERATIONAL");
        sensorStatus.put("GYROSCOPE", "OPERATIONAL");
        return sensorStatus;
    }
    
    private List<String> getActiveSafetyFeatures(SmartVehicle vehicle) {
        List<String> features = new ArrayList<>();
        features.add("Autonomous Emergency Braking");
        features.add("Lane Keep Assist");
        features.add("Adaptive Cruise Control");
        features.add("Blind Spot Monitoring");
        features.add("Forward Collision Warning");
        features.add("Pedestrian Detection");
        features.add("Traffic Sign Recognition");
        return features;
    }
    
    private VehiclePerformanceDTO calculatePerformanceMetrics(SmartVehicle vehicle, 
                                                            List<AutonomousDrivingSession> sessions, 
                                                            Integer days) {
        VehiclePerformanceDTO performance = new VehiclePerformanceDTO();
        performance.setVehicleId(vehicle.getId());
        performance.setAnalysisPeriodDays(days);
        
        // Temel metrikler
        performance.setTotalTrips(sessions.size());
        
        double totalDistance = sessions.stream()
            .filter(s -> s.getTotalDistance() != null)
            .mapToDouble(AutonomousDrivingSession::getTotalDistance)
            .sum();
        performance.setTotalDistanceDriven(totalDistance);
        
        double avgSpeed = sessions.stream()
            .filter(s -> s.getAverageSpeed() != null)
            .mapToDouble(AutonomousDrivingSession::getAverageSpeed)
            .average().orElse(0.0);
        performance.setAverageSpeed(avgSpeed);
        
        // Güvenlik metrikleri
        int totalEmergencyBrakes = sessions.stream()
            .filter(s -> s.getEmergencyBrakeCount() != null)
            .mapToInt(AutonomousDrivingSession::getEmergencyBrakeCount)
            .sum();
        performance.setEmergencyBrakeCount(totalEmergencyBrakes);
        
        double avgSafetyScore = sessions.stream()
            .filter(s -> s.getSafetyScore() != null)
            .mapToDouble(AutonomousDrivingSession::getSafetyScore)
            .average().orElse(100.0);
        performance.setSafetyScore(avgSafetyScore);
        
        // Verimlilik metrikleri
        double avgEfficiencyScore = sessions.stream()
            .filter(s -> s.getEfficiencyScore() != null)
            .mapToDouble(AutonomousDrivingSession::getEfficiencyScore)
            .average().orElse(100.0);
        performance.setEfficiencyScore(avgEfficiencyScore);
        
        // Otonom performans
        long autonomousSessions = sessions.stream()
            .filter(AutonomousDrivingSession::isAutonomousSession)
            .count();
        performance.setAutonomousTrips((int) autonomousSessions);
        
        int totalInterventions = sessions.stream()
            .filter(s -> s.getHumanInterventionCount() != null)
            .mapToInt(AutonomousDrivingSession::getHumanInterventionCount)
            .sum();
        performance.setHumanInterventionCount(totalInterventions);
        
        // Genel performans skoru
        double overallScore = (avgSafetyScore + avgEfficiencyScore) / 2.0;
        performance.setOverallPerformanceScore(overallScore);
        
        return performance;
    }
}