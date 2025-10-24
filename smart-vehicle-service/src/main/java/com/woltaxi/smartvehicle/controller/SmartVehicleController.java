package com.woltaxi.smartvehicle.controller;

import com.woltaxi.smartvehicle.entity.SmartVehicle;
import com.woltaxi.smartvehicle.service.SmartVehicleService;
import com.woltaxi.smartvehicle.dto.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Akıllı Araç Controller - WOLTAXI Smart Vehicle Integration
 * Gelecekteki akıllı araçlar için REST API endpoint'leri
 */
@RestController
@RequestMapping("/api/v1/smart-vehicles")
@Validated
@CrossOrigin(origins = "*")
public class SmartVehicleController {
    
    @Autowired
    private SmartVehicleService smartVehicleService;
    
    /**
     * Yeni akıllı araç kaydı
     */
    @PostMapping("/register")
    public ResponseEntity<SmartVehicleResponseDTO> registerVehicle(
            @Valid @RequestBody SmartVehicleRegisterDTO registerDTO) {
        try {
            SmartVehicle vehicle = smartVehicleService.registerVehicle(registerDTO);
            SmartVehicleResponseDTO response = convertToResponseDTO(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Araç durumunu güncelle
     */
    @PutMapping("/{vehicleId}/status")
    public ResponseEntity<SmartVehicleResponseDTO> updateVehicleStatus(
            @PathVariable @NotNull Long vehicleId,
            @Valid @RequestBody VehicleStatusUpdateDTO statusUpdate) {
        try {
            SmartVehicle vehicle = smartVehicleService.updateVehicleStatus(vehicleId, statusUpdate);
            SmartVehicleResponseDTO response = convertToResponseDTO(vehicle);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Araç konum güncelleme (gerçek zamanlı)
     */
    @PutMapping("/{vehicleId}/location")
    public ResponseEntity<String> updateVehicleLocation(
            @PathVariable @NotNull Long vehicleId,
            @Valid @RequestBody VehicleLocationUpdateDTO locationUpdate) {
        try {
            smartVehicleService.updateVehicleLocation(vehicleId, locationUpdate);
            return ResponseEntity.ok("Konum güncellendi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Otonom mod aktivasyonu
     */
    @PutMapping("/{vehicleId}/autonomous")
    public ResponseEntity<AutonomousModeResponseDTO> activateAutonomousMode(
            @PathVariable @NotNull Long vehicleId,
            @Valid @RequestBody AutonomousModeActivationDTO activationDTO) {
        try {
            AutonomousModeResponseDTO response = smartVehicleService.activateAutonomousMode(vehicleId, activationDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Otonom mod deaktivasyonu
     */
    @PutMapping("/{vehicleId}/autonomous/deactivate")
    public ResponseEntity<String> deactivateAutonomousMode(
            @PathVariable @NotNull Long vehicleId) {
        try {
            smartVehicleService.deactivateAutonomousMode(vehicleId);
            return ResponseEntity.ok("Otonom mod deaktif edildi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Müsait akıllı araçları getir
     */
    @GetMapping("/available")
    public ResponseEntity<List<SmartVehicleResponseDTO>> getAvailableVehicles(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "5.0") Double radiusKm,
            @RequestParam(required = false) SmartVehicle.AutonomyLevel minAutonomyLevel) {
        try {
            List<SmartVehicle> vehicles = smartVehicleService.findAvailableVehicles(
                latitude, longitude, radiusKm, minAutonomyLevel);
            List<SmartVehicleResponseDTO> response = vehicles.stream()
                .map(this::convertToResponseDTO)
                .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Araç sağlık durumu kontrolü
     */
    @GetMapping("/{vehicleId}/health")
    public ResponseEntity<VehicleHealthResponseDTO> getVehicleHealth(
            @PathVariable @NotNull Long vehicleId) {
        try {
            VehicleHealthResponseDTO health = smartVehicleService.getVehicleHealth(vehicleId);
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Araç performans istatistikleri
     */
    @GetMapping("/{vehicleId}/performance")
    public ResponseEntity<VehiclePerformanceDTO> getVehiclePerformance(
            @PathVariable @NotNull Long vehicleId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        try {
            VehiclePerformanceDTO performance = smartVehicleService.getVehiclePerformance(vehicleId, days);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * AI modeli güncelleme
     */
    @PutMapping("/{vehicleId}/ai-models")
    public ResponseEntity<String> updateAIModels(
            @PathVariable @NotNull Long vehicleId,
            @Valid @RequestBody AIModelUpdateDTO modelUpdate) {
        try {
            smartVehicleService.updateAIModels(vehicleId, modelUpdate);
            return ResponseEntity.ok("AI modelleri güncellendi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * V2X (Vehicle-to-Everything) iletişim durumu
     */
    @GetMapping("/{vehicleId}/v2x-status")
    public ResponseEntity<V2XStatusDTO> getV2XStatus(
            @PathVariable @NotNull Long vehicleId) {
        try {
            V2XStatusDTO status = smartVehicleService.getV2XStatus(vehicleId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Acil durum protokolü aktivasyonu
     */
    @PostMapping("/{vehicleId}/emergency")
    public ResponseEntity<String> activateEmergencyProtocol(
            @PathVariable @NotNull Long vehicleId,
            @Valid @RequestBody EmergencyProtocolDTO emergencyData) {
        try {
            smartVehicleService.activateEmergencyProtocol(vehicleId, emergencyData);
            return ResponseEntity.ok("Acil durum protokolü aktif edildi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Predictive maintenance (öngörülü bakım) analizi
     */
    @GetMapping("/{vehicleId}/predictive-maintenance")
    public ResponseEntity<PredictiveMaintenanceDTO> getPredictiveMaintenance(
            @PathVariable @NotNull Long vehicleId) {
        try {
            PredictiveMaintenanceDTO maintenance = smartVehicleService.getPredictiveMaintenance(vehicleId);
            return ResponseEntity.ok(maintenance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Eco-routing (çevreci rota) optimizasyonu
     */
    @PostMapping("/{vehicleId}/eco-route")
    public ResponseEntity<EcoRouteResponseDTO> calculateEcoRoute(
            @PathVariable @NotNull Long vehicleId,
            @Valid @RequestBody EcoRouteRequestDTO routeRequest) {
        try {
            EcoRouteResponseDTO route = smartVehicleService.calculateEcoRoute(vehicleId, routeRequest);
            return ResponseEntity.ok(route);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Fleet (filo) yönetimi - tüm araçların durumu
     */
    @GetMapping("/fleet-status")
    public ResponseEntity<FleetStatusDTO> getFleetStatus() {
        try {
            FleetStatusDTO fleetStatus = smartVehicleService.getFleetStatus();
            return ResponseEntity.ok(fleetStatus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Gelişmiş sürücü analizi (driver behavior)
     */
    @GetMapping("/{vehicleId}/driver-analysis")
    public ResponseEntity<DriverAnalysisDTO> getDriverAnalysis(
            @PathVariable @NotNull Long vehicleId,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        try {
            DriverAnalysisDTO analysis = smartVehicleService.getDriverAnalysis(vehicleId, days);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Yakıt/enerji optimizasyonu önerileri
     */
    @GetMapping("/{vehicleId}/energy-optimization")
    public ResponseEntity<EnergyOptimizationDTO> getEnergyOptimization(
            @PathVariable @NotNull Long vehicleId) {
        try {
            EnergyOptimizationDTO optimization = smartVehicleService.getEnergyOptimization(vehicleId);
            return ResponseEntity.ok(optimization);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Trafic pattern analysis (trafik deseni analizi)
     */
    @GetMapping("/traffic-analysis")
    public ResponseEntity<TrafficAnalysisDTO> getTrafficAnalysis(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "10.0") Double radiusKm) {
        try {
            TrafficAnalysisDTO analysis = smartVehicleService.getTrafficAnalysis(latitude, longitude, radiusKm);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Araç güvenlik durumu ve sertifika yönetimi
     */
    @GetMapping("/{vehicleId}/security-status")
    public ResponseEntity<VehicleSecurityStatusDTO> getSecurityStatus(
            @PathVariable @NotNull Long vehicleId) {
        try {
            VehicleSecurityStatusDTO security = smartVehicleService.getSecurityStatus(vehicleId);
            return ResponseEntity.ok(security);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * OTA (Over-The-Air) güncelleme durumu
     */
    @GetMapping("/{vehicleId}/ota-status")
    public ResponseEntity<OTAUpdateStatusDTO> getOTAStatus(
            @PathVariable @NotNull Long vehicleId) {
        try {
            OTAUpdateStatusDTO otaStatus = smartVehicleService.getOTAStatus(vehicleId);
            return ResponseEntity.ok(otaStatus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Araç detaylarını getir
     */
    @GetMapping("/{vehicleId}")
    public ResponseEntity<SmartVehicleResponseDTO> getVehicleById(
            @PathVariable @NotNull Long vehicleId) {
        try {
            SmartVehicle vehicle = smartVehicleService.findById(vehicleId);
            SmartVehicleResponseDTO response = convertToResponseDTO(vehicle);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Sürücüye ait tüm araçları getir
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<SmartVehicleResponseDTO>> getVehiclesByDriver(
            @PathVariable @NotNull Long driverId) {
        try {
            List<SmartVehicle> vehicles = smartVehicleService.findByDriverId(driverId);
            List<SmartVehicleResponseDTO> response = vehicles.stream()
                .map(this::convertToResponseDTO)
                .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Araç silme
     */
    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<String> deleteVehicle(@PathVariable @NotNull Long vehicleId) {
        try {
            smartVehicleService.deleteVehicle(vehicleId);
            return ResponseEntity.ok("Araç başarıyla silindi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Utility Methods
    private SmartVehicleResponseDTO convertToResponseDTO(SmartVehicle vehicle) {
        SmartVehicleResponseDTO dto = new SmartVehicleResponseDTO();
        dto.setId(vehicle.getId());
        dto.setVin(vehicle.getVin());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setVehicleType(vehicle.getVehicleType());
        dto.setStatus(vehicle.getStatus());
        dto.setAutonomyLevel(vehicle.getAutonomyLevel());
        dto.setDriverId(vehicle.getDriverId());
        dto.setDriverName(vehicle.getDriverName());
        dto.setCurrentLatitude(vehicle.getCurrentLatitude());
        dto.setCurrentLongitude(vehicle.getCurrentLongitude());
        dto.setCurrentSpeed(vehicle.getCurrentSpeed());
        dto.setBatteryLevel(vehicle.getBatteryLevel());
        dto.setFuelLevel(vehicle.getFuelLevel());
        dto.setCurrentPassengerCount(vehicle.getCurrentPassengerCount());
        dto.setMaxPassengerCapacity(vehicle.getMaxPassengerCapacity());
        dto.setTotalTrips(vehicle.getTotalTrips());
        dto.setAverageRating(vehicle.getAverageRating());
        dto.setV2vEnabled(vehicle.getV2vEnabled());
        dto.setV2iEnabled(vehicle.getV2iEnabled());
        dto.setV2pEnabled(vehicle.getV2pEnabled());
        dto.setConnectivityType(vehicle.getConnectivityType());
        dto.setEcoModeActive(vehicle.getEcoModeActive());
        dto.setPredictiveRoutingActive(vehicle.getPredictiveRoutingActive());
        dto.setCreatedAt(vehicle.getCreatedAt());
        dto.setUpdatedAt(vehicle.getUpdatedAt());
        dto.setLastActiveAt(vehicle.getLastActiveAt());
        return dto;
    }
}