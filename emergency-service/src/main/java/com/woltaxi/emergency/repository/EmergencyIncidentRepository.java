package com.woltaxi.emergency.repository;

import com.woltaxi.emergency.entity.EmergencyIncident;
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
 * Emergency Incident Repository - Acil Durum Olayları Veri Erişim Katmanı
 * Bu interface, emergency_incidents tablosu için veri erişim operasyonlarını sağlar
 */
@Repository
public interface EmergencyIncidentRepository extends JpaRepository<EmergencyIncident, String> {

    /**
     * Kullanıcının acil durum olaylarını al
     */
    List<EmergencyIncident> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Kullanıcının acil durum olaylarını sayfalı al
     */
    Page<EmergencyIncident> findByUserId(String userId, Pageable pageable);

    /**
     * Sürücünün dahil olduğu acil durum olaylarını al
     */
    List<EmergencyIncident> findByDriverIdOrderByCreatedAtDesc(String driverId);

    /**
     * Yolculuğa ait acil durum olaylarını al
     */
    List<EmergencyIncident> findByTripIdOrderByCreatedAtDesc(String tripId);

    /**
     * Belirli durumda olan acil durum olaylarını al
     */
    List<EmergencyIncident> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Belirli öncelik seviyesindeki acil durum olaylarını al
     */
    List<EmergencyIncident> findByPriorityOrderByCreatedAtDesc(String priority);

    /**
     * Olay numarası ile acil durum olayını bul
     */
    Optional<EmergencyIncident> findByIncidentNumber(String incidentNumber);

    /**
     * Aktif acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS') ORDER BY e.createdAt DESC")
    List<EmergencyIncident> findActiveIncidents();

    /**
     * Belirli tarih aralığındaki acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.createdAt BETWEEN :startDate AND :endDate ORDER BY e.createdAt DESC")
    List<EmergencyIncident> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Kullanıcının belirli tarih aralığındaki acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.userId = :userId AND e.createdAt BETWEEN :startDate AND :endDate ORDER BY e.createdAt DESC")
    List<EmergencyIncident> findByUserIdAndDateRange(@Param("userId") String userId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Belirli olay türündeki acil durum olaylarını al
     */
    List<EmergencyIncident> findByIncidentTypeOrderByCreatedAtDesc(String incidentType);

    /**
     * Kritik öncelikli ve aktif acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.priority = 'CRITICAL' AND e.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS') ORDER BY e.createdAt DESC")
    List<EmergencyIncident> findCriticalActiveIncidents();

    /**
     * Belirli güvenlik modundaki acil durum olaylarını al
     */
    List<EmergencyIncident> findBySafetyModeOrderByCreatedAtDesc(String safetyMode);

    /**
     * Uzun süredir çözülmemiş acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS') AND e.createdAt < :threshold ORDER BY e.createdAt ASC")
    List<EmergencyIncident> findUnresolvedIncidentsOlderThan(@Param("threshold") LocalDateTime threshold);

    /**
     * Eskalasyon gerektiren acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.escalated = false AND e.status IN ('ACTIVE', 'ACKNOWLEDGED') AND e.createdAt < :escalationThreshold ORDER BY e.priority DESC, e.createdAt ASC")
    List<EmergencyIncident> findIncidentsRequiringEscalation(@Param("escalationThreshold") LocalDateTime escalationThreshold);

    /**
     * Test olmayan acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.isTestIncident = false ORDER BY e.createdAt DESC")
    List<EmergencyIncident> findNonTestIncidents(Pageable pageable);

    /**
     * Belirli bölgedeki acil durum olaylarını al (koordinat aralığı)
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.latitude BETWEEN :minLat AND :maxLat AND e.longitude BETWEEN :minLng AND :maxLng ORDER BY e.createdAt DESC")
    List<EmergencyIncident> findByLocationBounds(@Param("minLat") Double minLatitude,
                                               @Param("maxLat") Double maxLatitude,
                                               @Param("minLng") Double minLongitude,
                                               @Param("maxLng") Double maxLongitude);

    /**
     * Belirli şehirdeki acil durum olaylarını al
     */
    List<EmergencyIncident> findByCityOrderByCreatedAtDesc(String city);

    /**
     * Belirli ülkedeki acil durum olaylarını al
     */
    List<EmergencyIncident> findByCountryCodeOrderByCreatedAtDesc(String countryCode);

    /**
     * Operatör atanmış acil durum olaylarını al
     */
    List<EmergencyIncident> findByAssignedOperatorIdOrderByCreatedAtDesc(String operatorId);

    /**
     * Operatör atanmamış aktif acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.assignedOperatorId IS NULL AND e.status IN ('ACTIVE', 'ACKNOWLEDGED') ORDER BY e.priority DESC, e.createdAt ASC")
    List<EmergencyIncident> findUnassignedActiveIncidents();

    /**
     * Belirli risk skoru üzerindeki acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.riskScore >= :minRiskScore ORDER BY e.riskScore DESC, e.createdAt DESC")
    List<EmergencyIncident> findByMinimumRiskScore(@Param("minRiskScore") Integer minRiskScore);

    /**
     * Yetkililerin iletişime geçtiği acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.authoritiesContacted = true ORDER BY e.createdAt DESC")
    List<EmergencyIncident> findIncidentsWithAuthoritiesContacted();

    /**
     * Takip gerektiren çözülmüş acil durum olaylarını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.status = 'RESOLVED' AND e.followUpRequired = true AND (e.followUpScheduledAt IS NULL OR e.followUpScheduledAt <= :now) ORDER BY e.resolvedAt ASC")
    List<EmergencyIncident> findIncidentsRequiringFollowUp(@Param("now") LocalDateTime now);

    /**
     * İstatistik sorguları
     */
    @Query("SELECT COUNT(e) FROM EmergencyIncident e WHERE e.createdAt >= :startDate")
    Long countIncidentsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(e) FROM EmergencyIncident e WHERE e.status = :status AND e.createdAt >= :startDate")
    Long countIncidentsByStatusSince(@Param("status") String status, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(e) FROM EmergencyIncident e WHERE e.priority = :priority AND e.createdAt >= :startDate")
    Long countIncidentsByPrioritySince(@Param("priority") String priority, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(e) FROM EmergencyIncident e WHERE e.incidentType = :incidentType AND e.createdAt >= :startDate")
    Long countIncidentsByTypeSince(@Param("incidentType") String incidentType, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(EXTRACT(EPOCH FROM (e.resolvedAt - e.createdAt))/60) FROM EmergencyIncident e WHERE e.resolvedAt IS NOT NULL AND e.createdAt >= :startDate")
    Double getAverageResolutionTimeMinutesSince(@Param("startDate") LocalDateTime startDate);

    /**
     * Performans metrikleri sorguları
     */
    @Query("SELECT COUNT(e) FROM EmergencyIncident e WHERE e.acknowledgedAt IS NOT NULL AND EXTRACT(EPOCH FROM (e.acknowledgedAt - e.createdAt)) <= :thresholdSeconds AND e.createdAt >= :startDate")
    Long countIncidentsAcknowledgedWithinThreshold(@Param("thresholdSeconds") Long thresholdSeconds, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(e) FROM EmergencyIncident e WHERE e.resolvedAt IS NOT NULL AND EXTRACT(EPOCH FROM (e.resolvedAt - e.createdAt)) <= :thresholdSeconds AND e.createdAt >= :startDate")
    Long countIncidentsResolvedWithinThreshold(@Param("thresholdSeconds") Long thresholdSeconds, @Param("startDate") LocalDateTime startDate);

    /**
     * Müşteri memnuniyeti sorguları
     */
    @Query("SELECT AVG(e.satisfactionRating) FROM EmergencyIncident e WHERE e.satisfactionRating IS NOT NULL AND e.createdAt >= :startDate")
    Double getAverageSatisfactionRatingSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(e) FROM EmergencyIncident e WHERE e.satisfactionRating >= :minRating AND e.createdAt >= :startDate")
    Long countIncidentsWithMinimumSatisfactionSince(@Param("minRating") Integer minRating, @Param("startDate") LocalDateTime startDate);

    /**
     * Eskalasyon istatistikleri
     */
    @Query("SELECT COUNT(e) FROM EmergencyIncident e WHERE e.escalated = true AND e.createdAt >= :startDate")
    Long countEscalatedIncidentsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(e) FROM EmergencyIncident e WHERE e.escalationLevel = :level AND e.createdAt >= :startDate")
    Long countIncidentsByEscalationLevelSince(@Param("level") String level, @Param("startDate") LocalDateTime startDate);

    /**
     * En son oluşturulan N olay
     */
    @Query("SELECT e FROM EmergencyIncident e ORDER BY e.createdAt DESC")
    List<EmergencyIncident> findLatestIncidents(Pageable pageable);

    /**
     * Kullanıcının son acil durum olayını al
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE e.userId = :userId ORDER BY e.createdAt DESC")
    Optional<EmergencyIncident> findLatestIncidentByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * Batch işlemler için aktif olay ID'lerini al
     */
    @Query("SELECT e.id FROM EmergencyIncident e WHERE e.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    List<String> findActiveIncidentIds();

    /**
     * Özel arama - metin tabanlı
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE " +
           "LOWER(e.incidentNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.address) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY e.createdAt DESC")
    List<EmergencyIncident> searchIncidents(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Kapsamlı filtreleme
     */
    @Query("SELECT e FROM EmergencyIncident e WHERE " +
           "(:userId IS NULL OR e.userId = :userId) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:priority IS NULL OR e.priority = :priority) AND " +
           "(:incidentType IS NULL OR e.incidentType = :incidentType) AND " +
           "(:startDate IS NULL OR e.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR e.createdAt <= :endDate) " +
           "ORDER BY e.createdAt DESC")
    Page<EmergencyIncident> findWithFilters(@Param("userId") String userId,
                                           @Param("status") String status,
                                           @Param("priority") String priority,
                                           @Param("incidentType") String incidentType,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);
}