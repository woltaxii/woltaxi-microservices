package com.woltaxi.globalperformance.controller;

import com.woltaxi.globalperformance.dto.*;
import com.woltaxi.globalperformance.entity.CountryPerformanceMetrics;
import com.woltaxi.globalperformance.service.GlobalPerformanceAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Küresel Performans Analitik Controller
 * Ülkeler arası performans karşılaştırması ve analitik API'leri
 */
@RestController
@RequestMapping("/api/v1/global-performance")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Global Performance Analytics", description = "Küresel performans analitik ve karşılaştırma API'leri")
public class GlobalPerformanceController {

    private final GlobalPerformanceAnalyticsService analyticsService;

    // =============================================================================
    // COUNTRY PERFORMANCE ANALYTICS - Ülke Performans Analitikleri
    // =============================================================================

    @GetMapping("/countries/performance")
    @Operation(summary = "Ülke performans metriklerini listele", 
               description = "Tüm ülkelerin performans metriklerini döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER', 'COUNTRY_MANAGER')")
    public ResponseEntity<Page<CountryPerformanceDTO>> getCountryPerformances(
            @Parameter(description = "Yıl filtresi") @RequestParam(required = false) @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay filtresi") @RequestParam(required = false) @Min(1) @Max(12) Integer month,
            @Parameter(description = "Ülke kodu filtresi") @RequestParam(required = false) String countryCode,
            @Parameter(description = "Performans katmanı filtresi") @RequestParam(required = false) CountryPerformanceMetrics.PerformanceTier tier,
            Pageable pageable) {
        
        log.info("Fetching country performances - year: {}, month: {}, countryCode: {}, tier: {}", 
                year, month, countryCode, tier);
        
        Page<CountryPerformanceDTO> performances = analyticsService.getCountryPerformances(
                year, month, countryCode, tier, pageable);
        
        return ResponseEntity.ok(performances);
    }

    @GetMapping("/countries/{countryCode}/performance")
    @Operation(summary = "Belirli ülkenin performans detayları", 
               description = "Belirtilen ülkenin detaylı performans metriklerini döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER', 'COUNTRY_MANAGER')")
    public ResponseEntity<CountryPerformanceDTO> getCountryPerformance(
            @Parameter(description = "Ülke kodu") @PathVariable String countryCode,
            @Parameter(description = "Yıl") @RequestParam @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay") @RequestParam @Min(1) @Max(12) Integer month) {
        
        log.info("Fetching performance for country: {} - {}/{}", countryCode, year, month);
        
        CountryPerformanceDTO performance = analyticsService.getCountryPerformance(countryCode, year, month);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/countries/rankings")
    @Operation(summary = "Ülke sıralamaları", 
               description = "Ülkelerin performans sıralamalarını döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER')")
    public ResponseEntity<List<CountryRankingDTO>> getCountryRankings(
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay") @RequestParam(defaultValue = "1") @Min(1) @Max(12) Integer month,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {
        
        log.info("Fetching country rankings for {}/{} - limit: {}", year, month, limit);
        
        List<CountryRankingDTO> rankings = analyticsService.getCountryRankings(year, month, limit);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/countries/comparison")
    @Operation(summary = "Ülke karşılaştırma analizi", 
               description = "Belirtilen ülkelerin detaylı karşılaştırma analizini döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER')")
    public ResponseEntity<CountryComparisonDTO> compareCountries(
            @Parameter(description = "Karşılaştırılacak ülke kodları") @RequestParam List<String> countryCodes,
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay") @RequestParam(defaultValue = "1") @Min(1) @Max(12) Integer month) {
        
        log.info("Comparing countries: {} for {}/{}", countryCodes, year, month);
        
        CountryComparisonDTO comparison = analyticsService.compareCountries(countryCodes, year, month);
        return ResponseEntity.ok(comparison);
    }

    @PostMapping("/countries/{countryCode}/performance")
    @Operation(summary = "Ülke performans metriği kaydet", 
               description = "Belirtilen ülke için performans metriği oluşturur veya günceller")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER', 'SYSTEM')")
    public ResponseEntity<CountryPerformanceDTO> saveCountryPerformance(
            @Parameter(description = "Ülke kodu") @PathVariable String countryCode,
            @Valid @RequestBody CountryPerformanceCreateDTO createDTO) {
        
        log.info("Saving performance metrics for country: {}", countryCode);
        
        CountryPerformanceDTO savedPerformance = analyticsService.saveCountryPerformance(countryCode, createDTO);
        return ResponseEntity.ok(savedPerformance);
    }

    // =============================================================================
    // GLOBAL DRIVER RANKINGS - Küresel Sürücü Sıralamaları
    // =============================================================================

    @GetMapping("/drivers/rankings")
    @Operation(summary = "Küresel sürücü sıralamaları", 
               description = "Küresel sürücü performans sıralamalarını döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER', 'DRIVER_MANAGER')")
    public ResponseEntity<Page<GlobalDriverRankingDTO>> getGlobalDriverRankings(
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay") @RequestParam(defaultValue = "1") @Min(1) @Max(12) Integer month,
            @Parameter(description = "Ülke kodu filtresi") @RequestParam(required = false) String countryCode,
            @Parameter(description = "Şehir filtresi") @RequestParam(required = false) String city,
            @Parameter(description = "Başarı katmanı filtresi") @RequestParam(required = false) String achievementTier,
            Pageable pageable) {
        
        log.info("Fetching global driver rankings - {}/{}, country: {}, city: {}, tier: {}", 
                year, month, countryCode, city, achievementTier);
        
        Page<GlobalDriverRankingDTO> rankings = analyticsService.getGlobalDriverRankings(
                year, month, countryCode, city, achievementTier, pageable);
        
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/drivers/{driverId}/ranking")
    @Operation(summary = "Sürücü sıralama detayları", 
               description = "Belirtilen sürücünün detaylı sıralama bilgilerini döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER', 'DRIVER_MANAGER')")
    public ResponseEntity<GlobalDriverRankingDTO> getDriverRanking(
            @Parameter(description = "Sürücü ID") @PathVariable Long driverId,
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay") @RequestParam(defaultValue = "1") @Min(1) @Max(12) Integer month) {
        
        log.info("Fetching ranking for driver: {} - {}/{}", driverId, year, month);
        
        GlobalDriverRankingDTO ranking = analyticsService.getDriverRanking(driverId, year, month);
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/drivers/top-performers")
    @Operation(summary = "En iyi performans gösteren sürücüler", 
               description = "Belirtilen kriterlere göre en iyi sürücüleri döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER')")
    public ResponseEntity<List<TopPerformerDTO>> getTopPerformers(
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay") @RequestParam(defaultValue = "1") @Min(1) @Max(12) Integer month,
            @Parameter(description = "Kategori") @RequestParam(defaultValue = "GLOBAL") String category,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit) {
        
        log.info("Fetching top performers - {}/{}, category: {}, limit: {}", year, month, category, limit);
        
        List<TopPerformerDTO> topPerformers = analyticsService.getTopPerformers(year, month, category, limit);
        return ResponseEntity.ok(topPerformers);
    }

    // =============================================================================
    // PERFORMANCE ANALYSIS & INSIGHTS - Performans Analizi ve İçgörüler
    // =============================================================================

    @GetMapping("/analytics/insights")
    @Operation(summary = "Performans içgörüleri", 
               description = "Küresel performans trendleri ve içgörüleri döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER')")
    public ResponseEntity<PerformanceInsightsDTO> getPerformanceInsights(
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay") @RequestParam(defaultValue = "1") @Min(1) @Max(12) Integer month) {
        
        log.info("Fetching performance insights for {}/{}", year, month);
        
        PerformanceInsightsDTO insights = analyticsService.getPerformanceInsights(year, month);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/analytics/benchmarks")
    @Operation(summary = "Performans benchmark'ları", 
               description = "Küresel ve bölgesel performans benchmark'larını döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER')")
    public ResponseEntity<List<PerformanceBenchmarkDTO>> getPerformanceBenchmarks(
            @Parameter(description = "Benchmark kapsamı") @RequestParam(defaultValue = "GLOBAL") String scope,
            @Parameter(description = "Kapsam identificatörü") @RequestParam(required = false) String scopeIdentifier,
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year) {
        
        log.info("Fetching performance benchmarks - scope: {}, identifier: {}, year: {}", 
                scope, scopeIdentifier, year);
        
        List<PerformanceBenchmarkDTO> benchmarks = analyticsService.getPerformanceBenchmarks(
                scope, scopeIdentifier, year);
        
        return ResponseEntity.ok(benchmarks);
    }

    @GetMapping("/analytics/trends")
    @Operation(summary = "Performans trendleri", 
               description = "Zaman bazlı performans trend analizlerini döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER')")
    public ResponseEntity<PerformanceTrendsDTO> getPerformanceTrends(
            @Parameter(description = "Başlangıç yılı") @RequestParam @Min(2020) @Max(2100) Integer startYear,
            @Parameter(description = "Bitiş yılı") @RequestParam @Min(2020) @Max(2100) Integer endYear,
            @Parameter(description = "Trend türü") @RequestParam(defaultValue = "MONTHLY") String trendType,
            @Parameter(description = "Ülke kodu filtresi") @RequestParam(required = false) String countryCode) {
        
        log.info("Fetching performance trends - {}-{}, type: {}, country: {}", 
                startYear, endYear, trendType, countryCode);
        
        PerformanceTrendsDTO trends = analyticsService.getPerformanceTrends(
                startYear, endYear, trendType, countryCode);
        
        return ResponseEntity.ok(trends);
    }

    // =============================================================================
    // CAPACITY OPTIMIZATION - Kapasite Optimizasyonu
    // =============================================================================

    @GetMapping("/optimization/capacity-analysis")
    @Operation(summary = "Kapasite analizi", 
               description = "Ülke ve şehir bazında kapasite optimizasyon analizini döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER', 'OPERATIONS_MANAGER')")
    public ResponseEntity<CapacityAnalysisDTO> getCapacityAnalysis(
            @Parameter(description = "Ülke kodu") @RequestParam(required = false) String countryCode,
            @Parameter(description = "Şehir") @RequestParam(required = false) String city,
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay") @RequestParam(defaultValue = "1") @Min(1) @Max(12) Integer month) {
        
        log.info("Performing capacity analysis - country: {}, city: {}, {}/{}", 
                countryCode, city, year, month);
        
        CapacityAnalysisDTO analysis = analyticsService.getCapacityAnalysis(countryCode, city, year, month);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/optimization/efficiency-recommendations")
    @Operation(summary = "Verimlilik önerileri", 
               description = "AI destekli verimlilik artırma önerilerini döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER', 'OPERATIONS_MANAGER')")
    public ResponseEntity<List<EfficiencyRecommendationDTO>> getEfficiencyRecommendations(
            @Parameter(description = "Ülke kodu") @RequestParam String countryCode,
            @Parameter(description = "Öneri türü") @RequestParam(defaultValue = "ALL") String recommendationType) {
        
        log.info("Generating efficiency recommendations for country: {}, type: {}", 
                countryCode, recommendationType);
        
        List<EfficiencyRecommendationDTO> recommendations = analyticsService.getEfficiencyRecommendations(
                countryCode, recommendationType);
        
        return ResponseEntity.ok(recommendations);
    }

    // =============================================================================
    // REPORTING & EXPORTS - Raporlama ve Dışa Aktarım
    // =============================================================================

    @GetMapping("/reports/executive-summary")
    @Operation(summary = "Yönetici özet raporu", 
               description = "Üst düzey yönetim için özet performans raporunu döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'EXECUTIVE', 'ANALYTICS_MANAGER')")
    public ResponseEntity<ExecutiveSummaryDTO> getExecutiveSummary(
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Çeyrek") @RequestParam(required = false) @Min(1) @Max(4) Integer quarter) {
        
        log.info("Generating executive summary for year: {}, quarter: {}", year, quarter);
        
        ExecutiveSummaryDTO summary = analyticsService.getExecutiveSummary(year, quarter);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/reports/performance-scorecard")
    @Operation(summary = "Performans karnesi", 
               description = "Detaylı performans karnesi raporu döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYTICS_MANAGER', 'COUNTRY_MANAGER')")
    public ResponseEntity<PerformanceScorecardDTO> getPerformanceScorecard(
            @Parameter(description = "Ülke kodu") @RequestParam(required = false) String countryCode,
            @Parameter(description = "Yıl") @RequestParam(defaultValue = "2025") @Min(2020) @Max(2100) Integer year,
            @Parameter(description = "Ay") @RequestParam(defaultValue = "1") @Min(1) @Max(12) Integer month) {
        
        log.info("Generating performance scorecard - country: {}, {}/{}", countryCode, year, month);
        
        PerformanceScorecardDTO scorecard = analyticsService.getPerformanceScorecard(countryCode, year, month);
        return ResponseEntity.ok(scorecard);
    }

    // =============================================================================
    // HEALTH CHECK & MONITORING - Sağlık Kontrolü ve İzleme
    // =============================================================================

    @GetMapping("/health")
    @Operation(summary = "Servis sağlık kontrolü", description = "Servisin sağlık durumunu döndürür")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = analyticsService.getServiceHealth();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/metrics")
    @Operation(summary = "Servis metrikleri", description = "Servisin performans metriklerini döndürür")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONITORING')")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = analyticsService.getServiceMetrics();
        return ResponseEntity.ok(metrics);
    }
}