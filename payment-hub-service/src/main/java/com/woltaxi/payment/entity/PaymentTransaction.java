package com.woltaxi.payment.entity;

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
 * Payment Transaction Entity
 * 
 * Represents payment transactions across all supported payment providers
 * with comprehensive audit trail and security features.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_payment_external_id", columnList = "external_transaction_id"),
    @Index(name = "idx_payment_user_id", columnList = "user_id"),
    @Index(name = "idx_payment_subscription_id", columnList = "subscription_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_provider", columnList = "payment_provider"),
    @Index(name = "idx_payment_created_at", columnList = "created_at"),
    @Index(name = "idx_payment_amount_currency", columnList = "amount, currency")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"encryptedCardData", "providerMetadata"})
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "external_transaction_id", unique = true, nullable = false, length = 100)
    @NotBlank(message = "External transaction ID is required")
    @Size(max = 100, message = "External transaction ID must not exceed 100 characters")
    private String externalTransactionId;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Amount format is invalid")
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    @Column(name = "original_amount", precision = 19, scale = 4)
    private BigDecimal originalAmount;

    @Column(name = "original_currency", length = 3)
    @Size(min = 3, max = 3, message = "Original currency must be exactly 3 characters")
    private String originalCurrency;

    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", nullable = false, length = 30)
    @NotNull(message = "Payment provider is required")
    private PaymentProvider paymentProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Column(name = "provider_transaction_id", length = 100)
    @Size(max = 100, message = "Provider transaction ID must not exceed 100 characters")
    private String providerTransactionId;

    @Column(name = "provider_response_code", length = 20)
    @Size(max = 20, message = "Provider response code must not exceed 20 characters")
    private String providerResponseCode;

    @Column(name = "provider_response_message", length = 500)
    @Size(max = 500, message = "Provider response message must not exceed 500 characters")
    private String providerResponseMessage;

    @Column(name = "encrypted_card_data", columnDefinition = "TEXT")
    private String encryptedCardData;

    @Column(name = "card_last_four", length = 4)
    @Size(min = 4, max = 4, message = "Card last four must be exactly 4 digits")
    @Pattern(regexp = "^\\d{4}$", message = "Card last four must be numeric")
    private String cardLastFour;

    @Column(name = "card_brand", length = 20)
    @Size(max = 20, message = "Card brand must not exceed 20 characters")
    private String cardBrand;

    @Column(name = "card_exp_month")
    @Min(value = 1, message = "Card expiry month must be between 1 and 12")
    @Max(value = 12, message = "Card expiry month must be between 1 and 12")
    private Integer cardExpMonth;

    @Column(name = "card_exp_year")
    @Min(value = 2024, message = "Card expiry year must be valid")
    private Integer cardExpYear;

    @Column(name = "billing_address_id")
    private UUID billingAddressId;

    @Column(name = "fraud_score")
    @DecimalMin(value = "0.0", message = "Fraud score must be non-negative")
    @DecimalMax(value = "100.0", message = "Fraud score must not exceed 100")
    private BigDecimal fraudScore;

    @Column(name = "risk_level", length = 10)
    @Size(max = 10, message = "Risk level must not exceed 10 characters")
    private String riskLevel;

    @Column(name = "ip_address", length = 45)
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    @Size(max = 500, message = "User agent must not exceed 500 characters")
    private String userAgent;

    @Column(name = "device_fingerprint", length = 100)
    @Size(max = 100, message = "Device fingerprint must not exceed 100 characters")
    private String deviceFingerprint;

    @Column(name = "provider_metadata", columnDefinition = "JSONB")
    private String providerMetadata;

    @Column(name = "webhook_received_at")
    private LocalDateTime webhookReceivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_amount", precision = 19, scale = 4)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "parent_transaction_id")
    private UUID parentTransactionId;

    @Column(name = "subscription_period_start")
    private LocalDateTime subscriptionPeriodStart;

    @Column(name = "subscription_period_end")
    private LocalDateTime subscriptionPeriodEnd;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 1;

    @Column(name = "next_retry_after")
    private LocalDateTime nextRetryAfter;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "is_test_transaction", nullable = false)
    @Builder.Default
    private Boolean isTestTransaction = false;

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
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        SUCCEEDED,
        FAILED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        DISPUTED,
        EXPIRED
    }

    public enum PaymentProvider {
        STRIPE,
        PAYPAL,
        SQUARE,
        ADYEN,
        BRAINTREE,
        IYZICO,
        PAYTR,
        APPLE_PAY,
        GOOGLE_PAY,
        SAMSUNG_PAY
    }

    public enum PaymentMethod {
        CARD,
        BANK_TRANSFER,
        DIGITAL_WALLET,
        MOBILE_PAYMENT,
        CRYPTOCURRENCY,
        BUY_NOW_PAY_LATER
    }

    public enum TransactionType {
        PAYMENT,
        REFUND,
        CHARGEBACK,
        SUBSCRIPTION,
        TOP_UP,
        WITHDRAWAL,
        FEE,
        ADJUSTMENT
    }

    // Helper methods
    public boolean isSuccessful() {
        return PaymentStatus.SUCCEEDED.equals(this.status);
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(this.status) || 
               PaymentStatus.CANCELLED.equals(this.status) ||
               PaymentStatus.EXPIRED.equals(this.status);
    }

    public boolean isRefundable() {
        return PaymentStatus.SUCCEEDED.equals(this.status) && 
               (this.refundAmount == null || 
                this.refundAmount.compareTo(this.amount) < 0);
    }

    public boolean hasHighRisk() {
        return this.fraudScore != null && 
               this.fraudScore.compareTo(BigDecimal.valueOf(75)) >= 0;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.attemptCount == null) {
            this.attemptCount = 1;
        }
        if (this.isRecurring == null) {
            this.isRecurring = false;
        }
        if (this.isTestTransaction == null) {
            this.isTestTransaction = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Increment attempt count for failed transactions
        if (PaymentStatus.FAILED.equals(this.status) && this.attemptCount != null) {
            this.attemptCount++;
        }
    }
}