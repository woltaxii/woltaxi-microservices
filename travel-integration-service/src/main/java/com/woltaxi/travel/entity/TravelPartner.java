package com.woltaxi.travel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Travel Partner Entity
 * 
 * Represents travel service providers including airlines, bus companies,
 * hotels, and car rental agencies with their commission structures
 * and API integration details.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "travel_partners", indexes = {
    @Index(name = "idx_partner_code", columnList = "partner_code", unique = true),
    @Index(name = "idx_partner_type", columnList = "partner_type"),
    @Index(name = "idx_partner_status", columnList = "status"),
    @Index(name = "idx_partner_country", columnList = "country"),
    @Index(name = "idx_partner_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"apiCredentials", "contractDetails"})
public class TravelPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "partner_code", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Partner code is required")
    @Size(max = 20, message = "Partner code must not exceed 20 characters")
    private String partnerCode;

    @Column(name = "partner_name", nullable = false, length = 200)
    @NotBlank(message = "Partner name is required")
    @Size(max = 200, message = "Partner name must not exceed 200 characters")
    private String partnerName;

    @Column(name = "display_name", nullable = false, length = 100)
    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_type", nullable = false, length = 20)
    @NotNull(message = "Partner type is required")
    private PartnerType partnerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Partner status is required")
    @Builder.Default
    private PartnerStatus status = PartnerStatus.ACTIVE;

    @Column(name = "description", length = 1000)
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Column(name = "website_url", length = 255)
    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    @Pattern(regexp = "^(https?://).*", message = "Website URL must be a valid HTTP/HTTPS URL")
    private String websiteUrl;

    @Column(name = "logo_url", length = 500)
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Column(name = "country", nullable = false, length = 3)
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 3, message = "Country must be 2-3 characters")
    private String country;

    @Column(name = "base_currency", nullable = false, length = 3)
    @NotBlank(message = "Base currency is required")
    @Size(min = 3, max = 3, message = "Base currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Base currency must be a valid ISO 4217 code")
    private String baseCurrency;

    @Column(name = "supported_currencies", length = 500)
    @Size(max = 500, message = "Supported currencies must not exceed 500 characters")
    private String supportedCurrencies;

    // API Integration Details
    @Column(name = "api_base_url", length = 255)
    @Size(max = 255, message = "API base URL must not exceed 255 characters")
    private String apiBaseUrl;

    @Column(name = "api_version", length = 20)
    @Size(max = 20, message = "API version must not exceed 20 characters")
    private String apiVersion;

    @Column(name = "api_timeout", nullable = false)
    @NotNull(message = "API timeout is required")
    @Min(value = 5000, message = "API timeout must be at least 5000 milliseconds")
    @Max(value = 120000, message = "API timeout must not exceed 120000 milliseconds")
    @Builder.Default
    private Integer apiTimeout = 30000;

    @Column(name = "api_credentials", columnDefinition = "TEXT")
    private String apiCredentials;

    @Column(name = "authentication_type", length = 20)
    @Size(max = 20, message = "Authentication type must not exceed 20 characters")
    @Builder.Default
    private String authenticationType = "API_KEY";

    // Commission Structure
    @Column(name = "default_commission_rate", nullable = false, precision = 5, scale = 4)
    @NotNull(message = "Default commission rate is required")
    @DecimalMin(value = "0.0000", message = "Default commission rate must be non-negative")
    @DecimalMax(value = "0.5000", message = "Default commission rate must not exceed 50%")
    @Builder.Default
    private BigDecimal defaultCommissionRate = BigDecimal.ZERO;

    @Column(name = "domestic_commission_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "Domestic commission rate must be non-negative")
    @DecimalMax(value = "0.5000", message = "Domestic commission rate must not exceed 50%")
    private BigDecimal domesticCommissionRate;

    @Column(name = "international_commission_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "International commission rate must be non-negative")
    @DecimalMax(value = "0.5000", message = "International commission rate must not exceed 50%")
    private BigDecimal internationalCommissionRate;

    @Column(name = "premium_commission_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "Premium commission rate must be non-negative")
    @DecimalMax(value = "0.5000", message = "Premium commission rate must not exceed 50%")
    private BigDecimal premiumCommissionRate;

    @Column(name = "minimum_commission_amount", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Minimum commission amount must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Minimum commission amount format is invalid")
    @Builder.Default
    private BigDecimal minimumCommissionAmount = BigDecimal.ZERO;

    // Payment Terms
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms", nullable = false, length = 20)
    @NotNull(message = "Payment terms are required")
    @Builder.Default
    private PaymentTerms paymentTerms = PaymentTerms.NET_30;

    @Column(name = "payment_method", length = 50)
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Builder.Default
    private String paymentMethod = "BANK_TRANSFER";

    @Column(name = "invoice_frequency", length = 20)
    @Size(max = 20, message = "Invoice frequency must not exceed 20 characters")
    @Builder.Default
    private String invoiceFrequency = "MONTHLY";

    // Contact Information
    @Column(name = "contact_person", length = 100)
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Column(name = "contact_email", length = 255)
    @Email(message = "Invalid contact email format")
    @Size(max = 255, message = "Contact email must not exceed 255 characters")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid contact phone format")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Column(name = "support_email", length = 255)
    @Email(message = "Invalid support email format")
    @Size(max = 255, message = "Support email must not exceed 255 characters")
    private String supportEmail;

    @Column(name = "support_phone", length = 20)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid support phone format")
    @Size(max = 20, message = "Support phone must not exceed 20 characters")
    private String supportPhone;

    // Business Details
    @Column(name = "business_registration_number", length = 50)
    @Size(max = 50, message = "Business registration number must not exceed 50 characters")
    private String businessRegistrationNumber;

    @Column(name = "tax_id", length = 50)
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Column(name = "address", length = 500)
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Column(name = "city", length = 100)
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Column(name = "postal_code", length = 20)
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    // Contract Details
    @Column(name = "contract_start_date")
    private LocalDateTime contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDateTime contractEndDate;

    @Column(name = "contract_details", columnDefinition = "TEXT")
    private String contractDetails;

    @Column(name = "sla_response_time_hours")
    @Min(value = 1, message = "SLA response time must be at least 1 hour")
    @Max(value = 168, message = "SLA response time must not exceed 168 hours")
    @Builder.Default
    private Integer slaResponseTimeHours = 24;

    // Statistics
    @Column(name = "total_bookings", nullable = false)
    @Min(value = 0, message = "Total bookings must be non-negative")
    @Builder.Default
    private Long totalBookings = 0L;

    @Column(name = "total_revenue", nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Total revenue must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Total revenue format is invalid")
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_commission", nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Total commission must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Total commission format is invalid")
    @Builder.Default
    private BigDecimal totalCommission = BigDecimal.ZERO;

    @Column(name = "success_rate", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Success rate must be non-negative")
    @DecimalMax(value = "100.00", message = "Success rate must not exceed 100%")
    @Builder.Default
    private BigDecimal successRate = BigDecimal.ZERO;

    @Column(name = "average_response_time_ms")
    @Min(value = 0, message = "Average response time must be non-negative")
    @Builder.Default
    private Long averageResponseTimeMs = 0L;

    @Column(name = "last_successful_connection")
    private LocalDateTime lastSuccessfulConnection;

    @Column(name = "last_failed_connection")
    private LocalDateTime lastFailedConnection;

    @Column(name = "consecutive_failures", nullable = false)
    @Min(value = 0, message = "Consecutive failures must be non-negative")
    @Builder.Default
    private Integer consecutiveFailures = 0;

    // Configuration
    @Column(name = "is_sandbox", nullable = false)
    @Builder.Default
    private Boolean isSandbox = false;

    @Column(name = "auto_confirmation_enabled", nullable = false)
    @Builder.Default
    private Boolean autoConfirmationEnabled = true;

    @Column(name = "real_time_availability", nullable = false)
    @Builder.Default
    private Boolean realTimeAvailability = true;

    @Column(name = "supports_cancellation", nullable = false)
    @Builder.Default
    private Boolean supportsCancellation = true;

    @Column(name = "supports_modification", nullable = false)
    @Builder.Default
    private Boolean supportsModification = true;

    @Column(name = "supports_seat_selection", nullable = false)
    @Builder.Default
    private Boolean supportsSeatSelection = false;

    @Column(name = "notification_webhook_url", length = 500)
    @Size(max = 500, message = "Notification webhook URL must not exceed 500 characters")
    private String notificationWebhookUrl;

    @Column(name = "webhook_secret", length = 100)
    @Size(max = 100, message = "Webhook secret must not exceed 100 characters")
    private String webhookSecret;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    @CreatedBy
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    @LastModifiedBy
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    // Enums
    public enum PartnerType {
        AIRLINE,
        BUS_COMPANY,
        HOTEL_CHAIN,
        CAR_RENTAL,
        TOUR_OPERATOR,
        TRAVEL_AGENCY,
        FERRY_COMPANY,
        TRAIN_OPERATOR
    }

    public enum PartnerStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        PENDING_APPROVAL,
        CONTRACT_EXPIRED,
        UNDER_MAINTENANCE
    }

    public enum PaymentTerms {
        NET_15,
        NET_30,
        NET_45,
        NET_60,
        IMMEDIATE,
        END_OF_MONTH
    }

    // Helper methods
    public boolean isActive() {
        return PartnerStatus.ACTIVE.equals(this.status);
    }

    public boolean isContractValid() {
        LocalDateTime now = LocalDateTime.now();
        return this.contractStartDate != null && 
               this.contractEndDate != null &&
               now.isAfter(this.contractStartDate) && 
               now.isBefore(this.contractEndDate);
    }

    public boolean requiresAttention() {
        return this.consecutiveFailures >= 5 || 
               this.successRate.compareTo(BigDecimal.valueOf(95)) < 0 ||
               !isContractValid();
    }

    public BigDecimal getCommissionRate(String bookingType, boolean isInternational) {
        // Return appropriate commission rate based on booking type and destination
        if (isInternational && this.internationalCommissionRate != null) {
            return this.internationalCommissionRate;
        } else if (!isInternational && this.domesticCommissionRate != null) {
            return this.domesticCommissionRate;
        } else if ("premium".equalsIgnoreCase(bookingType) && this.premiumCommissionRate != null) {
            return this.premiumCommissionRate;
        }
        return this.defaultCommissionRate;
    }

    public boolean isHealthy() {
        return isActive() && 
               this.consecutiveFailures < 3 && 
               this.successRate.compareTo(BigDecimal.valueOf(90)) >= 0;
    }

    public long getDaysSinceLastConnection() {
        if (this.lastSuccessfulConnection == null) return Long.MAX_VALUE;
        return java.time.Duration.between(this.lastSuccessfulConnection, LocalDateTime.now()).toDays();
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.partnerCode == null) {
            this.partnerCode = generatePartnerCode();
        }
    }

    private String generatePartnerCode() {
        String typePrefix = switch (this.partnerType) {
            case AIRLINE -> "AL";
            case BUS_COMPANY -> "BC";
            case HOTEL_CHAIN -> "HC";
            case CAR_RENTAL -> "CR";
            case TOUR_OPERATOR -> "TO";
            case TRAVEL_AGENCY -> "TA";
            case FERRY_COMPANY -> "FC";
            case TRAIN_OPERATOR -> "TR";
        };
        
        String countryCode = this.country != null ? this.country.substring(0, 2) : "XX";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        
        return typePrefix + "-" + countryCode + "-" + timestamp;
    }

    public void recordSuccessfulConnection() {
        this.lastSuccessfulConnection = LocalDateTime.now();
        this.consecutiveFailures = 0;
        updateSuccessRate();
    }

    public void recordFailedConnection() {
        this.lastFailedConnection = LocalDateTime.now();
        this.consecutiveFailures++;
        updateSuccessRate();
    }

    private void updateSuccessRate() {
        if (this.totalBookings > 0) {
            long successfulBookings = this.totalBookings - this.consecutiveFailures;
            this.successRate = BigDecimal.valueOf(successfulBookings)
                    .divide(BigDecimal.valueOf(this.totalBookings), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }

    public void incrementBookingStats(BigDecimal bookingAmount, BigDecimal commissionAmount) {
        this.totalBookings++;
        this.totalRevenue = this.totalRevenue.add(bookingAmount);
        this.totalCommission = this.totalCommission.add(commissionAmount);
        updateSuccessRate();
    }
}