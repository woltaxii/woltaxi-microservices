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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ödül Programları
 * Küresel ve bölgesel ödül programlarının yönetimi
 */
@Entity
@Table(name = "reward_programs",
       indexes = {
           @Index(name = "idx_reward_programs_type", columnList = "program_type"),
           @Index(name = "idx_reward_programs_scope", columnList = "program_scope"),
           @Index(name = "idx_reward_programs_active", columnList = "is_active"),
           @Index(name = "idx_reward_programs_dates", columnList = "start_date, end_date")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Program Details
    @NotNull
    @Size(min = 5, max = 200)
    @Column(name = "program_name", nullable = false, length = 200)
    private String programName;

    @NotNull
    @Size(min = 3, max = 50)
    @Column(name = "program_code", nullable = false, length = 50, unique = true)
    private String programCode;

    @Size(max = 1000)
    @Column(name = "program_description", columnDefinition = "TEXT")
    private String programDescription;

    // Program Type & Scope
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "program_type", nullable = false, length = 30)
    private ProgramType programType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "program_scope", nullable = false, length = 20)
    private ProgramScope programScope;

    // Eligibility Criteria
    @Min(0)
    @Builder.Default
    @Column(name = "min_trips_required")
    private Integer minTripsRequired = 0;

    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Builder.Default
    @Column(name = "min_rating_required", precision = 3, scale = 2)
    private BigDecimal minRatingRequired = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "min_earnings_required", precision = 10, scale = 2)
    private BigDecimal minEarningsRequired = BigDecimal.ZERO;

    @Min(0)
    @Builder.Default
    @Column(name = "min_hours_worked")
    private Integer minHoursWorked = 0;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "max_cancellation_rate", precision = 5, scale = 2)
    private BigDecimal maxCancellationRate = new BigDecimal("100.00");

    @ElementCollection
    @CollectionTable(name = "program_required_badges", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "badge_name")
    private List<String> requiredBadges;

    // Reward Structure
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    private RewardType rewardType;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "reward_value", precision = 10, scale = 2)
    private BigDecimal rewardValue = BigDecimal.ZERO;

    @Size(max = 3)
    @Builder.Default
    @Column(name = "currency_code", length = 3)
    private String currencyCode = "USD";

    // Winner Selection
    @Min(1)
    @Builder.Default
    @Column(name = "max_winners_global")
    private Integer maxWinnersGlobal = 1;

    @Min(0)
    @Builder.Default
    @Column(name = "max_winners_per_country")
    private Integer maxWinnersPerCountry = 1;

    @Min(0)
    @Builder.Default
    @Column(name = "max_winners_per_city")
    private Integer maxWinnersPerCity = 1;

    @Size(max = 50)
    @Builder.Default
    @Column(name = "selection_algorithm", length = 50)
    private String selectionAlgorithm = "TOP_PERFORMER";

    // Program Period
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "announcement_date")
    private LocalDate announcementDate;

    // Status
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Size(max = 50)
    @Column(name = "recurrence_pattern", length = 50)
    private String recurrencePattern;

    // Metrics & Analytics
    @Min(0)
    @Builder.Default
    @Column(name = "total_participants")
    private Integer totalParticipants = 0;

    @Min(0)
    @Builder.Default
    @Column(name = "total_winners")
    private Integer totalWinners = 0;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "total_rewards_distributed", precision = 12, scale = 2)
    private BigDecimal totalRewardsDistributed = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Builder.Default
    @Column(name = "engagement_score", precision = 5, scale = 2)
    private BigDecimal engagementScore = BigDecimal.ZERO;

    // Display & Marketing
    @Size(max = 100)
    @Column(name = "program_icon", length = 100)
    private String programIcon;

    @Size(max = 7)
    @Column(name = "program_color", length = 7)
    private String programColor;

    @Size(max = 500)
    @Column(name = "marketing_message", columnDefinition = "TEXT")
    private String marketingMessage;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum ProgramType {
        MONTHLY, QUARTERLY, ANNUAL, SPECIAL_EVENT, MILESTONE
    }

    public enum ProgramScope {
        GLOBAL, COUNTRY, CITY, CATEGORY
    }

    public enum RewardType {
        CASH_BONUS, POINTS, BADGE, SUBSCRIPTION_DISCOUNT, GIFT_CARD, TROPHY, RECOGNITION
    }

    // Helper methods
    public boolean isCurrentlyActive() {
        LocalDate now = LocalDate.now();
        return isActive && !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    public boolean isEligible(BigDecimal driverRating, Integer trips, BigDecimal earnings, 
                             Integer hoursWorked, BigDecimal cancellationRate) {
        return driverRating.compareTo(minRatingRequired) >= 0 &&
               trips >= minTripsRequired &&
               earnings.compareTo(minEarningsRequired) >= 0 &&
               hoursWorked >= minHoursWorked &&
               cancellationRate.compareTo(maxCancellationRate) <= 0;
    }

    public String getProgramTypeDescription() {
        return switch (programType) {
            case MONTHLY -> "Monthly recognition program";
            case QUARTERLY -> "Quarterly excellence award";
            case ANNUAL -> "Annual achievement program";
            case SPECIAL_EVENT -> "Special event celebration";
            case MILESTONE -> "Milestone achievement program";
        };
    }

    public String getScopeDescription() {
        return switch (programScope) {
            case GLOBAL -> "Global competition across all countries";
            case COUNTRY -> "Country-level competition";
            case CITY -> "City-level competition";
            case CATEGORY -> "Category-specific competition";
        };
    }

    public BigDecimal calculateParticipationRate() {
        if (totalParticipants == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(totalWinners)
                         .divide(BigDecimal.valueOf(totalParticipants), 4, BigDecimal.ROUND_HALF_UP)
                         .multiply(new BigDecimal("100"));
    }

    public boolean requiresBadges() {
        return requiredBadges != null && !requiredBadges.isEmpty();
    }

    public int getDurationInDays() {
        return (int) (endDate.toEpochDay() - startDate.toEpochDay());
    }
}