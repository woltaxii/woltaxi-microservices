package com.woltaxi.analytics.controller;

import com.woltaxi.analytics.dto.*;
import com.woltaxi.analytics.service.DriverAnalyticsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Sürücü Performans ve Kar-Zarar Analizi Controller
 * 
 * Sürücülerin günlük, aylık ve yıllık performans verilerini
 * yönetmek ve analiz etmek için API endpoint'leri
 */
@RestController
@RequestMapping("/drivers")
@CrossOrigin(origins = "*")
public class DriverAnalyticsController {
    
    @Autowired
    private DriverAnalyticsService driverAnalyticsService;
    
    /**
     * Sürücü günlük performans verisi kaydet
     * 
     * @param request Günlük performans verisi
     * @return Kaydedilen performans bilgileri
     */
    @PostMapping("/performance/daily")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DailyPerformanceResponse> recordDailyPerformance(
            @Valid @RequestBody DailyPerformanceRequest request) {
        
        DailyPerformanceResponse response = driverAnalyticsService.recordDailyPerformance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Sürücü günlük performansını getir
     * 
     * @param driverId Sürücü ID
     * @param date Tarih
     * @return Günlük performans detayları
     */
    @GetMapping("/{driverId}/performance/daily/{date}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DailyPerformanceResponse> getDailyPerformance(
            @PathVariable Long driverId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        DailyPerformanceResponse response = driverAnalyticsService.getDailyPerformance(driverId, date);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Sürücü aylık performans özetini getir
     * 
     * @param driverId Sürücü ID
     * @param year Yıl
     * @param month Ay
     * @return Aylık performans özeti
     */
    @GetMapping("/{driverId}/performance/monthly/{year}/{month}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MonthlyPerformanceResponse> getMonthlyPerformance(
            @PathVariable Long driverId,
            @PathVariable Integer year,
            @PathVariable Integer month) {
        
        MonthlyPerformanceResponse response = driverAnalyticsService.getMonthlyPerformance(driverId, year, month);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Sürücü yıllık performans özetini getir
     * 
     * @param driverId Sürücü ID
     * @param year Yıl
     * @return Yıllık performans özeti
     */
    @GetMapping("/{driverId}/performance/yearly/{year}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<YearlyPerformanceResponse> getYearlyPerformance(
            @PathVariable Long driverId,
            @PathVariable Integer year) {
        
        YearlyPerformanceResponse response = driverAnalyticsService.getYearlyPerformance(driverId, year);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Sürücü performans dashboard'unu getir
     * 
     * @param driverId Sürücü ID
     * @return Dashboard verileri
     */
    @GetMapping("/{driverId}/dashboard")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DriverDashboardResponse> getDriverDashboard(
            @PathVariable Long driverId) {
        
        DriverDashboardResponse response = driverAnalyticsService.getDriverDashboard(driverId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Sürücü gider kaydı ekle
     * 
     * @param driverId Sürücü ID
     * @param request Gider bilgileri
     * @return Kaydedilen gider bilgileri
     */
    @PostMapping("/{driverId}/expenses")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<ExpenseResponse> addExpense(
            @PathVariable Long driverId,
            @Valid @RequestBody ExpenseRequest request) {
        
        ExpenseResponse response = driverAnalyticsService.addExpense(driverId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Sürücü giderlerini listele
     * 
     * @param driverId Sürücü ID
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @param pageable Sayfalama bilgileri
     * @return Gider listesi
     */
    @GetMapping("/{driverId}/expenses")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<ExpenseResponse>> getExpenses(
            @PathVariable Long driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        
        Page<ExpenseResponse> expenses = driverAnalyticsService.getExpenses(driverId, startDate, endDate, pageable);
        return ResponseEntity.ok(expenses);
    }
    
    /**
     * Sürücü mali hedefleri getir
     * 
     * @param driverId Sürücü ID
     * @return Mali hedefler listesi
     */
    @GetMapping("/{driverId}/goals")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<FinancialGoalResponse>> getFinancialGoals(
            @PathVariable Long driverId) {
        
        List<FinancialGoalResponse> goals = driverAnalyticsService.getFinancialGoals(driverId);
        return ResponseEntity.ok(goals);
    }
    
    /**
     * Sürücü mali hedefi oluştur
     * 
     * @param driverId Sürücü ID
     * @param request Hedef bilgileri
     * @return Oluşturulan hedef bilgileri
     */
    @PostMapping("/{driverId}/goals")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<FinancialGoalResponse> createFinancialGoal(
            @PathVariable Long driverId,
            @Valid @RequestBody FinancialGoalRequest request) {
        
        FinancialGoalResponse response = driverAnalyticsService.createFinancialGoal(driverId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Tarih aralığında performans verilerini getir
     * 
     * @param driverId Sürücü ID
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Performans trend verileri
     */
    @GetMapping("/{driverId}/performance/range")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PerformanceTrendResponse> getPerformanceRange(
            @PathVariable Long driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        PerformanceTrendResponse response = driverAnalyticsService.getPerformanceRange(driverId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * En iyi performans gösteren sürücüleri listele
     * 
     * @param year Yıl
     * @param month Ay (opsiyonel)
     * @param limit Listeleme limiti
     * @return En iyi sürücüler listesi
     */
    @GetMapping("/top-performers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<TopPerformerResponse>> getTopPerformers(
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<TopPerformerResponse> topPerformers = driverAnalyticsService.getTopPerformers(year, month, limit);
        return ResponseEntity.ok(topPerformers);
    }
    
    /**
     * Sürücü performans karşılaştırması
     * 
     * @param driverId Sürücü ID
     * @param compareWithDriverId Karşılaştırılacak sürücü ID
     * @param year Yıl
     * @param month Ay (opsiyonel)
     * @return Karşılaştırma sonuçları
     */
    @GetMapping("/{driverId}/compare/{compareWithDriverId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PerformanceComparisonResponse> compareDrivers(
            @PathVariable Long driverId,
            @PathVariable Long compareWithDriverId,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        
        PerformanceComparisonResponse comparison = driverAnalyticsService.compareDrivers(
                driverId, compareWithDriverId, year, month);
        return ResponseEntity.ok(comparison);
    }
    
    /**
     * Sürücü performans raporu oluştur (PDF/Excel)
     * 
     * @param driverId Sürücü ID
     * @param format Rapor formatı (PDF, EXCEL, CSV)
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Rapor dosyası
     */
    @GetMapping("/{driverId}/reports")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<byte[]> generatePerformanceReport(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "PDF") String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] reportData = driverAnalyticsService.generatePerformanceReport(driverId, format, startDate, endDate);
        
        String contentType = switch (format.toUpperCase()) {
            case "PDF" -> "application/pdf";
            case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "CSV" -> "text/csv";
            default -> "application/octet-stream";
        };
        
        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", String.format("attachment; filename=driver_%d_report.%s", 
                        driverId, format.toLowerCase()))
                .body(reportData);
    }
    
    /**
     * Sürücü karlılık analizi
     * 
     * @param driverId Sürücü ID
     * @param year Yıl
     * @return Karlılık analizi
     */
    @GetMapping("/{driverId}/profitability/{year}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProfitabilityAnalysisResponse> getProfitabilityAnalysis(
            @PathVariable Long driverId,
            @PathVariable Integer year) {
        
        ProfitabilityAnalysisResponse analysis = driverAnalyticsService.getProfitabilityAnalysis(driverId, year);
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * Genel performans istatistikleri
     * 
     * @return Sistem geneli performans istatistikleri
     */
    @GetMapping("/statistics/overall")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<OverallStatisticsResponse> getOverallStatistics() {
        
        OverallStatisticsResponse statistics = driverAnalyticsService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Sürücü performans trendleri
     * 
     * @param driverId Sürücü ID
     * @param months Son kaç ay
     * @return Trend analizi
     */
    @GetMapping("/{driverId}/trends")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PerformanceTrendAnalysisResponse> getPerformanceTrends(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "12") Integer months) {
        
        PerformanceTrendAnalysisResponse trends = driverAnalyticsService.getPerformanceTrends(driverId, months);
        return ResponseEntity.ok(trends);
    }
}