package com.woltaxi.marketing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * Ülke ve Pazar Bilgileri Entity
 * 
 * Her ülke için detaylı pazar bilgileri, yerelleştirme ayarları,
 * hedef kitle demografisi ve reklam bütçesi yönetimi
 */
@Entity
@Table(name = "countries")
public class Country {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "country_code", nullable = false, unique = true, length = 3)
    @NotBlank(message = "Country code is required")
    @Size(max = 3, message = "Country code must be 3 characters")
    private String countryCode; // USA, TUR, DEU
    
    @Column(name = "country_name", nullable = false, length = 100)
    @NotBlank(message = "Country name is required")
    @Size(max = 100, message = "Country name too long")
    private String countryName;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Continent is required")
    private String continent;
    
    // Localization Settings
    @Column(name = "primary_language", nullable = false, length = 10)
    @NotBlank(message = "Primary language is required")
    private String primaryLanguage; // en, tr, de, fr, es
    
    @Column(name = "secondary_languages")
    private String[] secondaryLanguages; // Multi-language support
    
    @Column(name = "currency_code", nullable = false, length = 3)
    @NotBlank(message = "Currency code is required")
    private String currencyCode; // USD, TRY, EUR
    
    @Column(name = "currency_symbol", nullable = false, length = 5)
    @NotBlank(message = "Currency symbol is required")
    private String currencySymbol; // $, ₺, €
    
    // Market Information
    @Column
    private Long population;
    
    @Column(name = "gdp_per_capita", precision = 10, scale = 2)
    private BigDecimal gdpPerCapita;
    
    @Column(name = "internet_penetration", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Internet penetration must be positive")
    @DecimalMax(value = "100.0", message = "Internet penetration cannot exceed 100%")
    private BigDecimal internetPenetration; // Percentage
    
    @Column(name = "smartphone_penetration", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Smartphone penetration must be positive")
    @DecimalMax(value = "100.0", message = "Smartphone penetration cannot exceed 100%")
    private BigDecimal smartphonePenetration; // Percentage
    
    // Time & Culture
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Timezone is required")
    private String timezone; // Europe/Istanbul, America/New_York
    
    @Column(name = "date_format", length = 20)
    private String dateFormat = "DD/MM/YYYY";
    
    @Column(name = "time_format", length = 10)
    private String timeFormat = "24h"; // 24h, 12h
    
    @Column(name = "weekend_days", length = 20)
    private String weekendDays = "saturday,sunday";
    
    // Business Environment
    @Enumerated(EnumType.STRING)
    @Column(name = "market_maturity")
    private MarketMaturity marketMaturity = MarketMaturity.EMERGING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "competition_level")
    private CompetitionLevel competitionLevel = CompetitionLevel.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "regulatory_complexity")
    private RegulatoryComplexity regulatoryComplexity = RegulatoryComplexity.MEDIUM;
    
    // Marketing Budget & Targeting
    @Column(name = "daily_ad_budget_usd", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Daily budget must be positive")
    private BigDecimal dailyAdBudgetUsd = BigDecimal.valueOf(1000.00);
    
    @Column(name = "target_audience_size")
    private Long targetAudienceSize;
    
    @Column(name = "peak_hours", length = 100)
    private String peakHours; // "08:00-10:00,17:00-19:00"
    
    @Column(name = "cultural_preferences", columnDefinition = "TEXT")
    private String culturalPreferences;
    
    // Status
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "launch_date")
    private LocalDateTime launchDate;
    
    @Column(name = "market_priority")
    @Min(value = 1, message = "Market priority must be at least 1")
    @Max(value = 3, message = "Market priority cannot exceed 3")
    private Integer marketPriority = 3; // 1=High, 2=Medium, 3=Low
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CountryPlatformConfig> platformConfigs;
    
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LocalizedCampaign> localizedCampaigns;
    
    // Enums
    public enum MarketMaturity {
        EMERGING, DEVELOPING, MATURE, SATURATED
    }
    
    public enum CompetitionLevel {
        LOW, MEDIUM, HIGH, EXTREME
    }
    
    public enum RegulatoryComplexity {
        LOW, MEDIUM, HIGH, COMPLEX
    }
    
    // Constructors
    public Country() {}
    
    public Country(String countryCode, String countryName, String continent,
                   String primaryLanguage, String currencyCode, String currencySymbol,
                   String timezone) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.continent = continent;
        this.primaryLanguage = primaryLanguage;
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.timezone = timezone;
    }
    
    // Business Methods
    
    /**
     * Ülkenin aktif reklam kampanyası var mı?
     */
    public boolean hasActiveCampaigns() {
        return localizedCampaigns != null && 
               localizedCampaigns.stream()
                   .anyMatch(campaign -> campaign.getStatus() == LocalizedCampaign.CampaignStatus.ACTIVE);
    }
    
    /**
     * Günlük reklam bütçesi local para birimi cinsinden
     */
    public BigDecimal getDailyBudgetInLocalCurrency(BigDecimal exchangeRate) {
        if (dailyAdBudgetUsd == null || exchangeRate == null) {
            return BigDecimal.ZERO;
        }
        return dailyAdBudgetUsd.multiply(exchangeRate);
    }
    
    /**
     * Peak hours listesi
     */
    public String[] getPeakHoursArray() {
        if (peakHours == null || peakHours.trim().isEmpty()) {
            return new String[0];
        }
        return peakHours.split(",");
    }
    
    /**
     * Ülkenin pazar olgunluğuna göre risk skoru (1-5)
     */
    public int getMarketRiskScore() {
        int score = 0;
        
        // Market maturity risk
        score += switch (marketMaturity) {
            case SATURATED -> 5;
            case MATURE -> 3;
            case DEVELOPING -> 2;
            case EMERGING -> 4;
        };
        
        // Competition risk
        score += switch (competitionLevel) {
            case EXTREME -> 5;
            case HIGH -> 4;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
        
        // Regulatory risk
        score += switch (regulatoryComplexity) {
            case COMPLEX -> 5;
            case HIGH -> 4;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
        
        return Math.min(score / 3, 5); // Average and cap at 5
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    
    public String getContinent() { return continent; }
    public void setContinent(String continent) { this.continent = continent; }
    
    public String getPrimaryLanguage() { return primaryLanguage; }
    public void setPrimaryLanguage(String primaryLanguage) { this.primaryLanguage = primaryLanguage; }
    
    public String[] getSecondaryLanguages() { return secondaryLanguages; }
    public void setSecondaryLanguages(String[] secondaryLanguages) { this.secondaryLanguages = secondaryLanguages; }
    
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    
    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    
    public Long getPopulation() { return population; }
    public void setPopulation(Long population) { this.population = population; }
    
    public BigDecimal getGdpPerCapita() { return gdpPerCapita; }
    public void setGdpPerCapita(BigDecimal gdpPerCapita) { this.gdpPerCapita = gdpPerCapita; }
    
    public BigDecimal getInternetPenetration() { return internetPenetration; }
    public void setInternetPenetration(BigDecimal internetPenetration) { this.internetPenetration = internetPenetration; }
    
    public BigDecimal getSmartphonePenetration() { return smartphonePenetration; }
    public void setSmartphonePenetration(BigDecimal smartphonePenetration) { this.smartphonePenetration = smartphonePenetration; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
    
    public String getTimeFormat() { return timeFormat; }
    public void setTimeFormat(String timeFormat) { this.timeFormat = timeFormat; }
    
    public String getWeekendDays() { return weekendDays; }
    public void setWeekendDays(String weekendDays) { this.weekendDays = weekendDays; }
    
    public MarketMaturity getMarketMaturity() { return marketMaturity; }
    public void setMarketMaturity(MarketMaturity marketMaturity) { this.marketMaturity = marketMaturity; }
    
    public CompetitionLevel getCompetitionLevel() { return competitionLevel; }
    public void setCompetitionLevel(CompetitionLevel competitionLevel) { this.competitionLevel = competitionLevel; }
    
    public RegulatoryComplexity getRegulatoryComplexity() { return regulatoryComplexity; }
    public void setRegulatoryComplexity(RegulatoryComplexity regulatoryComplexity) { this.regulatoryComplexity = regulatoryComplexity; }
    
    public BigDecimal getDailyAdBudgetUsd() { return dailyAdBudgetUsd; }
    public void setDailyAdBudgetUsd(BigDecimal dailyAdBudgetUsd) { this.dailyAdBudgetUsd = dailyAdBudgetUsd; }
    
    public Long getTargetAudienceSize() { return targetAudienceSize; }
    public void setTargetAudienceSize(Long targetAudienceSize) { this.targetAudienceSize = targetAudienceSize; }
    
    public String getPeakHours() { return peakHours; }
    public void setPeakHours(String peakHours) { this.peakHours = peakHours; }
    
    public String getCulturalPreferences() { return culturalPreferences; }
    public void setCulturalPreferences(String culturalPreferences) { this.culturalPreferences = culturalPreferences; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDateTime launchDate) { this.launchDate = launchDate; }
    
    public Integer getMarketPriority() { return marketPriority; }
    public void setMarketPriority(Integer marketPriority) { this.marketPriority = marketPriority; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<CountryPlatformConfig> getPlatformConfigs() { return platformConfigs; }
    public void setPlatformConfigs(List<CountryPlatformConfig> platformConfigs) { this.platformConfigs = platformConfigs; }
    
    public List<LocalizedCampaign> getLocalizedCampaigns() { return localizedCampaigns; }
    public void setLocalizedCampaigns(List<LocalizedCampaign> localizedCampaigns) { this.localizedCampaigns = localizedCampaigns; }
    
    @Override
    public String toString() {
        return String.format("Country{id=%d, code='%s', name='%s', continent='%s', language='%s', currency='%s'}",
                id, countryCode, countryName, continent, primaryLanguage, currencyCode);
    }
}