package com.woltaxi.ipprotection.service;

import com.woltaxi.ipprotection.entity.UsageTracking;
import com.woltaxi.ipprotection.entity.SoftwareLicense;
import com.woltaxi.ipprotection.repository.UsageTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Usage Tracking Service
 * Handles real-time usage tracking, analytics, and compliance monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsageTrackingService {
    
    private final UsageTrackingRepository usageTrackingRepository;
    
    /**
     * Track license usage asynchronously
     */
    @Async
    @Transactional
    public CompletableFuture<Void> trackLicenseUsage(SoftwareLicense license, 
                                                     LicenseValidationService.ValidationContext context) {
        try {
            UsageTracking usage = UsageTracking.builder()
                    .licenseKey(license.getLicenseKey())
                    .customerId(license.getCustomerId())
                    .eventType(UsageTracking.EventType.LICENSE_VALIDATION.name())
                    .userId(context.getUserId())
                    .sessionId(context.getSessionId())
                    .ipAddress(context.getIpAddress())
                    .userAgent(context.getUserAgent())
                    .country(context.getCountry())
                    .hardwareInfo(context.getHardwareInfo())
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            usageTrackingRepository.save(usage);
            log.debug("License usage tracked for: {}", license.getLicenseKey());
            
        } catch (Exception e) {
            log.error("Error tracking license usage: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Get active sessions count for a license
     */
    public long getActiveSessionsCount(String licenseKey) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return usageTrackingRepository.findUsageByLicenseWithinDateRange(
                licenseKey, oneHourAgo, LocalDateTime.now()).size();
    }
    
    /**
     * Track API usage
     */
    @Async
    @Transactional
    public CompletableFuture<Void> trackApiUsage(String licenseKey, String customerId, 
                                                String apiEndpoint, long responseTime) {
        try {
            UsageTracking usage = UsageTracking.builder()
                    .licenseKey(licenseKey)
                    .customerId(customerId)
                    .eventType(UsageTracking.EventType.API_CALL.name())
                    .apiEndpoint(apiEndpoint)
                    .responseTimeMs(responseTime)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            usageTrackingRepository.save(usage);
            
        } catch (Exception e) {
            log.error("Error tracking API usage: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Track feature access
     */
    @Async
    @Transactional
    public CompletableFuture<Void> trackFeatureAccess(String licenseKey, String customerId, 
                                                     String featureName, String userId) {
        try {
            UsageTracking usage = UsageTracking.builder()
                    .licenseKey(licenseKey)
                    .customerId(customerId)
                    .eventType(UsageTracking.EventType.FEATURE_ACCESS.name())
                    .featureName(featureName)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            usageTrackingRepository.save(usage);
            
        } catch (Exception e) {
            log.error("Error tracking feature access: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Get usage statistics for a license
     */
    public List<Object[]> getUsageStatistics(String licenseKey) {
        return usageTrackingRepository.getUsageStatisticsByLicense(licenseKey);
    }
    
    /**
     * Find suspicious activities
     */
    public List<UsageTracking> findSuspiciousActivities() {
        return usageTrackingRepository.findSuspiciousActivities();
    }
    
    /**
     * Clean old usage records (data retention)
     */
    @Transactional
    public void cleanOldUsageRecords(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        usageTrackingRepository.deleteOldUsageRecords(cutoffDate);
        log.info("Cleaned usage records older than {} days", retentionDays);
    }
}