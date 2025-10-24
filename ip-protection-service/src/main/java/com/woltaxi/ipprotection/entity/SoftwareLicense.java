package com.woltaxi.ipprotection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Software License Entity
 * Manages software licenses, validation, and usage tracking
 */
@Entity
@Table(name = "software_licenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoftwareLicense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String licenseKey;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private String productVersion;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenseType licenseType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenseStatus status;
    
    // Customer Information
    @Column(nullable = false)
    private String customerId;
    
    private String customerName;
    private String customerEmail;
    private String organizationName;
    
    // License Validity
    @Column(nullable = false)
    private LocalDateTime issuedAt;
    
    private LocalDateTime expiresAt;
    private LocalDateTime activatedAt;
    private LocalDateTime lastValidatedAt;
    
    // Usage Limits
    private Integer maxUsers;
    private Integer currentUsers;
    private Integer maxInstallations;
    private Integer currentInstallations;
    private Long maxApiCalls;
    private Long currentApiCalls;
    
    // Feature Permissions
    @ElementCollection
    @CollectionTable(name = "license_features")
    private Map<String, Boolean> enabledFeatures;
    
    // Hardware Binding
    private String hardwareFingerprint;
    private String macAddress;
    private String cpuId;
    
    // Geographic Restrictions
    private String allowedCountries;
    private String restrictedCountries;
    
    // Legal Information
    @Column(length = 2000)
    private String termsAndConditions;
    
    @Column(length = 1000)
    private String licenseAgreement;
    
    // Security
    private String digitalSignature;
    private String encryptionKey;
    
    // Tracking
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String lastModifiedBy;
    
    // Audit Trail
    @Column(length = 4000)
    private String auditLog;
    
    /**
     * License Types
     */
    public enum LicenseType {
        TRIAL,              // Trial license
        PERSONAL,           // Personal use
        COMMERCIAL,         // Commercial use
        ENTERPRISE,         // Enterprise license
        DEVELOPER,          // Developer license
        EDUCATIONAL,        // Educational use
        NON_PROFIT,         // Non-profit organizations
        OEM,                // OEM licensing
        SUBSCRIPTION,       // Subscription-based
        PERPETUAL          // One-time purchase
    }
    
    /**
     * License Status
     */
    public enum LicenseStatus {
        ACTIVE,            // Active and valid
        EXPIRED,           // License expired
        SUSPENDED,         // Temporarily suspended
        REVOKED,           // Permanently revoked
        PENDING,           // Pending activation
        INACTIVE,          // Not yet activated
        VIOLATED           // License terms violated
    }
    
    /**
     * Check if license is currently valid
     */
    public boolean isValid() {
        if (status != LicenseStatus.ACTIVE) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Check expiration
        if (expiresAt != null && now.isAfter(expiresAt)) {
            return false;
        }
        
        // Check usage limits
        if (maxUsers != null && currentUsers != null && currentUsers > maxUsers) {
            return false;
        }
        
        if (maxInstallations != null && currentInstallations != null && 
            currentInstallations > maxInstallations) {
            return false;
        }
        
        if (maxApiCalls != null && currentApiCalls != null && currentApiCalls > maxApiCalls) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if feature is enabled
     */
    public boolean isFeatureEnabled(String feature) {
        return enabledFeatures != null && 
               enabledFeatures.getOrDefault(feature, false);
    }
    
    /**
     * Get remaining days until expiration
     */
    public Long getRemainingDays() {
        if (expiresAt == null) {
            return null; // Perpetual license
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return 0L; // Expired
        }
        
        return java.time.Duration.between(now, expiresAt).toDays();
    }
    
    /**
     * Calculate usage percentage
     */
    public double getUsagePercentage() {
        if (maxUsers == null || currentUsers == null) {
            return 0.0;
        }
        
        return (double) currentUsers / maxUsers * 100.0;
    }
    
    /**
     * Add audit log entry
     */
    public void addAuditEntry(String entry) {
        String timestamp = LocalDateTime.now().toString();
        String newEntry = timestamp + ": " + entry;
        
        if (auditLog == null) {
            auditLog = newEntry;
        } else {
            auditLog += "\n" + newEntry;
        }
    }
}