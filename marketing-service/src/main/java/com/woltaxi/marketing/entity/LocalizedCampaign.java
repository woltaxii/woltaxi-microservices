package com.woltaxi.marketing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Yerelleştirilmiş Reklam Kampanyaları Entity
 * 
 * Global kampanyaların belirli ülke ve platform için
 * yerelleştirilmiş versiyonları. Her LocalizedCampaign
 * bir ülke + platform kombinasyonunu temsil eder.
 */
@Entity
@Table(name = "localized_campaigns", indexes = {
    @Index(name = "idx_localized_campaigns_global", columnList = "global_campaign_id"),
    @Index(name = "idx_localized_campaigns_country", columnList = "country_id"),
    @Index(name = "idx_localized_campaigns_platform", columnList = "platform_id"),
    @Index(name = "idx_localized_campaigns_status", columnList = "status")
})
public class LocalizedCampaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "campaign_uuid", nullable = false, unique = true, updatable = false)
    private UUID campaignUuid = UUID.randomUUID();
    
    // References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_campaign_id", nullable = false)
    private GlobalCampaign globalCampaign;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private AdPlatform platform;
    
    // Localized Content
    @Column(name = "localized_headline", nullable = false, length = 200)
    @NotBlank(message = "Localized headline is required")
    @Size(max = 200, message = "Localized headline too long")
    private String localizedHeadline;
    
    @Column(name = "localized_description", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Localized description is required")
    private String localizedDescription;
    
    @Column(name = "localized_cta", nullable = false, length = 50)
    @NotBlank(message = "Localized CTA is required")
    @Size(max = 50, message = "Localized CTA too long")
    private String localizedCta;
    
    @Column(name = "localized_landing_url", nullable = false, length = 500)
    @NotBlank(message = "Localized landing URL is required")
    @Size(max = 500, message = "Localized landing URL too long")
    private String localizedLandingUrl;
    
    // Local Media
    @Column(name = "localized_image_url", length = 500)
    private String localizedImageUrl;
    
    @Column(name = "localized_video_url", length = 500)
    private String localizedVideoUrl;
    
    @Column(name = "cultural_adaptations", columnDefinition = "TEXT")
    private String culturalAdaptations; // JSON of cultural modifications
    
    // Local Budget & Targeting
    @Column(name = "local_budget", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Local budget is required")
    @DecimalMin(value = "0.0", message = "Local budget must be positive")
    private BigDecimal localBudget;
    
    @Column(name = "local_currency", nullable = false, length = 3)
    @NotBlank(message = "Local currency is required")
    private String localCurrency;
    
    @Column(name = "budget_usd_equivalent", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "USD equivalent budget is required")
    @DecimalMin(value = "0.0", message = "USD equivalent budget must be positive")
    private BigDecimal budgetUsdEquivalent;
    
    // Platform Specific IDs
    @Column(name = "platform_campaign_id", length = 100)
    private String platformCampaignId; // ID from the ad platform
    
    @Column(name = "platform_adset_id", length = 100)
    private String platformAdsetId;
    
    @Column(name = "platform_ad_id", length = 100)
    private String platformAdId;
    
    // Local Performance
    @Column(name = "impressions")
    private Long impressions = 0L;
    
    @Column(name = "clicks")
    private Long clicks = 0L;
    
    @Column(name = "conversions")
    private Long conversions = 0L;
    
    @Column(name = "spend_local", precision = 10, scale = 2)
    private BigDecimal spendLocal = BigDecimal.ZERO;
    
    @Column(name = "spend_usd", precision = 10, scale = 2)
    private BigDecimal spendUsd = BigDecimal.ZERO;
    
    // Calculated Metrics
    @Column(name = "ctr", precision = 5, scale = 4)
    private BigDecimal ctr = BigDecimal.ZERO; // Click Through Rate
    
    @Column(name = "cpc_local", precision = 8, scale = 4)
    private BigDecimal cpcLocal = BigDecimal.ZERO; // Cost Per Click
    
    @Column(name = "cpm_local", precision = 8, scale = 4)
    private BigDecimal cpmLocal = BigDecimal.ZERO; // Cost Per Mille
    
    @Column(name = "conversion_rate", precision = 5, scale = 4)
    private BigDecimal conversionRate = BigDecimal.ZERO;
    
    @Column(name = "roas", precision = 8, scale = 4)
    private BigDecimal roas = BigDecimal.ZERO; // Return on Ad Spend
    
    // Status & Sync
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CampaignStatus status = CampaignStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    private SyncStatus syncStatus = SyncStatus.PENDING;
    
    @Column(name = "last_synced")
    private LocalDateTime lastSynced;
    
    @Column(name = "sync_errors", columnDefinition = "TEXT")
    private String syncErrors;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "launched_at")
    private LocalDateTime launchedAt;
    
    // Enums
    public enum CampaignStatus {
        PENDING, ACTIVE, PAUSED, COMPLETED, FAILED
    }
    
    public enum SyncStatus {
        PENDING, SYNCING, SYNCED, FAILED
    }
    
    // Constructors
    public LocalizedCampaign() {}
    
    public LocalizedCampaign(GlobalCampaign globalCampaign, Country country, AdPlatform platform,
                           BigDecimal localBudget, String localCurrency, BigDecimal budgetUsdEquivalent) {
        this.globalCampaign = globalCampaign;
        this.country = country;
        this.platform = platform;
        this.localBudget = localBudget;
        this.localCurrency = localCurrency;
        this.budgetUsdEquivalent = budgetUsdEquivalent;
        
        // Initialize localized content from global campaign
        if (globalCampaign != null) {
            this.localizedHeadline = globalCampaign.getPrimaryHeadline();
            this.localizedDescription = globalCampaign.getPrimaryDescription();
            this.localizedCta = globalCampaign.getCallToAction();
            this.localizedLandingUrl = globalCampaign.getLandingPageUrl();
        }
    }
    
    // Business Methods
    
    /**
     * Kampanya aktif durumda mı?
     */
    public boolean isActive() {
        return status == CampaignStatus.ACTIVE;
    }
    
    /**
     * Platform ile senkronize durumda mı?
     */
    public boolean isSynced() {
        return syncStatus == SyncStatus.SYNCED && 
               platformCampaignId != null && 
               !platformCampaignId.trim().isEmpty();
    }
    
    /**
     * Performans metrikleri hesapla
     */
    public void calculateMetrics() {
        // CTR (Click Through Rate)
        if (impressions != null && impressions > 0 && clicks != null) {
            ctr = BigDecimal.valueOf(clicks)
                    .divide(BigDecimal.valueOf(impressions), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            ctr = BigDecimal.ZERO;
        }
        
        // CPC (Cost Per Click)
        if (clicks != null && clicks > 0 && spendLocal != null) {
            cpcLocal = spendLocal.divide(BigDecimal.valueOf(clicks), 4, BigDecimal.ROUND_HALF_UP);
        } else {
            cpcLocal = BigDecimal.ZERO;
        }
        
        // CPM (Cost Per Mille)
        if (impressions != null && impressions > 0 && spendLocal != null) {
            cpmLocal = spendLocal
                    .multiply(BigDecimal.valueOf(1000))
                    .divide(BigDecimal.valueOf(impressions), 4, BigDecimal.ROUND_HALF_UP);
        } else {
            cpmLocal = BigDecimal.ZERO;
        }
        
        // Conversion Rate
        if (clicks != null && clicks > 0 && conversions != null) {
            conversionRate = BigDecimal.valueOf(conversions)
                    .divide(BigDecimal.valueOf(clicks), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            conversionRate = BigDecimal.ZERO;
        }
        
        // ROAS would require revenue data, for now set to 0
        roas = BigDecimal.ZERO;
    }
    
    /**
     * Kampanya performansı iyi mi? (CTR >= 1.0%)
     */
    public boolean hasGoodPerformance() {
        return ctr != null && ctr.compareTo(BigDecimal.valueOf(1.0)) >= 0;
    }
    
    /**
     * Bütçe kullanım oranı (%)
     */
    public BigDecimal getBudgetUtilizationRate() {
        if (localBudget == null || localBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        if (spendLocal == null) {
            return BigDecimal.ZERO;
        }
        
        return spendLocal.divide(localBudget, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Platform spesifik kampanya URL'si
     */
    public String getPlatformCampaignUrl() {
        if (platform == null || platformCampaignId == null) {
            return null;
        }
        
        return switch (platform.getPlatformCode()) {
            case "FB", "IG" -> String.format("https://business.facebook.com/adsmanager/manage/campaigns/detail?campaign_id=%s", platformCampaignId);
            case "GOOGLE" -> String.format("https://ads.google.com/aw/campaigns?campaignId=%s", platformCampaignId);
            case "TT" -> String.format("https://ads.tiktok.com/i18n/campaign/detail/%s", platformCampaignId);
            case "TW" -> String.format("https://ads.twitter.com/accounts/%s/campaigns/%s", platformAdsetId, platformCampaignId);
            default -> null;
        };
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getCampaignUuid() { return campaignUuid; }
    public void setCampaignUuid(UUID campaignUuid) { this.campaignUuid = campaignUuid; }
    
    public GlobalCampaign getGlobalCampaign() { return globalCampaign; }
    public void setGlobalCampaign(GlobalCampaign globalCampaign) { this.globalCampaign = globalCampaign; }
    
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
    
    public AdPlatform getPlatform() { return platform; }
    public void setPlatform(AdPlatform platform) { this.platform = platform; }
    
    public String getLocalizedHeadline() { return localizedHeadline; }
    public void setLocalizedHeadline(String localizedHeadline) { this.localizedHeadline = localizedHeadline; }
    
    public String getLocalizedDescription() { return localizedDescription; }
    public void setLocalizedDescription(String localizedDescription) { this.localizedDescription = localizedDescription; }
    
    public String getLocalizedCta() { return localizedCta; }
    public void setLocalizedCta(String localizedCta) { this.localizedCta = localizedCta; }
    
    public String getLocalizedLandingUrl() { return localizedLandingUrl; }
    public void setLocalizedLandingUrl(String localizedLandingUrl) { this.localizedLandingUrl = localizedLandingUrl; }
    
    public String getLocalizedImageUrl() { return localizedImageUrl; }
    public void setLocalizedImageUrl(String localizedImageUrl) { this.localizedImageUrl = localizedImageUrl; }
    
    public String getLocalizedVideoUrl() { return localizedVideoUrl; }
    public void setLocalizedVideoUrl(String localizedVideoUrl) { this.localizedVideoUrl = localizedVideoUrl; }
    
    public String getCulturalAdaptations() { return culturalAdaptations; }
    public void setCulturalAdaptations(String culturalAdaptations) { this.culturalAdaptations = culturalAdaptations; }
    
    public BigDecimal getLocalBudget() { return localBudget; }
    public void setLocalBudget(BigDecimal localBudget) { this.localBudget = localBudget; }
    
    public String getLocalCurrency() { return localCurrency; }
    public void setLocalCurrency(String localCurrency) { this.localCurrency = localCurrency; }
    
    public BigDecimal getBudgetUsdEquivalent() { return budgetUsdEquivalent; }
    public void setBudgetUsdEquivalent(BigDecimal budgetUsdEquivalent) { this.budgetUsdEquivalent = budgetUsdEquivalent; }
    
    public String getPlatformCampaignId() { return platformCampaignId; }
    public void setPlatformCampaignId(String platformCampaignId) { this.platformCampaignId = platformCampaignId; }
    
    public String getPlatformAdsetId() { return platformAdsetId; }
    public void setPlatformAdsetId(String platformAdsetId) { this.platformAdsetId = platformAdsetId; }
    
    public String getPlatformAdId() { return platformAdId; }
    public void setPlatformAdId(String platformAdId) { this.platformAdId = platformAdId; }
    
    public Long getImpressions() { return impressions; }
    public void setImpressions(Long impressions) { this.impressions = impressions; }
    
    public Long getClicks() { return clicks; }
    public void setClicks(Long clicks) { this.clicks = clicks; }
    
    public Long getConversions() { return conversions; }
    public void setConversions(Long conversions) { this.conversions = conversions; }
    
    public BigDecimal getSpendLocal() { return spendLocal; }
    public void setSpendLocal(BigDecimal spendLocal) { this.spendLocal = spendLocal; }
    
    public BigDecimal getSpendUsd() { return spendUsd; }
    public void setSpendUsd(BigDecimal spendUsd) { this.spendUsd = spendUsd; }
    
    public BigDecimal getCtr() { return ctr; }
    public void setCtr(BigDecimal ctr) { this.ctr = ctr; }
    
    public BigDecimal getCpcLocal() { return cpcLocal; }
    public void setCpcLocal(BigDecimal cpcLocal) { this.cpcLocal = cpcLocal; }
    
    public BigDecimal getCpmLocal() { return cpmLocal; }
    public void setCpmLocal(BigDecimal cpmLocal) { this.cpmLocal = cpmLocal; }
    
    public BigDecimal getConversionRate() { return conversionRate; }
    public void setConversionRate(BigDecimal conversionRate) { this.conversionRate = conversionRate; }
    
    public BigDecimal getRoas() { return roas; }
    public void setRoas(BigDecimal roas) { this.roas = roas; }
    
    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }
    
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
    
    public LocalDateTime getLastSynced() { return lastSynced; }
    public void setLastSynced(LocalDateTime lastSynced) { this.lastSynced = lastSynced; }
    
    public String getSyncErrors() { return syncErrors; }
    public void setSyncErrors(String syncErrors) { this.syncErrors = syncErrors; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLaunchedAt() { return launchedAt; }
    public void setLaunchedAt(LocalDateTime launchedAt) { this.launchedAt = launchedAt; }
    
    @Override
    public String toString() {
        return String.format("LocalizedCampaign{id=%d, uuid=%s, country=%s, platform=%s, status=%s, budget=%.2f %s}",
                id, campaignUuid, 
                country != null ? country.getCountryCode() : "N/A",
                platform != null ? platform.getPlatformCode() : "N/A",
                status, localBudget, localCurrency);
    }
}