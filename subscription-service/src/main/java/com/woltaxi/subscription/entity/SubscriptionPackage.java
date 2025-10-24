package com.woltaxi.subscription.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Subscription Package Entity - Abonelik Paketi Varlık Sınıfı
 * 
 * WOLTAXI platformundaki sürücü abonelik paketlerini temsil eder.
 * Basic, Premium, Gold, Diamond gibi farklı seviyeli paketler içerir.
 */
@Entity
@Table(name = "subscription_packages")
public class SubscriptionPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Paket adı boş olamaz")
    @Column(name = "package_name", nullable = false, unique = true, length = 50)
    private String packageName;

    @NotBlank(message = "Paket kodu boş olamaz")
    @Column(name = "package_code", nullable = false, unique = true, length = 20)
    private String packageCode;

    @NotBlank(message = "Paket açıklaması boş olamaz")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "features", columnDefinition = "TEXT[]")
    private String[] features;

    // Fiyatlandırma
    @NotNull(message = "Aylık fiyat boş olamaz")
    @DecimalMin(value = "0.0", message = "Aylık fiyat negatif olamaz")
    @Column(name = "monthly_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal monthlyPrice;

    @NotNull(message = "Yıllık fiyat boş olamaz")
    @DecimalMin(value = "0.0", message = "Yıllık fiyat negatif olamaz")
    @Column(name = "yearly_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal yearlyPrice;

    @Column(name = "yearly_discount_percent", precision = 5, scale = 2)
    private BigDecimal yearlyDiscountPercent = BigDecimal.valueOf(15.00);

    // Limitler ve Avantajlar
    @Column(name = "max_daily_rides")
    private Integer maxDailyRides = -1; // -1 = sınırsız

    @Column(name = "max_monthly_rides")
    private Integer maxMonthlyRides = -1;

    @Column(name = "priority_level")
    private Integer priorityLevel = 1; // 1=Normal, 2=Yüksek, 3=VIP, 4=Diamond

    @Column(name = "customer_portfolio_limit")
    private Integer customerPortfolioLimit = 100;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate = BigDecimal.valueOf(0.15); // %15

    // Durum
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_popular", nullable = false)
    private Boolean isPopular = false;

    // Tarih Alanları
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // İlişkiler
    @OneToMany(mappedBy = "subscriptionPackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DriverSubscription> driverSubscriptions;

    // Constructors
    public SubscriptionPackage() {}

    public SubscriptionPackage(String packageName, String packageCode, String description,
                             BigDecimal monthlyPrice, BigDecimal yearlyPrice) {
        this.packageName = packageName;
        this.packageCode = packageCode;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
    }

    // Enum for Package Types
    public enum PackageType {
        BASIC("BSC", "Temel Paket"),
        PREMIUM("PRM", "Premium Paket"),
        GOLD("GLD", "Gold Paket"),
        DIAMOND("DMD", "Diamond Paket");

        private final String code;
        private final String displayName;

        PackageType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() { return code; }
        public String getDisplayName() { return displayName; }
    }

    // Helper Methods
    public boolean isUnlimitedRides() {
        return maxDailyRides == -1 || maxMonthlyRides == -1;
    }

    public boolean isUnlimitedPortfolio() {
        return customerPortfolioLimit == -1;
    }

    public BigDecimal getYearlySavings() {
        if (yearlyDiscountPercent != null && monthlyPrice != null) {
            BigDecimal yearlyRegularPrice = monthlyPrice.multiply(BigDecimal.valueOf(12));
            return yearlyRegularPrice.subtract(yearlyPrice);
        }
        return BigDecimal.ZERO;
    }

    public String getPriorityLevelText() {
        return switch (priorityLevel) {
            case 1 -> "Standart";
            case 2 -> "Yüksek";
            case 3 -> "VIP";
            case 4 -> "Diamond";
            default -> "Bilinmiyor";
        };
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String[] getFeatures() { return features; }
    public void setFeatures(String[] features) { this.features = features; }

    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(BigDecimal monthlyPrice) { this.monthlyPrice = monthlyPrice; }

    public BigDecimal getYearlyPrice() { return yearlyPrice; }
    public void setYearlyPrice(BigDecimal yearlyPrice) { this.yearlyPrice = yearlyPrice; }

    public BigDecimal getYearlyDiscountPercent() { return yearlyDiscountPercent; }
    public void setYearlyDiscountPercent(BigDecimal yearlyDiscountPercent) { this.yearlyDiscountPercent = yearlyDiscountPercent; }

    public Integer getMaxDailyRides() { return maxDailyRides; }
    public void setMaxDailyRides(Integer maxDailyRides) { this.maxDailyRides = maxDailyRides; }

    public Integer getMaxMonthlyRides() { return maxMonthlyRides; }
    public void setMaxMonthlyRides(Integer maxMonthlyRides) { this.maxMonthlyRides = maxMonthlyRides; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }

    public Integer getCustomerPortfolioLimit() { return customerPortfolioLimit; }
    public void setCustomerPortfolioLimit(Integer customerPortfolioLimit) { this.customerPortfolioLimit = customerPortfolioLimit; }

    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsPopular() { return isPopular; }
    public void setIsPopular(Boolean isPopular) { this.isPopular = isPopular; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<DriverSubscription> getDriverSubscriptions() { return driverSubscriptions; }
    public void setDriverSubscriptions(List<DriverSubscription> driverSubscriptions) { this.driverSubscriptions = driverSubscriptions; }
}