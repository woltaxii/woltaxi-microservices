package com.woltaxi.ipprotection.service;

import com.woltaxi.ipprotection.entity.SoftwareLicense;
import com.woltaxi.ipprotection.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * License Validation Service
 * Handles comprehensive license validation, verification, and management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LicenseValidationService {
    
    private final LicenseRepository licenseRepository;
    private final UsageTrackingService usageTrackingService;
    private final SecurityService securityService;
    
    @Value("${ip-protection.license.max-grace-period-days:7}")
    private int maxGracePeriodDays;
    
    @Value("${ip-protection.license.validation-enabled:true}")
    private boolean validationEnabled;
    
    @Value("${ip-protection.license.strict-mode:false}")
    private boolean strictMode;
    
    /**
     * Comprehensive license validation
     */
    @Transactional
    public LicenseValidationResult validateLicense(String licenseKey, ValidationContext context) {
        log.info("Validating license: {}", licenseKey);
        
        if (!validationEnabled) {
            log.warn("License validation is disabled");
            return LicenseValidationResult.success("Validation disabled");
        }
        
        try {
            // Basic validation
            if (!isValidLicenseKeyFormat(licenseKey)) {
                return LicenseValidationResult.failure("Invalid license key format");
            }
            
            // Find license
            Optional<SoftwareLicense> licenseOpt = licenseRepository.findByLicenseKey(licenseKey);
            if (licenseOpt.isEmpty()) {
                return LicenseValidationResult.failure("License not found");
            }
            
            SoftwareLicense license = licenseOpt.get();
            
            // Perform all validation checks
            LicenseValidationResult result = performValidationChecks(license, context);
            
            // Track usage
            if (result.isValid()) {
                usageTrackingService.trackLicenseUsage(license, context);
                updateLicenseUsage(license);
            }
            
            // Log validation result
            log.info("License validation result for {}: {}", licenseKey, result.isValid() ? "VALID" : "INVALID");
            
            return result;
            
        } catch (Exception e) {
            log.error("Error validating license {}: {}", licenseKey, e.getMessage(), e);
            return LicenseValidationResult.failure("Validation error: " + e.getMessage());
        }
    }
    
    /**
     * Perform comprehensive validation checks
     */
    private LicenseValidationResult performValidationChecks(SoftwareLicense license, ValidationContext context) {
        
        // 1. Active status check
        if (!license.getIsActive()) {
            return LicenseValidationResult.failure("License is inactive");
        }
        
        // 2. Status check
        if (license.getStatus() != SoftwareLicense.LicenseStatus.ACTIVE) {
            return LicenseValidationResult.failure("License status: " + license.getStatus());
        }
        
        // 3. Expiration check
        if (isExpired(license)) {
            return LicenseValidationResult.failure("License expired");
        }
        
        // 4. Usage limits check
        if (isUsageLimitExceeded(license)) {
            return LicenseValidationResult.failure("Usage limit exceeded");
        }
        
        // 5. Hardware fingerprint check
        if (strictMode && !isValidHardwareFingerprint(license, context)) {
            return LicenseValidationResult.failure("Hardware fingerprint mismatch");
        }
        
        // 6. Geographic restrictions check
        if (!isValidGeographicAccess(license, context)) {
            return LicenseValidationResult.failure("Geographic access restricted");
        }
        
        // 7. Feature access check
        if (!isValidFeatureAccess(license, context)) {
            return LicenseValidationResult.failure("Feature access restricted");
        }
        
        // 8. Concurrent usage check
        if (isConcurrentUsageExceeded(license)) {
            return LicenseValidationResult.failure("Concurrent usage limit exceeded");
        }
        
        // 9. Security validation
        if (!securityService.isSecureEnvironment(context)) {
            return LicenseValidationResult.failure("Insecure environment detected");
        }
        
        return LicenseValidationResult.success("License validation successful");
    }
    
    /**
     * Check if license key format is valid
     */
    private boolean isValidLicenseKeyFormat(String licenseKey) {
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            return false;
        }
        
        // License key format: XXXX-XXXX-XXXX-XXXX (alphanumeric)
        Pattern pattern = Pattern.compile("^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$");
        return pattern.matcher(licenseKey.toUpperCase()).matches();
    }
    
    /**
     * Check if license is expired
     */
    private boolean isExpired(SoftwareLicense license) {
        if (license.getExpirationDate() == null) {
            return false; // No expiration date
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gracePeriodEnd = license.getExpirationDate().plusDays(maxGracePeriodDays);
        
        return now.isAfter(gracePeriodEnd);
    }
    
    /**
     * Check if usage limit is exceeded
     */
    private boolean isUsageLimitExceeded(SoftwareLicense license) {
        if (license.getUsageLimit() == null || license.getUsageLimit() <= 0) {
            return false; // No usage limit
        }
        
        return license.getUsageCount() >= license.getUsageLimit();
    }
    
    /**
     * Validate hardware fingerprint
     */
    private boolean isValidHardwareFingerprint(SoftwareLicense license, ValidationContext context) {
        if (license.getHardwareFingerprint() == null || license.getHardwareFingerprint().isEmpty()) {
            return true; // No hardware binding
        }
        
        String currentFingerprint = generateHardwareFingerprint(context);
        return license.getHardwareFingerprint().equals(currentFingerprint);
    }
    
    /**
     * Check geographic access restrictions
     */
    private boolean isValidGeographicAccess(SoftwareLicense license, ValidationContext context) {
        String allowedCountries = license.getAllowedCountries();
        if (allowedCountries == null || allowedCountries.isEmpty()) {
            return true; // No geographic restrictions
        }
        
        String userCountry = context.getCountry();
        if (userCountry == null) {
            return !strictMode; // Allow if country unknown and not in strict mode
        }
        
        return allowedCountries.contains(userCountry);
    }
    
    /**
     * Check feature access permissions
     */
    private boolean isValidFeatureAccess(SoftwareLicense license, ValidationContext context) {
        String allowedFeatures = license.getAllowedFeatures();
        if (allowedFeatures == null || allowedFeatures.isEmpty()) {
            return true; // No feature restrictions
        }
        
        String requestedFeature = context.getRequestedFeature();
        if (requestedFeature == null) {
            return true; // No specific feature requested
        }
        
        return allowedFeatures.contains(requestedFeature);
    }
    
    /**
     * Check concurrent usage limits
     */
    private boolean isConcurrentUsageExceeded(SoftwareLicense license) {
        if (license.getConcurrentUsers() == null || license.getConcurrentUsers() <= 0) {
            return false; // No concurrent usage limit
        }
        
        // Get current active sessions count
        long activeSessions = usageTrackingService.getActiveSessionsCount(license.getLicenseKey());
        
        return activeSessions >= license.getConcurrentUsers();
    }
    
    /**
     * Generate hardware fingerprint
     */
    private String generateHardwareFingerprint(ValidationContext context) {
        try {
            String hardwareInfo = context.getHardwareInfo();
            if (hardwareInfo == null) {
                return "";
            }
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(hardwareInfo.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().toUpperCase();
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating hardware fingerprint", e);
            return "";
        }
    }
    
    /**
     * Update license usage statistics
     */
    @Transactional
    public void updateLicenseUsage(SoftwareLicense license) {
        license.setUsageCount(license.getUsageCount() + 1);
        license.setLastUsedAt(LocalDateTime.now());
        
        // Calculate usage percentage
        if (license.getUsageLimit() != null && license.getUsageLimit() > 0) {
            double percentage = (double) license.getUsageCount() / license.getUsageLimit() * 100;
            license.setUsagePercentage(percentage);
        }
        
        licenseRepository.save(license);
    }
    
    /**
     * Get license information
     */
    public Optional<SoftwareLicense> getLicense(String licenseKey) {
        return licenseRepository.findByLicenseKey(licenseKey);
    }
    
    /**
     * Get active licenses for customer
     */
    public List<SoftwareLicense> getActiveLicenses(String customerId) {
        return licenseRepository.findActiveLicensesByCustomerId(customerId);
    }
    
    /**
     * Check if license needs renewal
     */
    public boolean needsRenewal(String licenseKey) {
        return licenseRepository.findByLicenseKey(licenseKey)
                .map(SoftwareLicense::needsRenewal)
                .orElse(false);
    }
    
    /**
     * Deactivate expired licenses (scheduled task)
     */
    @Transactional
    public void deactivateExpiredLicenses() {
        LocalDateTime now = LocalDateTime.now();
        licenseRepository.deactivateExpiredLicenses(now);
        log.info("Deactivated expired licenses");
    }
    
    /**
     * Validation Context
     */
    public static class ValidationContext {
        private String ipAddress;
        private String userAgent;
        private String country;
        private String hardwareInfo;
        private String requestedFeature;
        private String userId;
        private String sessionId;
        
        // Getters and setters
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getHardwareInfo() { return hardwareInfo; }
        public void setHardwareInfo(String hardwareInfo) { this.hardwareInfo = hardwareInfo; }
        
        public String getRequestedFeature() { return requestedFeature; }
        public void setRequestedFeature(String requestedFeature) { this.requestedFeature = requestedFeature; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
    
    /**
     * License Validation Result
     */
    public static class LicenseValidationResult {
        private final boolean valid;
        private final String message;
        private final LocalDateTime validatedAt;
        
        private LicenseValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
            this.validatedAt = LocalDateTime.now();
        }
        
        public static LicenseValidationResult success(String message) {
            return new LicenseValidationResult(true, message);
        }
        
        public static LicenseValidationResult failure(String message) {
            return new LicenseValidationResult(false, message);
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public LocalDateTime getValidatedAt() { return validatedAt; }
    }
}