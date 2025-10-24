package com.woltaxi.marketing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

/**
 * Küresel Reklam Kampanyaları Entity
 * 
 * Tüm ülkelerde eş zamanlı çalışacak ana kampanya bilgileri.
 * Her global kampanya birden fazla ülke ve platform için 
 * yerelleştirilmiş kampanyalara dönüştürülür.
 */
@Entity
@Table(name = "global_campaigns")
public class GlobalCampaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "campaign_uuid", nullable = false, unique = true, updatable = false)
    private UUID campaignUuid = UUID.randomUUID();
    
    // Campaign Basics
    @Column(name = "campaign_name", nullable = false, length = 200)
    @NotBlank(message = "Campaign name is required")
    @Size(max = 200, message = "Campaign name too long")
    private String campaignName;
    
    @Column(name = "campaign_description", columnDefinition = "TEXT")
    private String campaignDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false)
    private CampaignType campaignType;
    
    // Global Settings
    @Column(name = "master_language", length = 10)
    private String masterLanguage = "en";
    
    @Column(name = "auto_translate")
    private Boolean autoTranslate = true;
    
    @Column(name = "auto_localize")
    private Boolean autoLocalize = true;
    
    // Targeting
    @Column(name = "target_countries")
    private String[] targetCountries; // Countries to target
    
    @Column(name = "exclude_countries")
    private String[] excludeCountries; // Countries to exclude
    
    @Column(name = "target_platforms")
    private String[] targetPlatforms; // Platform codes to use
    
    // Budget & Timing
    @Column(name = "total_budget_usd", precision = 12, scale = 2, nullable = false)
    @NotNull(message = "Total budget is required")
    @DecimalMin(value = "0.0", message = "Total budget must be positive")
    private BigDecimal totalBudgetUsd;
    
    @Column(name = "daily_budget_usd", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Daily budget is required")
    @DecimalMin(value = "0.0", message = "Daily budget must be positive")
    private BigDecimal dailyBudgetUsd;
    
    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    // Creative Assets
    @Column(name = "primary_headline", nullable = false, length = 200)
    @NotBlank(message = "Primary headline is required")
    @Size(max = 200, message = "Primary headline too long")
    private String primaryHeadline;
    
    @Column(name = "primary_description", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Primary description is required")
    private String primaryDescription;
    
    @Column(name = "call_to_action", nullable = false, length = 50)
    @NotBlank(message = "Call to action is required")
    @Size(max = 50, message = "Call to action too long")
    private String callToAction;
    
    @Column(name = "landing_page_url", nullable = false, length = 500)
    @NotBlank(message = "Landing page URL is required")
    @Size(max = 500, message = "Landing page URL too long")
    private String landingPageUrl;
    
    // Media Assets
    @Column(name = "primary_image_url", length = 500)
    private String primaryImageUrl;
    
    @Column(name = "primary_video_url", length = 500)
    private String primaryVideoUrl;
    
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @Column(name = "additional_assets")
    private String[] additionalAssets; // Array of asset URLs
    
    // Advanced Settings
    @Column(name = "frequency_cap")
    @Min(value = 1, message = "Frequency cap must be at least 1")
    @Max(value = 10, message = "Frequency cap cannot exceed 10")
    private Integer frequencyCap = 3; // Max impressions per user per day
    
    @Column(name = "audience_network")
    private Boolean audienceNetwork = true;
    
    @Column(name = "instagram_placement")
    private Boolean instagramPlacement = true;
    
    @Column(name = "facebook_placement")
    private Boolean facebookPlacement = true;
    
    @Column(name = "stories_placement")
    private Boolean storiesPlacement = true;
    
    // AI & Optimization
    @Column(name = "use_ai_optimization")
    private Boolean useAiOptimization = true;
    
    @Column(name = "auto_budget_reallocation")
    private Boolean autoBudgetReallocation = true;
    
    @Column(name = "auto_pause_underperforming")
    private Boolean autoPauseUnderperforming = true;
    
    @Column(name = "performance_threshold", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Performance threshold must be positive")
    private BigDecimal performanceThreshold = BigDecimal.valueOf(2.0); // Min CTR %
    
    // Status & Control
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CampaignStatus status = CampaignStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    
    @Column(name = "created_by")
    private Long createdBy; // User ID
    
    @Column(name = "approved_by")
    private Long approvedBy; // User ID
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "launched_at")
    private LocalDateTime launchedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Relationships
    @OneToMany(mappedBy = "globalCampaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LocalizedCampaign> localizedCampaigns;
    
    // Enums
    public enum CampaignType {
        BRAND_AWARENESS, APP_INSTALL, LEAD_GENERATION, CONVERSION, ENGAGEMENT, TRAFFIC
    }
    
    public enum CampaignStatus {
        DRAFT, PENDING_APPROVAL, ACTIVE, PAUSED, COMPLETED, CANCELLED
    }
    
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
    
    // Constructors
    public GlobalCampaign() {}
    
    public GlobalCampaign(String campaignName, CampaignType campaignType, 
                         BigDecimal totalBudgetUsd, BigDecimal dailyBudgetUsd,
                         LocalDateTime startDate, String primaryHeadline, 
                         String primaryDescription, String callToAction, 
                         String landingPageUrl) {
        this.campaignName = campaignName;
        this.campaignType = campaignType;
        this.totalBudgetUsd = totalBudgetUsd;
        this.dailyBudgetUsd = dailyBudgetUsd;
        this.startDate = startDate;
        this.primaryHeadline = primaryHeadline;
        this.primaryDescription = primaryDescription;
        this.callToAction = callToAction;
        this.landingPageUrl = landingPageUrl;
    }
    
    // Business Methods
    
    /**
     * Kampanya aktif durumda mı?
     */
    public boolean isActive() {
        return status == CampaignStatus.ACTIVE && 
               approvalStatus == ApprovalStatus.APPROVED;
    }
    
    /**
     * Kampanya başlamış mı?
     */
    public boolean hasStarted() {
        return startDate != null && LocalDateTime.now().isAfter(startDate);
    }
    
    /**
     * Kampanya bitmiş mı?
     */
    public boolean hasEnded() {
        return endDate != null && LocalDateTime.now().isAfter(endDate);
    }
    
    /**
     * Kampanyanın kalan günü
     */
    public long getRemainingDays() {
        if (endDate == null) {
            return Long.MAX_VALUE; // Unlimited
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDate)) {
            return 0;
        }
        
        return java.time.Duration.between(now, endDate).toDays();
    }
    
    /**
     * Belirtilen ülke hedefleniyor mu?
     */
    public boolean isTargetingCountry(String countryCode) {
        // Check exclude list first
        if (excludeCountries != null) {
            for (String excluded : excludeCountries) {
                if (excluded.equals(countryCode)) {
                    return false;
                }
            }
        }
        
        // If no target countries specified, target all (except excluded)
        if (targetCountries == null || targetCountries.length == 0) {
            return true;
        }
        
        // Check if country is in target list
        for (String target : targetCountries) {
            if (target.equals(countryCode)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Belirtilen platform kullanılıyor mu?
     */
    public boolean isUsingPlatform(String platformCode) {
        if (targetPlatforms == null || targetPlatforms.length == 0) {
            return true; // Use all platforms if none specified
        }
        
        for (String platform : targetPlatforms) {
            if (platform.equals(platformCode)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Kampanyanın toplam yerelleştirilmiş kampanya sayısı
     */
    public int getLocalizedCampaignCount() {
        return localizedCampaigns != null ? localizedCampaigns.size() : 0;
    }
    
    /**
     * Kampanyanın aktif yerelleştirilmiş kampanya sayısı
     */
    public long getActiveLocalizedCampaignCount() {
        if (localizedCampaigns == null) {
            return 0;
        }
        
        return localizedCampaigns.stream()
                .filter(campaign -> campaign.getStatus() == LocalizedCampaign.CampaignStatus.ACTIVE)
                .count();
    }
    
    /**
     * Günlük bütçe dağılımı (ülke sayısına göre)
     */
    public BigDecimal getDailyBudgetPerCountry() {
        int targetCountryCount = targetCountries != null ? targetCountries.length : 1;
        if (targetCountryCount == 0) targetCountryCount = 1;
        
        return dailyBudgetUsd.divide(BigDecimal.valueOf(targetCountryCount), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getCampaignUuid() { return campaignUuid; }
    public void setCampaignUuid(UUID campaignUuid) { this.campaignUuid = campaignUuid; }
    
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    
    public String getCampaignDescription() { return campaignDescription; }
    public void setCampaignDescription(String campaignDescription) { this.campaignDescription = campaignDescription; }
    
    public CampaignType getCampaignType() { return campaignType; }
    public void setCampaignType(CampaignType campaignType) { this.campaignType = campaignType; }
    
    public String getMasterLanguage() { return masterLanguage; }
    public void setMasterLanguage(String masterLanguage) { this.masterLanguage = masterLanguage; }
    
    public Boolean getAutoTranslate() { return autoTranslate; }
    public void setAutoTranslate(Boolean autoTranslate) { this.autoTranslate = autoTranslate; }
    
    public Boolean getAutoLocalize() { return autoLocalize; }
    public void setAutoLocalize(Boolean autoLocalize) { this.autoLocalize = autoLocalize; }
    
    public String[] getTargetCountries() { return targetCountries; }
    public void setTargetCountries(String[] targetCountries) { this.targetCountries = targetCountries; }
    
    public String[] getExcludeCountries() { return excludeCountries; }
    public void setExcludeCountries(String[] excludeCountries) { this.excludeCountries = excludeCountries; }
    
    public String[] getTargetPlatforms() { return targetPlatforms; }
    public void setTargetPlatforms(String[] targetPlatforms) { this.targetPlatforms = targetPlatforms; }
    
    public BigDecimal getTotalBudgetUsd() { return totalBudgetUsd; }
    public void setTotalBudgetUsd(BigDecimal totalBudgetUsd) { this.totalBudgetUsd = totalBudgetUsd; }
    
    public BigDecimal getDailyBudgetUsd() { return dailyBudgetUsd; }
    public void setDailyBudgetUsd(BigDecimal dailyBudgetUsd) { this.dailyBudgetUsd = dailyBudgetUsd; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getPrimaryHeadline() { return primaryHeadline; }
    public void setPrimaryHeadline(String primaryHeadline) { this.primaryHeadline = primaryHeadline; }
    
    public String getPrimaryDescription() { return primaryDescription; }
    public void setPrimaryDescription(String primaryDescription) { this.primaryDescription = primaryDescription; }
    
    public String getCallToAction() { return callToAction; }
    public void setCallToAction(String callToAction) { this.callToAction = callToAction; }
    
    public String getLandingPageUrl() { return landingPageUrl; }
    public void setLandingPageUrl(String landingPageUrl) { this.landingPageUrl = landingPageUrl; }
    
    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public void setPrimaryImageUrl(String primaryImageUrl) { this.primaryImageUrl = primaryImageUrl; }
    
    public String getPrimaryVideoUrl() { return primaryVideoUrl; }
    public void setPrimaryVideoUrl(String primaryVideoUrl) { this.primaryVideoUrl = primaryVideoUrl; }
    
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    
    public String[] getAdditionalAssets() { return additionalAssets; }
    public void setAdditionalAssets(String[] additionalAssets) { this.additionalAssets = additionalAssets; }
    
    public Integer getFrequencyCap() { return frequencyCap; }
    public void setFrequencyCap(Integer frequencyCap) { this.frequencyCap = frequencyCap; }
    
    public Boolean getAudienceNetwork() { return audienceNetwork; }
    public void setAudienceNetwork(Boolean audienceNetwork) { this.audienceNetwork = audienceNetwork; }
    
    public Boolean getInstagramPlacement() { return instagramPlacement; }
    public void setInstagramPlacement(Boolean instagramPlacement) { this.instagramPlacement = instagramPlacement; }
    
    public Boolean getFacebookPlacement() { return facebookPlacement; }
    public void setFacebookPlacement(Boolean facebookPlacement) { this.facebookPlacement = facebookPlacement; }
    
    public Boolean getStoriesPlacement() { return storiesPlacement; }
    public void setStoriesPlacement(Boolean storiesPlacement) { this.storiesPlacement = storiesPlacement; }
    
    public Boolean getUseAiOptimization() { return useAiOptimization; }
    public void setUseAiOptimization(Boolean useAiOptimization) { this.useAiOptimization = useAiOptimization; }
    
    public Boolean getAutoBudgetReallocation() { return autoBudgetReallocation; }
    public void setAutoBudgetReallocation(Boolean autoBudgetReallocation) { this.autoBudgetReallocation = autoBudgetReallocation; }
    
    public Boolean getAutoPauseUnderperforming() { return autoPauseUnderperforming; }
    public void setAutoPauseUnderperforming(Boolean autoPauseUnderperforming) { this.autoPauseUnderperforming = autoPauseUnderperforming; }
    
    public BigDecimal getPerformanceThreshold() { return performanceThreshold; }
    public void setPerformanceThreshold(BigDecimal performanceThreshold) { this.performanceThreshold = performanceThreshold; }
    
    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }
    
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLaunchedAt() { return launchedAt; }
    public void setLaunchedAt(LocalDateTime launchedAt) { this.launchedAt = launchedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public List<LocalizedCampaign> getLocalizedCampaigns() { return localizedCampaigns; }
    public void setLocalizedCampaigns(List<LocalizedCampaign> localizedCampaigns) { this.localizedCampaigns = localizedCampaigns; }
    
    @Override
    public String toString() {
        return String.format("GlobalCampaign{id=%d, uuid=%s, name='%s', type=%s, status=%s, dailyBudget=$%.2f}",
                id, campaignUuid, campaignName, campaignType, status, dailyBudgetUsd);
    }
}