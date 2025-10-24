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
import java.util.List;

/**
 * Küresel Sürücü Sıralaması
 * Tüm sürücülerin küresel performans verilerini ve sıralamalarını saklar
 */
@Entity
@Table(name = "global_driver_rankings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"driver_id", "ranking_year", "ranking_month"}),
       indexes = {
           @Index(name = "idx_global_rankings_period", columnList = "ranking_year, ranking_month"),
           @Index(name = "idx_global_rankings_performance", columnList = "performance_score DESC"),
           @Index(name = "idx_global_rankings_global", columnList = "global_ranking"),
           @Index(name = "idx_global_rankings_country", columnList = "country_code, country_ranking")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalDriverRankings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Driver & Location Info
    @NotNull
    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @NotNull
    @Size(min = 2, max = 3)
    @Column(name = "country_code", nullable = false, length = 3)
    private String countryCode;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    // Ranking Period
    @NotNull
    @Min(2020)
    @Max(2100)
    @Column(name = "ranking_year", nullable = false)
    private Integer rankingYear;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(name = "ranking_month", nullable = false)
    private Integer rankingMonth;

    // Performance Metrics
    @Min(0)
    @Builder.Default
    @Column(name = "total_trips_completed")
    private Integer totalTripsCompleted = 0;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "total_distance_km", precision = 10, scale = 2)
    private BigDecimal totalDistanceKm = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "total_earnings_local", precision = 12, scale = 2)
    private BigDecimal totalEarningsLocal = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "total_earnings_usd", precision = 12, scale = 2)
    private BigDecimal totalEarningsUsd = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "average_customer_rating", precision = 3, scale = 2)
    private BigDecimal averageCustomerRating = BigDecimal.ZERO;

    @Min(0)
    @Builder.Default
    @Column(name = "total_customer_ratings")
    private Integer totalCustomerRatings = 0;

    // Efficiency Scores
    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "trips_per_hour", precision = 5, scale = 2)
    private BigDecimal tripsPerHour = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "earnings_per_hour_usd", precision = 8, scale = 2)
    private BigDecimal earningsPerHourUsd = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "fuel_efficiency_score", precision = 5, scale = 2)
    private BigDecimal fuelEfficiencyScore = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "route_optimization_score", precision = 5, scale = 2)
    private BigDecimal routeOptimizationScore = BigDecimal.ZERO;

    // Quality Scores
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "punctuality_score", precision = 5, scale = 2)
    private BigDecimal punctualityScore = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "customer_satisfaction_score", precision = 5, scale = 2)
    private BigDecimal customerSatisfactionScore = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "vehicle_cleanliness_score", precision = 3, scale = 2)
    private BigDecimal vehicleCleanlinessScore = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "professionalism_score", precision = 3, scale = 2)
    private BigDecimal professionalismScore = BigDecimal.ZERO;

    // Reliability Metrics
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "completion_rate", precision = 5, scale = 2)
    private BigDecimal completionRate = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "cancellation_rate", precision = 5, scale = 2)
    private BigDecimal cancellationRate = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "availability_hours", precision = 6, scale = 2)
    private BigDecimal availabilityHours = BigDecimal.ZERO;

    // Innovation & Technology
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "app_usage_score", precision = 5, scale = 2)
    private BigDecimal appUsageScore = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "navigation_accuracy", precision = 5, scale = 2)
    private BigDecimal navigationAccuracy = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "communication_score", precision = 3, scale = 2)
    private BigDecimal communicationScore = BigDecimal.ZERO;

    // Overall Performance Score (Weighted Algorithm)
    @DecimalMin("0.0000")
    @DecimalMax("100.0000")
    @Builder.Default
    @Column(name = "performance_score", precision = 8, scale = 4)
    private BigDecimal performanceScore = BigDecimal.ZERO;

    // Rankings
    @Column(name = "global_ranking")
    private Integer globalRanking;

    @Column(name = "country_ranking")
    private Integer countryRanking;

    @Column(name = "city_ranking")
    private Integer cityRanking;

    @Column(name = "category_ranking")
    private Integer categoryRanking;

    // Achievement Level
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "achievement_tier", length = 20)
    private AchievementTier achievementTier = AchievementTier.STANDARD;

    @Min(0)
    @Builder.Default
    @Column(name = "points_earned")
    private Integer pointsEarned = 0;

    @ElementCollection
    @CollectionTable(name = "driver_badges", joinColumns = @JoinColumn(name = "ranking_id"))
    @Column(name = "badge_name")
    private List<String> badgesEarned;

    // Improvement Metrics
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "performance_trend", length = 20)
    private PerformanceTrend performanceTrend = PerformanceTrend.STABLE;

    @Builder.Default
    @Column(name = "month_over_month_improvement", precision = 5, scale = 2)
    private BigDecimal monthOverMonthImprovement = BigDecimal.ZERO;

    @ElementCollection
    @CollectionTable(name = "driver_improvement_areas", joinColumns = @JoinColumn(name = "ranking_id"))
    @Column(name = "improvement_area")
    private List<String> areasForImprovement;

    // Eligibility for Rewards
    @Builder.Default
    @Column(name = "eligible_for_monthly_reward")
    private Boolean eligibleForMonthlyReward = false;

    @Builder.Default
    @Column(name = "eligible_for_quarterly_reward")
    private Boolean eligibleForQuarterlyReward = false;

    @Builder.Default
    @Column(name = "eligible_for_annual_reward")
    private Boolean eligibleForAnnualReward = false;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum AchievementTier {
        DIAMOND, PLATINUM, GOLD, SILVER, BRONZE, STANDARD
    }

    public enum PerformanceTrend {
        IMPROVING, STABLE, DECLINING
    }

    // Helper methods
    public boolean isTopPerformer() {
        return achievementTier == AchievementTier.DIAMOND || 
               achievementTier == AchievementTier.PLATINUM || 
               achievementTier == AchievementTier.GOLD;
    }

    public boolean isEligibleForAnyReward() {
        return eligibleForMonthlyReward || eligibleForQuarterlyReward || eligibleForAnnualReward;
    }

    public String getAchievementDescription() {
        return switch (achievementTier) {
            case DIAMOND -> "Elite performer - Top 1% globally";
            case PLATINUM -> "Exceptional performer - Top 5% globally";
            case GOLD -> "Outstanding performer - Top 15% globally";
            case SILVER -> "Strong performer - Top 30% globally";
            case BRONZE -> "Good performer - Top 50% globally";
            case STANDARD -> "Standard performer - Room for improvement";
        };
    }

    public BigDecimal calculateEfficiencyRatio() {
        if (availabilityHours.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalEarningsUsd.divide(availabilityHours, 2, BigDecimal.ROUND_HALF_UP);
    }

    public String getPerformanceTrendDescription() {
        return switch (performanceTrend) {
            case IMPROVING -> "Performance is consistently improving";
            case STABLE -> "Performance is stable with minor fluctuations";
            case DECLINING -> "Performance needs attention and improvement";
        };
    }
}