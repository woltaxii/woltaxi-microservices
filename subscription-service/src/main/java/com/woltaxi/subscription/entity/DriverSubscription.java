package com.woltaxi.subscription.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Driver Subscription Entity - Sürücü Aboneliği Varlık Sınıfı
 * 
 * Sürücülerin aldığı abonelik paketlerini ve bunların durumlarını yönetir.
 */
@Entity
@Table(name = "driver_subscriptions", indexes = {
    @Index(name = "idx_driver_subscription_driver", columnList = "driver_id"),
    @Index(name = "idx_driver_subscription_status", columnList = "status"),
    @Index(name = "idx_driver_subscription_dates", columnList = "start_date, end_date")
})
public class DriverSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_uuid", nullable = false, unique = true)
    private UUID subscriptionUuid = UUID.randomUUID();

    // İlişkiler
    @NotNull(message = "Sürücü ID boş olamaz")
    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    @NotNull(message = "Paket seçimi boş olamaz")
    private SubscriptionPackage subscriptionPackage;

    // Abonelik Detayları
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false)
    private SubscriptionType subscriptionType;

    @NotNull(message = "Başlangıç tarihi boş olamaz")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "Bitiş tarihi boş olamaz")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // Ödeme Bilgileri
    @NotNull(message = "Ödenen tutar boş olamaz")
    @DecimalMin(value = "0.0", message = "Ödenen tutar negatif olamaz")
    @Column(name = "amount_paid", nullable = false, precision = 8, scale = 2)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    // Durum
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "auto_renewal", nullable = false)
    private Boolean autoRenewal = true;

    // Kullanım Takibi
    @Column(name = "rides_used_this_period", nullable = false)
    private Integer ridesUsedThisPeriod = 0;

    @Column(name = "last_ride_date")
    private LocalDateTime lastRideDate;

    // Müşteri Portföyü
    @Column(name = "customer_portfolio_count", nullable = false)
    private Integer customerPortfolioCount = 0;

    @Column(name = "portfolio_limit", nullable = false)
    private Integer portfolioLimit;

    // Tarih Alanları
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // İlişkiler
    @OneToMany(mappedBy = "driverSubscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SubscriptionPayment> payments;

    @OneToMany(mappedBy = "driverSubscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerPortfolio> customerPortfolios;

    // Constructors
    public DriverSubscription() {}

    public DriverSubscription(Long driverId, SubscriptionPackage subscriptionPackage, 
                            SubscriptionType subscriptionType, BigDecimal amountPaid) {
        this.driverId = driverId;
        this.subscriptionPackage = subscriptionPackage;
        this.subscriptionType = subscriptionType;
        this.amountPaid = amountPaid;
        this.portfolioLimit = subscriptionPackage.getCustomerPortfolioLimit();
        
        // Tarihleri hesapla
        this.startDate = LocalDateTime.now();
        if (subscriptionType == SubscriptionType.MONTHLY) {
            this.endDate = startDate.plusMonths(1);
        } else {
            this.endDate = startDate.plusYears(1);
        }
    }

    // Enums
    public enum SubscriptionType {
        MONTHLY("Aylık"),
        YEARLY("Yıllık");

        private final String displayName;

        SubscriptionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    public enum PaymentStatus {
        PENDING("Beklemede"),
        PAID("Ödendi"),
        FAILED("Başarısız"),
        CANCELLED("İptal Edildi"),
        REFUNDED("İade Edildi");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    public enum PaymentMethod {
        CARD("Kredi/Banka Kartı"),
        BANK_TRANSFER("Havale/EFT"),
        MOBILE_PAYMENT("Mobil Ödeme");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    public enum SubscriptionStatus {
        ACTIVE("Aktif"),
        EXPIRED("Süresi Dolmuş"),
        CANCELLED("İptal Edildi"),
        SUSPENDED("Askıya Alındı");

        private final String displayName;

        SubscriptionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Helper Methods
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE && 
               LocalDateTime.now().isBefore(endDate) &&
               paymentStatus == PaymentStatus.PAID;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public boolean canAddMoreCustomers() {
        if (portfolioLimit == -1) return true; // Sınırsız
        return customerPortfolioCount < portfolioLimit;
    }

    public boolean canMakeMoreRides() {
        if (subscriptionPackage.getMaxDailyRides() == -1) return true; // Sınırsız
        // Günlük ride kontrolü burada yapılabilir
        return true;
    }

    public int getDaysRemaining() {
        if (isExpired()) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }

    public BigDecimal getUsagePercentage() {
        if (subscriptionPackage.getMaxMonthlyRides() == -1) return BigDecimal.ZERO;
        return BigDecimal.valueOf(ridesUsedThisPeriod)
                .divide(BigDecimal.valueOf(subscriptionPackage.getMaxMonthlyRides()), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getSubscriptionUuid() { return subscriptionUuid; }
    public void setSubscriptionUuid(UUID subscriptionUuid) { this.subscriptionUuid = subscriptionUuid; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public SubscriptionPackage getSubscriptionPackage() { return subscriptionPackage; }
    public void setSubscriptionPackage(SubscriptionPackage subscriptionPackage) { this.subscriptionPackage = subscriptionPackage; }

    public SubscriptionType getSubscriptionType() { return subscriptionType; }
    public void setSubscriptionType(SubscriptionType subscriptionType) { this.subscriptionType = subscriptionType; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public Boolean getAutoRenewal() { return autoRenewal; }
    public void setAutoRenewal(Boolean autoRenewal) { this.autoRenewal = autoRenewal; }

    public Integer getRidesUsedThisPeriod() { return ridesUsedThisPeriod; }
    public void setRidesUsedThisPeriod(Integer ridesUsedThisPeriod) { this.ridesUsedThisPeriod = ridesUsedThisPeriod; }

    public LocalDateTime getLastRideDate() { return lastRideDate; }
    public void setLastRideDate(LocalDateTime lastRideDate) { this.lastRideDate = lastRideDate; }

    public Integer getCustomerPortfolioCount() { return customerPortfolioCount; }
    public void setCustomerPortfolioCount(Integer customerPortfolioCount) { this.customerPortfolioCount = customerPortfolioCount; }

    public Integer getPortfolioLimit() { return portfolioLimit; }
    public void setPortfolioLimit(Integer portfolioLimit) { this.portfolioLimit = portfolioLimit; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public List<SubscriptionPayment> getPayments() { return payments; }
    public void setPayments(List<SubscriptionPayment> payments) { this.payments = payments; }

    public List<CustomerPortfolio> getCustomerPortfolios() { return customerPortfolios; }
    public void setCustomerPortfolios(List<CustomerPortfolio> customerPortfolios) { this.customerPortfolios = customerPortfolios; }
}