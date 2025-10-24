package com.woltaxi.ipprotection.repository;

import com.woltaxi.ipprotection.entity.UsageTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for UsageTracking entity
 * Handles database operations for usage tracking and analytics
 */
@Repository
public interface UsageTrackingRepository extends JpaRepository<UsageTracking, Long> {
    
    /**
     * Find usage records by license key
     */
    List<UsageTracking> findByLicenseKey(String licenseKey);
    
    /**
     * Find usage records by customer ID
     */
    List<UsageTracking> findByCustomerId(String customerId);
    
    /**
     * Find usage records by event type
     */
    List<UsageTracking> findByEventType(String eventType);
    
    /**
     * Find usage records within date range
     */
    @Query("SELECT u FROM UsageTracking u WHERE u.timestamp BETWEEN :startDate AND :endDate")
    List<UsageTracking> findUsageWithinDateRange(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find usage records by license key within date range
     */
    @Query("SELECT u FROM UsageTracking u WHERE u.licenseKey = :licenseKey AND u.timestamp BETWEEN :startDate AND :endDate")
    List<UsageTracking> findUsageByLicenseWithinDateRange(@Param("licenseKey") String licenseKey,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count usage events by license key
     */
    @Query("SELECT COUNT(u) FROM UsageTracking u WHERE u.licenseKey = :licenseKey")
    Long countUsageByLicense(@Param("licenseKey") String licenseKey);
    
    /**
     * Count usage events by customer ID
     */
    @Query("SELECT COUNT(u) FROM UsageTracking u WHERE u.customerId = :customerId")
    Long countUsageByCustomer(@Param("customerId") String customerId);
    
    /**
     * Find suspicious activities
     */
    @Query("SELECT u FROM UsageTracking u WHERE u.eventType IN ('SECURITY_VIOLATION', 'UNAUTHORIZED_ACCESS') ORDER BY u.timestamp DESC")
    List<UsageTracking> findSuspiciousActivities();
    
    /**
     * Find high-frequency usage (potential abuse)
     */
    @Query("SELECT u.licenseKey, COUNT(u) as usageCount FROM UsageTracking u WHERE u.timestamp > :threshold GROUP BY u.licenseKey HAVING COUNT(u) > :maxUsage")
    List<Object[]> findHighFrequencyUsage(@Param("threshold") LocalDateTime threshold, @Param("maxUsage") Long maxUsage);
    
    /**
     * Find usage by IP address
     */
    List<UsageTracking> findByIpAddress(String ipAddress);
    
    /**
     * Find usage by country
     */
    List<UsageTracking> findByCountry(String country);
    
    /**
     * Find usage by feature name
     */
    List<UsageTracking> findByFeatureName(String featureName);
    
    /**
     * Find API usage statistics
     */
    @Query("SELECT u.apiEndpoint, COUNT(u) as callCount, AVG(u.responseTimeMs) as avgResponseTime FROM UsageTracking u WHERE u.apiEndpoint IS NOT NULL GROUP BY u.apiEndpoint")
    List<Object[]> findApiUsageStatistics();
    
    /**
     * Find usage by session ID
     */
    List<UsageTracking> findBySessionId(String sessionId);
    
    /**
     * Find usage by user ID
     */
    List<UsageTracking> findByUserId(String userId);
    
    /**
     * Find recent usage records
     */
    @Query("SELECT u FROM UsageTracking u WHERE u.timestamp > :since ORDER BY u.timestamp DESC")
    List<UsageTracking> findRecentUsage(@Param("since") LocalDateTime since);
    
    /**
     * Find error events
     */
    @Query("SELECT u FROM UsageTracking u WHERE u.errorMessage IS NOT NULL ORDER BY u.timestamp DESC")
    List<UsageTracking> findErrorEvents();
    
    /**
     * Find long-running sessions
     */
    @Query("SELECT u FROM UsageTracking u WHERE u.sessionStartTime IS NOT NULL AND u.sessionEndTime IS NOT NULL AND TIMESTAMPDIFF(MINUTE, u.sessionStartTime, u.sessionEndTime) > :minDuration")
    List<UsageTracking> findLongRunningSessions(@Param("minDuration") Integer minDuration);
    
    /**
     * Get usage statistics by license
     */
    @Query("SELECT " +
           "u.licenseKey, " +
           "COUNT(u) as totalEvents, " +
           "COUNT(DISTINCT u.sessionId) as uniqueSessions, " +
           "COUNT(DISTINCT u.userId) as uniqueUsers, " +
           "AVG(u.responseTimeMs) as avgResponseTime, " +
           "MAX(u.timestamp) as lastUsed " +
           "FROM UsageTracking u " +
           "WHERE u.licenseKey = :licenseKey " +
           "GROUP BY u.licenseKey")
    List<Object[]> getUsageStatisticsByLicense(@Param("licenseKey") String licenseKey);
    
    /**
     * Find usage patterns (hourly distribution)
     */
    @Query("SELECT HOUR(u.timestamp) as hour, COUNT(u) as eventCount FROM UsageTracking u WHERE u.licenseKey = :licenseKey GROUP BY HOUR(u.timestamp) ORDER BY hour")
    List<Object[]> findHourlyUsagePattern(@Param("licenseKey") String licenseKey);
    
    /**
     * Find geographic usage distribution
     */
    @Query("SELECT u.country, u.region, COUNT(u) as eventCount FROM UsageTracking u WHERE u.licenseKey = :licenseKey GROUP BY u.country, u.region ORDER BY eventCount DESC")
    List<Object[]> findGeographicUsageDistribution(@Param("licenseKey") String licenseKey);
    
    /**
     * Delete old usage records (data retention)
     */
    @Query("DELETE FROM UsageTracking u WHERE u.timestamp < :cutoffDate")
    void deleteOldUsageRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find most used features
     */
    @Query("SELECT u.featureName, COUNT(u) as usageCount FROM UsageTracking u WHERE u.featureName IS NOT NULL AND u.licenseKey = :licenseKey GROUP BY u.featureName ORDER BY usageCount DESC")
    List<Object[]> findMostUsedFeatures(@Param("licenseKey") String licenseKey);
}