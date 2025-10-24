package com.woltaxi.marketing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Ülke-Platform Konfigürasyon Entity
 * 
 * Her ülke için her reklam platformunun özel ayarları,
 * API anahtarları, bütçe limitleri ve hedefleme tercihleri
 */
@Entity
@Table(name = "country_platform_configs")
public class CountryPlatformConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private AdPlatform platform;
    
    // Platform Specific Settings (Encrypted)
    @Column(name = "api_key_encrypted", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "API key is required")
    private String apiKeyEncrypted;
    
    @Column(name = "api_secret_encrypted", columnDefinition = "TEXT")
    private String apiSecretEncrypted;
    
    @Column(name = "access_token_encrypted", columnDefinition = "TEXT")
    private String accessTokenEncrypted;
    
    @Column(name = "account_id", length = 100)
    private String accountId;
    
    // Budget & Bidding
    @Column(name = "daily_budget_local", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Daily budget is required")
    @DecimalMin(value = "0.0", message = "Daily budget must be positive")
    private BigDecimal dailyBudgetLocal;
    
    @Column(name = "daily_budget_usd", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Daily budget USD is required")
    @DecimalMin(value = "0.0", message = "Daily budget USD must be positive")
    private BigDecimal dailyBudgetUsd;
    
    @Column(name = "max_cpc_local", precision = 6, scale = 4)
    @DecimalMin(value = "0.0", message = "Max CPC must be positive")
    private BigDecimal maxCpcLocal;
    
    @Column(name = "max_cpm_local", precision = 6, scale = 4)
    @DecimalMin(value = "0.0", message = "Max CPM must be positive")
    private BigDecimal maxCpmLocal;
    
    // Targeting Preferences
    @Column(name = "preferred_age_groups", length = 100)
    private String preferredAgeGroups; // "18-24,25-34,35-44"
    
    @Column(name = "preferred_genders", length = 20)
    private String preferredGenders; // "male,female,all"
    
    @Column(name = "preferred_interests", columnDefinition = "TEXT[]")
    private String[] preferredInterests; // Array of interests
    
    @Column(name = "preferred_behaviors", columnDefinition = "TEXT[]")
    private String[] preferredBehaviors; // User behaviors
    
    @Column(name = "excluded_audiences", columnDefinition = "TEXT[]")
    private String[] excludedAudiences; // Excluded segments
    
    // Creative Preferences
    @Column(name = "primary_language", nullable = false, length = 10)
    @NotBlank(message = "Primary language is required")
    private String primaryLanguage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ad_tone")
    private AdTone adTone = AdTone.PROFESSIONAL;
    
    @Column(name = "brand_colors", length = 100)
    private String brandColors; // "#E30613,#FFD700,#FFFFFF"
    
    @Column(name = "logo_variant", length = 50)
    private String logoVariant; // "horizontal", "vertical", "icon"
    
    // Scheduling
    @Column(name = "active_hours", length = 100)
    private String activeHours; // "00:00-23:59" or specific hours
    
    @Column(name = "active_days", length = 50)
    private String activeDays; // "monday,tuesday,wednesday,thursday,friday,saturday,sunday"
    
    @Column(name = "timezone_override", length = 50)
    private String timezoneOverride;
    
    // Performance Settings
    @Enumerated(EnumType.STRING)
    @Column(name = "optimization_goal")
    private OptimizationGoal optimizationGoal = OptimizationGoal.CONVERSIONS;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "bid_strategy")
    private BidStrategy bidStrategy = BidStrategy.AUTOMATIC;
    
    // Status
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "auto_optimization")
    private Boolean autoOptimization = true;
    
    // Performance Tracking
    @Column(name = "last_sync")
    private LocalDateTime lastSync;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    private SyncStatus syncStatus = SyncStatus.PENDING;
    
    @Column(name = "error_count")
    private Integer errorCount = 0;
    
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum AdTone {
        CASUAL, PROFESSIONAL, FRIENDLY, AUTHORITATIVE, HUMOROUS
    }
    
    public enum OptimizationGoal {
        REACH, IMPRESSIONS, CLICKS, CONVERSIONS, APP_INSTALLS
    }
    
    public enum BidStrategy {
        AUTOMATIC, MANUAL, TARGET_CPA, TARGET_ROAS
    }
    
    public enum SyncStatus {
        PENDING, SYNCING, SYNCED, FAILED
    }
    
    // Constructors
    public CountryPlatformConfig() {}
    
    public CountryPlatformConfig(Country country, AdPlatform platform,
                               String apiKeyEncrypted, BigDecimal dailyBudgetLocal, 
                               BigDecimal dailyBudgetUsd, String primaryLanguage) {
        this.country = country;
        this.platform = platform;
        this.apiKeyEncrypted = apiKeyEncrypted;
        this.dailyBudgetLocal = dailyBudgetLocal;
        this.dailyBudgetUsd = dailyBudgetUsd;
        this.primaryLanguage = primaryLanguage;
    }
    
    // Business Methods
    
    /**
     * Konfigürasyon aktif ve senkronize durumda mı?
     */
    public boolean isReadyForCampaigns() {
        return isActive != null && isActive && 
               syncStatus == SyncStatus.SYNCED &&
               apiKeyEncrypted != null && !apiKeyEncrypted.trim().isEmpty();
    }
    
    /**
     * Son senkronizasyon başarılı mı?
     */
    public boolean isLastSyncSuccessful() {
        return syncStatus == SyncStatus.SYNCED && 
               (lastError == null || lastError.trim().isEmpty());
    }
    
    /**
     * Hata sayısı kritik seviyede mi? (>10)
     */
    public boolean hasCriticalErrors() {
        return errorCount != null && errorCount >= 10;
    }
    
    /**
     * Tercih edilen yaş grupları listesi
     */
    public String[] getPreferredAgeGroupsArray() {
        if (preferredAgeGroups == null || preferredAgeGroups.trim().isEmpty()) {
            return new String[0];
        }
        return preferredAgeGroups.split(",");
    }
    
    /**
     * Tercih edilen cinsiyetler listesi
     */
    public String[] getPreferredGendersArray() {
        if (preferredGenders == null || preferredGenders.trim().isEmpty()) {
            return new String[]{"all"};
        }
        return preferredGenders.split(",");
    }
    
    /**
     * Aktif günler listesi
     */
    public String[] getActiveDaysArray() {
        if (activeDays == null || activeDays.trim().isEmpty()) {
            return new String[]{"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        }
        return activeDays.split(",");
    }
    
    /**
     * Brand renkleri listesi
     */
    public String[] getBrandColorsArray() {
        if (brandColors == null || brandColors.trim().isEmpty()) {
            return new String[0];
        }
        return brandColors.split(",");
    }
    
    /**
     * Platform spesifik hesap URL'si
     */
    public String getPlatformAccountUrl() {
        if (platform == null || accountId == null) {
            return null;
        }
        
        return switch (platform.getPlatformCode()) {
            case "FB", "IG" -> String.format("https://business.facebook.com/settings/accounts?business_id=%s", accountId);
            case "GOOGLE" -> String.format("https://ads.google.com/aw/overview?aid=%s", accountId);
            case "TT" -> String.format("https://ads.tiktok.com/i18n/account/info?account_id=%s", accountId);
            case "TW" -> String.format("https://ads.twitter.com/accounts/%s", accountId);
            case "LI" -> String.format("https://www.linkedin.com/campaignmanager/accounts/%s", accountId);
            default -> null;
        };
    }
    
    /**
     * Konfigürasyon health skoru (0-100)
     */
    public int getHealthScore() {
        int score = 100;
        
        // API credentials check
        if (apiKeyEncrypted == null || apiKeyEncrypted.trim().isEmpty()) {
            score -= 40;
        }
        
        // Sync status check
        if (syncStatus != SyncStatus.SYNCED) {
            score -= 20;
        }
        
        // Error count penalty
        if (errorCount != null && errorCount > 0) {
            score -= Math.min(errorCount * 2, 20);
        }
        
        // Budget check
        if (dailyBudgetUsd == null || dailyBudgetUsd.compareTo(BigDecimal.ZERO) == 0) {
            score -= 15;
        }
        
        // Activity check
        if (isActive == null || !isActive) {
            score -= 10;
        }
        
        return Math.max(score, 0);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
    
    public AdPlatform getPlatform() { return platform; }
    public void setPlatform(AdPlatform platform) { this.platform = platform; }
    
    public String getApiKeyEncrypted() { return apiKeyEncrypted; }
    public void setApiKeyEncrypted(String apiKeyEncrypted) { this.apiKeyEncrypted = apiKeyEncrypted; }
    
    public String getApiSecretEncrypted() { return apiSecretEncrypted; }
    public void setApiSecretEncrypted(String apiSecretEncrypted) { this.apiSecretEncrypted = apiSecretEncrypted; }
    
    public String getAccessTokenEncrypted() { return accessTokenEncrypted; }
    public void setAccessTokenEncrypted(String accessTokenEncrypted) { this.accessTokenEncrypted = accessTokenEncrypted; }
    
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    
    public BigDecimal getDailyBudgetLocal() { return dailyBudgetLocal; }
    public void setDailyBudgetLocal(BigDecimal dailyBudgetLocal) { this.dailyBudgetLocal = dailyBudgetLocal; }
    
    public BigDecimal getDailyBudgetUsd() { return dailyBudgetUsd; }
    public void setDailyBudgetUsd(BigDecimal dailyBudgetUsd) { this.dailyBudgetUsd = dailyBudgetUsd; }
    
    public BigDecimal getMaxCpcLocal() { return maxCpcLocal; }
    public void setMaxCpcLocal(BigDecimal maxCpcLocal) { this.maxCpcLocal = maxCpcLocal; }
    
    public BigDecimal getMaxCpmLocal() { return maxCpmLocal; }
    public void setMaxCpmLocal(BigDecimal maxCpmLocal) { this.maxCpmLocal = maxCpmLocal; }
    
    public String getPreferredAgeGroups() { return preferredAgeGroups; }
    public void setPreferredAgeGroups(String preferredAgeGroups) { this.preferredAgeGroups = preferredAgeGroups; }
    
    public String getPreferredGenders() { return preferredGenders; }
    public void setPreferredGenders(String preferredGenders) { this.preferredGenders = preferredGenders; }
    
    public String[] getPreferredInterests() { return preferredInterests; }
    public void setPreferredInterests(String[] preferredInterests) { this.preferredInterests = preferredInterests; }
    
    public String[] getPreferredBehaviors() { return preferredBehaviors; }
    public void setPreferredBehaviors(String[] preferredBehaviors) { this.preferredBehaviors = preferredBehaviors; }
    
    public String[] getExcludedAudiences() { return excludedAudiences; }
    public void setExcludedAudiences(String[] excludedAudiences) { this.excludedAudiences = excludedAudiences; }
    
    public String getPrimaryLanguage() { return primaryLanguage; }
    public void setPrimaryLanguage(String primaryLanguage) { this.primaryLanguage = primaryLanguage; }
    
    public AdTone getAdTone() { return adTone; }
    public void setAdTone(AdTone adTone) { this.adTone = adTone; }
    
    public String getBrandColors() { return brandColors; }
    public void setBrandColors(String brandColors) { this.brandColors = brandColors; }
    
    public String getLogoVariant() { return logoVariant; }
    public void setLogoVariant(String logoVariant) { this.logoVariant = logoVariant; }
    
    public String getActiveHours() { return activeHours; }
    public void setActiveHours(String activeHours) { this.activeHours = activeHours; }
    
    public String getActiveDays() { return activeDays; }
    public void setActiveDays(String activeDays) { this.activeDays = activeDays; }
    
    public String getTimezoneOverride() { return timezoneOverride; }
    public void setTimezoneOverride(String timezoneOverride) { this.timezoneOverride = timezoneOverride; }
    
    public OptimizationGoal getOptimizationGoal() { return optimizationGoal; }
    public void setOptimizationGoal(OptimizationGoal optimizationGoal) { this.optimizationGoal = optimizationGoal; }
    
    public BidStrategy getBidStrategy() { return bidStrategy; }
    public void setBidStrategy(BidStrategy bidStrategy) { this.bidStrategy = bidStrategy; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getAutoOptimization() { return autoOptimization; }
    public void setAutoOptimization(Boolean autoOptimization) { this.autoOptimization = autoOptimization; }
    
    public LocalDateTime getLastSync() { return lastSync; }
    public void setLastSync(LocalDateTime lastSync) { this.lastSync = lastSync; }
    
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
    
    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return String.format("CountryPlatformConfig{id=%d, country=%s, platform=%s, budget=%.2f USD, status=%s}",
                id, 
                country != null ? country.getCountryCode() : "N/A",
                platform != null ? platform.getPlatformCode() : "N/A",
                dailyBudgetUsd, syncStatus);
    }
}