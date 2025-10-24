package com.woltaxi.ipprotection.repository;

import com.woltaxi.ipprotection.entity.DigitalWatermark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DigitalWatermark entity
 * Handles database operations for digital watermarks and content protection
 */
@Repository
public interface DigitalWatermarkRepository extends JpaRepository<DigitalWatermark, Long> {
    
    /**
     * Find watermark by watermark ID
     */
    Optional<DigitalWatermark> findByWatermarkId(String watermarkId);
    
    /**
     * Find watermarks by license key
     */
    List<DigitalWatermark> findByLicenseKey(String licenseKey);
    
    /**
     * Find watermarks by customer ID
     */
    List<DigitalWatermark> findByCustomerId(String customerId);
    
    /**
     * Find watermarks by content ID
     */
    List<DigitalWatermark> findByContentId(String contentId);
    
    /**
     * Find watermarks by content type
     */
    List<DigitalWatermark> findByContentType(String contentType);
    
    /**
     * Find watermarks by file hash
     */
    Optional<DigitalWatermark> findByFileHash(String fileHash);
    
    /**
     * Find active watermarks
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.isActive = true")
    List<DigitalWatermark> findActiveWatermarksBy();
    
    /**
     * Find watermarks by status
     */
    List<DigitalWatermark> findByStatus(DigitalWatermark.WatermarkStatus status);
    
    /**
     * Find watermarks by type
     */
    List<DigitalWatermark> findByWatermarkType(DigitalWatermark.WatermarkType watermarkType);
    
    /**
     * Find watermarks by method
     */
    List<DigitalWatermark> findByMethod(DigitalWatermark.WatermarkMethod method);
    
    /**
     * Find expired watermarks
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.expirationDate < :currentDate AND w.isActive = true")
    List<DigitalWatermark> findExpiredWatermarksBy(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find watermarks expiring within specified days
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.expirationDate BETWEEN :currentDate AND :expirationThreshold AND w.isActive = true")
    List<DigitalWatermark> findWatermarksExpiringWithin(@Param("currentDate") LocalDateTime currentDate,
                                                         @Param("expirationThreshold") LocalDateTime expirationThreshold);
    
    /**
     * Find watermarks by device fingerprint
     */
    List<DigitalWatermark> findByDeviceFingerprint(String deviceFingerprint);
    
    /**
     * Find watermarks by geo location
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.geoLocation LIKE %:location%")
    List<DigitalWatermark> findByGeoLocation(@Param("location") String location);
    
    /**
     * Find watermarks by user info
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.userInfo LIKE %:userInfo%")
    List<DigitalWatermark> findByUserInfo(@Param("userInfo") String userInfo);
    
    /**
     * Find watermarks by file name
     */
    List<DigitalWatermark> findByFileName(String fileName);
    
    /**
     * Find watermarks by original source
     */
    List<DigitalWatermark> findByOriginalSource(String originalSource);
    
    /**
     * Find watermarks by distribution channel
     */
    List<DigitalWatermark> findByDistributionChannel(String distributionChannel);
    
    /**
     * Find visible watermarks
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.isVisible = true")
    List<DigitalWatermark> findVisibleWatermarksBy();
    
    /**
     * Find invisible watermarks
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.isVisible = false")
    List<DigitalWatermark> findInvisibleWatermarksBy();
    
    /**
     * Find detectable watermarks
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.isDetectable = true")
    List<DigitalWatermark> findDetectableWatermarksBy();
    
    /**
     * Find removable watermarks
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.isRemovable = true")
    List<DigitalWatermark> findRemovableWatermarksBy();
    
    /**
     * Find watermarks by strength level
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.strengthLevel >= :minStrength")
    List<DigitalWatermark> findWatermarksByMinStrength(@Param("minStrength") Integer minStrength);
    
    /**
     * Find watermarks by algorithm
     */
    List<DigitalWatermark> findByAlgorithm(String algorithm);
    
    /**
     * Find watermarks created within date range
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.createdAt BETWEEN :startDate AND :endDate")
    List<DigitalWatermark> findWatermarksCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find recently validated watermarks
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.lastValidated > :since ORDER BY w.lastValidated DESC")
    List<DigitalWatermark> findRecentlyValidated(@Param("since") LocalDateTime since);
    
    /**
     * Find suspicious watermarks
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.status = 'SUSPICIOUS' OR w.status = 'CORRUPTED'")
    List<DigitalWatermark> findSuspiciousWatermarksBy();
    
    /**
     * Count watermarks by customer
     */
    @Query("SELECT COUNT(w) FROM DigitalWatermark w WHERE w.customerId = :customerId AND w.isActive = true")
    Long countActiveWatermarksByCustomer(@Param("customerId") String customerId);
    
    /**
     * Count watermarks by content type
     */
    @Query("SELECT w.contentType, COUNT(w) FROM DigitalWatermark w GROUP BY w.contentType")
    List<Object[]> countWatermarksByContentTypeBy();
    
    /**
     * Find duplicate watermarks (same content hash)
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.fileHash = :fileHash AND w.isActive = true")
    List<DigitalWatermark> findDuplicateWatermarksBy(@Param("fileHash") String fileHash);
    
    /**
     * Update watermark validation timestamp
     */
    @Query("UPDATE DigitalWatermark w SET w.lastValidated = :validationTime, w.status = :status WHERE w.watermarkId = :watermarkId")
    void updateValidationTimestamp(@Param("watermarkId") String watermarkId,
                                   @Param("validationTime") LocalDateTime validationTime,
                                   @Param("status") DigitalWatermark.WatermarkStatus status);
    
    /**
     * Deactivate expired watermarks
     */
    @Query("UPDATE DigitalWatermark w SET w.isActive = false, w.status = 'EXPIRED' WHERE w.expirationDate < :currentDate AND w.isActive = true")
    void deactivateExpiredWatermarksBy(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find watermarks needing renewal
     */
    @Query("SELECT w FROM DigitalWatermark w WHERE w.expirationDate IS NOT NULL AND w.expirationDate < :renewalThreshold AND w.isActive = true")
    List<DigitalWatermark> findWatermarksNeedingRenewal(@Param("renewalThreshold") LocalDateTime renewalThreshold);
    
    /**
     * Find watermarks by access level
     */
    List<DigitalWatermark> findByAccessLevel(String accessLevel);
    
    /**
     * Get watermark statistics
     */
    @Query("SELECT " +
           "COUNT(w) as totalWatermarks, " +
           "COUNT(CASE WHEN w.isActive = true THEN 1 END) as activeWatermarks, " +
           "COUNT(CASE WHEN w.status = 'VALIDATED' THEN 1 END) as validatedWatermarks, " +
           "COUNT(CASE WHEN w.status = 'SUSPICIOUS' THEN 1 END) as suspiciousWatermarks " +
           "FROM DigitalWatermark w")
    List<Object[]> getWatermarkStatisticsBy();
}