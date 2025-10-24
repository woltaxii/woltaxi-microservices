package com.woltaxi.aiml.controller;

import com.woltaxi.aiml.dto.*;
import com.woltaxi.aiml.service.AIAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * AI Analytics Controller
 * 
 * Yapay zeka destekli analitik, tahmin ve iş zekası işlemlerini yöneten controller
 */
@RestController
@RequestMapping("/api/ai-ml/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Analytics", description = "AI destekli analitik ve tahmin API'leri")
public class AIAnalyticsController {

    private final AIAnalyticsService aiAnalyticsService;

    @PostMapping("/demand-forecast")
    @Operation(summary = "Talep tahmini", description = "Gelecekteki talebi makine öğrenmesi ile tahmin et")
    public ResponseEntity<DemandForecastResponse> forecastDemand(
            @Valid @RequestBody DemandForecastRequest request) {
        log.info("Processing demand forecast for service: {} in city: {}", 
                request.getServiceName(), request.getCity());
        DemandForecastResponse response = aiAnalyticsService.forecastDemand(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pricing-optimization")
    @Operation(summary = "Fiyat optimizasyonu", description = "Dinamik fiyatlandırma için optimal fiyat hesapla")
    public ResponseEntity<PricingOptimizationResponse> optimizePrice(
            @Valid @RequestBody PricingOptimizationRequest request) {
        log.info("Processing pricing optimization for service: {} in location: {}/{}", 
                request.getServiceName(), request.getCity(), request.getDistrict());
        PricingOptimizationResponse response = aiAnalyticsService.optimizePrice(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fraud-detection")
    @Operation(summary = "Fraud tespiti", description = "Şüpheli işlemleri ve fraud girişimlerini tespit et")
    public ResponseEntity<FraudDetectionResponse> detectFraud(
            @Valid @RequestBody FraudDetectionRequest request) {
        log.info("Processing fraud detection for transaction ID: {}", request.getTransactionId());
        FraudDetectionResponse response = aiAnalyticsService.detectFraud(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/customer-segmentation")
    @Operation(summary = "Müşteri segmentasyonu", description = "Müşterileri AI ile segmentlere ayır")
    public ResponseEntity<CustomerSegmentationResponse> segmentCustomers(
            @Valid @RequestBody CustomerSegmentationRequest request) {
        log.info("Processing customer segmentation with {} customers", request.getCustomerIds().size());
        CustomerSegmentationResponse response = aiAnalyticsService.segmentCustomers(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/churn-prediction")
    @Operation(summary = "Müşteri kaybı tahmini", description = "Hangi müşterilerin kaybolma riski taşıdığını tahmin et")
    public ResponseEntity<ChurnPredictionResponse> predictChurn(
            @Valid @RequestBody ChurnPredictionRequest request) {
        log.info("Processing churn prediction for {} customers", request.getCustomerIds().size());
        ChurnPredictionResponse response = aiAnalyticsService.predictChurn(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/route-optimization")
    @Operation(summary = "Rota optimizasyonu", description = "AI ile optimal rota hesapla")
    public ResponseEntity<RouteOptimizationResponse> optimizeRoute(
            @Valid @RequestBody RouteOptimizationRequest request) {
        log.info("Processing route optimization from {} to {}", 
                request.getStartLocation(), request.getEndLocation());
        RouteOptimizationResponse response = aiAnalyticsService.optimizeRoute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/driver-performance")
    @Operation(summary = "Sürücü performans analizi", description = "Sürücü performansını AI ile değerlendir")
    public ResponseEntity<DriverPerformanceResponse> analyzeDriverPerformance(
            @Valid @RequestBody DriverPerformanceRequest request) {
        log.info("Analyzing driver performance for driver ID: {}", request.getDriverId());
        DriverPerformanceResponse response = aiAnalyticsService.analyzeDriverPerformance(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/traffic-analysis")
    @Operation(summary = "Trafik analizi", description = "Trafik desenlerini AI ile analiz et")
    public ResponseEntity<TrafficAnalysisResponse> analyzeTraffic(
            @Valid @RequestBody TrafficAnalysisRequest request) {
        log.info("Analyzing traffic for city: {} on date: {}", request.getCity(), request.getAnalysisDate());
        TrafficAnalysisResponse response = aiAnalyticsService.analyzeTraffic(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/customer-satisfaction")
    @Operation(summary = "Müşteri memnuniyeti analizi", description = "Müşteri memnuniyet seviyesini AI ile analiz et")
    public ResponseEntity<CustomerSatisfactionResponse> analyzeCustomerSatisfaction(
            @Valid @RequestBody CustomerSatisfactionRequest request) {
        log.info("Analyzing customer satisfaction for service: {}", request.getServiceName());
        CustomerSatisfactionResponse response = aiAnalyticsService.analyzeCustomerSatisfaction(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/market-analysis")
    @Operation(summary = "Pazar analizi", description = "Pazar trendlerini ve rekabet durumunu analiz et")
    public ResponseEntity<MarketAnalysisResponse> analyzeMarket(
            @Valid @RequestBody MarketAnalysisRequest request) {
        log.info("Processing market analysis for city: {}", request.getCity());
        MarketAnalysisResponse response = aiAnalyticsService.analyzeMarket(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/predictive-maintenance")
    @Operation(summary = "Öngörülü bakım", description = "Araç bakım ihtiyaçlarını AI ile tahmin et")
    public ResponseEntity<PredictiveMaintenanceResponse> predictMaintenance(
            @Valid @RequestBody PredictiveMaintenanceRequest request) {
        log.info("Processing predictive maintenance for vehicle ID: {}", request.getVehicleId());
        PredictiveMaintenanceResponse response = aiAnalyticsService.predictMaintenance(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/overview")
    @Operation(summary = "Analytics dashboard", description = "AI analytics için genel bakış dashboard verisi")
    public ResponseEntity<AnalyticsDashboardResponse> getDashboardOverview(
            @Parameter(description = "Servis adı") @RequestParam(required = false) String serviceName,
            @Parameter(description = "Şehir") @RequestParam(required = false) String city,
            @Parameter(description = "Zaman aralığı") @RequestParam(defaultValue = "7") int days) {
        log.info("Fetching analytics dashboard overview for last {} days", days);
        AnalyticsDashboardResponse response = aiAnalyticsService.getDashboardOverview(serviceName, city, days);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/{reportType}")
    @Operation(summary = "Analytics raporu", description = "Belirtilen tip için detaylı analytics raporu")
    public ResponseEntity<AnalyticsReportResponse> generateReport(
            @PathVariable String reportType,
            @Parameter(description = "Başlangıç tarihi") @RequestParam String startDate,
            @Parameter(description = "Bitiş tarihi") @RequestParam String endDate,
            @Parameter(description = "Şehir filtresi") @RequestParam(required = false) String city,
            @Parameter(description = "Servis filtresi") @RequestParam(required = false) String serviceName) {
        log.info("Generating analytics report type: {} for period: {} to {}", reportType, startDate, endDate);
        AnalyticsReportResponse response = aiAnalyticsService.generateReport(
            reportType, startDate, endDate, city, serviceName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trends")
    @Operation(summary = "Trend analizi", description = "Belirtilen metriklerin trend analizini getir")
    public ResponseEntity<TrendAnalysisResponse> analyzeTrends(
            @Parameter(description = "Analiz edilecek metrikler") @RequestParam List<String> metrics,
            @Parameter(description = "Zaman periyodu") @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Şehir") @RequestParam(required = false) String city,
            @Parameter(description = "Servis") @RequestParam(required = false) String serviceName) {
        log.info("Analyzing trends for metrics: {} over {} days", metrics, days);
        TrendAnalysisResponse response = aiAnalyticsService.analyzeTrends(metrics, days, city, serviceName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/real-time-insights")
    @Operation(summary = "Gerçek zamanlı insights", description = "Anlık veri üzerinden gerçek zamanlı analiz")
    public ResponseEntity<RealTimeInsightsResponse> getRealTimeInsights(
            @Valid @RequestBody RealTimeInsightsRequest request) {
        log.info("Processing real-time insights for services: {}", request.getServiceNames());
        RealTimeInsightsResponse response = aiAnalyticsService.getRealTimeInsights(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/anomaly-detection")
    @Operation(summary = "Anomali tespiti", description = "Sistemdeki anormal durumları tespit et")
    public ResponseEntity<AnomalyDetectionResponse> detectAnomalies(
            @Valid @RequestBody AnomalyDetectionRequest request) {
        log.info("Processing anomaly detection for metric: {}", request.getMetricName());
        AnomalyDetectionResponse response = aiAnalyticsService.detectAnomalies(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Analytics kayıtları", description = "Geçmiş analytics kayıtlarını listele")
    public ResponseEntity<Page<AIAnalyticsResponse>> getAnalytics(
            @Parameter(description = "Analytics tipi") @RequestParam(required = false) String analyticsType,
            @Parameter(description = "Servis adı") @RequestParam(required = false) String serviceName,
            @Parameter(description = "Şehir") @RequestParam(required = false) String city,
            @Parameter(description = "Başlangıç tarihi") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi") @RequestParam(required = false) String endDate,
            Pageable pageable) {
        log.info("Fetching analytics records with filters");
        Page<AIAnalyticsResponse> response = aiAnalyticsService.getAnalytics(
            analyticsType, serviceName, city, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/{id}")
    @Operation(summary = "Analytics detayı", description = "Belirtilen analytics kaydının detayını getir")
    public ResponseEntity<AIAnalyticsResponse> getAnalyticsById(@PathVariable Long id) {
        log.info("Fetching analytics details for ID: {}", id);
        AIAnalyticsResponse response = aiAnalyticsService.getAnalyticsById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/a-b-test")
    @Operation(summary = "A/B test analizi", description = "A/B test sonuçlarını AI ile analiz et")
    public ResponseEntity<ABTestAnalysisResponse> analyzeABTest(
            @Valid @RequestBody ABTestAnalysisRequest request) {
        log.info("Processing A/B test analysis for test: {}", request.getTestName());
        ABTestAnalysisResponse response = aiAnalyticsService.analyzeABTest(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recommendation-engine")
    @Operation(summary = "Öneri motoru", description = "Kullanıcılar için kişiselleştirilmiş öneriler üret")
    public ResponseEntity<RecommendationResponse> generateRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        log.info("Generating recommendations for user: {}", request.getUserId());
        RecommendationResponse response = aiAnalyticsService.generateRecommendations(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kpi-dashboard")
    @Operation(summary = "KPI dashboard", description = "Ana performans göstergelerinin AI destekli dashboard'u")
    public ResponseEntity<KPIDashboardResponse> getKPIDashboard(
            @Parameter(description = "Zaman aralığı (gün)") @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Şehir filtresi") @RequestParam(required = false) String city,
            @Parameter(description = "Servis filtresi") @RequestParam(required = false) String serviceName) {
        log.info("Fetching KPI dashboard for last {} days", days);
        KPIDashboardResponse response = aiAnalyticsService.getKPIDashboard(days, city, serviceName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Analytics servis durumu", description = "AI Analytics servislerinin sağlık durumu")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        log.info("Checking AI Analytics service health");
        Map<String, Object> response = aiAnalyticsService.getHealthStatus();
        return ResponseEntity.ok(response);
    }
}