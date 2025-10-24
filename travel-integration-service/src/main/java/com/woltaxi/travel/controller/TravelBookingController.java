package com.woltaxi.travel.controller;

import com.woltaxi.travel.dto.request.TravelBookingRequest;
import com.woltaxi.travel.dto.response.TravelBookingResponse;
import com.woltaxi.travel.entity.TravelBooking;
import com.woltaxi.travel.entity.CommissionRecord;
import com.woltaxi.travel.service.TravelBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Travel Booking Controller
 * 
 * REST API endpoints for travel booking operations including
 * flights, buses, hotels, and car rentals with commission tracking.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Travel Booking", description = "Comprehensive Travel Booking & Commission Management API")
@SecurityRequirement(name = "bearerAuth")
public class TravelBookingController {

    private final TravelBookingService bookingService;

    @Operation(
        summary = "Create Travel Booking",
        description = "Create a new travel booking for flights, buses, hotels, or car rentals"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Booking created successfully",
            content = @Content(schema = @Schema(implementation = TravelBookingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid booking request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "422", description = "Booking validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<TravelBookingResponse> createBooking(
            @Valid @RequestBody TravelBookingRequest request) {
        
        log.info("Creating travel booking for user: {} type: {} provider: {}", 
                request.getUserId(), request.getBookingType(), request.getTravelProvider());
        
        TravelBookingResponse response = bookingService.createBooking(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Search Available Options",
        description = "Search for available travel options based on criteria"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search criteria")
    })
    @PostMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<List<TravelBookingResponse>> searchTravelOptions(
            @Valid @RequestBody TravelBookingRequest searchCriteria) {
        
        log.info("Searching travel options: {} from {} to {} on {}", 
                searchCriteria.getBookingType(), searchCriteria.getOrigin(), 
                searchCriteria.getDestination(), searchCriteria.getTravelDate());
        
        List<TravelBookingResponse> options = bookingService.searchTravelOptions(searchCriteria);
        
        return ResponseEntity.ok(options);
    }

    @Operation(
        summary = "Confirm Booking",
        description = "Confirm a pending travel booking"
    )
    @PostMapping("/{bookingId}/confirm")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<TravelBookingResponse> confirmBooking(
            @Parameter(description = "Booking ID to confirm") 
            @PathVariable UUID bookingId) {
        
        log.info("Confirming booking: {}", bookingId);
        
        TravelBookingResponse response = bookingService.confirmBooking(bookingId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Cancel Booking",
        description = "Cancel a confirmed travel booking"
    )
    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<TravelBookingResponse> cancelBooking(
            @Parameter(description = "Booking ID to cancel") 
            @PathVariable UUID bookingId,
            
            @Parameter(description = "Cancellation reason") 
            @RequestParam String reason) {
        
        log.info("Cancelling booking: {} with reason: {}", bookingId, reason);
        
        TravelBookingResponse response = bookingService.cancelBooking(bookingId, reason);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Modify Booking",
        description = "Modify an existing travel booking"
    )
    @PutMapping("/{bookingId}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<TravelBookingResponse> modifyBooking(
            @Parameter(description = "Booking ID to modify") 
            @PathVariable UUID bookingId,
            
            @Valid @RequestBody TravelBookingRequest modificationRequest) {
        
        log.info("Modifying booking: {}", bookingId);
        
        TravelBookingResponse response = bookingService.modifyBooking(bookingId, modificationRequest);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Check-in for Booking",
        description = "Perform online check-in for flight bookings"
    )
    @PostMapping("/{bookingId}/checkin")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<TravelBookingResponse> checkInBooking(
            @Parameter(description = "Booking ID for check-in") 
            @PathVariable UUID bookingId) {
        
        log.info("Checking in booking: {}", bookingId);
        
        TravelBookingResponse response = bookingService.checkInBooking(bookingId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get Booking Details",
        description = "Retrieve detailed information about a travel booking"
    )
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<TravelBookingResponse> getBooking(
            @Parameter(description = "Booking ID") 
            @PathVariable UUID bookingId) {
        
        TravelBookingResponse response = bookingService.getBookingById(bookingId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get Booking by Reference",
        description = "Retrieve booking details using booking reference"
    )
    @GetMapping("/reference/{bookingReference}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<TravelBookingResponse> getBookingByReference(
            @Parameter(description = "Booking reference") 
            @PathVariable String bookingReference) {
        
        TravelBookingResponse response = bookingService.getBookingByReference(bookingReference);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get User Bookings",
        description = "Retrieve all bookings for a specific user"
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<Page<TravelBookingResponse>> getUserBookings(
            @Parameter(description = "User ID") 
            @PathVariable UUID userId,
            
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") @Min(1) int size,
            
            @Parameter(description = "Booking status filter") 
            @RequestParam(required = false) TravelBooking.BookingStatus status) {
        
        List<TravelBookingResponse> bookings = bookingService.getUserBookings(userId, status, page, size);
        Page<TravelBookingResponse> result = new PageImpl<>(bookings, PageRequest.of(page, size), bookings.size());
        
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Get Bookings by Provider",
        description = "Retrieve bookings from a specific travel provider"
    )
    @GetMapping("/provider/{provider}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<Page<TravelBookingResponse>> getBookingsByProvider(
            @Parameter(description = "Travel provider") 
            @PathVariable TravelBooking.TravelProvider provider,
            
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        List<TravelBookingResponse> bookings = bookingService.getBookingsByProvider(provider, page, size);
        Page<TravelBookingResponse> result = new PageImpl<>(bookings, PageRequest.of(page, size), bookings.size());
        
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Get Booking Statistics",
        description = "Retrieve booking statistics for a user or global statistics"
    )
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<TravelBookingService.BookingStatistics> getBookingStatistics(
            @Parameter(description = "User ID (optional for global stats)") 
            @RequestParam(required = false) UUID userId,
            
            @Parameter(description = "Start date for statistics") 
            @RequestParam(required = false) String startDate,
            
            @Parameter(description = "End date for statistics") 
            @RequestParam(required = false) String endDate) {
        
        TravelBookingService.BookingStatistics statistics = bookingService.getBookingStatistics(userId, startDate, endDate);
        
        return ResponseEntity.ok(statistics);
    }

    @Operation(
        summary = "Generate Booking Documents",
        description = "Generate PDF tickets, invoices, and other booking documents"
    )
    @PostMapping("/{bookingId}/documents")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<Map<String, String>> generateBookingDocuments(
            @Parameter(description = "Booking ID") 
            @PathVariable UUID bookingId,
            
            @Parameter(description = "Document types to generate") 
            @RequestParam List<String> documentTypes) {
        
        log.info("Generating documents for booking: {} types: {}", bookingId, documentTypes);
        
        Map<String, String> documentUrls = bookingService.generateBookingDocuments(bookingId, documentTypes);
        
        return ResponseEntity.ok(documentUrls);
    }

    @Operation(
        summary = "Get Commission Records",
        description = "Retrieve commission records for bookings"
    )
    @GetMapping("/commissions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<Page<CommissionRecord>> getCommissionRecords(
            @Parameter(description = "Partner ID filter") 
            @RequestParam(required = false) UUID partnerId,
            
            @Parameter(description = "Billing period (YYYY-MM)") 
            @RequestParam(required = false) String billingPeriod,
            
            @Parameter(description = "Commission status filter") 
            @RequestParam(required = false) CommissionRecord.CommissionStatus status,
            
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        List<CommissionRecord> records = bookingService.getCommissionRecords(partnerId, billingPeriod, status, page, size);
        Page<CommissionRecord> result = new PageImpl<>(records, PageRequest.of(page, size), records.size());
        
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Calculate Monthly Commissions",
        description = "Calculate and process monthly commissions for all partners"
    )
    @PostMapping("/commissions/calculate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<TravelBookingService.CommissionCalculationResult> calculateMonthlyCommissions(
            @Parameter(description = "Year for commission calculation") 
            @RequestParam int year,
            
            @Parameter(description = "Month for commission calculation") 
            @RequestParam int month) {
        
        log.info("Calculating monthly commissions for {}-{:02d}", year, month);
        
        TravelBookingService.CommissionCalculationResult result = bookingService.calculateMonthlyCommissions(year, month);
        
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Generate Commission Invoice",
        description = "Generate monthly commission invoice for a partner"
    )
    @PostMapping("/commissions/{partnerId}/invoice")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<String> generateCommissionInvoice(
            @Parameter(description = "Partner ID") 
            @PathVariable UUID partnerId,
            
            @Parameter(description = "Billing period (YYYY-MM)") 
            @RequestParam String billingPeriod) {
        
        log.info("Generating commission invoice for partner: {} period: {}", partnerId, billingPeriod);
        
        String invoicePdfUrl = bookingService.generateCommissionInvoice(partnerId, billingPeriod);
        
        return ResponseEntity.ok(invoicePdfUrl);
    }

    @Operation(
        summary = "Mark Commission as Paid",
        description = "Mark commission records as paid"
    )
    @PostMapping("/commissions/{partnerId}/pay")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<Void> markCommissionAsPaid(
            @Parameter(description = "Partner ID") 
            @PathVariable UUID partnerId,
            
            @Parameter(description = "Billing period (YYYY-MM)") 
            @RequestParam String billingPeriod,
            
            @Parameter(description = "Payment amount") 
            @RequestParam BigDecimal paymentAmount,
            
            @Parameter(description = "Payment method") 
            @RequestParam String paymentMethod,
            
            @Parameter(description = "Payment reference") 
            @RequestParam String paymentReference) {
        
        log.info("Marking commission as paid for partner: {} period: {} amount: {}", 
                partnerId, billingPeriod, paymentAmount);
        
        bookingService.markCommissionAsPaid(partnerId, billingPeriod, paymentAmount, paymentMethod, paymentReference);
        
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get Travel Options",
        description = "Get available travel options for booking type and route"
    )
    @GetMapping("/options")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<List<TravelBookingService.TravelOption>> getTravelOptions(
            @Parameter(description = "Booking type") 
            @RequestParam TravelBooking.BookingType bookingType,
            
            @Parameter(description = "Origin") 
            @RequestParam String origin,
            
            @Parameter(description = "Destination") 
            @RequestParam String destination,
            
            @Parameter(description = "Travel date") 
            @RequestParam String travelDate,
            
            @Parameter(description = "Passenger count") 
            @RequestParam(defaultValue = "1") int passengerCount) {
        
        List<TravelBookingService.TravelOption> options = bookingService.getTravelOptions(
                bookingType, origin, destination, java.time.LocalDateTime.parse(travelDate), passengerCount);
        
        return ResponseEntity.ok(options);
    }

    // Exception handlers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Travel booking controller error", e);
        
        ErrorResponse error = ErrorResponse.builder()
                .error("BOOKING_ERROR")
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @lombok.Data
    @lombok.Builder
    private static class ErrorResponse {
        private String error;
        private String message;
        private long timestamp;
    }
}