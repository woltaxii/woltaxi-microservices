package com.woltaxi.travel.repository;

import com.woltaxi.travel.entity.TravelBooking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Travel Booking Repository
 * 
 * Data access layer for travel booking operations
 * including custom queries for analytics and reporting.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Repository
public interface TravelBookingRepository extends JpaRepository<TravelBooking, UUID> {

    /**
     * Find booking by booking reference
     */
    Optional<TravelBooking> findByBookingReference(String bookingReference);

    /**
     * Find bookings by user ID
     */
    List<TravelBooking> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find bookings by user ID and status
     */
    List<TravelBooking> findByUserIdAndBookingStatusOrderByCreatedAtDesc(UUID userId, TravelBooking.BookingStatus status, Pageable pageable);

    /**
     * Find bookings by travel provider
     */
    List<TravelBooking> findByTravelProviderOrderByCreatedAtDesc(TravelBooking.TravelProvider provider, Pageable pageable);

    /**
     * Find bookings by travel provider and status within date range
     */
    List<TravelBooking> findByTravelProviderAndBookingStatusAndCreatedAtBetween(
            TravelBooking.TravelProvider provider, 
            TravelBooking.BookingStatus status, 
            LocalDateTime startDate, 
            LocalDateTime endDate);

    /**
     * Find bookings by user within date range
     */
    List<TravelBooking> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find bookings within date range
     */
    List<TravelBooking> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find bookings by booking type and status
     */
    List<TravelBooking> findByBookingTypeAndBookingStatus(TravelBooking.BookingType bookingType, TravelBooking.BookingStatus status);

    /**
     * Find upcoming bookings for reminders
     */
    @Query("SELECT b FROM TravelBooking b WHERE b.travelDate BETWEEN :startDate AND :endDate AND b.bookingStatus = 'CONFIRMED'")
    List<TravelBooking> findUpcomingBookings(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count bookings by status
     */
    long countByBookingStatus(TravelBooking.BookingStatus status);

    /**
     * Count bookings by user and status
     */
    long countByUserIdAndBookingStatus(UUID userId, TravelBooking.BookingStatus status);

    /**
     * Count bookings by provider
     */
    long countByTravelProvider(TravelBooking.TravelProvider provider);

    /**
     * Find bookings requiring commission calculation
     */
    @Query("SELECT b FROM TravelBooking b WHERE b.bookingStatus = 'CONFIRMED' AND b.createdAt BETWEEN :startDate AND :endDate AND b.travelProvider = :provider")
    List<TravelBooking> findBookingsForCommissionCalculation(
            @Param("provider") TravelBooking.TravelProvider provider,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get revenue statistics by provider
     */
    @Query("SELECT b.travelProvider, SUM(b.totalPrice), COUNT(b) FROM TravelBooking b WHERE b.bookingStatus = 'CONFIRMED' AND b.createdAt BETWEEN :startDate AND :endDate GROUP BY b.travelProvider")
    List<Object[]> getRevenueStatsByProvider(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Get booking statistics by type
     */
    @Query("SELECT b.bookingType, COUNT(b), AVG(b.totalPrice) FROM TravelBooking b WHERE b.createdAt BETWEEN :startDate AND :endDate GROUP BY b.bookingType")
    List<Object[]> getBookingStatsByType(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find popular routes
     */
    @Query("SELECT b.origin, b.destination, COUNT(b) as bookingCount FROM TravelBooking b WHERE b.bookingStatus = 'CONFIRMED' GROUP BY b.origin, b.destination ORDER BY bookingCount DESC")
    List<Object[]> findPopularRoutes(Pageable pageable);

    /**
     * Find bookings by confirmation number
     */
    Optional<TravelBooking> findByConfirmationNumber(String confirmationNumber);

    /**
     * Find expired bookings (for cleanup)
     */
    @Query("SELECT b FROM TravelBooking b WHERE b.travelDate < :cutoffDate AND b.bookingStatus IN ('PENDING', 'CONFIRMED')")
    List<TravelBooking> findExpiredBookings(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Search bookings by passenger name or email
     */
    @Query("SELECT b FROM TravelBooking b WHERE LOWER(b.contactEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(b.passengerDetails) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TravelBooking> searchBookings(@Param("searchTerm") String searchTerm, Pageable pageable);
}

/**
 * Travel Partner Repository
 */
@Repository
interface TravelPartnerRepository extends JpaRepository<com.woltaxi.travel.entity.TravelPartner, UUID> {
    
    List<com.woltaxi.travel.entity.TravelPartner> findByStatus(com.woltaxi.travel.entity.TravelPartner.PartnerStatus status);
    
    List<com.woltaxi.travel.entity.TravelPartner> findByBookingTypeAndStatus(
            TravelBooking.BookingType bookingType, 
            com.woltaxi.travel.entity.TravelPartner.PartnerStatus status);
    
    Optional<com.woltaxi.travel.entity.TravelPartner> findByProvider(TravelBooking.TravelProvider provider);
    
    List<com.woltaxi.travel.entity.TravelPartner> findByPartnerNameContainingIgnoreCase(String partnerName);
}

/**
 * Commission Record Repository
 */
@Repository
interface CommissionRecordRepository extends JpaRepository<com.woltaxi.travel.entity.CommissionRecord, UUID> {
    
    List<com.woltaxi.travel.entity.CommissionRecord> findByPartnerId(UUID partnerId, Pageable pageable);
    
    List<com.woltaxi.travel.entity.CommissionRecord> findByBillingPeriod(String billingPeriod, Pageable pageable);
    
    List<com.woltaxi.travel.entity.CommissionRecord> findByPartnerIdAndBillingPeriod(UUID partnerId, String billingPeriod, Pageable pageable);
    
    List<com.woltaxi.travel.entity.CommissionRecord> findByPartnerIdAndBillingPeriodAndStatus(
            UUID partnerId, String billingPeriod, com.woltaxi.travel.entity.CommissionRecord.CommissionStatus status, Pageable pageable);
    
    List<com.woltaxi.travel.entity.CommissionRecord> findByStatus(com.woltaxi.travel.entity.CommissionRecord.CommissionStatus status);
    
    List<com.woltaxi.travel.entity.CommissionRecord> findByBillingPeriod(String billingPeriod);
    
    Optional<com.woltaxi.travel.entity.CommissionRecord> findByBookingId(UUID bookingId);
    
    @Query("SELECT SUM(c.totalCommission) FROM CommissionRecord c WHERE c.status = :status AND c.billingPeriod = :billingPeriod")
    java.math.BigDecimal getTotalCommissionByStatusAndPeriod(
            @Param("status") com.woltaxi.travel.entity.CommissionRecord.CommissionStatus status, 
            @Param("billingPeriod") String billingPeriod);
}