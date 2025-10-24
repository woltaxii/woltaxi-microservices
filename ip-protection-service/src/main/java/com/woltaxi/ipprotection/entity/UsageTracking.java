package com.woltaxi.ipprotection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Usage Tracking Entity
 * Tracks software usage, API calls, and user behavior for compliance
 */
@Entity
@Table(name = "usage_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // License Information
    @Column(nullable = false)
    private String licenseKey;
    
    @Column(nullable = false)
    private String customerId;
    
    // Usage Details
    @Column(nullable = false)
    private String eventType;
    
    private String featureName;
    private String moduleName;
    private String apiEndpoint;
    private String userAgent;
    
    // Session Information
    private String sessionId;
    private String userId;
    private String username;
    private String ipAddress;
    
    // System Information
    private String operatingSystem;
    private String browserName;
    private String deviceType;
    private String hardwareInfo;
    
    // Geographic Information
    private String country;
    private String region;
    private String city;
    private Double latitude;
    private Double longitude;
    
    // Performance Metrics
    private Long responseTimeMs;
    private Long memoryUsageMb;
    private Double cpuUsagePercent;
    
    // Additional Data
    @Column(length = 4000)
    private String additionalData;
    
    @Column(length = 1000)
    private String errorMessage;
    
    // Timestamps
    @CreationTimestamp
    private LocalDateTime timestamp;
    
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    
    /**
     * Usage Event Types
     */
    public enum EventType {
        APPLICATION_START,
        APPLICATION_STOP,
        FEATURE_ACCESS,
        API_CALL,
        USER_LOGIN,
        USER_LOGOUT,
        LICENSE_VALIDATION,
        ERROR_OCCURRED,
        SECURITY_VIOLATION,
        UNAUTHORIZED_ACCESS
    }
    
    /**
     * Calculate session duration in minutes
     */
    public Long getSessionDurationMinutes() {
        if (sessionStartTime == null || sessionEndTime == null) {
            return null;
        }
        
        return java.time.Duration.between(sessionStartTime, sessionEndTime).toMinutes();
    }
    
    /**
     * Check if this is a suspicious activity
     */
    public boolean isSuspiciousActivity() {
        // Multiple login attempts from different IPs
        // Unusual API usage patterns
        // Access from restricted countries
        // High-frequency requests
        return eventType.equals("SECURITY_VIOLATION") || 
               eventType.equals("UNAUTHORIZED_ACCESS") ||
               (responseTimeMs != null && responseTimeMs > 10000) ||
               (cpuUsagePercent != null && cpuUsagePercent > 90.0);
    }
}