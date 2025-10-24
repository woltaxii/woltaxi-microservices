package com.woltaxi.marketing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * Reklam Platformları Entity
 * 
 * Facebook, Google, TikTok, Twitter gibi reklam platformlarının
 * API bilgileri, özellikler ve global ayarları
 */
@Entity
@Table(name = "ad_platforms")
public class AdPlatform {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "platform_name", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Platform name is required")
    @Size(max = 50, message = "Platform name too long")
    private String platformName;
    
    @Column(name = "platform_code", nullable = false, unique = true, length = 20)
    @NotBlank(message = "Platform code is required")
    @Size(max = 20, message = "Platform code too long")
    private String platformCode; // FB, IG, GOOGLE, TT, TW, LI
    
    // Platform Details
    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type", nullable = false)
    private PlatformType platformType;
    
    @Column(name = "api_endpoint", nullable = false, length = 200)
    @NotBlank(message = "API endpoint is required")
    @Size(max = 200, message = "API endpoint too long")
    private String apiEndpoint;
    
    @Column(name = "api_version", length = 10)
    private String apiVersion;
    
    // Features
    @Column(name = "supports_video")
    private Boolean supportsVideo = false;
    
    @Column(name = "supports_carousel")
    private Boolean supportsCarousel = false;
    
    @Column(name = "supports_stories")
    private Boolean supportsStories = false;
    
    @Column(name = "supports_retargeting")
    private Boolean supportsRetargeting = false;
    
    @Column(name = "supports_lookalike")
    private Boolean supportsLookalike = false;
    
    @Column(name = "supports_geotargeting")
    private Boolean supportsGeotargeting = false;
    
    // Pricing Model
    @Column(name = "pricing_models")
    private String[] pricingModels; // CPC, CPM, CPA, CPV
    
    @Column(name = "min_daily_budget_usd", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Minimum daily budget must be positive")
    private BigDecimal minDailyBudgetUsd = BigDecimal.valueOf(5.00);
    
    @Column(name = "avg_cpc_usd", precision = 6, scale = 4)
    private BigDecimal avgCpcUsd; // Average Cost Per Click
    
    @Column(name = "avg_cpm_usd", precision = 6, scale = 4)
    private BigDecimal avgCpmUsd; // Average Cost Per Mille
    
    // Global Availability
    @Column(name = "available_countries")
    private String[] availableCountries; // Country codes where platform is available
    
    @Column(name = "restricted_countries")
    private String[] restrictedCountries; // Restricted countries
    
    // Status
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "integration_status")
    private IntegrationStatus integrationStatus = IntegrationStatus.PENDING;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "platform", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CountryPlatformConfig> countryConfigs;
    
    @OneToMany(mappedBy = "platform", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LocalizedCampaign> localizedCampaigns;
    
    // Enums
    public enum PlatformType {
        SOCIAL_MEDIA, SEARCH_ENGINE, VIDEO, DISPLAY, MOBILE
    }
    
    public enum IntegrationStatus {
        PENDING, ACTIVE, SUSPENDED, DEPRECATED
    }
    
    // Constructors
    public AdPlatform() {}
    
    public AdPlatform(String platformName, String platformCode, PlatformType platformType, String apiEndpoint) {
        this.platformName = platformName;
        this.platformCode = platformCode;
        this.platformType = platformType;
        this.apiEndpoint = apiEndpoint;
    }
    
    // Business Methods
    
    /**
     * Platform belirtilen ülkede kullanılabilir mi?
     */
    public boolean isAvailableInCountry(String countryCode) {
        if (availableCountries == null || availableCountries.length == 0) {
            return true; // If no restrictions, available everywhere
        }
        
        // Check if country is in restricted list
        if (restrictedCountries != null) {
            for (String restricted : restrictedCountries) {
                if (restricted.equals(countryCode)) {
                    return false;
                }
            }
        }
        
        // Check if country is in available list
        for (String available : availableCountries) {
            if (available.equals(countryCode)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Platform belirtilen pricing model'i destekliyor mu?
     */
    public boolean supportsPricingModel(String pricingModel) {
        if (pricingModels == null) {
            return false;
        }
        
        for (String model : pricingModels) {
            if (model.equalsIgnoreCase(pricingModel)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Platform aktif ve entegre durumda mı?
     */
    public boolean isReadyForCampaigns() {
        return isActive != null && isActive && 
               integrationStatus == IntegrationStatus.ACTIVE;
    }
    
    /**
     * Platform'un desteklediği özellik sayısı
     */
    public int getSupportedFeaturesCount() {
        int count = 0;
        if (Boolean.TRUE.equals(supportsVideo)) count++;
        if (Boolean.TRUE.equals(supportsCarousel)) count++;
        if (Boolean.TRUE.equals(supportsStories)) count++;
        if (Boolean.TRUE.equals(supportsRetargeting)) count++;
        if (Boolean.TRUE.equals(supportsLookalike)) count++;
        if (Boolean.TRUE.equals(supportsGeotargeting)) count++;
        return count;
    }
    
    /**
     * Platform capability skoru (0-10)
     */
    public int getCapabilityScore() {
        int score = 0;
        
        // Feature support (max 6 points)
        score += getSupportedFeaturesCount();
        
        // Pricing model variety (max 2 points)
        if (pricingModels != null) {
            score += Math.min(pricingModels.length / 2, 2);
        }
        
        // Global availability (max 2 points)
        if (availableCountries != null && availableCountries.length > 20) {
            score += 2;
        } else if (availableCountries != null && availableCountries.length > 10) {
            score += 1;
        }
        
        return Math.min(score, 10);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }
    
    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }
    
    public PlatformType getPlatformType() { return platformType; }
    public void setPlatformType(PlatformType platformType) { this.platformType = platformType; }
    
    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
    
    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }
    
    public Boolean getSupportsVideo() { return supportsVideo; }
    public void setSupportsVideo(Boolean supportsVideo) { this.supportsVideo = supportsVideo; }
    
    public Boolean getSupportsCarousel() { return supportsCarousel; }
    public void setSupportsCarousel(Boolean supportsCarousel) { this.supportsCarousel = supportsCarousel; }
    
    public Boolean getSupportsStories() { return supportsStories; }
    public void setSupportsStories(Boolean supportsStories) { this.supportsStories = supportsStories; }
    
    public Boolean getSupportsRetargeting() { return supportsRetargeting; }
    public void setSupportsRetargeting(Boolean supportsRetargeting) { this.supportsRetargeting = supportsRetargeting; }
    
    public Boolean getSupportsLookalike() { return supportsLookalike; }
    public void setSupportsLookalike(Boolean supportsLookalike) { this.supportsLookalike = supportsLookalike; }
    
    public Boolean getSupportsGeotargeting() { return supportsGeotargeting; }
    public void setSupportsGeotargeting(Boolean supportsGeotargeting) { this.supportsGeotargeting = supportsGeotargeting; }
    
    public String[] getPricingModels() { return pricingModels; }
    public void setPricingModels(String[] pricingModels) { this.pricingModels = pricingModels; }
    
    public BigDecimal getMinDailyBudgetUsd() { return minDailyBudgetUsd; }
    public void setMinDailyBudgetUsd(BigDecimal minDailyBudgetUsd) { this.minDailyBudgetUsd = minDailyBudgetUsd; }
    
    public BigDecimal getAvgCpcUsd() { return avgCpcUsd; }
    public void setAvgCpcUsd(BigDecimal avgCpcUsd) { this.avgCpcUsd = avgCpcUsd; }
    
    public BigDecimal getAvgCpmUsd() { return avgCpmUsd; }
    public void setAvgCpmUsd(BigDecimal avgCpmUsd) { this.avgCpmUsd = avgCpmUsd; }
    
    public String[] getAvailableCountries() { return availableCountries; }
    public void setAvailableCountries(String[] availableCountries) { this.availableCountries = availableCountries; }
    
    public String[] getRestrictedCountries() { return restrictedCountries; }
    public void setRestrictedCountries(String[] restrictedCountries) { this.restrictedCountries = restrictedCountries; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public IntegrationStatus getIntegrationStatus() { return integrationStatus; }
    public void setIntegrationStatus(IntegrationStatus integrationStatus) { this.integrationStatus = integrationStatus; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<CountryPlatformConfig> getCountryConfigs() { return countryConfigs; }
    public void setCountryConfigs(List<CountryPlatformConfig> countryConfigs) { this.countryConfigs = countryConfigs; }
    
    public List<LocalizedCampaign> getLocalizedCampaigns() { return localizedCampaigns; }
    public void setLocalizedCampaigns(List<LocalizedCampaign> localizedCampaigns) { this.localizedCampaigns = localizedCampaigns; }
    
    @Override
    public String toString() {
        return String.format("AdPlatform{id=%d, name='%s', code='%s', type=%s, status=%s}",
                id, platformName, platformCode, platformType, integrationStatus);
    }
}