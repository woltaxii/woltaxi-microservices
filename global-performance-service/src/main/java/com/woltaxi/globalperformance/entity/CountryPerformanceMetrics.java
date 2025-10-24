package com.woltaxi.globalperformance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ülke Performans Metrikleri
 * Her ülkenin aylık performans verilerini saklar
 */
@Entity
@Table(name = "country_performance_metrics",
       uniqueConstraints = @UniqueConstraint(columnNames = {"country_code", "year", "month"}),
       indexes = {
           @Index(name = "idx_country_performance_period", columnList = "year, month"),
           @Index(name = "idx_country_performance_ranking", columnList = "global_ranking"),
           @Index(name = "idx_country_performance_revenue", columnList = "total_gross_revenue_usd DESC")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryPerformanceMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Country & Period Info
    @NotNull
    @Size(min = 2, max = 3)
    @Column(name = "country_code", nullable = false, length = 3)
    private String countryCode;

    @NotNull
    @Min(2020)
    @Max(2100)
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(name = "month", nullable = false)
    private Integer month;

    // Market Capacity & Coverage
    @Min(0)
    @Builder.Default
    @Column(name = "total_registered_drivers")
    private Integer totalRegisteredDrivers = 0;

    @Min(0)
    @Builder.Default
    @Column(name = "active_drivers")
    private Integer activeDrivers = 0;

    @Min(0)
    @Builder.Default
    @Column(name = "new_driver_registrations")
    private Integer newDriverRegistrations = 0;

    @Min(0)
    @Builder.Default
    @Column(name = "driver_churn_count")
    private Integer driverChurnCount = 0;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "driver_retention_rate", precision = 5, scale = 2)
    private BigDecimal driverRetentionRate = BigDecimal.ZERO;

    // Service Coverage
    @Min(0)
    @Builder.Default
    @Column(name = "total_cities_covered")
    private Integer totalCitiesCovered = 0;

    @Min(0)
    @Builder.Default
    @Column(name = "total_service_areas")
    private Integer totalServiceAreas = 0;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "population_coverage_percentage", precision = 5, scale = 2)
    private BigDecimal populationCoveragePercentage = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "market_penetration_rate", precision = 5, scale = 2)
    private BigDecimal marketPenetrationRate = BigDecimal.ZERO;

    // Trip & Demand Analytics
    @Min(0)
    @Builder.Default
    @Column(name = "total_trips_completed")
    private Integer totalTripsCompleted = 0;

    @Min(0)
    @Builder.Default
    @Column(name = "total_trips_requested")
    private Integer totalTripsRequested = 0;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "trip_fulfillment_rate", precision = 5, scale = 2)
    private BigDecimal tripFulfillmentRate = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "average_wait_time_minutes", precision = 5, scale = 2)
    private BigDecimal averageWaitTimeMinutes = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "peak_hour_efficiency", precision = 5, scale = 2)
    private BigDecimal peakHourEfficiency = BigDecimal.ZERO;

    // Financial Performance
    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "total_gross_revenue", precision = 15, scale = 2)
    private BigDecimal totalGrossRevenue = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "total_gross_revenue_usd", precision = 15, scale = 2)
    private BigDecimal totalGrossRevenueUsd = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "platform_commission_earned", precision = 12, scale = 2)
    private BigDecimal platformCommissionEarned = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "driver_total_earnings", precision = 15, scale = 2)
    private BigDecimal driverTotalEarnings = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "average_trip_value", precision = 8, scale = 2)
    private BigDecimal averageTripValue = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "revenue_per_driver", precision = 10, scale = 2)
    private BigDecimal revenuePerDriver = BigDecimal.ZERO;

    // Operational Efficiency
    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "average_trips_per_driver", precision = 6, scale = 2)
    private BigDecimal averageTripsPerDriver = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "average_hours_per_driver", precision = 6, scale = 2)
    private BigDecimal averageHoursPerDriver = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "driver_utilization_rate", precision = 5, scale = 2)
    private BigDecimal driverUtilizationRate = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "fuel_efficiency_average", precision = 5, scale = 2)
    private BigDecimal fuelEfficiencyAverage = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "operational_cost_ratio", precision = 5, scale = 2)
    private BigDecimal operationalCostRatio = BigDecimal.ZERO;

    // Customer Satisfaction
    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "average_customer_rating", precision = 3, scale = 2)
    private BigDecimal averageCustomerRating = BigDecimal.ZERO;

    @Min(0)
    @Builder.Default
    @Column(name = "total_customer_ratings")
    private Integer totalCustomerRatings = 0;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "customer_complaint_rate", precision = 5, scale = 2)
    private BigDecimal customerComplaintRate = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "customer_retention_rate", precision = 5, scale = 2)
    private BigDecimal customerRetentionRate = BigDecimal.ZERO;

    @Min(-100)
    @Max(100)
    @Builder.Default
    @Column(name = "net_promoter_score")
    private Integer netPromoterScore = 0;

    // Quality Metrics
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "on_time_arrival_rate", precision = 5, scale = 2)
    private BigDecimal onTimeArrivalRate = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "trip_cancellation_rate", precision = 5, scale = 2)
    private BigDecimal tripCancellationRate = BigDecimal.ZERO;

    @DecimalMin("0.0000")
    @Builder.Default
    @Column(name = "safety_incident_rate", precision = 5, scale = 4)
    private BigDecimal safetyIncidentRate = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "vehicle_quality_score", precision = 3, scale = 2)
    private BigDecimal vehicleQualityScore = BigDecimal.ZERO;

    // Growth & Trends
    @Builder.Default
    @Column(name = "month_over_month_growth", precision = 5, scale = 2)
    private BigDecimal monthOverMonthGrowth = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "year_over_year_growth", precision = 5, scale = 2)
    private BigDecimal yearOverYearGrowth = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "seasonal_demand_factor", precision = 4, scale = 2)
    private BigDecimal seasonalDemandFactor = BigDecimal.ONE;

    // Competitive Analysis
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "market_share_percentage", precision = 5, scale = 2)
    private BigDecimal marketSharePercentage = BigDecimal.ZERO;

    @Min(0)
    @Builder.Default
    @Column(name = "competitor_count")
    private Integer competitorCount = 0;

    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "competitive_advantage_score", precision = 3, scale = 2)
    private BigDecimal competitiveAdvantageScore = BigDecimal.ZERO;

    // Technology & Innovation
    @Min(0)
    @Builder.Default
    @Column(name = "app_download_count")
    private Integer appDownloadCount = 0;

    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "app_rating", precision = 3, scale = 2)
    private BigDecimal appRating = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "technology_adoption_rate", precision = 5, scale = 2)
    private BigDecimal technologyAdoptionRate = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "mobile_payment_usage_rate", precision = 5, scale = 2)
    private BigDecimal mobilePaymentUsageRate = BigDecimal.ZERO;

    // Regulatory & Compliance
    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "regulatory_compliance_score", precision = 3, scale = 2)
    private BigDecimal regulatoryComplianceScore = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "license_compliance_rate", precision = 5, scale = 2)
    private BigDecimal licenseComplianceRate = new BigDecimal("100.00");

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "tax_compliance_rate", precision = 5, scale = 2)
    private BigDecimal taxComplianceRate = new BigDecimal("100.00");

    // Environmental Impact
    @DecimalMin("0.000")
    @Builder.Default
    @Column(name = "average_co2_emissions_per_trip", precision = 6, scale = 3)
    private BigDecimal averageCo2EmissionsPerTrip = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "electric_vehicle_percentage", precision = 5, scale = 2)
    private BigDecimal electricVehiclePercentage = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "environmental_score", precision = 3, scale = 2)
    private BigDecimal environmentalScore = BigDecimal.ZERO;

    // Performance Ranking
    @Column(name = "global_ranking")
    private Integer globalRanking;

    @Column(name = "regional_ranking")
    private Integer regionalRanking;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "performance_tier", length = 20)
    private PerformanceTier performanceTier = PerformanceTier.DEVELOPING;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum PerformanceTier {
        PLATINUM, GOLD, SILVER, BRONZE, DEVELOPING
    }

    // Helper methods
    public BigDecimal calculateCompositeScore() {
        BigDecimal ratingScore = averageCustomerRating.divide(new BigDecimal("5"), 4, BigDecimal.ROUND_HALF_UP)
                                                     .multiply(new BigDecimal("100"));
        BigDecimal revenueScore = revenuePerDriver.divide(new BigDecimal("5000"), 4, BigDecimal.ROUND_HALF_UP)
                                                 .multiply(new BigDecimal("100"));
        
        return ratingScore.multiply(new BigDecimal("0.25"))
                .add(tripFulfillmentRate.multiply(new BigDecimal("0.25")))
                .add(driverUtilizationRate.multiply(new BigDecimal("0.25")))
                .add(revenueScore.multiply(new BigDecimal("0.25")));
    }

    public boolean isTopPerformer() {
        return performanceTier == PerformanceTier.PLATINUM || performanceTier == PerformanceTier.GOLD;
    }

    public String getPerformanceDescription() {
        return switch (performanceTier) {
            case PLATINUM -> "Exceptional performance across all metrics";
            case GOLD -> "Outstanding performance with minor areas for improvement";
            case SILVER -> "Good performance with some optimization opportunities";
            case BRONZE -> "Average performance requiring focused improvements";
            case DEVELOPING -> "Emerging market with significant growth potential";
        };
    }
}