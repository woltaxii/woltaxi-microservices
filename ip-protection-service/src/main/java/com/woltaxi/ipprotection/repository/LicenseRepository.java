package com.woltaxi.ipprotection.repository;

import com.woltaxi.ipprotection.entity.SoftwareLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SoftwareLicense entity
 * Handles all database operations for software licenses
 */
@Repository
public interface LicenseRepository extends JpaRepository<SoftwareLicense, Long> {
    
    /**
     * Find license by license key
     */
    Optional<SoftwareLicense> findByLicenseKey(String licenseKey);
    
    /**
     * Find active license by license key
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.licenseKey = :licenseKey AND l.isActive = true")
    Optional<SoftwareLicense> findActiveLicenseByKey(@Param("licenseKey") String licenseKey);
    
    /**
     * Find licenses by customer ID
     */
    List<SoftwareLicense> findByCustomerId(String customerId);
    
    /**
     * Find active licenses by customer ID
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.customerId = :customerId AND l.isActive = true")
    List<SoftwareLicense> findActiveLicensesByCustomerId(@Param("customerId") String customerId);
    
    /**
     * Find licenses by product name
     */
    List<SoftwareLicense> findByProductName(String productName);
    
    /**
     * Find licenses by license type
     */
    List<SoftwareLicense> findByLicenseType(SoftwareLicense.LicenseType licenseType);
    
    /**
     * Find licenses by status
     */
    List<SoftwareLicense> findByStatus(SoftwareLicense.LicenseStatus status);
    
    /**
     * Find expired licenses
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.expirationDate < :currentDate AND l.isActive = true")
    List<SoftwareLicense> findExpiredLicenses(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find licenses expiring within specified days
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.expirationDate BETWEEN :currentDate AND :expirationThreshold AND l.isActive = true")
    List<SoftwareLicense> findLicensesExpiringWithin(@Param("currentDate") LocalDateTime currentDate, 
                                                     @Param("expirationThreshold") LocalDateTime expirationThreshold);
    
    /**
     * Find licenses by hardware fingerprint
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.hardwareFingerprint = :fingerprint AND l.isActive = true")
    List<SoftwareLicense> findByHardwareFingerprint(@Param("fingerprint") String fingerprint);
    
    /**
     * Find licenses by company name
     */
    List<SoftwareLicense> findByCompanyName(String companyName);
    
    /**
     * Find trial licenses
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.licenseType = 'TRIAL' AND l.isActive = true")
    List<SoftwareLicense> findActiveTrial licenses();
    
    /**
     * Find commercial licenses
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.licenseType IN ('STANDARD', 'PROFESSIONAL', 'ENTERPRISE') AND l.isActive = true")
    List<SoftwareLicense> findActiveCommercialLicenses();
    
    /**
     * Count active licenses by customer
     */
    @Query("SELECT COUNT(l) FROM SoftwareLicense l WHERE l.customerId = :customerId AND l.isActive = true")
    Long countActiveLicensesByCustomer(@Param("customerId") String customerId);
    
    /**
     * Find licenses with high usage
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.usagePercentage > :threshold AND l.isActive = true")
    List<SoftwareLicense> findHighUsageLicenses(@Param("threshold") Double threshold);
    
    /**
     * Find licenses in specific countries
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.allowedCountries LIKE %:country% AND l.isActive = true")
    List<SoftwareLicense> findLicensesByCountry(@Param("country") String country);
    
    /**
     * Find licenses with specific features
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.allowedFeatures LIKE %:feature% AND l.isActive = true")
    List<SoftwareLicense> findLicensesWithFeature(@Param("feature") String feature);
    
    /**
     * Find licenses created within date range
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.createdAt BETWEEN :startDate AND :endDate")
    List<SoftwareLicense> findLicensesCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find licenses by version
     */
    List<SoftwareLicense> findByVersion(String version);
    
    /**
     * Update license usage
     */
    @Query("UPDATE SoftwareLicense l SET l.usageCount = l.usageCount + 1, l.lastUsedAt = :currentTime WHERE l.licenseKey = :licenseKey")
    void incrementUsageCount(@Param("licenseKey") String licenseKey, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Deactivate expired licenses
     */
    @Query("UPDATE SoftwareLicense l SET l.isActive = false, l.status = 'EXPIRED' WHERE l.expirationDate < :currentDate AND l.isActive = true")
    void deactivateExpiredLicenses(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find duplicate licenses (same customer and product)
     */
    @Query("SELECT l FROM SoftwareLicense l WHERE l.customerId = :customerId AND l.productName = :productName AND l.isActive = true")
    List<SoftwareLicense> findDuplicateLicenses(@Param("customerId") String customerId, @Param("productName") String productName);
}