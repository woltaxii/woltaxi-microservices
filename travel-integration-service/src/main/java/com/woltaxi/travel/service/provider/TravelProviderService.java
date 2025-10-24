package com.woltaxi.travel.service.provider;

import com.woltaxi.travel.dto.request.TravelBookingRequest;
import com.woltaxi.travel.service.TravelBookingService.TravelOption;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Travel Provider Service Interface
 * 
 * Common interface for all travel provider integrations
 * including airlines, bus companies, hotels, and car rentals.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
public interface TravelProviderService {

    /**
     * Search for available travel options
     */
    List<TravelOption> searchOptions(TravelBookingRequest request);

    /**
     * Check availability for a specific option
     */
    TravelOption checkAvailability(TravelBookingRequest request);

    /**
     * Make a booking reservation
     */
    BookingResult makeBooking(TravelBookingRequest request, TravelOption option);

    /**
     * Confirm a pending booking
     */
    BookingResult confirmBooking(String providerBookingId);

    /**
     * Cancel a booking
     */
    CancellationResult cancelBooking(String providerBookingId, String reason);

    /**
     * Modify an existing booking
     */
    ModificationResult modifyBooking(String providerBookingId, TravelBookingRequest modificationRequest);

    /**
     * Check-in for flight bookings
     */
    CheckInResult checkIn(String providerBookingId);

    /**
     * Get booking status from provider
     */
    BookingStatus getBookingStatus(String providerBookingId);

    /**
     * Test provider connection and health
     */
    HealthCheckResult healthCheck();

    // Result classes
    @Data
    @Builder
    class BookingResult {
        private boolean success;
        private String bookingReference;
        private String providerBookingId;
        private String confirmationData;
        private String errorMessage;
        private Map<String, Object> additionalData;
    }

    @Data
    @Builder
    class CancellationResult {
        private boolean success;
        private BigDecimal cancellationFee;
        private BigDecimal refundAmount;
        private String cancellationReference;
        private String errorMessage;
        private LocalDateTime cancellationDate;
    }

    @Data
    @Builder
    class ModificationResult {
        private boolean success;
        private BigDecimal priceDifference;
        private String modificationReference;
        private String errorMessage;
        private Map<String, Object> updatedBookingData;
    }

    @Data
    @Builder
    class CheckInResult {
        private boolean success;
        private List<String> boardingPasses;
        private Map<String, String> seatAssignments;
        private String errorMessage;
    }

    @Data
    @Builder
    class BookingStatus {
        private String status;
        private String providerStatus;
        private LocalDateTime lastUpdated;
        private Map<String, Object> statusDetails;
    }

    @Data
    @Builder
    class HealthCheckResult {
        private boolean healthy;
        private String status;
        private long responseTime;
        private String errorMessage;
        private LocalDateTime checkTime;
    }
}