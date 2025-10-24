package com.woltaxi.travel.service;

import com.woltaxi.travel.dto.request.TravelBookingRequest;
import com.woltaxi.travel.dto.response.TravelBookingResponse;
import com.woltaxi.travel.entity.TravelBooking;
import com.woltaxi.travel.entity.TravelPartner;
import com.woltaxi.travel.entity.CommissionRecord;
import com.woltaxi.travel.service.provider.TravelProviderService;
import com.woltaxi.travel.service.provider.TravelProviderFactory;
import com.woltaxi.travel.service.commission.CommissionCalculationService;
import com.woltaxi.travel.service.document.DocumentGenerationService;
import com.woltaxi.travel.service.notification.TravelNotificationService;
import com.woltaxi.travel.repository.TravelBookingRepository;
import com.woltaxi.travel.repository.TravelPartnerRepository;
import com.woltaxi.travel.repository.CommissionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Travel Booking Service
 * 
 * Core service for managing travel bookings including flights, buses, 
 * hotels, and car rentals with comprehensive commission tracking.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TravelBookingService {

    private final TravelBookingRepository bookingRepository;
    private final TravelPartnerRepository partnerRepository;
    private final CommissionRecordRepository commissionRecordRepository;
    private final TravelProviderFactory providerFactory;
    private final CommissionCalculationService commissionCalculationService;
    private final DocumentGenerationService documentGenerationService;
    private final TravelNotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Create a new travel booking
     */
    public TravelBookingResponse createBooking(TravelBookingRequest request) {
        log.info("Creating booking for user: {} type: {} provider: {}", 
                request.getUserId(), request.getBookingType(), request.getTravelProvider());

        // Validate booking request
        validateBookingRequest(request);

        // Get travel provider service
        TravelProviderService providerService = providerFactory.getProviderService(request.getTravelProvider());

        // Check availability and pricing
        TravelOption selectedOption = providerService.checkAvailability(request);
        if (selectedOption == null) {
            throw new BookingException("No availability for requested travel option");
        }

        // Create booking entity
        TravelBooking booking = createBookingEntity(request, selectedOption);

        // Save booking as pending
        booking.setBookingStatus(TravelBooking.BookingStatus.PENDING);
        booking = bookingRepository.save(booking);

        // Make reservation with provider
        TravelProviderService.BookingResult providerResult = providerService.makeBooking(request, selectedOption);

        if (providerResult.isSuccess()) {
            // Update booking with provider details
            booking.setBookingReference(providerResult.getBookingReference());
            booking.setProviderBookingId(providerResult.getProviderBookingId());
            booking.setBookingStatus(TravelBooking.BookingStatus.CONFIRMED);
            booking.setConfirmationNumber(generateConfirmationNumber());
            booking.setProviderConfirmationData(providerResult.getConfirmationData());

            // Create commission record
            createCommissionRecord(booking, selectedOption);

            // Generate booking documents
            generateInitialDocuments(booking);

            // Send confirmation notifications
            notificationService.sendBookingConfirmation(booking, request.getContactEmail());

            booking = bookingRepository.save(booking);

            log.info("Booking created successfully: {} reference: {}", 
                    booking.getId(), booking.getBookingReference());

        } else {
            booking.setBookingStatus(TravelBooking.BookingStatus.FAILED);
            booking.setNotes("Provider booking failed: " + providerResult.getErrorMessage());
            booking = bookingRepository.save(booking);

            throw new BookingException("Failed to create booking with provider: " + providerResult.getErrorMessage());
        }

        // Clear cache
        clearBookingCaches(request.getUserId());

        return convertToResponse(booking);
    }

    /**
     * Search for available travel options
     */
    @Cacheable(value = "travel-search", key = "#request.hashCode()")
    public List<TravelBookingResponse> searchTravelOptions(TravelBookingRequest request) {
        log.info("Searching travel options: {} from {} to {}", 
                request.getBookingType(), request.getOrigin(), request.getDestination());

        List<TravelBookingResponse> allOptions = new ArrayList<>();

        // Get relevant providers for booking type
        List<TravelPartner> activeProviders = partnerRepository.findByBookingTypeAndStatus(
                request.getBookingType(), TravelPartner.PartnerStatus.ACTIVE);

        for (TravelPartner partner : activeProviders) {
            try {
                TravelProviderService providerService = providerFactory.getProviderService(partner.getProvider());
                List<TravelOption> options = providerService.searchOptions(request);
                
                // Convert to responses
                List<TravelBookingResponse> responses = options.stream()
                        .map(option -> convertOptionToResponse(option, partner))
                        .collect(Collectors.toList());
                
                allOptions.addAll(responses);
                
            } catch (Exception e) {
                log.error("Error searching with provider: {}", partner.getProvider(), e);
                // Continue with other providers
            }
        }

        // Sort by price and rating
        allOptions.sort((a, b) -> {
            int priceCompare = a.getTotalPrice().compareTo(b.getTotalPrice());
            if (priceCompare != 0) return priceCompare;
            return Double.compare(b.getProviderRating(), a.getProviderRating());
        });

        return allOptions;
    }

    /**
     * Confirm a pending booking
     */
    public TravelBookingResponse confirmBooking(UUID bookingId) {
        TravelBooking booking = getBookingById(bookingId);
        
        if (booking.getBookingStatus() != TravelBooking.BookingStatus.PENDING) {
            throw new BookingException("Booking is not in pending status");
        }

        TravelProviderService providerService = providerFactory.getProviderService(booking.getTravelProvider());
        TravelProviderService.BookingResult result = providerService.confirmBooking(booking.getProviderBookingId());

        if (result.isSuccess()) {
            booking.setBookingStatus(TravelBooking.BookingStatus.CONFIRMED);
            booking.setConfirmationNumber(generateConfirmationNumber());
            booking.setConfirmedAt(LocalDateTime.now());
            
            // Generate final documents
            generateInitialDocuments(booking);
            
            // Send confirmation
            notificationService.sendBookingConfirmation(booking, booking.getContactEmail());
            
            booking = bookingRepository.save(booking);
            
            log.info("Booking confirmed: {}", bookingId);
        } else {
            booking.setBookingStatus(TravelBooking.BookingStatus.FAILED);
            booking.setNotes("Confirmation failed: " + result.getErrorMessage());
            booking = bookingRepository.save(booking);
            
            throw new BookingException("Failed to confirm booking: " + result.getErrorMessage());
        }

        clearBookingCaches(booking.getUserId());
        return convertToResponse(booking);
    }

    /**
     * Cancel a booking
     */
    public TravelBookingResponse cancelBooking(UUID bookingId, String reason) {
        TravelBooking booking = getBookingById(bookingId);
        
        if (booking.getBookingStatus() == TravelBooking.BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        TravelProviderService providerService = providerFactory.getProviderService(booking.getTravelProvider());
        TravelProviderService.CancellationResult result = providerService.cancelBooking(
                booking.getProviderBookingId(), reason);

        booking.setBookingStatus(TravelBooking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        booking.setCancellationFee(result.getCancellationFee());
        booking.setRefundAmount(result.getRefundAmount());

        // Update commission record
        updateCommissionForCancellation(booking, result.getRefundAmount());

        // Send cancellation notification
        notificationService.sendBookingCancellation(booking, booking.getContactEmail());

        booking = bookingRepository.save(booking);

        log.info("Booking cancelled: {} reason: {}", bookingId, reason);

        clearBookingCaches(booking.getUserId());
        return convertToResponse(booking);
    }

    /**
     * Modify an existing booking
     */
    public TravelBookingResponse modifyBooking(UUID bookingId, TravelBookingRequest modificationRequest) {
        TravelBooking booking = getBookingById(bookingId);
        
        if (booking.getBookingStatus() != TravelBooking.BookingStatus.CONFIRMED) {
            throw new BookingException("Only confirmed bookings can be modified");
        }

        TravelProviderService providerService = providerFactory.getProviderService(booking.getTravelProvider());
        TravelProviderService.ModificationResult result = providerService.modifyBooking(
                booking.getProviderBookingId(), modificationRequest);

        if (result.isSuccess()) {
            // Update booking details
            updateBookingFromRequest(booking, modificationRequest);
            booking.setPriceDifference(result.getPriceDifference());
            booking.setModifiedAt(LocalDateTime.now());
            
            // Update commission if price changed
            if (result.getPriceDifference().compareTo(BigDecimal.ZERO) != 0) {
                updateCommissionForModification(booking, result.getPriceDifference());
            }
            
            // Generate updated documents
            generateInitialDocuments(booking);
            
            // Send modification notification
            notificationService.sendBookingModification(booking, booking.getContactEmail());
            
            booking = bookingRepository.save(booking);
            
            log.info("Booking modified: {} price difference: {}", bookingId, result.getPriceDifference());
        } else {
            throw new BookingException("Failed to modify booking: " + result.getErrorMessage());
        }

        clearBookingCaches(booking.getUserId());
        return convertToResponse(booking);
    }

    /**
     * Check-in for flight bookings
     */
    public TravelBookingResponse checkInBooking(UUID bookingId) {
        TravelBooking booking = getBookingById(bookingId);
        
        if (booking.getBookingType() != TravelBooking.BookingType.FLIGHT) {
            throw new BookingException("Check-in is only available for flight bookings");
        }
        
        if (booking.getBookingStatus() != TravelBooking.BookingStatus.CONFIRMED) {
            throw new BookingException("Only confirmed bookings can be checked in");
        }

        TravelProviderService providerService = providerFactory.getProviderService(booking.getTravelProvider());
        TravelProviderService.CheckInResult result = providerService.checkIn(booking.getProviderBookingId());

        if (result.isSuccess()) {
            booking.setCheckedInAt(LocalDateTime.now());
            booking.setBoardingPasses(result.getBoardingPasses());
            booking.setSeatAssignments(result.getSeatAssignments());
            
            // Generate boarding passes
            documentGenerationService.generateBoardingPasses(booking);
            
            // Send check-in notification
            notificationService.sendCheckInConfirmation(booking, booking.getContactEmail());
            
            booking = bookingRepository.save(booking);
            
            log.info("Check-in completed for booking: {}", bookingId);
        } else {
            throw new BookingException("Check-in failed: " + result.getErrorMessage());
        }

        return convertToResponse(booking);
    }

    /**
     * Get booking by ID
     */
    @Cacheable(value = "bookings", key = "#bookingId")
    public TravelBookingResponse getBookingById(UUID bookingId) {
        TravelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));
        
        return convertToResponse(booking);
    }

    /**
     * Get booking by reference
     */
    @Cacheable(value = "booking-references", key = "#bookingReference")
    public TravelBookingResponse getBookingByReference(String bookingReference) {
        TravelBooking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with reference: " + bookingReference));
        
        return convertToResponse(booking);
    }

    /**
     * Get user bookings
     */
    @Cacheable(value = "user-bookings", key = "#userId + '-' + #status + '-' + #page + '-' + #size")
    public List<TravelBookingResponse> getUserBookings(UUID userId, TravelBooking.BookingStatus status, int page, int size) {
        List<TravelBooking> bookings;
        
        if (status != null) {
            bookings = bookingRepository.findByUserIdAndBookingStatusOrderByCreatedAtDesc(
                    userId, status, PageRequest.of(page, size));
        } else {
            bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, PageRequest.of(page, size));
        }
        
        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get bookings by provider
     */
    public List<TravelBookingResponse> getBookingsByProvider(TravelBooking.TravelProvider provider, int page, int size) {
        List<TravelBooking> bookings = bookingRepository.findByTravelProviderOrderByCreatedAtDesc(
                provider, PageRequest.of(page, size));
        
        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get booking statistics
     */
    @Cacheable(value = "booking-stats", key = "#userId + '-' + #startDate + '-' + #endDate")
    public BookingStatistics getBookingStatistics(UUID userId, String startDate, String endDate) {
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusMonths(12);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();

        List<TravelBooking> bookings;
        if (userId != null) {
            bookings = bookingRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
        } else {
            bookings = bookingRepository.findByCreatedAtBetween(start, end);
        }

        return calculateBookingStatistics(bookings);
    }

    /**
     * Generate booking documents
     */
    public Map<String, String> generateBookingDocuments(UUID bookingId, List<String> documentTypes) {
        TravelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        Map<String, String> documentUrls = new HashMap<>();

        for (String documentType : documentTypes) {
            try {
                String documentUrl = switch (documentType.toLowerCase()) {
                    case "ticket", "e-ticket" -> documentGenerationService.generateETicket(booking);
                    case "invoice" -> documentGenerationService.generateInvoice(booking);
                    case "voucher" -> documentGenerationService.generateVoucher(booking);
                    case "boarding-pass" -> documentGenerationService.generateBoardingPasses(booking);
                    case "itinerary" -> documentGenerationService.generateItinerary(booking);
                    default -> throw new DocumentGenerationException("Unknown document type: " + documentType);
                };
                
                documentUrls.put(documentType, documentUrl);
                
            } catch (Exception e) {
                log.error("Failed to generate document: {} for booking: {}", documentType, bookingId, e);
                documentUrls.put(documentType, "ERROR: " + e.getMessage());
            }
        }

        return documentUrls;
    }

    /**
     * Get commission records
     */
    public List<CommissionRecord> getCommissionRecords(UUID partnerId, String billingPeriod, 
            CommissionRecord.CommissionStatus status, int page, int size) {
        
        if (partnerId != null && billingPeriod != null && status != null) {
            return commissionRecordRepository.findByPartnerIdAndBillingPeriodAndStatus(
                    partnerId, billingPeriod, status, PageRequest.of(page, size));
        } else if (partnerId != null && billingPeriod != null) {
            return commissionRecordRepository.findByPartnerIdAndBillingPeriod(
                    partnerId, billingPeriod, PageRequest.of(page, size));
        } else if (partnerId != null) {
            return commissionRecordRepository.findByPartnerId(
                    partnerId, PageRequest.of(page, size));
        } else if (billingPeriod != null) {
            return commissionRecordRepository.findByBillingPeriod(
                    billingPeriod, PageRequest.of(page, size));
        } else {
            return commissionRecordRepository.findAll(PageRequest.of(page, size)).getContent();
        }
    }

    /**
     * Calculate monthly commissions
     */
    public CommissionCalculationResult calculateMonthlyCommissions(int year, int month) {
        YearMonth billingMonth = YearMonth.of(year, month);
        String billingPeriod = billingMonth.toString();

        log.info("Calculating monthly commissions for period: {}", billingPeriod);

        // Get all active partners
        List<TravelPartner> activePartners = partnerRepository.findByStatus(TravelPartner.PartnerStatus.ACTIVE);

        CommissionCalculationResult.CommissionCalculationResultBuilder resultBuilder = 
                CommissionCalculationResult.builder()
                        .billingPeriod(billingPeriod)
                        .calculationDate(LocalDateTime.now())
                        .totalPartners(activePartners.size());

        BigDecimal totalCommissions = BigDecimal.ZERO;
        int successfulCalculations = 0;
        List<String> errors = new ArrayList<>();

        for (TravelPartner partner : activePartners) {
            try {
                CommissionRecord commissionRecord = commissionCalculationService.calculatePartnerCommission(
                        partner, billingMonth);
                
                if (commissionRecord != null) {
                    totalCommissions = totalCommissions.add(commissionRecord.getTotalCommission());
                    successfulCalculations++;
                }
                
            } catch (Exception e) {
                log.error("Failed to calculate commission for partner: {}", partner.getId(), e);
                errors.add("Partner " + partner.getPartnerName() + ": " + e.getMessage());
            }
        }

        return resultBuilder
                .totalCommissionAmount(totalCommissions)
                .successfulCalculations(successfulCalculations)
                .errors(errors)
                .build();
    }

    /**
     * Generate commission invoice
     */
    public String generateCommissionInvoice(UUID partnerId, String billingPeriod) {
        TravelPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + partnerId));

        List<CommissionRecord> commissionRecords = commissionRecordRepository.findByPartnerIdAndBillingPeriod(
                partnerId, billingPeriod, PageRequest.of(0, Integer.MAX_VALUE));

        if (commissionRecords.isEmpty()) {
            throw new CommissionException("No commission records found for partner: " + partnerId + 
                    " in period: " + billingPeriod);
        }

        return documentGenerationService.generateCommissionInvoice(partner, commissionRecords, billingPeriod);
    }

    /**
     * Mark commission as paid
     */
    public void markCommissionAsPaid(UUID partnerId, String billingPeriod, BigDecimal paymentAmount, 
            String paymentMethod, String paymentReference) {
        
        List<CommissionRecord> commissionRecords = commissionRecordRepository.findByPartnerIdAndBillingPeriod(
                partnerId, billingPeriod, PageRequest.of(0, Integer.MAX_VALUE));

        if (commissionRecords.isEmpty()) {
            throw new CommissionException("No commission records found for partner: " + partnerId + 
                    " in period: " + billingPeriod);
        }

        BigDecimal totalDue = commissionRecords.stream()
                .map(CommissionRecord::getTotalCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (paymentAmount.compareTo(totalDue) != 0) {
            log.warn("Payment amount {} does not match total due {} for partner {} period {}", 
                    paymentAmount, totalDue, partnerId, billingPeriod);
        }

        for (CommissionRecord record : commissionRecords) {
            record.setStatus(CommissionRecord.CommissionStatus.PAID);
            record.setPaidAt(LocalDateTime.now());
            record.setPaymentAmount(paymentAmount);
            record.setPaymentMethod(paymentMethod);
            record.setPaymentReference(paymentReference);
        }

        commissionRecordRepository.saveAll(commissionRecords);

        log.info("Marked commission as paid for partner: {} period: {} amount: {}", 
                partnerId, billingPeriod, paymentAmount);
    }

    /**
     * Get travel options
     */
    @Cacheable(value = "travel-options", key = "#bookingType + '-' + #origin + '-' + #destination + '-' + #travelDate")
    public List<TravelOption> getTravelOptions(TravelBooking.BookingType bookingType, String origin, 
            String destination, LocalDateTime travelDate, int passengerCount) {
        
        List<TravelPartner> activeProviders = partnerRepository.findByBookingTypeAndStatus(
                bookingType, TravelPartner.PartnerStatus.ACTIVE);

        List<TravelOption> allOptions = new ArrayList<>();

        for (TravelPartner partner : activeProviders) {
            try {
                TravelProviderService providerService = providerFactory.getProviderService(partner.getProvider());
                
                TravelBookingRequest searchRequest = TravelBookingRequest.builder()
                        .bookingType(bookingType)
                        .origin(origin)
                        .destination(destination)
                        .travelDate(travelDate)
                        .passengerCount(passengerCount)
                        .build();

                List<TravelOption> options = providerService.searchOptions(searchRequest);
                allOptions.addAll(options);
                
            } catch (Exception e) {
                log.error("Error getting options from provider: {}", partner.getProvider(), e);
            }
        }

        // Sort by price
        allOptions.sort(Comparator.comparing(TravelOption::getPrice));

        return allOptions;
    }

    // Private helper methods

    private void validateBookingRequest(TravelBookingRequest request) {
        if (request.getTravelDate().isBefore(LocalDateTime.now())) {
            throw new BookingValidationException("Travel date cannot be in the past");
        }

        if (request.getPassengerCount() < 1 || request.getPassengerCount() > 20) {
            throw new BookingValidationException("Passenger count must be between 1 and 20");
        }

        // Additional validation based on booking type
        switch (request.getBookingType()) {
            case FLIGHT -> validateFlightBooking(request);
            case BUS -> validateBusBooking(request);
            case HOTEL -> validateHotelBooking(request);
            case CAR_RENTAL -> validateCarRentalBooking(request);
        }
    }

    private void validateFlightBooking(TravelBookingRequest request) {
        if (request.getOrigin() == null || request.getDestination() == null) {
            throw new BookingValidationException("Origin and destination are required for flight bookings");
        }
    }

    private void validateBusBooking(TravelBookingRequest request) {
        if (request.getOrigin() == null || request.getDestination() == null) {
            throw new BookingValidationException("Origin and destination are required for bus bookings");
        }
    }

    private void validateHotelBooking(TravelBookingRequest request) {
        if (request.getDestination() == null) {
            throw new BookingValidationException("Destination is required for hotel bookings");
        }
        if (request.getCheckOutDate() == null || request.getCheckOutDate().isBefore(request.getTravelDate())) {
            throw new BookingValidationException("Valid check-out date is required for hotel bookings");
        }
    }

    private void validateCarRentalBooking(TravelBookingRequest request) {
        if (request.getDestination() == null) {
            throw new BookingValidationException("Pickup location is required for car rental bookings");
        }
        if (request.getCheckOutDate() == null || request.getCheckOutDate().isBefore(request.getTravelDate())) {
            throw new BookingValidationException("Valid return date is required for car rental bookings");
        }
    }

    private TravelBooking createBookingEntity(TravelBookingRequest request, TravelOption option) {
        return TravelBooking.builder()
                .userId(request.getUserId())
                .bookingType(request.getBookingType())
                .travelProvider(request.getTravelProvider())
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .travelDate(request.getTravelDate())
                .returnDate(request.getReturnDate())
                .checkOutDate(request.getCheckOutDate())
                .passengerCount(request.getPassengerCount())
                .passengerDetails(request.getPassengerDetails())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .totalPrice(option.getPrice())
                .currency(option.getCurrency())
                .bookingPreferences(request.getBookingPreferences())
                .specialRequests(request.getSpecialRequests())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void createCommissionRecord(TravelBooking booking, TravelOption option) {
        TravelPartner partner = partnerRepository.findByProvider(booking.getTravelProvider())
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found for provider: " + booking.getTravelProvider()));

        BigDecimal commissionRate = getCommissionRate(partner, booking);
        BigDecimal commissionAmount = booking.getTotalPrice().multiply(commissionRate).divide(BigDecimal.valueOf(100));

        CommissionRecord commissionRecord = CommissionRecord.builder()
                .partnerId(partner.getId())
                .bookingId(booking.getId())
                .bookingType(booking.getBookingType())
                .bookingAmount(booking.getTotalPrice())
                .commissionRate(commissionRate)
                .commissionAmount(commissionAmount)
                .currency(booking.getCurrency())
                .billingPeriod(YearMonth.now().toString())
                .status(CommissionRecord.CommissionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        commissionRecordRepository.save(commissionRecord);
    }

    private BigDecimal getCommissionRate(TravelPartner partner, TravelBooking booking) {
        return switch (booking.getBookingType()) {
            case FLIGHT -> partner.getFlightCommissionRate();
            case BUS -> partner.getBusCommissionRate();
            case HOTEL -> partner.getHotelCommissionRate();
            case CAR_RENTAL -> partner.getCarRentalCommissionRate();
        };
    }

    private void generateInitialDocuments(TravelBooking booking) {
        try {
            switch (booking.getBookingType()) {
                case FLIGHT -> {
                    documentGenerationService.generateETicket(booking);
                    documentGenerationService.generateItinerary(booking);
                }
                case BUS -> {
                    documentGenerationService.generateETicket(booking);
                }
                case HOTEL -> {
                    documentGenerationService.generateVoucher(booking);
                }
                case CAR_RENTAL -> {
                    documentGenerationService.generateVoucher(booking);
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate initial documents for booking: {}", booking.getId(), e);
        }
    }

    private String generateConfirmationNumber() {
        return "WTX" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void updateCommissionForCancellation(TravelBooking booking, BigDecimal refundAmount) {
        CommissionRecord commissionRecord = commissionRecordRepository.findByBookingId(booking.getId())
                .orElse(null);

        if (commissionRecord != null) {
            BigDecimal refundCommission = refundAmount.multiply(commissionRecord.getCommissionRate())
                    .divide(BigDecimal.valueOf(100));
            commissionRecord.setCommissionAmount(commissionRecord.getCommissionAmount().subtract(refundCommission));
            commissionRecord.setUpdatedAt(LocalDateTime.now());
            commissionRecordRepository.save(commissionRecord);
        }
    }

    private void updateCommissionForModification(TravelBooking booking, BigDecimal priceDifference) {
        CommissionRecord commissionRecord = commissionRecordRepository.findByBookingId(booking.getId())
                .orElse(null);

        if (commissionRecord != null) {
            BigDecimal commissionDifference = priceDifference.multiply(commissionRecord.getCommissionRate())
                    .divide(BigDecimal.valueOf(100));
            commissionRecord.setCommissionAmount(commissionRecord.getCommissionAmount().add(commissionDifference));
            commissionRecord.setBookingAmount(commissionRecord.getBookingAmount().add(priceDifference));
            commissionRecord.setUpdatedAt(LocalDateTime.now());
            commissionRecordRepository.save(commissionRecord);
        }
    }

    private void updateBookingFromRequest(TravelBooking booking, TravelBookingRequest request) {
        if (request.getTravelDate() != null) {
            booking.setTravelDate(request.getTravelDate());
        }
        if (request.getReturnDate() != null) {
            booking.setReturnDate(request.getReturnDate());
        }
        if (request.getPassengerDetails() != null) {
            booking.setPassengerDetails(request.getPassengerDetails());
        }
        if (request.getSpecialRequests() != null) {
            booking.setSpecialRequests(request.getSpecialRequests());
        }
    }

    private TravelBookingResponse convertToResponse(TravelBooking booking) {
        return TravelBookingResponse.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .confirmationNumber(booking.getConfirmationNumber())
                .userId(booking.getUserId())
                .bookingType(booking.getBookingType())
                .travelProvider(booking.getTravelProvider())
                .origin(booking.getOrigin())
                .destination(booking.getDestination())
                .travelDate(booking.getTravelDate())
                .returnDate(booking.getReturnDate())
                .checkOutDate(booking.getCheckOutDate())
                .passengerCount(booking.getPassengerCount())
                .passengerDetails(booking.getPassengerDetails())
                .totalPrice(booking.getTotalPrice())
                .currency(booking.getCurrency())
                .bookingStatus(booking.getBookingStatus().name())
                .contactEmail(booking.getContactEmail())
                .contactPhone(booking.getContactPhone())
                .specialRequests(booking.getSpecialRequests())
                .createdAt(booking.getCreatedAt())
                .confirmedAt(booking.getConfirmedAt())
                .build();
    }

    private TravelBookingResponse convertOptionToResponse(TravelOption option, TravelPartner partner) {
        return TravelBookingResponse.builder()
                .travelProvider(partner.getProvider())
                .providerName(partner.getPartnerName())
                .providerRating(partner.getSuccessRate())
                .origin(option.getOrigin())
                .destination(option.getDestination())
                .travelDate(option.getDepartureTime())
                .returnDate(option.getArrivalTime())
                .totalPrice(option.getPrice())
                .currency(option.getCurrency())
                .availableSeats(option.getAvailableSeats())
                .serviceClass(option.getServiceClass())
                .amenities(option.getAmenities())
                .refundable(option.isRefundable())
                .changeable(option.isChangeable())
                .duration(option.getDuration())
                .build();
    }

    private BookingStatistics calculateBookingStatistics(List<TravelBooking> bookings) {
        Map<TravelBooking.BookingType, Long> bookingsByType = bookings.stream()
                .collect(Collectors.groupingBy(TravelBooking::getBookingType, Collectors.counting()));

        Map<TravelBooking.BookingStatus, Long> bookingsByStatus = bookings.stream()
                .collect(Collectors.groupingBy(TravelBooking::getBookingStatus, Collectors.counting()));

        BigDecimal totalRevenue = bookings.stream()
                .map(TravelBooking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double averageBookingValue = bookings.isEmpty() ? 0.0 : 
                totalRevenue.divide(BigDecimal.valueOf(bookings.size()), 2, java.math.RoundingMode.HALF_UP).doubleValue();

        return BookingStatistics.builder()
                .totalBookings(bookings.size())
                .bookingsByType(bookingsByType)
                .bookingsByStatus(bookingsByStatus)
                .totalRevenue(totalRevenue)
                .averageBookingValue(averageBookingValue)
                .build();
    }

    private void clearBookingCaches(UUID userId) {
        redisTemplate.delete("user-bookings::" + userId + "-*");
        redisTemplate.delete("booking-stats::" + userId + "-*");
    }

    private TravelBooking getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));
    }

    // Data classes for responses
    @Data
    @Builder
    public static class BookingStatistics {
        private int totalBookings;
        private Map<TravelBooking.BookingType, Long> bookingsByType;
        private Map<TravelBooking.BookingStatus, Long> bookingsByStatus;
        private BigDecimal totalRevenue;
        private double averageBookingValue;
    }

    @Data
    @Builder
    public static class CommissionCalculationResult {
        private String billingPeriod;
        private LocalDateTime calculationDate;
        private int totalPartners;
        private int successfulCalculations;
        private BigDecimal totalCommissionAmount;
        private List<String> errors;
    }

    @Data
    @Builder
    public static class TravelOption {
        private String providerId;
        private String providerName;
        private String origin;
        private String destination;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private BigDecimal price;
        private String currency;
        private int availableSeats;
        private String serviceClass;
        private List<String> amenities;
        private boolean refundable;
        private boolean changeable;
        private String duration;
        private Map<String, Object> additionalData;
    }

    // Exception classes
    public static class BookingException extends RuntimeException {
        public BookingException(String message) { super(message); }
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(String message) { super(message); }
    }

    public static class BookingValidationException extends RuntimeException {
        public BookingValidationException(String message) { super(message); }
    }

    public static class DocumentGenerationException extends RuntimeException {
        public DocumentGenerationException(String message) { super(message); }
    }

    public static class PartnerNotFoundException extends RuntimeException {
        public PartnerNotFoundException(String message) { super(message); }
    }

    public static class CommissionException extends RuntimeException {
        public CommissionException(String message) { super(message); }
    }
}