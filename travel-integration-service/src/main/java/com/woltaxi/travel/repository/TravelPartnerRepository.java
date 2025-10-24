package com.woltaxi.travel.repository;

import com.woltaxi.travel.entity.TravelPartner;
import com.woltaxi.travel.entity.TravelBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Travel Partner Repository
 * 
 * Data access layer for travel partner operations.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Repository
public interface TravelPartnerRepository extends JpaRepository<TravelPartner, UUID> {
    
    List<TravelPartner> findByStatus(TravelPartner.PartnerStatus status);
    
    List<TravelPartner> findByBookingTypeAndStatus(
            TravelBooking.BookingType bookingType, 
            TravelPartner.PartnerStatus status);
    
    Optional<TravelPartner> findByProvider(TravelBooking.TravelProvider provider);
    
    List<TravelPartner> findByPartnerNameContainingIgnoreCase(String partnerName);
}