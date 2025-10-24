package com.woltaxi.aiml.repository;

import com.woltaxi.aiml.entity.MLModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ML Model Repository
 * 
 * Machine Learning model verilerinin veritabanı işlemleri
 */
@Repository
public interface MLModelRepository extends JpaRepository<MLModel, Long> {

    /**
     * Model adına göre arama
     */
    Optional<MLModel> findByModelName(String modelName);

    /**
     * Model adının varlığını kontrol et
     */
    boolean existsByModelName(String modelName);

    /**
     * Aktif modelleri getir
     */
    List<MLModel> findByIsActiveTrue();

    /**
     * Model tipine göre arama
     */
    List<MLModel> findByModelType(String modelType);

    /**
     * Framework'e göre arama
     */
    List<MLModel> findByFramework(String framework);

    /**
     * Duruma göre arama
     */
    List<MLModel> findByStatus(MLModel.ModelStatus status);

    /**
     * Aktif model sayısını getir
     */
    long countByIsActiveTrue();

    /**
     * Model tipine göre sayı
     */
    long countByModelType(String modelType);

    /**
     * Framework'e göre sayı
     */
    long countByFramework(String framework);

    /**
     * Belirli tarihten sonra oluşturulan modeller
     */
    List<MLModel> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Belirli tarihten sonra güncellenen modeller
     */
    List<MLModel> findByUpdatedAtAfter(LocalDateTime date);

    /**
     * Drift tespit edilen modeller
     */
    List<MLModel> findByDriftDetectedTrue();

    /**
     * Son tahmin tarihine göre sıralı modeller
     */
    List<MLModel> findByIsActiveTrueOrderByLastPredictionDateDesc();

    /**
     * En yüksek accuracy'ye sahip modeller
     */
    @Query("SELECT m FROM MLModel m WHERE m.accuracy IS NOT NULL ORDER BY m.accuracy DESC")
    List<MLModel> findTopModelsByAccuracy(Pageable pageable);

    /**
     * Belirli kullanıcı tarafından oluşturulan modeller
     */
    List<MLModel> findByCreatedBy(String createdBy);

    /**
     * Deployment ortamına göre modeller
     */
    List<MLModel> findByDeploymentEnvironment(String environment);

    /**
     * Filtreleme ile arama
     */
    @Query("SELECT m FROM MLModel m WHERE " +
           "(:modelType IS NULL OR m.modelType = :modelType) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:framework IS NULL OR m.framework = :framework)")
    Page<MLModel> findByFilters(@Param("modelType") String modelType,
                               @Param("status") String status,
                               @Param("framework") String framework,
                               Pageable pageable);

    /**
     * Performans aralığına göre modeller
     */
    @Query("SELECT m FROM MLModel m WHERE " +
           "m.accuracy BETWEEN :minAccuracy AND :maxAccuracy")
    List<MLModel> findByAccuracyRange(@Param("minAccuracy") Double minAccuracy,
                                     @Param("maxAccuracy") Double maxAccuracy);

    /**
     * Belirli süre içinde tahmin yapılmamış modeller
     */
    @Query("SELECT m FROM MLModel m WHERE " +
           "m.isActive = true AND " +
           "(m.lastPredictionDate IS NULL OR m.lastPredictionDate < :date)")
    List<MLModel> findInactiveModels(@Param("date") LocalDateTime date);

    /**
     * En çok kullanılan modeller
     */
    @Query("SELECT m FROM MLModel m WHERE m.totalPredictions > 0 " +
           "ORDER BY m.totalPredictions DESC")
    List<MLModel> findMostUsedModels(Pageable pageable);

    /**
     * Model versiyonlarını getir
     */
    @Query("SELECT m FROM MLModel m WHERE m.modelName = :modelName " +
           "ORDER BY m.version DESC")
    List<MLModel> findModelVersions(@Param("modelName") String modelName);

    /**
     * Son deployment edilen modeller
     */
    @Query("SELECT m FROM MLModel m WHERE m.deploymentDate IS NOT NULL " +
           "ORDER BY m.deploymentDate DESC")
    List<MLModel> findRecentlyDeployedModels(Pageable pageable);

    /**
     * Ortalama response time'a göre modeller
     */
    @Query("SELECT m FROM MLModel m WHERE m.avgResponseTime IS NOT NULL " +
           "ORDER BY m.avgResponseTime ASC")
    List<MLModel> findFastestModels(Pageable pageable);

    /**
     * Model istatistikleri
     */
    @Query("SELECT " +
           "COUNT(m) as totalCount, " +
           "COUNT(CASE WHEN m.isActive = true THEN 1 END) as activeCount, " +
           "COUNT(CASE WHEN m.status = 'DEPLOYED' THEN 1 END) as deployedCount, " +
           "COUNT(CASE WHEN m.driftDetected = true THEN 1 END) as driftCount " +
           "FROM MLModel m")
    Object[] getModelStatistics();

    /**
     * Framework istatistikleri
     */
    @Query("SELECT m.framework, COUNT(m) FROM MLModel m GROUP BY m.framework")
    List<Object[]> getFrameworkStatistics();

    /**
     * Model tipi istatistikleri
     */
    @Query("SELECT m.modelType, COUNT(m) FROM MLModel m GROUP BY m.modelType")
    List<Object[]> getModelTypeStatistics();

    /**
     * Aylık model oluşturma istatistikleri
     */
    @Query("SELECT " +
           "EXTRACT(YEAR FROM m.createdAt) as year, " +
           "EXTRACT(MONTH FROM m.createdAt) as month, " +
           "COUNT(m) as count " +
           "FROM MLModel m " +
           "WHERE m.createdAt >= :startDate " +
           "GROUP BY EXTRACT(YEAR FROM m.createdAt), EXTRACT(MONTH FROM m.createdAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyCreationStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Performans trend analizi
     */
    @Query("SELECT " +
           "m.modelType, " +
           "AVG(m.accuracy) as avgAccuracy, " +
           "AVG(m.precision) as avgPrecision, " +
           "AVG(m.recall) as avgRecall, " +
           "AVG(m.f1Score) as avgF1Score " +
           "FROM MLModel m " +
           "WHERE m.accuracy IS NOT NULL " +
           "GROUP BY m.modelType")
    List<Object[]> getPerformanceTrends();

    /**
     * Kullanım istatistikleri
     */
    @Query("SELECT " +
           "m.modelType, " +
           "SUM(m.totalPredictions) as totalPredictions, " +
           "AVG(m.avgResponseTime) as avgResponseTime " +
           "FROM MLModel m " +
           "WHERE m.totalPredictions > 0 " +
           "GROUP BY m.modelType")
    List<Object[]> getUsageStatistics();

    /**
     * Drift analizi istatistikleri
     */
    @Query("SELECT " +
           "m.framework, " +
           "COUNT(CASE WHEN m.driftDetected = true THEN 1 END) as driftCount, " +
           "COUNT(m) as totalCount, " +
           "AVG(m.driftScore) as avgDriftScore " +
           "FROM MLModel m " +
           "WHERE m.lastDriftCheck IS NOT NULL " +
           "GROUP BY m.framework")
    List<Object[]> getDriftStatistics();

    /**
     * Model sağlık durumu kontrolü
     */
    @Query("SELECT m FROM MLModel m WHERE " +
           "m.isActive = true AND " +
           "(m.lastPredictionDate < :staleDate OR " +
           " m.avgResponseTime > :slowThreshold OR " +
           " m.driftDetected = true)")
    List<MLModel> findUnhealthyModels(@Param("staleDate") LocalDateTime staleDate,
                                     @Param("slowThreshold") Double slowThreshold);

    /**
     * En iyi performans gösteren modeller
     */
    @Query("SELECT m FROM MLModel m WHERE " +
           "m.accuracy IS NOT NULL AND " +
           "m.precision IS NOT NULL AND " +
           "m.recall IS NOT NULL AND " +
           "m.f1Score IS NOT NULL " +
           "ORDER BY (m.accuracy + m.precision + m.recall + m.f1Score) DESC")
    List<MLModel> findBestPerformingModels(Pageable pageable);

    /**
     * Resource kullanımına göre modeller
     */
    @Query("SELECT m FROM MLModel m WHERE " +
           "m.avgResponseTime IS NOT NULL " +
           "ORDER BY m.avgResponseTime ASC, m.totalPredictions DESC")
    List<MLModel> findMostEfficientModels(Pageable pageable);

    /**
     * Belirli tarih aralığında deploy edilen modeller
     */
    @Query("SELECT m FROM MLModel m WHERE " +
           "m.deploymentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY m.deploymentDate DESC")
    List<MLModel> findModelsDeployedBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Backup için tüm model bilgilerini getir
     */
    @Query("SELECT m FROM MLModel m ORDER BY m.createdAt ASC")
    List<MLModel> findAllForBackup();
}