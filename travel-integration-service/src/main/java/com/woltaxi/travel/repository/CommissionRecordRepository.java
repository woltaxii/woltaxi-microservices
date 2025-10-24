package com.woltaxi.travel.repository;

import com.woltaxi.travel.entity.CommissionRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Commission Record Repository
 * 
 * Data access layer for commission record operations.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Repository
public interface CommissionRecordRepository extends JpaRepository<CommissionRecord, UUID> {
    
    List<CommissionRecord> findByPartnerId(UUID partnerId, Pageable pageable);
    
    List<CommissionRecord> findByBillingPeriod(String billingPeriod, Pageable pageable);
    
    List<CommissionRecord> findByPartnerIdAndBillingPeriod(UUID partnerId, String billingPeriod, Pageable pageable);
    
    List<CommissionRecord> findByPartnerIdAndBillingPeriodAndStatus(
            UUID partnerId, String billingPeriod, CommissionRecord.CommissionStatus status, Pageable pageable);
    
    List<CommissionRecord> findByStatus(CommissionRecord.CommissionStatus status);
    
    List<CommissionRecord> findByBillingPeriod(String billingPeriod);
    
    Optional<CommissionRecord> findByBookingId(UUID bookingId);
    
    @Query("SELECT SUM(c.totalCommission) FROM CommissionRecord c WHERE c.status = :status AND c.billingPeriod = :billingPeriod")
    BigDecimal getTotalCommissionByStatusAndPeriod(
            @Param("status") CommissionRecord.CommissionStatus status, 
            @Param("billingPeriod") String billingPeriod);
}