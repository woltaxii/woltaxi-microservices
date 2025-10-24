package com.woltaxi.smartvehicle.repository;

import com.woltaxi.smartvehicle.entity.SmartVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Akıllı Araç Repository - Gelişmiş sorgular ve analytics
 */
@Repository
public interface SmartVehicleRepository extends JpaRepository<SmartVehicle, Long> {
    
    // Temel Sorgular
    boolean existsByVin(String vin);
    boolean existsByLicensePlate(String licensePlate);
    List<SmartVehicle> findByDriverId(Long driverId);
    List<SmartVehicle> findByStatus(SmartVehicle.VehicleStatus status);
    List<SmartVehicle> findByAutonomyLevel(SmartVehicle.AutonomyLevel autonomyLevel);
    
    // Müsait Araçlar
    @Query("SELECT v FROM SmartVehicle v WHERE v.status = 'ONLINE' AND " +
           "(:minAutonomyLevel IS NULL OR v.autonomyLevel >= :minAutonomyLevel) AND " +
           "v.currentPassengerCount < v.maxPassengerCapacity AND " +
           "v.batteryLevel > 20")
    List<SmartVehicle> findAvailableVehicles(@Param("minAutonomyLevel") SmartVehicle.AutonomyLevel minAutonomyLevel);
    
    // Belirli Yarıçaptaki Müsait Araçlar (Haversine formülü)
    @Query(value = "SELECT * FROM smart_vehicles v WHERE v.status = 'ONLINE' AND " +
           "(:minAutonomyLevel IS NULL OR v.autonomy_level >= CAST(:#{#minAutonomyLevel?.ordinal()} AS INTEGER)) AND " +
           "v.current_passenger_count < v.max_passenger_capacity AND " +
           "v.battery_level > 20 AND " +
           "v.current_latitude IS NOT NULL AND v.current_longitude IS NOT NULL AND " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(v.current_latitude)) * " +
           "cos(radians(v.current_longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(v.current_latitude)))) <= :radiusKm " +
           "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(v.current_latitude)) * " +
           "cos(radians(v.current_longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(v.current_latitude))))",
           nativeQuery = true)
    List<SmartVehicle> findAvailableVehiclesInRadius(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusKm") Double radiusKm,
        @Param("minAutonomyLevel") SmartVehicle.AutonomyLevel minAutonomyLevel
    );
    
    // Bakım Gereken Araçlar
    @Query("SELECT v FROM SmartVehicle v WHERE v.nextMaintenanceDate <= :date OR " +
           "v.batteryLevel < 15 OR v.engineTemperature > 95")
    List<SmartVehicle> findVehiclesNeedingMaintenance(@Param("date") LocalDateTime date);
    
    // En Yakın Otonom Araç
    @Query(value = "SELECT * FROM smart_vehicles v WHERE v.status = 'ONLINE' AND " +
           "v.autonomy_level >= 3 AND " + // L3 ve üzeri
           "v.current_latitude IS NOT NULL AND v.current_longitude IS NOT NULL AND " +
           "v.current_passenger_count < v.max_passenger_capacity " +
           "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(v.current_latitude)) * " +
           "cos(radians(v.current_longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(v.current_latitude)))) " +
           "LIMIT 1",
           nativeQuery = true)
    Optional<SmartVehicle> findNearestAutonomousVehicle(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude
    );
    
    // Yüksek Performanslı Araçlar
    @Query("SELECT v FROM SmartVehicle v WHERE v.averageRating >= :minRating AND " +
           "v.totalTrips >= :minTrips ORDER BY v.averageRating DESC")
    List<SmartVehicle> findHighPerformanceVehicles(
        @Param("minRating") Double minRating,
        @Param("minTrips") Integer minTrips
    );
    
    // Aktif Otonom Araçlar
    @Query("SELECT v FROM SmartVehicle v WHERE v.status = 'AUTONOMOUS'")
    List<SmartVehicle> findActiveAutonomousVehicles();
    
    // Belirli Marka/Model Araçlar
    List<SmartVehicle> findByMakeAndModel(String make, String model);
    List<SmartVehicle> findByMake(String make);
    List<SmartVehicle> findByYearGreaterThan(Integer year);
    
    // V2X Etkin Araçlar
    @Query("SELECT v FROM SmartVehicle v WHERE v.v2vEnabled = true AND " +
           "v.v2iEnabled = true AND v.status = 'ONLINE'")
    List<SmartVehicle> findV2XEnabledVehicles();
    
    // Enerji Durumu Analizi
    @Query("SELECT v FROM SmartVehicle v WHERE v.batteryLevel < :batteryThreshold")
    List<SmartVehicle> findLowBatteryVehicles(@Param("batteryThreshold") Double batteryThreshold);
    
    @Query("SELECT v FROM SmartVehicle v WHERE v.fuelLevel < :fuelThreshold")
    List<SmartVehicle> findLowFuelVehicles(@Param("fuelThreshold") Double fuelThreshold);
    
    // İstatistiksel Sorgular
    @Query("SELECT COUNT(v) FROM SmartVehicle v WHERE v.status = :status")
    Long countByStatus(@Param("status") SmartVehicle.VehicleStatus status);
    
    @Query("SELECT COUNT(v) FROM SmartVehicle v WHERE v.autonomyLevel = :level")
    Long countByAutonomyLevel(@Param("level") SmartVehicle.AutonomyLevel level);
    
    @Query("SELECT AVG(v.averageRating) FROM SmartVehicle v WHERE v.totalTrips > 0")
    Double getAverageFleetRating();
    
    @Query("SELECT AVG(v.batteryLevel) FROM SmartVehicle v WHERE v.batteryLevel IS NOT NULL")
    Double getAverageFleetBatteryLevel();
    
    @Query("SELECT SUM(v.totalDistanceDriven) FROM SmartVehicle v WHERE v.totalDistanceDriven IS NOT NULL")
    Double getTotalFleetDistance();
    
    // Son Aktivite Bazlı Sorgular
    List<SmartVehicle> findByLastActiveAtBefore(LocalDateTime dateTime);
    List<SmartVehicle> findByLastActiveAtAfter(LocalDateTime dateTime);
    
    // Çoklu Kriter Araması
    @Query("SELECT v FROM SmartVehicle v WHERE " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:autonomyLevel IS NULL OR v.autonomyLevel = :autonomyLevel) AND " +
           "(:make IS NULL OR LOWER(v.make) LIKE LOWER(CONCAT('%', :make, '%'))) AND " +
           "(:minYear IS NULL OR v.year >= :minYear) AND " +
           "(:maxYear IS NULL OR v.year <= :maxYear) AND " +
           "(:minRating IS NULL OR v.averageRating >= :minRating)")
    List<SmartVehicle> findVehiclesByCriteria(
        @Param("status") SmartVehicle.VehicleStatus status,
        @Param("autonomyLevel") SmartVehicle.AutonomyLevel autonomyLevel,
        @Param("make") String make,
        @Param("minYear") Integer minYear,
        @Param("maxYear") Integer maxYear,
        @Param("minRating") Double minRating
    );
    
    // Filo Optimizasyonu için Sorgular
    @Query("SELECT v FROM SmartVehicle v WHERE v.status = 'ONLINE' AND " +
           "v.ecoModeActive = true ORDER BY v.averageFuelConsumption ASC")
    List<SmartVehicle> findEcoFriendlyVehicles();
    
    @Query("SELECT v FROM SmartVehicle v WHERE v.predictiveRoutingActive = true AND " +
           "v.status IN ('ONLINE', 'IN_RIDE') ORDER BY v.averageRating DESC")
    List<SmartVehicle> findSmartRoutingVehicles();
    
    // Güvenlik ve İzleme
    @Query("SELECT v FROM SmartVehicle v WHERE v.intrusionDetectionActive = true AND " +
           "v.status != 'OFFLINE'")
    List<SmartVehicle> findSecurityMonitoredVehicles();
    
    // Custom Native Queries for Complex Analytics
    @Query(value = "SELECT make, COUNT(*) as count, AVG(average_rating) as avg_rating " +
           "FROM smart_vehicles GROUP BY make ORDER BY count DESC",
           nativeQuery = true)
    List<Object[]> getVehicleStatsByMake();
    
    @Query(value = "SELECT autonomy_level, COUNT(*) as count, " +
           "AVG(total_distance_driven) as avg_distance " +
           "FROM smart_vehicles GROUP BY autonomy_level",
           nativeQuery = true)
    List<Object[]> getAutonomyLevelStats();
    
    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as new_vehicles " +
           "FROM smart_vehicles WHERE created_at >= :startDate " +
           "GROUP BY DATE(created_at) ORDER BY date",
           nativeQuery = true)
    List<Object[]> getNewVehiclesByDate(@Param("startDate") LocalDateTime startDate);
}