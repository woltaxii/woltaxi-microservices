package com.woltaxi.aiml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Analytics Entity
 * 
 * AI/ML sisteminin performans metrikleri, iş analitiği ve
 * karar destek sistemi verilerini saklar.
 */
@Entity
@Table(name = "ai_analytics", indexes = {
    @Index(name = "idx_analytics_date", columnList = "analysisDate"),
    @Index(name = "idx_analytics_service", columnList = "serviceName"),
    @Index(name = "idx_analytics_type", columnList = "analyticsType"),
    @Index(name = "idx_analytics_city", columnList = "city")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String analyticsType; // DEMAND_FORECAST, PRICING_OPTIMIZATION, PERFORMANCE_ANALYSIS, etc.

    @Column(nullable = false)
    private String serviceName; // WOLTAXI, WOLKURYE, EMERGENCY, PAYMENT

    @Column(nullable = false)
    private LocalDateTime analysisDate;

    @Column
    private String timeframe; // HOURLY, DAILY, WEEKLY, MONTHLY

    @Column
    private String city;

    @Column
    private String district;

    @Column
    private String vehicleType;

    // Demand Analytics
    @Column(precision = 12, scale = 2)
    private BigDecimal predictedDemand;

    @Column(precision = 12, scale = 2)
    private BigDecimal actualDemand;

    @Column(precision = 10, scale = 4)
    private BigDecimal demandAccuracy;

    @Column(precision = 12, scale = 2)
    private BigDecimal peakHourDemand;

    @Column(precision = 10, scale = 2)
    private BigDecimal averageWaitTime; // minutes

    // Pricing Analytics
    @Column(precision = 10, scale = 2)
    private BigDecimal recommendedPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal actualPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceElasticity;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(precision = 15, scale = 2)
    private BigDecimal projectedRevenue;

    // Performance Metrics
    @Column(precision = 10, scale = 2)
    private BigDecimal customerSatisfactionScore;

    @Column(precision = 10, scale = 2)
    private BigDecimal driverPerformanceScore;

    @Column(precision = 10, scale = 2)
    private BigDecimal systemEfficiencyScore;

    @Column(precision = 10, scale = 4)
    private BigDecimal cancellationRate;

    @Column(precision = 10, scale = 4)
    private BigDecimal completionRate;

    // Route Optimization
    @Column(precision = 10, scale = 2)
    private BigDecimal averageRouteTime; // minutes

    @Column(precision = 10, scale = 2)
    private BigDecimal optimizedRouteTime; // minutes

    @Column(precision = 10, scale = 4)
    private BigDecimal routeEfficiencyGain; // percentage

    @Column(precision = 12, scale = 2)
    private BigDecimal fuelSavings; // TL

    // Fraud Detection
    @Column
    private Integer fraudAttempts;

    @Column
    private Integer fraudDetected;

    @Column
    private Integer falsePositives;

    @Column(precision = 10, scale = 4)
    private BigDecimal fraudDetectionAccuracy;

    @Column(precision = 15, scale = 2)
    private BigDecimal preventedLosses; // TL

    // Customer Analytics
    @Column
    private Integer totalCustomers;

    @Column
    private Integer activeCustomers;

    @Column
    private Integer newCustomers;

    @Column
    private Integer churnedCustomers;

    @Column(precision = 10, scale = 4)
    private BigDecimal churnRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal customerLifetimeValue;

    // Driver Analytics
    @Column
    private Integer totalDrivers;

    @Column
    private Integer activeDrivers;

    @Column
    private Integer onlineDrivers;

    @Column(precision = 10, scale = 2)
    private BigDecimal averageDriverRating;

    @Column(precision = 10, scale = 2)
    private BigDecimal driverUtilizationRate;

    // Traffic & Weather Impact
    @Column(columnDefinition = "TEXT")
    private String trafficConditions; // JSON format

    @Column(columnDefinition = "TEXT")
    private String weatherConditions; // JSON format

    @Column(precision = 10, scale = 4)
    private BigDecimal trafficImpactScore;

    @Column(precision = 10, scale = 4)
    private BigDecimal weatherImpactScore;

    // AI Model Performance
    @Column(columnDefinition = "TEXT")
    private String modelPerformanceMetrics; // JSON format

    @Column(precision = 10, scale = 4)
    private BigDecimal overallModelAccuracy;

    @Column
    private Integer totalPredictions;

    @Column
    private Integer successfulPredictions;

    // Business Insights
    @Column(columnDefinition = "TEXT")
    private String keyInsights; // JSON array of insights

    @Column(columnDefinition = "TEXT")
    private String recommendations; // JSON array of recommendations

    @Column(columnDefinition = "TEXT")
    private String alertsAndWarnings; // JSON array of alerts

    // Competitive Analysis
    @Column(precision = 10, scale = 2)
    private BigDecimal marketShareEstimate;

    @Column(precision = 10, scale = 2)
    private BigDecimal competitorPricing;

    @Column(precision = 10, scale = 4)
    private BigDecimal priceAdvantage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String generatedBy; // System or user who generated this analytics

    @Column(columnDefinition = "TEXT")
    private String rawData; // Original data used for analysis (JSON)
}