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
import java.time.YearMonth;
import java.util.UUID;

/**
 * Commission Record Entity
 * 
 * Tracks commission earnings from travel bookings with monthly billing
 * cycles, payment status, and detailed breakdown by partner and booking type.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "commission_records", indexes = {
    @Index(name = "idx_commission_partner_id", columnList = "partner_id"),
    @Index(name = "idx_commission_booking_id", columnList = "booking_id"),
    @Index(name = "idx_commission_billing_period", columnList = "billing_year, billing_month"),
    @Index(name = "idx_commission_status", columnList = "commission_status"),
    @Index(name = "idx_commission_payment_status", columnList = "payment_status"),
    @Index(name = "idx_commission_booking_date", columnList = "booking_date"),
    @Index(name = "idx_commission_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"paymentDetails", "adjustmentNotes"})
public class CommissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "commission_reference", unique = true, nullable = false, length = 30)
    @NotBlank(message = "Commission reference is required")
    @Size(max = 30, message = "Commission reference must not exceed 30 characters")
    private String commissionReference;

    @Column(name = "partner_id", nullable = false)
    @NotNull(message = "Partner ID is required")
    private UUID partnerId;

    @Column(name = "booking_id", nullable = false)
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    @Column(name = "booking_reference", nullable = false, length = 20)
    @NotBlank(message = "Booking reference is required")
    @Size(max = 20, message = "Booking reference must not exceed 20 characters")
    private String bookingReference;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false, length = 20)
    @NotNull(message = "Booking type is required")
    private TravelBooking.BookingType bookingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_provider", nullable = false, length = 30)
    @NotNull(message = "Travel provider is required")
    private TravelBooking.TravelProvider travelProvider;

    @Column(name = "booking_date", nullable = false)
    @NotNull(message = "Booking date is required")
    private LocalDateTime bookingDate;

    @Column(name = "travel_date", nullable = false)
    @NotNull(message = "Travel date is required")
    private LocalDateTime travelDate;

    @Column(name = "booking_amount", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Booking amount is required")
    @DecimalMin(value = "0.01", message = "Booking amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Booking amount format is invalid")
    private BigDecimal bookingAmount;

    @Column(name = "booking_currency", nullable = false, length = 3)
    @NotBlank(message = "Booking currency is required")
    @Size(min = 3, max = 3, message = "Booking currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Booking currency must be a valid ISO 4217 code")
    private String bookingCurrency;

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

    @Column(name = "commission_currency", nullable = false, length = 3)
    @NotBlank(message = "Commission currency is required")
    @Size(min = 3, max = 3, message = "Commission currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Commission currency must be a valid ISO 4217 code")
    private String commissionCurrency;

    @Column(name = "exchange_rate", precision = 10, scale = 6)
    @DecimalMin(value = "0.000001", message = "Exchange rate must be positive")
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_status", nullable = false, length = 20)
    @NotNull(message = "Commission status is required")
    @Builder.Default
    private CommissionStatus commissionStatus = CommissionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @NotNull(message = "Payment status is required")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    // Billing Period
    @Column(name = "billing_year", nullable = false)
    @NotNull(message = "Billing year is required")
    @Min(value = 2024, message = "Billing year must be valid")
    private Integer billingYear;

    @Column(name = "billing_month", nullable = false)
    @NotNull(message = "Billing month is required")
    @Min(value = 1, message = "Billing month must be between 1 and 12")
    @Max(value = 12, message = "Billing month must be between 1 and 12")
    private Integer billingMonth;

    @Column(name = "billing_period", nullable = false, length = 7)
    @NotBlank(message = "Billing period is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Billing period must be in format YYYY-MM")
    private String billingPeriod;

    // Invoice Details
    @Column(name = "invoice_number", length = 50)
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDateTime invoiceDate;

    @Column(name = "invoice_due_date")
    private LocalDateTime invoiceDueDate;

    @Column(name = "invoice_amount", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Invoice amount must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Invoice amount format is invalid")
    private BigDecimal invoiceAmount;

    @Column(name = "invoice_pdf_url", length = 500)
    @Size(max = 500, message = "Invoice PDF URL must not exceed 500 characters")
    private String invoicePdfUrl;

    // Payment Details
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_amount", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Payment amount must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Payment amount format is invalid")
    private BigDecimal paymentAmount;

    @Column(name = "payment_method", length = 50)
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    @Column(name = "payment_reference", length = 100)
    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    private String paymentReference;

    @Column(name = "payment_details", columnDefinition = "JSONB")
    private String paymentDetails;

    // Adjustments
    @Column(name = "adjustment_amount", precision = 19, scale = 4)
    @Digits(integer = 15, fraction = 4, message = "Adjustment amount format is invalid")
    @Builder.Default
    private BigDecimal adjustmentAmount = BigDecimal.ZERO;

    @Column(name = "adjustment_reason", length = 500)
    @Size(max = 500, message = "Adjustment reason must not exceed 500 characters")
    private String adjustmentReason;

    @Column(name = "adjustment_date")
    private LocalDateTime adjustmentDate;

    @Column(name = "adjustment_notes", length = 1000)
    @Size(max = 1000, message = "Adjustment notes must not exceed 1000 characters")
    private String adjustmentNotes;

    // Tax Information
    @Column(name = "tax_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "Tax rate must be non-negative")
    @DecimalMax(value = "1.0000", message = "Tax rate must not exceed 100%")
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Tax amount must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Tax amount format is invalid")
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "tax_inclusive", nullable = false)
    @Builder.Default
    private Boolean taxInclusive = false;

    // Tracking
    @Column(name = "is_domestic", nullable = false)
    @Builder.Default
    private Boolean isDomestic = true;

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;

    @Column(name = "is_group_booking", nullable = false)
    @Builder.Default
    private Boolean isGroupBooking = false;

    @Column(name = "passenger_count")
    @Min(value = 1, message = "Passenger count must be at least 1")
    private Integer passengerCount;

    @Column(name = "booking_cancelled", nullable = false)
    @Builder.Default
    private Boolean bookingCancelled = false;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(name = "refund_processed", nullable = false)
    @Builder.Default
    private Boolean refundProcessed = false;

    @Column(name = "refund_amount", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Refund amount must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Refund amount format is invalid")
    private BigDecimal refundAmount;

    @Column(name = "net_commission", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Net commission must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Net commission format is invalid")
    private BigDecimal netCommission;

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
    public enum CommissionStatus {
        PENDING,
        CALCULATED,
        CONFIRMED,
        INVOICED,
        DISPUTED,
        CANCELLED,
        REFUNDED
    }

    public enum PaymentStatus {
        UNPAID,
        PARTIALLY_PAID,
        PAID,
        OVERDUE,
        DISPUTED,
        CANCELLED,
        REFUNDED
    }

    // Helper methods
    public boolean isEligibleForPayment() {
        return CommissionStatus.CONFIRMED.equals(this.commissionStatus) && 
               PaymentStatus.UNPAID.equals(this.paymentStatus) &&
               !Boolean.TRUE.equals(this.bookingCancelled);
    }

    public boolean isOverdue() {
        return PaymentStatus.UNPAID.equals(this.paymentStatus) &&
               this.invoiceDueDate != null &&
               this.invoiceDueDate.isBefore(LocalDateTime.now());
    }

    public boolean requiresInvoicing() {
        return CommissionStatus.CONFIRMED.equals(this.commissionStatus) &&
               this.invoiceNumber == null &&
               this.netCommission.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getFinalCommissionAmount() {
        BigDecimal base = this.commissionAmount.add(this.adjustmentAmount);
        if (Boolean.TRUE.equals(this.refundProcessed) && this.refundAmount != null) {
            base = base.subtract(this.refundAmount);
        }
        return base.max(BigDecimal.ZERO);
    }

    public BigDecimal getTotalAmountDue() {
        BigDecimal commission = getFinalCommissionAmount();
        if (Boolean.TRUE.equals(this.taxInclusive)) {
            return commission;
        } else {
            return commission.add(this.taxAmount);
        }
    }

    public int getDaysOverdue() {
        if (!isOverdue()) return 0;
        return (int) java.time.Duration.between(this.invoiceDueDate, LocalDateTime.now()).toDays();
    }

    public String getDisplayPeriod() {
        return YearMonth.of(this.billingYear, this.billingMonth).toString();
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.commissionReference == null) {
            this.commissionReference = generateCommissionReference();
        }
        if (this.billingPeriod == null && this.billingYear != null && this.billingMonth != null) {
            this.billingPeriod = String.format("%04d-%02d", this.billingYear, this.billingMonth);
        }
        if (this.netCommission == null) {
            this.netCommission = getFinalCommissionAmount();
        }
        calculateTaxAmount();
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.billingPeriod == null && this.billingYear != null && this.billingMonth != null) {
            this.billingPeriod = String.format("%04d-%02d", this.billingYear, this.billingMonth);
        }
        this.netCommission = getFinalCommissionAmount();
        calculateTaxAmount();
    }

    private String generateCommissionReference() {
        String typePrefix = switch (this.bookingType) {
            case FLIGHT -> "CM-FL";
            case BUS -> "CM-BS";
            case HOTEL -> "CM-HT";
            case CAR_RENTAL -> "CM-CR";
            case PACKAGE_TOUR -> "CM-PT";
            case TRAIN -> "CM-TR";
            case FERRY -> "CM-FR";
        };
        
        String yearMonth = String.format("%04d%02d", this.billingYear, this.billingMonth);
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        
        return typePrefix + "-" + yearMonth + "-" + timestamp;
    }

    private void calculateTaxAmount() {
        if (this.taxRate != null && this.taxRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal commission = getFinalCommissionAmount();
            if (Boolean.TRUE.equals(this.taxInclusive)) {
                // Tax is included in commission amount
                BigDecimal taxDivisor = BigDecimal.ONE.add(this.taxRate);
                this.taxAmount = commission.multiply(this.taxRate).divide(taxDivisor, 4, java.math.RoundingMode.HALF_UP);
            } else {
                // Tax is additional to commission amount
                this.taxAmount = commission.multiply(this.taxRate);
            }
        } else {
            this.taxAmount = BigDecimal.ZERO;
        }
    }

    public void confirm() {
        this.commissionStatus = CommissionStatus.CONFIRMED;
        this.netCommission = getFinalCommissionAmount();
    }

    public void invoice(String invoiceNumber, LocalDateTime invoiceDate, int paymentTermsDays) {
        if (!CommissionStatus.CONFIRMED.equals(this.commissionStatus)) {
            throw new IllegalStateException("Commission must be confirmed before invoicing");
        }
        
        this.commissionStatus = CommissionStatus.INVOICED;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.invoiceDueDate = invoiceDate.plusDays(paymentTermsDays);
        this.invoiceAmount = getTotalAmountDue();
    }

    public void markPaid(BigDecimal paidAmount, String paymentMethod, String paymentReference) {
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentDate = LocalDateTime.now();
        this.paymentAmount = paidAmount;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
    }

    public void markPartiallyPaid(BigDecimal paidAmount, String paymentMethod, String paymentReference) {
        this.paymentStatus = PaymentStatus.PARTIALLY_PAID;
        this.paymentDate = LocalDateTime.now();
        this.paymentAmount = (this.paymentAmount != null ? this.paymentAmount : BigDecimal.ZERO).add(paidAmount);
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
    }

    public void processRefund(BigDecimal refundAmount) {
        this.refundProcessed = true;
        this.refundAmount = refundAmount;
        this.netCommission = getFinalCommissionAmount();
        
        if (this.netCommission.compareTo(BigDecimal.ZERO) == 0) {
            this.commissionStatus = CommissionStatus.REFUNDED;
            this.paymentStatus = PaymentStatus.REFUNDED;
        }
    }

    public void cancel(String reason) {
        this.bookingCancelled = true;
        this.cancellationDate = LocalDateTime.now();
        this.commissionStatus = CommissionStatus.CANCELLED;
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.adjustmentReason = reason;
    }

    public void addAdjustment(BigDecimal adjustmentAmount, String reason) {
        this.adjustmentAmount = (this.adjustmentAmount != null ? this.adjustmentAmount : BigDecimal.ZERO).add(adjustmentAmount);
        this.adjustmentReason = reason;
        this.adjustmentDate = LocalDateTime.now();
        this.netCommission = getFinalCommissionAmount();
    }
}