package com.woltaxi.ipprotection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Digital Watermark Entity
 * Manages digital watermarks for content protection and tracking
 */
@Entity
@Table(name = "digital_watermarks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalWatermark {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Watermark Identification
    @Column(unique = true, nullable = false)
    private String watermarkId;
    
    @Column(nullable = false)
    private String licenseKey;
    
    @Column(nullable = false)
    private String customerId;
    
    // Content Information
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private String contentId;
    
    private String fileName;
    private String fileHash;
    private Long fileSize;
    
    // Watermark Details
    @Enumerated(EnumType.STRING)
    private WatermarkType watermarkType;
    
    @Enumerated(EnumType.STRING)
    private WatermarkMethod method;
    
    @Column(length = 4000)
    private String watermarkData;
    
    @Column(length = 2000)
    private String embeddedMetadata;
    
    // Security Information
    private String algorithm;
    private String secretKey;
    private Integer strengthLevel; // 1-10 scale
    
    // Visibility and Detection
    private Boolean isVisible;
    private Boolean isDetectable;
    private Boolean isRemovable;
    
    // Tracking Information
    private String originalSource;
    private String distributionChannel;
    private String accessLevel;
    
    // Geographic and User Info
    private String userInfo;
    private String geoLocation;
    private String deviceFingerprint;
    
    // Status and Validation
    @Enumerated(EnumType.STRING)
    private WatermarkStatus status;
    
    private LocalDateTime expirationDate;
    private Boolean isActive;
    
    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastValidated;
    
    // Additional Properties
    @Column(length = 2000)
    private String additionalProperties;
    
    @Column(length = 1000)
    private String notes;
    
    /**
     * Watermark Types
     */
    public enum WatermarkType {
        TEXT_OVERLAY,
        IMAGE_OVERLAY,
        DIGITAL_SIGNATURE,
        STEGANOGRAPHIC,
        METADATA_EMBEDDING,
        BLOCKCHAIN_PROOF,
        BIOMETRIC_SIGNATURE,
        FREQUENCY_DOMAIN,
        SPATIAL_DOMAIN,
        HYBRID
    }
    
    /**
     * Watermark Methods
     */
    public enum WatermarkMethod {
        LSB_INSERTION,
        DCT_EMBEDDING,
        DWT_EMBEDDING,
        SVD_DECOMPOSITION,
        SPREAD_SPECTRUM,
        ECHO_HIDING,
        PHASE_CODING,
        CORRELATION_BASED,
        FRAGILE_WATERMARK,
        ROBUST_WATERMARK
    }
    
    /**
     * Watermark Status
     */
    public enum WatermarkStatus {
        EMBEDDED,
        VALIDATED,
        CORRUPTED,
        REMOVED,
        EXPIRED,
        SUSPICIOUS,
        ACTIVE,
        INACTIVE
    }
    
    /**
     * Check if watermark is valid and active
     */
    public boolean isValidWatermark() {
        return isActive && 
               status == WatermarkStatus.ACTIVE &&
               (expirationDate == null || expirationDate.isAfter(LocalDateTime.now()));
    }
    
    /**
     * Generate watermark fingerprint for verification
     */
    public String generateFingerprint() {
        return String.format("%s-%s-%s-%d", 
                            watermarkId, 
                            customerId, 
                            contentId, 
                            fileHash != null ? fileHash.hashCode() : 0);
    }
    
    /**
     * Check if watermark needs renewal
     */
    public boolean needsRenewal() {
        if (expirationDate == null) return false;
        
        return expirationDate.isBefore(LocalDateTime.now().plusDays(7));
    }
}