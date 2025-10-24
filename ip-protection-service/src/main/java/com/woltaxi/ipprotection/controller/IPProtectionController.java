package com.woltaxi.ipprotection.controller;

import com.woltaxi.ipprotection.entity.SoftwareLicense;
import com.woltaxi.ipprotection.service.LicenseValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * IP Protection Controller
 * REST API endpoints for license validation and IP protection services
 */
@RestController
@RequestMapping("/api/v1/ip-protection")
@RequiredArgsConstructor
@Slf4j
public class IPProtectionController {
    
    private final LicenseValidationService licenseValidationService;
    
    /**
     * Validate software license
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateLicense(
            @RequestParam String licenseKey,
            @RequestParam(required = false) String feature,
            HttpServletRequest request) {
        
        try {
            // Build validation context
            LicenseValidationService.ValidationContext context = new LicenseValidationService.ValidationContext();
            context.setIpAddress(getClientIpAddress(request));
            context.setUserAgent(request.getHeader("User-Agent"));
            context.setRequestedFeature(feature);
            
            // Validate license
            LicenseValidationService.LicenseValidationResult result = 
                licenseValidationService.validateLicense(licenseKey, context);
            
            if (result.isValid()) {
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", result.getMessage(),
                    "validatedAt", result.getValidatedAt(),
                    "licenseKey", licenseKey
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", result.getMessage(),
                    "validatedAt", result.getValidatedAt()
                ));
            }
            
        } catch (Exception e) {
            log.error("License validation error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "valid", false,
                "message", "Internal validation error"
            ));
        }
    }
    
    /**
     * Get license information
     */
    @GetMapping("/license/{licenseKey}")
    public ResponseEntity<?> getLicenseInfo(@PathVariable String licenseKey) {
        try {
            return licenseValidationService.getLicense(licenseKey)
                .map(license -> ResponseEntity.ok(buildLicenseResponse(license)))
                .orElse(ResponseEntity.notFound().build());
                
        } catch (Exception e) {
            log.error("Error retrieving license info: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Unable to retrieve license information"
            ));
        }
    }
    
    /**
     * Get active licenses for customer
     */
    @GetMapping("/customer/{customerId}/licenses")
    public ResponseEntity<?> getCustomerLicenses(@PathVariable String customerId) {
        try {
            List<SoftwareLicense> licenses = licenseValidationService.getActiveLicenses(customerId);
            
            List<Map<String, Object>> licenseResponses = licenses.stream()
                .map(this::buildLicenseResponse)
                .toList();
                
            return ResponseEntity.ok(Map.of(
                "customerId", customerId,
                "licenses", licenseResponses,
                "count", licenses.size()
            ));
            
        } catch (Exception e) {
            log.error("Error retrieving customer licenses: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Unable to retrieve customer licenses"
            ));
        }
    }
    
    /**
     * Check if license needs renewal
     */
    @GetMapping("/license/{licenseKey}/renewal-status")
    public ResponseEntity<?> checkRenewalStatus(@PathVariable String licenseKey) {
        try {
            boolean needsRenewal = licenseValidationService.needsRenewal(licenseKey);
            
            return ResponseEntity.ok(Map.of(
                "licenseKey", licenseKey,
                "needsRenewal", needsRenewal,
                "message", needsRenewal ? "License renewal required" : "License is current"
            ));
            
        } catch (Exception e) {
            log.error("Error checking renewal status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Unable to check renewal status"
            ));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "IP Protection Service",
            "timestamp", java.time.LocalDateTime.now(),
            "version", "1.0.0"
        ));
    }
    
    /**
     * Get service statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getServiceStats() {
        try {
            return ResponseEntity.ok(Map.of(
                "service", "WOLTAXI IP Protection",
                "features", List.of(
                    "License Validation",
                    "Usage Tracking", 
                    "Digital Watermarking",
                    "Security Validation",
                    "Anti-Tampering"
                ),
                "status", "ACTIVE",
                "uptime", getUptime()
            ));
            
        } catch (Exception e) {
            log.error("Error retrieving service stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Unable to retrieve service statistics"
            ));
        }
    }
    
    /**
     * Build license response object
     */
    private Map<String, Object> buildLicenseResponse(SoftwareLicense license) {
        return Map.of(
            "licenseKey", license.getLicenseKey(),
            "productName", license.getProductName(),
            "productVersion", license.getProductVersion(),
            "licenseType", license.getLicenseType(),
            "status", license.getStatus(),
            "customerId", license.getCustomerId(),
            "customerName", license.getCustomerName() != null ? license.getCustomerName() : "",
            "issuedAt", license.getIssuedAt(),
            "expiresAt", license.getExpiresAt(),
            "isValid", license.isValid(),
            "remainingDays", license.getRemainingDays(),
            "usagePercentage", license.getUsagePercentage()
        );
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Get service uptime
     */
    private String getUptime() {
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeSeconds = uptimeMs / 1000;
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}