package com.woltaxi.analytics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Sürücü Günlük Performans Entity
 * 
 * Her sürücü için günlük bazda detaylı performans metrikleri,
 * kar-zarar hesaplaması, çalışma saatleri ve kalite göstergeleri
 */
@Entity
@Table(name = "driver_daily_performance", indexes = {
    @Index(name = "idx_driver_performance_date", columnList = "performance_date"),
    @Index(name = "idx_driver_performance_driver", columnList = "driver_id"),
    @Index(name = "idx_driver_performance_profit", columnList = "net_profit DESC"),
    @Index(name = "idx_driver_performance_rating", columnList = "customer_rating_average DESC")
})
public class DriverDailyPerformance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Driver Info
    @Column(name = "driver_id", nullable = false)
    private Long driverId;
    
    @Column(name = "performance_date", nullable = false)
    @NotNull(message = "Performance date is required")
    private LocalDate performanceDate;
    
    // Working Hours
    @Column(name = "work_start_time")
    private LocalDateTime workStartTime;
    
    @Column(name = "work_end_time")
    private LocalDateTime workEndTime;
    
    @Column(name = "total_work_hours", precision = 4, scale = 2)
    @DecimalMin(value = "0.0", message = "Work hours must be positive")
    @DecimalMax(value = "24.0", message = "Work hours cannot exceed 24")
    private BigDecimal totalWorkHours = BigDecimal.ZERO;
    
    @Column(name = "active_driving_hours", precision = 4, scale = 2)
    @DecimalMin(value = "0.0", message = "Active driving hours must be positive")
    private BigDecimal activeDrivingHours = BigDecimal.ZERO;
    
    @Column(name = "idle_hours", precision = 4, scale = 2)
    @DecimalMin(value = "0.0", message = "Idle hours must be positive")
    private BigDecimal idleHours = BigDecimal.ZERO;
    
    @Column(name = "break_hours", precision = 4, scale = 2)
    @DecimalMin(value = "0.0", message = "Break hours must be positive")
    private BigDecimal breakHours = BigDecimal.ZERO;
    
    // Trip Statistics
    @Column(name = "total_trips")
    @Min(value = 0, message = "Total trips cannot be negative")
    private Integer totalTrips = 0;
    
    @Column(name = "completed_trips")
    @Min(value = 0, message = "Completed trips cannot be negative")
    private Integer completedTrips = 0;
    
    @Column(name = "cancelled_trips")
    @Min(value = 0, message = "Cancelled trips cannot be negative")
    private Integer cancelledTrips = 0;
    
    @Column(name = "customer_cancelled")
    @Min(value = 0, message = "Customer cancelled trips cannot be negative")
    private Integer customerCancelled = 0;
    
    @Column(name = "driver_cancelled")
    @Min(value = 0, message = "Driver cancelled trips cannot be negative")
    private Integer driverCancelled = 0;
    
    // Distance & Time
    @Column(name = "total_distance_km", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Total distance must be positive")
    private BigDecimal totalDistanceKm = BigDecimal.ZERO;
    
    @Column(name = "passenger_distance_km", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Passenger distance must be positive")
    private BigDecimal passengerDistanceKm = BigDecimal.ZERO;
    
    @Column(name = "empty_distance_km", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Empty distance must be positive")
    private BigDecimal emptyDistanceKm = BigDecimal.ZERO;
    
    @Column(name = "total_trip_time_minutes")
    @Min(value = 0, message = "Total trip time cannot be negative")
    private Integer totalTripTimeMinutes = 0;
    
    @Column(name = "average_trip_time_minutes", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Average trip time must be positive")
    private BigDecimal averageTripTimeMinutes = BigDecimal.ZERO;
    
    // Financial Performance
    @Column(name = "gross_earnings", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Gross earnings must be positive")
    private BigDecimal grossEarnings = BigDecimal.ZERO;
    
    @Column(name = "base_fare_earnings", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Base fare earnings must be positive")
    private BigDecimal baseFareEarnings = BigDecimal.ZERO;
    
    @Column(name = "distance_earnings", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Distance earnings must be positive")
    private BigDecimal distanceEarnings = BigDecimal.ZERO;
    
    @Column(name = "time_earnings", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Time earnings must be positive")
    private BigDecimal timeEarnings = BigDecimal.ZERO;
    
    @Column(name = "surge_earnings", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Surge earnings must be positive")
    private BigDecimal surgeEarnings = BigDecimal.ZERO;
    
    @Column(name = "tips_received", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Tips received must be positive")
    private BigDecimal tipsReceived = BigDecimal.ZERO;
    
    @Column(name = "bonuses_earned", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Bonuses earned must be positive")
    private BigDecimal bonusesEarned = BigDecimal.ZERO;
    
    // Costs & Expenses
    @Column(name = "fuel_cost", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Fuel cost must be positive")
    private BigDecimal fuelCost = BigDecimal.ZERO;
    
    @Column(name = "vehicle_maintenance_cost", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Maintenance cost must be positive")
    private BigDecimal vehicleMaintenanceCost = BigDecimal.ZERO;
    
    @Column(name = "platform_commission", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Platform commission must be positive")
    private BigDecimal platformCommission = BigDecimal.ZERO;
    
    @Column(name = "subscription_fee", precision = 6, scale = 2)
    @DecimalMin(value = "0.0", message = "Subscription fee must be positive")
    private BigDecimal subscriptionFee = BigDecimal.ZERO;
    
    @Column(name = "insurance_cost", precision = 6, scale = 2)
    @DecimalMin(value = "0.0", message = "Insurance cost must be positive")
    private BigDecimal insuranceCost = BigDecimal.ZERO;
    
    @Column(name = "parking_fees", precision = 6, scale = 2)
    @DecimalMin(value = "0.0", message = "Parking fees must be positive")
    private BigDecimal parkingFees = BigDecimal.ZERO;
    
    @Column(name = "toll_fees", precision = 6, scale = 2)
    @DecimalMin(value = "0.0", message = "Toll fees must be positive")
    private BigDecimal tollFees = BigDecimal.ZERO;
    
    @Column(name = "other_expenses", precision = 6, scale = 2)
    @DecimalMin(value = "0.0", message = "Other expenses must be positive")
    private BigDecimal otherExpenses = BigDecimal.ZERO;
    
    // Calculated Metrics (auto-calculated by trigger)
    @Column(name = "total_expenses", precision = 10, scale = 2)
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    
    @Column(name = "net_profit", precision = 10, scale = 2)
    private BigDecimal netProfit = BigDecimal.ZERO;
    
    @Column(name = "profit_margin", precision = 5, scale = 2)
    private BigDecimal profitMargin = BigDecimal.ZERO;
    
    @Column(name = "earnings_per_hour", precision = 8, scale = 2)
    private BigDecimal earningsPerHour = BigDecimal.ZERO;
    
    @Column(name = "earnings_per_km", precision = 6, scale = 4)
    private BigDecimal earningsPerKm = BigDecimal.ZERO;
    
    @Column(name = "earnings_per_trip", precision = 8, scale = 2)
    private BigDecimal earningsPerTrip = BigDecimal.ZERO;
    
    // Quality Metrics
    @Column(name = "customer_rating_average", precision = 3, scale = 2)
    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private BigDecimal customerRatingAverage = BigDecimal.ZERO;
    
    @Column(name = "total_ratings_received")
    @Min(value = 0, message = "Total ratings cannot be negative")
    private Integer totalRatingsReceived = 0;
    
    @Column(name = "five_star_ratings")
    @Min(value = 0, message = "Five star ratings cannot be negative")
    private Integer fiveStarRatings = 0;
    
    @Column(name = "four_star_ratings")
    @Min(value = 0, message = "Four star ratings cannot be negative")
    private Integer fourStarRatings = 0;
    
    @Column(name = "three_star_ratings")
    @Min(value = 0, message = "Three star ratings cannot be negative")
    private Integer threeStarRatings = 0;
    
    @Column(name = "two_star_ratings")
    @Min(value = 0, message = "Two star ratings cannot be negative")
    private Integer twoStarRatings = 0;
    
    @Column(name = "one_star_ratings")
    @Min(value = 0, message = "One star ratings cannot be negative")
    private Integer oneStarRatings = 0;
    
    // Efficiency Metrics (auto-calculated by trigger)
    @Column(name = "acceptance_rate", precision = 5, scale = 2)
    private BigDecimal acceptanceRate = BigDecimal.ZERO;
    
    @Column(name = "cancellation_rate", precision = 5, scale = 2)
    private BigDecimal cancellationRate = BigDecimal.ZERO;
    
    @Column(name = "completion_rate", precision = 5, scale = 2)
    private BigDecimal completionRate = BigDecimal.ZERO;
    
    @Column(name = "utilization_rate", precision = 5, scale = 2)
    private BigDecimal utilizationRate = BigDecimal.ZERO;
    
    // Location & Area Performance
    @Column(name = "primary_work_area", length = 100)
    private String primaryWorkArea;
    
    @Column(name = "areas_covered")
    private String[] areasCovered;
    
    @Column(name = "peak_hour_earnings", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Peak hour earnings must be positive")
    private BigDecimal peakHourEarnings = BigDecimal.ZERO;
    
    @Column(name = "off_peak_earnings", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Off peak earnings must be positive")
    private BigDecimal offPeakEarnings = BigDecimal.ZERO;
    
    // Weather & External Factors
    @Column(name = "weather_condition", length = 50)
    private String weatherCondition;
    
    @Column(name = "temperature_celsius")
    private Integer temperatureCelsius;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "traffic_level")
    private TrafficLevel trafficLevel = TrafficLevel.NORMAL;
    
    @Column(name = "special_events", columnDefinition = "TEXT")
    private String specialEvents;
    
    // Performance Goals & Targets
    @Column(name = "daily_earnings_target", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Daily earnings target must be positive")
    private BigDecimal dailyEarningsTarget;
    
    @Column(name = "daily_trips_target")
    @Min(value = 0, message = "Daily trips target cannot be negative")
    private Integer dailyTripsTarget;
    
    @Column(name = "target_achievement_rate", precision = 5, scale = 2)
    private BigDecimal targetAchievementRate = BigDecimal.ZERO;
    
    // Status & Notes
    @Enumerated(EnumType.STRING)
    @Column(name = "performance_status")
    private PerformanceStatus performanceStatus = PerformanceStatus.ACTIVE;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum TrafficLevel {
        LOW, NORMAL, HIGH, EXTREME
    }
    
    public enum PerformanceStatus {
        ACTIVE, SICK_LEAVE, VACATION, SUSPENDED, MAINTENANCE
    }
    
    // Constructors
    public DriverDailyPerformance() {}
    
    public DriverDailyPerformance(Long driverId, LocalDate performanceDate) {
        this.driverId = driverId;
        this.performanceDate = performanceDate;
    }
    
    // Business Methods
    
    /**
     * Günlük hedefler karşılandı mı?
     */
    public boolean areTargetsAchieved() {
        boolean earningsTargetMet = dailyEarningsTarget == null || 
                netProfit.compareTo(dailyEarningsTarget) >= 0;
        boolean tripsTargetMet = dailyTripsTarget == null || 
                completedTrips >= dailyTripsTarget;
        
        return earningsTargetMet && tripsTargetMet;
    }
    
    /**
     * Performans seviyesi belirleme
     */
    public String getPerformanceLevel() {
        if (customerRatingAverage.compareTo(BigDecimal.valueOf(4.8)) >= 0 && 
            completionRate.compareTo(BigDecimal.valueOf(95.0)) >= 0 &&
            cancellationRate.compareTo(BigDecimal.valueOf(5.0)) <= 0) {
            return "EXCELLENT";
        } else if (customerRatingAverage.compareTo(BigDecimal.valueOf(4.5)) >= 0 && 
                   completionRate.compareTo(BigDecimal.valueOf(90.0)) >= 0 &&
                   cancellationRate.compareTo(BigDecimal.valueOf(10.0)) <= 0) {
            return "GOOD";
        } else if (customerRatingAverage.compareTo(BigDecimal.valueOf(4.0)) >= 0 && 
                   completionRate.compareTo(BigDecimal.valueOf(80.0)) >= 0) {
            return "AVERAGE";
        } else {
            return "POOR";
        }
    }
    
    /**
     * Çalışma verimliliği hesaplama
     */
    public BigDecimal getWorkEfficiency() {
        if (totalWorkHours.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return activeDrivingHours.divide(totalWorkHours, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Günün en karlı saati
     */
    public String getMostProfitableHour() {
        // This would be calculated based on hourly breakdown data
        // For now, return peak hours if peak earnings > off-peak earnings
        if (peakHourEarnings.compareTo(offPeakEarnings) > 0) {
            return "PEAK_HOURS";
        } else {
            return "OFF_PEAK_HOURS";
        }
    }
    
    /**
     * Yakıt verimliliği (km/litre)
     */
    public BigDecimal getFuelEfficiency() {
        // Assuming average fuel price for calculation
        BigDecimal fuelPricePerLitre = BigDecimal.valueOf(30.0); // 30 TL per litre
        
        if (fuelCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal litresUsed = fuelCost.divide(fuelPricePerLitre, 4, BigDecimal.ROUND_HALF_UP);
        
        if (litresUsed.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalDistanceKm.divide(litresUsed, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public LocalDate getPerformanceDate() { return performanceDate; }
    public void setPerformanceDate(LocalDate performanceDate) { this.performanceDate = performanceDate; }
    
    public LocalDateTime getWorkStartTime() { return workStartTime; }
    public void setWorkStartTime(LocalDateTime workStartTime) { this.workStartTime = workStartTime; }
    
    public LocalDateTime getWorkEndTime() { return workEndTime; }
    public void setWorkEndTime(LocalDateTime workEndTime) { this.workEndTime = workEndTime; }
    
    public BigDecimal getTotalWorkHours() { return totalWorkHours; }
    public void setTotalWorkHours(BigDecimal totalWorkHours) { this.totalWorkHours = totalWorkHours; }
    
    public BigDecimal getActiveDrivingHours() { return activeDrivingHours; }
    public void setActiveDrivingHours(BigDecimal activeDrivingHours) { this.activeDrivingHours = activeDrivingHours; }
    
    public BigDecimal getIdleHours() { return idleHours; }
    public void setIdleHours(BigDecimal idleHours) { this.idleHours = idleHours; }
    
    public BigDecimal getBreakHours() { return breakHours; }
    public void setBreakHours(BigDecimal breakHours) { this.breakHours = breakHours; }
    
    public Integer getTotalTrips() { return totalTrips; }
    public void setTotalTrips(Integer totalTrips) { this.totalTrips = totalTrips; }
    
    public Integer getCompletedTrips() { return completedTrips; }
    public void setCompletedTrips(Integer completedTrips) { this.completedTrips = completedTrips; }
    
    public Integer getCancelledTrips() { return cancelledTrips; }
    public void setCancelledTrips(Integer cancelledTrips) { this.cancelledTrips = cancelledTrips; }
    
    public Integer getCustomerCancelled() { return customerCancelled; }
    public void setCustomerCancelled(Integer customerCancelled) { this.customerCancelled = customerCancelled; }
    
    public Integer getDriverCancelled() { return driverCancelled; }
    public void setDriverCancelled(Integer driverCancelled) { this.driverCancelled = driverCancelled; }
    
    public BigDecimal getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(BigDecimal totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    
    public BigDecimal getPassengerDistanceKm() { return passengerDistanceKm; }
    public void setPassengerDistanceKm(BigDecimal passengerDistanceKm) { this.passengerDistanceKm = passengerDistanceKm; }
    
    public BigDecimal getEmptyDistanceKm() { return emptyDistanceKm; }
    public void setEmptyDistanceKm(BigDecimal emptyDistanceKm) { this.emptyDistanceKm = emptyDistanceKm; }
    
    public Integer getTotalTripTimeMinutes() { return totalTripTimeMinutes; }
    public void setTotalTripTimeMinutes(Integer totalTripTimeMinutes) { this.totalTripTimeMinutes = totalTripTimeMinutes; }
    
    public BigDecimal getAverageTripTimeMinutes() { return averageTripTimeMinutes; }
    public void setAverageTripTimeMinutes(BigDecimal averageTripTimeMinutes) { this.averageTripTimeMinutes = averageTripTimeMinutes; }
    
    public BigDecimal getGrossEarnings() { return grossEarnings; }
    public void setGrossEarnings(BigDecimal grossEarnings) { this.grossEarnings = grossEarnings; }
    
    public BigDecimal getBaseFareEarnings() { return baseFareEarnings; }
    public void setBaseFareEarnings(BigDecimal baseFareEarnings) { this.baseFareEarnings = baseFareEarnings; }
    
    public BigDecimal getDistanceEarnings() { return distanceEarnings; }
    public void setDistanceEarnings(BigDecimal distanceEarnings) { this.distanceEarnings = distanceEarnings; }
    
    public BigDecimal getTimeEarnings() { return timeEarnings; }
    public void setTimeEarnings(BigDecimal timeEarnings) { this.timeEarnings = timeEarnings; }
    
    public BigDecimal getSurgeEarnings() { return surgeEarnings; }
    public void setSurgeEarnings(BigDecimal surgeEarnings) { this.surgeEarnings = surgeEarnings; }
    
    public BigDecimal getTipsReceived() { return tipsReceived; }
    public void setTipsReceived(BigDecimal tipsReceived) { this.tipsReceived = tipsReceived; }
    
    public BigDecimal getBonusesEarned() { return bonusesEarned; }
    public void setBonusesEarned(BigDecimal bonusesEarned) { this.bonusesEarned = bonusesEarned; }
    
    public BigDecimal getFuelCost() { return fuelCost; }
    public void setFuelCost(BigDecimal fuelCost) { this.fuelCost = fuelCost; }
    
    public BigDecimal getVehicleMaintenanceCost() { return vehicleMaintenanceCost; }
    public void setVehicleMaintenanceCost(BigDecimal vehicleMaintenanceCost) { this.vehicleMaintenanceCost = vehicleMaintenanceCost; }
    
    public BigDecimal getPlatformCommission() { return platformCommission; }
    public void setPlatformCommission(BigDecimal platformCommission) { this.platformCommission = platformCommission; }
    
    public BigDecimal getSubscriptionFee() { return subscriptionFee; }
    public void setSubscriptionFee(BigDecimal subscriptionFee) { this.subscriptionFee = subscriptionFee; }
    
    public BigDecimal getInsuranceCost() { return insuranceCost; }
    public void setInsuranceCost(BigDecimal insuranceCost) { this.insuranceCost = insuranceCost; }
    
    public BigDecimal getParkingFees() { return parkingFees; }
    public void setParkingFees(BigDecimal parkingFees) { this.parkingFees = parkingFees; }
    
    public BigDecimal getTollFees() { return tollFees; }
    public void setTollFees(BigDecimal tollFees) { this.tollFees = tollFees; }
    
    public BigDecimal getOtherExpenses() { return otherExpenses; }
    public void setOtherExpenses(BigDecimal otherExpenses) { this.otherExpenses = otherExpenses; }
    
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
    
    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }
    
    public BigDecimal getProfitMargin() { return profitMargin; }
    public void setProfitMargin(BigDecimal profitMargin) { this.profitMargin = profitMargin; }
    
    public BigDecimal getEarningsPerHour() { return earningsPerHour; }
    public void setEarningsPerHour(BigDecimal earningsPerHour) { this.earningsPerHour = earningsPerHour; }
    
    public BigDecimal getEarningsPerKm() { return earningsPerKm; }
    public void setEarningsPerKm(BigDecimal earningsPerKm) { this.earningsPerKm = earningsPerKm; }
    
    public BigDecimal getEarningsPerTrip() { return earningsPerTrip; }
    public void setEarningsPerTrip(BigDecimal earningsPerTrip) { this.earningsPerTrip = earningsPerTrip; }
    
    public BigDecimal getCustomerRatingAverage() { return customerRatingAverage; }
    public void setCustomerRatingAverage(BigDecimal customerRatingAverage) { this.customerRatingAverage = customerRatingAverage; }
    
    public Integer getTotalRatingsReceived() { return totalRatingsReceived; }
    public void setTotalRatingsReceived(Integer totalRatingsReceived) { this.totalRatingsReceived = totalRatingsReceived; }
    
    public Integer getFiveStarRatings() { return fiveStarRatings; }
    public void setFiveStarRatings(Integer fiveStarRatings) { this.fiveStarRatings = fiveStarRatings; }
    
    public Integer getFourStarRatings() { return fourStarRatings; }
    public void setFourStarRatings(Integer fourStarRatings) { this.fourStarRatings = fourStarRatings; }
    
    public Integer getThreeStarRatings() { return threeStarRatings; }
    public void setThreeStarRatings(Integer threeStarRatings) { this.threeStarRatings = threeStarRatings; }
    
    public Integer getTwoStarRatings() { return twoStarRatings; }
    public void setTwoStarRatings(Integer twoStarRatings) { this.twoStarRatings = twoStarRatings; }
    
    public Integer getOneStarRatings() { return oneStarRatings; }
    public void setOneStarRatings(Integer oneStarRatings) { this.oneStarRatings = oneStarRatings; }
    
    public BigDecimal getAcceptanceRate() { return acceptanceRate; }
    public void setAcceptanceRate(BigDecimal acceptanceRate) { this.acceptanceRate = acceptanceRate; }
    
    public BigDecimal getCancellationRate() { return cancellationRate; }
    public void setCancellationRate(BigDecimal cancellationRate) { this.cancellationRate = cancellationRate; }
    
    public BigDecimal getCompletionRate() { return completionRate; }
    public void setCompletionRate(BigDecimal completionRate) { this.completionRate = completionRate; }
    
    public BigDecimal getUtilizationRate() { return utilizationRate; }
    public void setUtilizationRate(BigDecimal utilizationRate) { this.utilizationRate = utilizationRate; }
    
    public String getPrimaryWorkArea() { return primaryWorkArea; }
    public void setPrimaryWorkArea(String primaryWorkArea) { this.primaryWorkArea = primaryWorkArea; }
    
    public String[] getAreasCovered() { return areasCovered; }
    public void setAreasCovered(String[] areasCovered) { this.areasCovered = areasCovered; }
    
    public BigDecimal getPeakHourEarnings() { return peakHourEarnings; }
    public void setPeakHourEarnings(BigDecimal peakHourEarnings) { this.peakHourEarnings = peakHourEarnings; }
    
    public BigDecimal getOffPeakEarnings() { return offPeakEarnings; }
    public void setOffPeakEarnings(BigDecimal offPeakEarnings) { this.offPeakEarnings = offPeakEarnings; }
    
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    
    public Integer getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(Integer temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }
    
    public TrafficLevel getTrafficLevel() { return trafficLevel; }
    public void setTrafficLevel(TrafficLevel trafficLevel) { this.trafficLevel = trafficLevel; }
    
    public String getSpecialEvents() { return specialEvents; }
    public void setSpecialEvents(String specialEvents) { this.specialEvents = specialEvents; }
    
    public BigDecimal getDailyEarningsTarget() { return dailyEarningsTarget; }
    public void setDailyEarningsTarget(BigDecimal dailyEarningsTarget) { this.dailyEarningsTarget = dailyEarningsTarget; }
    
    public Integer getDailyTripsTarget() { return dailyTripsTarget; }
    public void setDailyTripsTarget(Integer dailyTripsTarget) { this.dailyTripsTarget = dailyTripsTarget; }
    
    public BigDecimal getTargetAchievementRate() { return targetAchievementRate; }
    public void setTargetAchievementRate(BigDecimal targetAchievementRate) { this.targetAchievementRate = targetAchievementRate; }
    
    public PerformanceStatus getPerformanceStatus() { return performanceStatus; }
    public void setPerformanceStatus(PerformanceStatus performanceStatus) { this.performanceStatus = performanceStatus; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return String.format("DriverDailyPerformance{id=%d, driverId=%d, date=%s, netProfit=%.2f TL, trips=%d, rating=%.2f}",
                id, driverId, performanceDate, netProfit, completedTrips, customerRatingAverage);
    }
}