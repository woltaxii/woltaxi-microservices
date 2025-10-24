package com.woltaxi.aiml.repository;

import com.woltaxi.aiml.entity.PredictionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Prediction Request Repository
 * 
 * AI/ML tahmin isteklerinin veritabanı işlemleri
 */
@Repository
public interface PredictionRequestRepository extends JpaRepository<PredictionRequest, Long> {

    /**
     * Model ID'ye göre tahmin istekleri
     */
    Page<PredictionRequest> findByModelId(Long modelId, Pageable pageable);

    /**
     * Kullanıcı ID'ye göre tahmin istekleri
     */
    Page<PredictionRequest> findByUserId(Long userId, Pageable pageable);

    /**
     * Servis adına göre tahmin istekleri
     */
    Page<PredictionRequest> findByServiceName(String serviceName, Pageable pageable);

    /**
     * Durum bazında tahmin istekleri
     */
    List<PredictionRequest> findByStatus(PredictionRequest.RequestStatus status);

    /**
     * Tarih aralığında tahmin istekleri
     */
    @Query("SELECT pr FROM PredictionRequest pr WHERE " +
           "pr.requestDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pr.requestDate DESC")
    List<PredictionRequest> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Model ve tarih aralığına göre istekler
     */
    @Query("SELECT pr FROM PredictionRequest pr WHERE " +
           "pr.modelId = :modelId AND " +
           "pr.requestDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pr.requestDate DESC")
    Page<PredictionRequest> findByModelIdAndDateRange(@Param("modelId") Long modelId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     Pageable pageable);

    /**
     * Kullanıcı ve servis bazında istekler
     */
    @Query("SELECT pr FROM PredictionRequest pr WHERE " +
           "pr.userId = :userId AND " +
           "pr.serviceName = :serviceName " +
           "ORDER BY pr.requestDate DESC")
    List<PredictionRequest> findByUserIdAndServiceName(@Param("userId") Long userId,
                                                      @Param("serviceName") String serviceName);

    /**
     * Session ID'ye göre istekler
     */
    List<PredictionRequest> findBySessionIdOrderByRequestDateAsc(String sessionId);

    /**
     * Başarısız istekler
     */
    @Query("SELECT pr FROM PredictionRequest pr WHERE " +
           "pr.status = 'FAILED' " +
           "ORDER BY pr.requestDate DESC")
    List<PredictionRequest> findFailedRequests(Pageable pageable);

    /**
     * Yavaş istekler (response time > threshold)
     */
    @Query("SELECT pr FROM PredictionRequest pr WHERE " +
           "pr.responseTimeMs > :threshold AND " +
           "pr.status = 'COMPLETED' " +
           "ORDER BY pr.responseTimeMs DESC")
    List<PredictionRequest> findSlowRequests(@Param("threshold") Double threshold, Pageable pageable);

    /**
     * Model ID'ye göre istekleri sil
     */
    @Modifying
    @Transactional
    void deleteByModelId(Long modelId);

    /**
     * Eski istekleri temizle
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PredictionRequest pr WHERE pr.requestDate < :cutoffDate")
    int deleteOldRequests(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Model performans istatistikleri
     */
    @Query("SELECT " +
           "COUNT(pr) as totalRequests, " +
           "COUNT(CASE WHEN pr.status = 'COMPLETED' THEN 1 END) as successfulRequests, " +
           "COUNT(CASE WHEN pr.status = 'FAILED' THEN 1 END) as failedRequests, " +
           "AVG(pr.responseTimeMs) as avgResponseTime, " +
           "AVG(pr.confidence) as avgConfidence " +
           "FROM PredictionRequest pr WHERE pr.modelId = :modelId")
    Object[] getModelPerformanceStats(@Param("modelId") Long modelId);

    /**
     * Günlük istekler
     */
    @Query("SELECT pr FROM PredictionRequest pr WHERE " +
           "pr.requestDate >= :startOfDay AND pr.requestDate < :endOfDay " +
           "ORDER BY pr.requestDate DESC")
    List<PredictionRequest> findDailyRequests(@Param("startOfDay") LocalDateTime startOfDay,
                                             @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * En popüler servisler
     */
    @Query("SELECT pr.serviceName, COUNT(pr) as requestCount " +
           "FROM PredictionRequest pr " +
           "WHERE pr.requestDate >= :startDate " +
           "GROUP BY pr.serviceName " +
           "ORDER BY requestCount DESC")
    List<Object[]> findMostPopularServices(@Param("startDate") LocalDateTime startDate);

    /**
     * En aktif kullanıcılar
     */
    @Query("SELECT pr.userId, COUNT(pr) as requestCount " +
           "FROM PredictionRequest pr " +
           "WHERE pr.userId IS NOT NULL AND pr.requestDate >= :startDate " +
           "GROUP BY pr.userId " +
           "ORDER BY requestCount DESC")
    List<Object[]> findMostActiveUsers(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    /**
     * Saatlik dağılım
     */
    @Query("SELECT " +
           "EXTRACT(HOUR FROM pr.requestDate) as hour, " +
           "COUNT(pr) as requestCount " +
           "FROM PredictionRequest pr " +
           "WHERE pr.requestDate >= :startDate " +
           "GROUP BY EXTRACT(HOUR FROM pr.requestDate) " +
           "ORDER BY hour")
    List<Object[]> findHourlyDistribution(@Param("startDate") LocalDateTime startDate);

    /**
     * Model kullanım trendi
     */
    @Query("SELECT " +
           "DATE(pr.requestDate) as requestDate, " +
           "COUNT(pr) as requestCount " +
           "FROM PredictionRequest pr " +
           "WHERE pr.modelId = :modelId AND pr.requestDate >= :startDate " +
           "GROUP BY DATE(pr.requestDate) " +
           "ORDER BY requestDate DESC")
    List<Object[]> getModelUsageTrend(@Param("modelId") Long modelId,
                                     @Param("startDate") LocalDateTime startDate);

    /**
     * Hata analizi
     */
    @Query("SELECT " +
           "pr.errorMessage, " +
           "COUNT(pr) as errorCount " +
           "FROM PredictionRequest pr " +
           "WHERE pr.status = 'FAILED' AND pr.requestDate >= :startDate " +
           "GROUP BY pr.errorMessage " +
           "ORDER BY errorCount DESC")
    List<Object[]> getErrorAnalysis(@Param("startDate") LocalDateTime startDate);

    /**
     * Performans metrikleri
     */
    @Query("SELECT " +
           "pr.modelId, " +
           "COUNT(pr) as totalRequests, " +
           "AVG(pr.responseTimeMs) as avgResponseTime, " +
           "MIN(pr.responseTimeMs) as minResponseTime, " +
           "MAX(pr.responseTimeMs) as maxResponseTime, " +
           "AVG(pr.confidence) as avgConfidence " +
           "FROM PredictionRequest pr " +
           "WHERE pr.status = 'COMPLETED' AND pr.requestDate >= :startDate " +
           "GROUP BY pr.modelId")
    List<Object[]> getPerformanceMetrics(@Param("startDate") LocalDateTime startDate);

    /**
     * Sistem yükü analizi
     */
    @Query("SELECT " +
           "DATE(pr.requestDate) as requestDate, " +
           "EXTRACT(HOUR FROM pr.requestDate) as hour, " +
           "COUNT(pr) as requestCount, " +
           "AVG(pr.responseTimeMs) as avgResponseTime " +
           "FROM PredictionRequest pr " +
           "WHERE pr.requestDate >= :startDate " +
           "GROUP BY DATE(pr.requestDate), EXTRACT(HOUR FROM pr.requestDate) " +
           "ORDER BY requestDate DESC, hour DESC")
    List<Object[]> getSystemLoadAnalysis(@Param("startDate") LocalDateTime startDate);

    /**
     * Güven skoru dağılımı
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN pr.confidence >= 0.9 THEN 'HIGH' " +
           "  WHEN pr.confidence >= 0.7 THEN 'MEDIUM' " +
           "  ELSE 'LOW' " +
           "END as confidenceLevel, " +
           "COUNT(pr) as count " +
           "FROM PredictionRequest pr " +
           "WHERE pr.confidence IS NOT NULL AND pr.requestDate >= :startDate " +
           "GROUP BY " +
           "CASE " +
           "  WHEN pr.confidence >= 0.9 THEN 'HIGH' " +
           "  WHEN pr.confidence >= 0.7 THEN 'MEDIUM' " +
           "  ELSE 'LOW' " +
           "END")
    List<Object[]> getConfidenceDistribution(@Param("startDate") LocalDateTime startDate);

    /**
     * İstek kaynak analizi
     */
    @Query("SELECT " +
           "pr.requestSource, " +
           "COUNT(pr) as requestCount, " +
           "AVG(pr.responseTimeMs) as avgResponseTime " +
           "FROM PredictionRequest pr " +
           "WHERE pr.requestDate >= :startDate " +
           "GROUP BY pr.requestSource " +
           "ORDER BY requestCount DESC")
    List<Object[]> getRequestSourceAnalysis(@Param("startDate") LocalDateTime startDate);

    /**
     * Tahmin tipi dağılımı
     */
    @Query("SELECT " +
           "pr.predictionType, " +
           "COUNT(pr) as requestCount, " +
           "AVG(pr.confidence) as avgConfidence " +
           "FROM PredictionRequest pr " +
           "WHERE pr.requestDate >= :startDate " +
           "GROUP BY pr.predictionType " +
           "ORDER BY requestCount DESC")
    List<Object[]> getPredictionTypeDistribution(@Param("startDate") LocalDateTime startDate);

    /**
     * Feedback sağlanan istekler
     */
    @Query("SELECT pr FROM PredictionRequest pr WHERE " +
           "pr.feedbackProvided = true " +
           "ORDER BY pr.requestDate DESC")
    List<PredictionRequest> findRequestsWithFeedback(Pageable pageable);

    /**
     * Yüksek hata oranına sahip modeller
     */
    @Query("SELECT " +
           "pr.modelId, " +
           "COUNT(pr) as totalRequests, " +
           "COUNT(CASE WHEN pr.status = 'FAILED' THEN 1 END) as failedRequests, " +
           "(COUNT(CASE WHEN pr.status = 'FAILED' THEN 1 END) * 100.0 / COUNT(pr)) as errorRate " +
           "FROM PredictionRequest pr " +
           "WHERE pr.requestDate >= :startDate " +
           "GROUP BY pr.modelId " +
           "HAVING (COUNT(CASE WHEN pr.status = 'FAILED' THEN 1 END) * 100.0 / COUNT(pr)) > :errorThreshold " +
           "ORDER BY errorRate DESC")
    List<Object[]> findHighErrorRateModels(@Param("startDate") LocalDateTime startDate,
                                          @Param("errorThreshold") Double errorThreshold);

    /**
     * Son N gün içindeki aktivite özeti
     */
    @Query("SELECT " +
           "DATE(pr.requestDate) as date, " +
           "COUNT(pr) as totalRequests, " +
           "COUNT(CASE WHEN pr.status = 'COMPLETED' THEN 1 END) as successfulRequests, " +
           "COUNT(CASE WHEN pr.status = 'FAILED' THEN 1 END) as failedRequests, " +
           "AVG(pr.responseTimeMs) as avgResponseTime, " +
           "COUNT(DISTINCT pr.modelId) as uniqueModels " +
           "FROM PredictionRequest pr " +
           "WHERE pr.requestDate >= :startDate " +
           "GROUP BY DATE(pr.requestDate) " +
           "ORDER BY date DESC")
    List<Object[]> getDailyActivitySummary(@Param("startDate") LocalDateTime startDate);

    /**
     * Model karşılaştırma metrikleri
     */
    @Query("SELECT " +
           "pr.modelId, " +
           "COUNT(pr) as requestCount, " +
           "AVG(pr.responseTimeMs) as avgResponseTime, " +
           "AVG(pr.confidence) as avgConfidence, " +
           "COUNT(CASE WHEN pr.status = 'COMPLETED' THEN 1 END) as successCount, " +
           "COUNT(CASE WHEN pr.status = 'FAILED' THEN 1 END) as failureCount " +
           "FROM PredictionRequest pr " +
           "WHERE pr.requestDate >= :startDate " +
           "GROUP BY pr.modelId " +
           "ORDER BY requestCount DESC")
    List<Object[]> getModelComparisonMetrics(@Param("startDate") LocalDateTime startDate);
}