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
 * Travel Booking Entity
 * 
 * Comprehensive travel booking entity supporting flights, buses, hotels,
 * and car rentals with commission tracking and payment integration.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "travel_bookings", indexes = {
    @Index(name = "idx_booking_reference", columnList = "booking_reference"),
    @Index(name = "idx_booking_user_id", columnList = "user_id"),
    @Index(name = "idx_booking_provider", columnList = "travel_provider"),
    @Index(name = "idx_booking_status", columnList = "booking_status"),
    @Index(name = "idx_booking_type", columnList = "booking_type"),
    @Index(name = "idx_booking_travel_date", columnList = "travel_date"),
    @Index(name = "idx_booking_created_at", columnList = "created_at"),
    @Index(name = "idx_booking_commission_status", columnList = "commission_status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"passengerDetails", "paymentDetails", "providerMetadata"})
public class TravelBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "booking_reference", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Booking reference is required")
    @Size(max = 20, message = "Booking reference must not exceed 20 characters")
    private String bookingReference;

    @Column(name = "provider_booking_id", unique = true, length = 100)
    @Size(max = 100, message = "Provider booking ID must not exceed 100 characters")
    private String providerBookingId;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false, length = 20)
    @NotNull(message = "Booking type is required")
    private BookingType bookingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_provider", nullable = false, length = 30)
    @NotNull(message = "Travel provider is required")
    private TravelProvider travelProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 20)
    @NotNull(message = "Booking status is required")
    @Builder.Default
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @Column(name = "origin", nullable = false, length = 100)
    @NotBlank(message = "Origin is required")
    @Size(max = 100, message = "Origin must not exceed 100 characters")
    private String origin;

    @Column(name = "destination", nullable = false, length = 100)
    @NotBlank(message = "Destination is required")
    @Size(max = 100, message = "Destination must not exceed 100 characters")
    private String destination;

    @Column(name = "travel_date", nullable = false)
    @NotNull(message = "Travel date is required")
    @Future(message = "Travel date must be in the future")
    private LocalDateTime travelDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "passenger_count", nullable = false)
    @NotNull(message = "Passenger count is required")
    @Min(value = 1, message = "Passenger count must be at least 1")
    @Max(value = 9, message = "Passenger count must not exceed 9")
    private Integer passengerCount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Total amount format is invalid")
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    @NotNull(message = "Commission rate is required")
    @DecimalMin(value = "0.0000", message = "Commission rate must be non-negative")
    @DecimalMax(value = "0.5000", message = "Commission rate must not exceed 50%")
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Commission amount is required")
    @DecimalMin(value = "0.00", message = "Commission amount must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Commission amount format is invalid")
    private BigDecimal commissionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_status", nullable = false, length = 20)
    @NotNull(message = "Commission status is required")
    @Builder.Default
    private CommissionStatus commissionStatus = CommissionStatus.PENDING;

    @Column(name = "passenger_details", columnDefinition = "JSONB")
    private String passengerDetails;

    @Column(name = "contact_email", nullable = false, length = 255)
    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Contact email must not exceed 255 characters")
    private String contactEmail;

    @Column(name = "contact_phone", nullable = false, length = 20)
    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Column(name = "payment_transaction_id")
    private UUID paymentTransactionId;

    @Column(name = "payment_details", columnDefinition = "JSONB")
    private String paymentDetails;

    @Column(name = "confirmation_code", length = 50)
    @Size(max = 50, message = "Confirmation code must not exceed 50 characters")
    private String confirmationCode;

    @Column(name = "ticket_number", length = 50)
    @Size(max = 50, message = "Ticket number must not exceed 50 characters")
    private String ticketNumber;

    @Column(name = "seat_numbers", length = 200)
    @Size(max = 200, message = "Seat numbers must not exceed 200 characters")
    private String seatNumbers;

    @Column(name = "special_requests", length = 1000)
    @Size(max = 1000, message = "Special requests must not exceed 1000 characters")
    private String specialRequests;

    @Column(name = "cancellation_policy", length = 2000)
    @Size(max = 2000, message = "Cancellation policy must not exceed 2000 characters")
    private String cancellationPolicy;

    @Column(name = "provider_metadata", columnDefinition = "JSONB")
    private String providerMetadata;

    @Column(name = "booking_confirmed_at")
    private LocalDateTime bookingConfirmedAt;

    @Column(name = "booking_cancelled_at")
    private LocalDateTime bookingCancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String cancellationReason;

    @Column(name = "refund_amount", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Refund amount must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Refund amount format is invalid")
    private BigDecimal refundAmount;

    @Column(name = "refund_processed_at")
    private LocalDateTime refundProcessedAt;

    @Column(name = "check_in_status", length = 20)
    @Size(max = 20, message = "Check-in status must not exceed 20 characters")
    @Builder.Default
    private String checkInStatus = "NOT_AVAILABLE";

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "boarding_pass_url", length = 500)
    @Size(max = 500, message = "Boarding pass URL must not exceed 500 characters")
    private String boardingPassUrl;

    @Column(name = "ticket_pdf_url", length = 500)
    @Size(max = 500, message = "Ticket PDF URL must not exceed 500 characters")
    private String ticketPdfUrl;

    @Column(name = "qr_code_url", length = 500)
    @Size(max = 500, message = "QR code URL must not exceed 500 characters")
    private String qrCodeUrl;

    @Column(name = "notification_sent", nullable = false)
    @Builder.Default
    private Boolean notificationSent = false;

    @Column(name = "reminder_sent", nullable = false)
    @Builder.Default
    private Boolean reminderSent = false;

    @Column(name = "is_group_booking", nullable = false)
    @Builder.Default
    private Boolean isGroupBooking = false;

    @Column(name = "group_discount_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "Group discount rate must be non-negative")
    @DecimalMax(value = "0.5000", message = "Group discount rate must not exceed 50%")
    private BigDecimal groupDiscountRate;

    @Column(name = "loyalty_points_earned")
    @Min(value = 0, message = "Loyalty points earned must be non-negative")
    @Builder.Default
    private Integer loyaltyPointsEarned = 0;

    @Column(name = "loyalty_points_used")
    @Min(value = 0, message = "Loyalty points used must be non-negative")
    @Builder.Default
    private Integer loyaltyPointsUsed = 0;

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
    public enum BookingType {
        FLIGHT,
        BUS,
        HOTEL,
        CAR_RENTAL,
        PACKAGE_TOUR,
        TRAIN,
        FERRY
    }

    public enum TravelProvider {
        // Airlines
        AMADEUS,
        TURKISH_AIRLINES,
        PEGASUS,
        SKYSCANNER,
        TRAVELPORT,
        
        // Bus Companies
        METRO_TURIZM,
        KAMIL_KOC,
        PAMUKKALE,
        VARAN,
        
        // Hotels
        BOOKING_COM,
        EXPEDIA,
        HOTELS_COM,
        
        // Car Rentals
        AVIS,
        HERTZ,
        
        // Other
        CUSTOM_PROVIDER
    }

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        EXPIRED,
        CHECKED_IN,
        COMPLETED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        NO_SHOW
    }

    public enum CommissionStatus {
        PENDING,
        CALCULATED,
        INVOICED,
        PAID,
        DISPUTED,
        CANCELLED
    }

    // Helper methods
    public boolean isConfirmed() {
        return BookingStatus.CONFIRMED.equals(this.bookingStatus);
    }

    public boolean isCancellable() {
        return BookingStatus.CONFIRMED.equals(this.bookingStatus) && 
               this.travelDate.isAfter(LocalDateTime.now().plusHours(24));
    }

    public boolean isModifiable() {
        return BookingStatus.CONFIRMED.equals(this.bookingStatus) && 
               this.travelDate.isAfter(LocalDateTime.now().plusHours(12));
    }

    public boolean isCheckInAvailable() {
        return BookingType.FLIGHT.equals(this.bookingType) &&
               BookingStatus.CONFIRMED.equals(this.bookingStatus) &&
               this.travelDate.isAfter(LocalDateTime.now()) &&
               this.travelDate.isBefore(LocalDateTime.now().plusHours(48));
    }

    public boolean isRefundable() {
        return BookingStatus.CONFIRMED.equals(this.bookingStatus) ||
               BookingStatus.CANCELLED.equals(this.bookingStatus);
    }

    public boolean requiresCommissionPayment() {
        return BookingStatus.COMPLETED.equals(this.bookingStatus) &&
               CommissionStatus.CALCULATED.equals(this.commissionStatus) &&
               this.commissionAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isRoundTrip() {
        return this.returnDate != null;
    }

    public boolean isMultiCity() {
        return this.origin.contains(",") || this.destination.contains(",");
    }

    public BigDecimal getNetAmount() {
        return this.totalAmount.subtract(this.commissionAmount);
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.bookingReference == null) {
            this.bookingReference = generateBookingReference();
        }
        if (this.commissionAmount == null && this.totalAmount != null && this.commissionRate != null) {
            this.commissionAmount = this.totalAmount.multiply(this.commissionRate);
        }
        if (this.loyaltyPointsEarned == null) {
            this.loyaltyPointsEarned = calculateLoyaltyPoints();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.commissionAmount == null && this.totalAmount != null && this.commissionRate != null) {
            this.commissionAmount = this.totalAmount.multiply(this.commissionRate);
        }
    }

    private String generateBookingReference() {
        String prefix = switch (this.bookingType) {
            case FLIGHT -> "WLT-FL";
            case BUS -> "WLT-BS";
            case HOTEL -> "WLT-HT";
            case CAR_RENTAL -> "WLT-CR";
            case PACKAGE_TOUR -> "WLT-PT";
            case TRAIN -> "WLT-TR";
            case FERRY -> "WLT-FR";
        };
        
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        String random = String.valueOf((int) (Math.random() * 1000));
        
        return prefix + "-" + timestamp + random;
    }

    private Integer calculateLoyaltyPoints() {
        if (this.totalAmount == null) return 0;
        
        // 1 point per 10 TRY spent, with bonus for premium bookings
        int basePoints = this.totalAmount.divide(BigDecimal.valueOf(10)).intValue();
        
        // Bonus points for different booking types
        int bonusMultiplier = switch (this.bookingType) {
            case FLIGHT -> 2;
            case HOTEL -> 3;
            case PACKAGE_TOUR -> 4;
            default -> 1;
        };
        
        return basePoints * bonusMultiplier;
    }

    public void confirm(String confirmationCode, String ticketNumber) {
        this.bookingStatus = BookingStatus.CONFIRMED;
        this.confirmationCode = confirmationCode;
        this.ticketNumber = ticketNumber;
        this.bookingConfirmedAt = LocalDateTime.now();
        this.commissionStatus = CommissionStatus.CALCULATED;
    }

    public void cancel(String reason) {
        this.bookingStatus = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.bookingCancelledAt = LocalDateTime.now();
        this.commissionStatus = CommissionStatus.CANCELLED;
    }

    public void checkIn() {
        if (isCheckInAvailable()) {
            this.checkInStatus = "CHECKED_IN";
            this.checkInTime = LocalDateTime.now();
            this.bookingStatus = BookingStatus.CHECKED_IN;
        }
    }

    public void complete() {
        this.bookingStatus = BookingStatus.COMPLETED;
        if (CommissionStatus.CALCULATED.equals(this.commissionStatus)) {
            this.commissionStatus = CommissionStatus.INVOICED;
        }
    }
}