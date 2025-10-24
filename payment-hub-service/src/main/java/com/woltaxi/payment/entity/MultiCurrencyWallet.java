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
import java.util.Map;
import java.util.UUID;

/**
 * Multi-Currency Wallet Entity
 * 
 * Manages multi-currency balances for users with automatic conversion,
 * transaction limits, and comprehensive audit logging.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "multi_currency_wallets", indexes = {
    @Index(name = "idx_wallet_user_id", columnList = "user_id"),
    @Index(name = "idx_wallet_currency", columnList = "currency"),
    @Index(name = "idx_wallet_user_currency", columnList = "user_id, currency", unique = true),
    @Index(name = "idx_wallet_status", columnList = "status"),
    @Index(name = "idx_wallet_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"transactionLimits", "metadata"})
public class MultiCurrencyWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Column(name = "currency", nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Available balance is required")
    @DecimalMin(value = "0.00", message = "Available balance must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Available balance format is invalid")
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "pending_balance", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Pending balance is required")
    @DecimalMin(value = "0.00", message = "Pending balance must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Pending balance format is invalid")
    @Builder.Default
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    @Column(name = "total_balance", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Total balance is required")
    @DecimalMin(value = "0.00", message = "Total balance must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Total balance format is invalid")
    @Builder.Default
    private BigDecimal totalBalance = BigDecimal.ZERO;

    @Column(name = "reserved_balance", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Reserved balance is required")
    @DecimalMin(value = "0.00", message = "Reserved balance must be non-negative")
    @Digits(integer = 15, fraction = 4, message = "Reserved balance format is invalid")
    @Builder.Default
    private BigDecimal reservedBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Wallet status is required")
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "auto_conversion_enabled", nullable = false)
    @Builder.Default
    private Boolean autoConversionEnabled = true;

    @Column(name = "minimum_balance", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Minimum balance must be non-negative")
    @Builder.Default
    private BigDecimal minimumBalance = new BigDecimal("0.01");

    @Column(name = "maximum_balance", precision = 19, scale = 4)
    @DecimalMin(value = "0.01", message = "Maximum balance must be positive")
    @Builder.Default
    private BigDecimal maximumBalance = new BigDecimal("50000.00");

    @Column(name = "daily_transaction_limit", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Daily transaction limit must be non-negative")
    @Builder.Default
    private BigDecimal dailyTransactionLimit = new BigDecimal("10000.00");

    @Column(name = "monthly_transaction_limit", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Monthly transaction limit must be non-negative")
    @Builder.Default
    private BigDecimal monthlyTransactionLimit = new BigDecimal("100000.00");

    @Column(name = "daily_spent_amount", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Daily spent amount must be non-negative")
    @Builder.Default
    private BigDecimal dailySpentAmount = BigDecimal.ZERO;

    @Column(name = "monthly_spent_amount", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Monthly spent amount must be non-negative")
    @Builder.Default
    private BigDecimal monthlySpentAmount = BigDecimal.ZERO;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @Column(name = "last_daily_reset")
    private LocalDateTime lastDailyReset;

    @Column(name = "last_monthly_reset")
    private LocalDateTime lastMonthlyReset;

    @Column(name = "total_deposits", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Total deposits must be non-negative")
    @Builder.Default
    private BigDecimal totalDeposits = BigDecimal.ZERO;

    @Column(name = "total_withdrawals", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Total withdrawals must be non-negative")
    @Builder.Default
    private BigDecimal totalWithdrawals = BigDecimal.ZERO;

    @Column(name = "total_fees_paid", precision = 19, scale = 4)
    @DecimalMin(value = "0.00", message = "Total fees paid must be non-negative")
    @Builder.Default
    private BigDecimal totalFeesPaid = BigDecimal.ZERO;

    @Column(name = "conversion_fee_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "Conversion fee rate must be non-negative")
    @DecimalMax(value = "0.1000", message = "Conversion fee rate must not exceed 10%")
    @Builder.Default
    private BigDecimal conversionFeeRate = new BigDecimal("0.0150");

    @Column(name = "preferred_providers", columnDefinition = "TEXT")
    private String preferredProviders;

    @Column(name = "transaction_limits", columnDefinition = "JSONB")
    private String transactionLimits;

    @Column(name = "risk_level", length = 10)
    @Size(max = 10, message = "Risk level must not exceed 10 characters")
    @Builder.Default
    private String riskLevel = "LOW";

    @Column(name = "kyc_verified", nullable = false)
    @Builder.Default
    private Boolean kycVerified = false;

    @Column(name = "kyc_verification_date")
    private LocalDateTime kycVerificationDate;

    @Column(name = "compliance_status", length = 20)
    @Size(max = 20, message = "Compliance status must not exceed 20 characters")
    @Builder.Default
    private String complianceStatus = "PENDING";

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

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
    public enum WalletStatus {
        ACTIVE,
        SUSPENDED,
        FROZEN,
        CLOSED,
        PENDING_VERIFICATION,
        UNDER_REVIEW
    }

    // Helper methods
    public boolean canDebit(BigDecimal amount) {
        return WalletStatus.ACTIVE.equals(this.status) && 
               this.availableBalance.compareTo(amount) >= 0;
    }

    public boolean canCredit(BigDecimal amount) {
        return WalletStatus.ACTIVE.equals(this.status) && 
               this.totalBalance.add(amount).compareTo(this.maximumBalance) <= 0;
    }

    public boolean hasReachedDailyLimit(BigDecimal additionalAmount) {
        return this.dailySpentAmount.add(additionalAmount)
                   .compareTo(this.dailyTransactionLimit) > 0;
    }

    public boolean hasReachedMonthlyLimit(BigDecimal additionalAmount) {
        return this.monthlySpentAmount.add(additionalAmount)
                   .compareTo(this.monthlyTransactionLimit) > 0;
    }

    public BigDecimal getUsableBalance() {
        return this.availableBalance.subtract(this.reservedBalance);
    }

    public boolean isLowBalance() {
        return this.availableBalance.compareTo(this.minimumBalance) <= 0;
    }

    public boolean requiresKycVerification() {
        return !this.kycVerified && 
               this.totalBalance.compareTo(new BigDecimal("1000.00")) > 0;
    }

    public BigDecimal getRemainingDailyLimit() {
        return this.dailyTransactionLimit.subtract(this.dailySpentAmount);
    }

    public BigDecimal getRemainingMonthlyLimit() {
        return this.monthlyTransactionLimit.subtract(this.monthlySpentAmount);
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.availableBalance == null) {
            this.availableBalance = BigDecimal.ZERO;
        }
        if (this.pendingBalance == null) {
            this.pendingBalance = BigDecimal.ZERO;
        }
        if (this.totalBalance == null) {
            this.totalBalance = BigDecimal.ZERO;
        }
        if (this.reservedBalance == null) {
            this.reservedBalance = BigDecimal.ZERO;
        }
        updateTotalBalance();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTotalBalance();
    }

    private void updateTotalBalance() {
        this.totalBalance = this.availableBalance.add(this.pendingBalance).add(this.reservedBalance);
    }

    public void debit(BigDecimal amount) {
        if (!canDebit(amount)) {
            throw new IllegalStateException("Insufficient balance for debit operation");
        }
        this.availableBalance = this.availableBalance.subtract(amount);
        this.lastTransactionDate = LocalDateTime.now();
        updateTotalBalance();
    }

    public void credit(BigDecimal amount) {
        if (!canCredit(amount)) {
            throw new IllegalStateException("Maximum balance exceeded for credit operation");
        }
        this.availableBalance = this.availableBalance.add(amount);
        this.lastTransactionDate = LocalDateTime.now();
        updateTotalBalance();
    }

    public void reserve(BigDecimal amount) {
        if (!canDebit(amount)) {
            throw new IllegalStateException("Insufficient balance for reserve operation");
        }
        this.availableBalance = this.availableBalance.subtract(amount);
        this.reservedBalance = this.reservedBalance.add(amount);
        updateTotalBalance();
    }

    public void releaseReserve(BigDecimal amount) {
        if (this.reservedBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient reserved balance for release operation");
        }
        this.reservedBalance = this.reservedBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
        updateTotalBalance();
    }

    public void addToPending(BigDecimal amount) {
        this.pendingBalance = this.pendingBalance.add(amount);
        updateTotalBalance();
    }

    public void removePending(BigDecimal amount) {
        if (this.pendingBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient pending balance for removal operation");
        }
        this.pendingBalance = this.pendingBalance.subtract(amount);
        updateTotalBalance();
    }

    public void confirmPending(BigDecimal amount) {
        removePending(amount);
        credit(amount);
    }
}