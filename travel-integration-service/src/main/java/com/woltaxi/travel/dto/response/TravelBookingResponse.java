package com.woltaxi.travel.dto.response;

import com.woltaxi.travel.entity.TravelBooking;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Travel Booking Response DTO
 * 
 * Comprehensive response object for travel booking operations with
 * booking confirmation details, ticket information, and status updates.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"paymentDetails", "providerMetadata"})
public class TravelBookingResponse {

    private UUID bookingId;

    private String bookingReference;

    private String providerBookingId;

    private UUID userId;

    private TravelBooking.BookingType bookingType;

    private TravelBooking.TravelProvider travelProvider;

    private TravelBooking.BookingStatus bookingStatus;

    private String origin;

    private String destination;

    private LocalDateTime travelDate;

    private LocalDateTime returnDate;

    private Integer passengerCount;

    private BigDecimal totalAmount;

    private String currency;

    private BigDecimal commissionAmount;

    private String confirmationCode;

    private String ticketNumber;

    private List<String> seatNumbers;

    private TicketInfo ticketInfo;

    private BookingDetails bookingDetails;

    private PaymentInfo paymentInfo;

    private CommissionInfo commissionInfo;

    private DocumentInfo documentInfo;

    private StatusInfo statusInfo;

    private Map<String, Object> providerMetadata;

    private LocalDateTime createdAt;

    private LocalDateTime bookingConfirmedAt;

    private Long version;

    /**
     * Ticket Information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketInfo {

        private String ticketNumber;

        private String confirmationCode;

        private List<String> seatNumbers;

        private String pnrCode;

        private String eTicketNumber;

        private String boardingPassUrl;

        private String ticketPdfUrl;

        private String qrCodeUrl;

        private String mobileTicketUrl;

        private Boolean isElectronicTicket;

        private Boolean requiresPrintedTicket;

        private String checkInUrl;

        private LocalDateTime checkInOpenTime;

        private LocalDateTime checkInCloseTime;

        private String boardingGate;

        private String terminal;

        private LocalDateTime departureTime;

        private LocalDateTime arrivalTime;

        private String flightNumber;

        private String aircraftType;

        private String operatingAirline;

        private List<FlightSegment> flightSegments;
    }

    /**
     * Flight Segment Information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlightSegment {

        private String segmentNumber;

        private String departureAirport;

        private String arrivalAirport;

        private LocalDateTime departureTime;

        private LocalDateTime arrivalTime;

        private String flightNumber;

        private String operatingAirline;

        private String aircraftType;

        private String cabin;

        private String bookingClass;

        private Integer durationMinutes;

        private String mealService;

        private String seatNumber;

        private Boolean isLayover;

        private Integer layoverDurationMinutes;
    }

    /**
     * Booking Details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingDetails {

        private String origin;

        private String destination;

        private String originName;

        private String destinationName;

        private LocalDateTime travelDate;

        private LocalDateTime returnDate;

        private Integer passengerCount;

        private String classType;

        private String cabinType;

        private Boolean isRoundTrip;

        private Boolean isMultiCity;

        private Boolean isRefundable;

        private Boolean isChangeable;

        private String cancellationPolicy;

        private String baggageAllowance;

        private List<PassengerInfo> passengers;

        private Map<String, Object> additionalServices;
    }

    /**
     * Passenger Information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PassengerInfo {

        private String firstName;

        private String lastName;

        private String passengerType;

        private String seatNumber;

        private String mealPreference;

        private String frequentFlyerNumber;

        private Boolean hasSpecialNeeds;

        private String specialNeeds;

        private String ticketNumber;

        private String boardingPassUrl;
    }

    /**
     * Payment Information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {

        private UUID paymentTransactionId;

        private String paymentStatus;

        private String paymentMethod;

        private BigDecimal paidAmount;

        private String paymentCurrency;

        private LocalDateTime paymentDate;

        private String paymentReference;

        private BigDecimal refundAmount;

        private LocalDateTime refundDate;

        private String refundStatus;

        private Map<String, Object> paymentDetails;
    }

    /**
     * Commission Information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommissionInfo {

        private BigDecimal commissionRate;

        private BigDecimal commissionAmount;

        private String commissionStatus;

        private String billingPeriod;

        private LocalDateTime commissionDate;

        private String invoiceNumber;

        private LocalDateTime invoiceDate;

        private BigDecimal netCommission;
    }

    /**
     * Document Information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentInfo {

        private String ticketPdfUrl;

        private String invoicePdfUrl;

        private String itineraryPdfUrl;

        private String boardingPassPdfUrl;

        private String qrCodeImageUrl;

        private String mobileWalletPassUrl;

        private Boolean documentsGenerated;

        private LocalDateTime documentsGeneratedAt;

        private List<String> additionalDocuments;
    }

    /**
     * Status Information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusInfo {

        private String currentStatus;

        private String statusDescription;

        private LocalDateTime statusUpdatedAt;

        private Boolean canCancel;

        private Boolean canModify;

        private Boolean canCheckIn;

        private Boolean canRefund;

        private LocalDateTime cancellationDeadline;

        private LocalDateTime modificationDeadline;

        private LocalDateTime checkInAvailableFrom;

        private LocalDateTime checkInAvailableUntil;

        private List<StatusHistory> statusHistory;

        private String nextAction;

        private String nextActionUrl;
    }

    /**
     * Status History
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusHistory {

        private String status;

        private String statusDescription;

        private LocalDateTime timestamp;

        private String updatedBy;

        private String notes;
    }

    // Helper methods for status checking
    public boolean isConfirmed() {
        return TravelBooking.BookingStatus.CONFIRMED.equals(this.bookingStatus);
    }

    public boolean isPending() {
        return TravelBooking.BookingStatus.PENDING.equals(this.bookingStatus);
    }

    public boolean isCancelled() {
        return TravelBooking.BookingStatus.CANCELLED.equals(this.bookingStatus);
    }

    public boolean isCompleted() {
        return TravelBooking.BookingStatus.COMPLETED.equals(this.bookingStatus);
    }

    public boolean isCheckedIn() {
        return TravelBooking.BookingStatus.CHECKED_IN.equals(this.bookingStatus);
    }

    public boolean canCancel() {
        return this.statusInfo != null && 
               Boolean.TRUE.equals(this.statusInfo.getCanCancel());
    }

    public boolean canModify() {
        return this.statusInfo != null && 
               Boolean.TRUE.equals(this.statusInfo.getCanModify());
    }

    public boolean canCheckIn() {
        return this.statusInfo != null && 
               Boolean.TRUE.equals(this.statusInfo.getCanCheckIn());
    }

    public boolean canRefund() {
        return this.statusInfo != null && 
               Boolean.TRUE.equals(this.statusInfo.getCanRefund());
    }

    public boolean hasTickets() {
        return this.ticketInfo != null && 
               this.ticketInfo.getTicketNumber() != null;
    }

    public boolean hasBoardingPasses() {
        return this.ticketInfo != null && 
               this.ticketInfo.getBoardingPassUrl() != null;
    }

    public boolean isElectronicTicket() {
        return this.ticketInfo != null && 
               Boolean.TRUE.equals(this.ticketInfo.getIsElectronicTicket());
    }

    public boolean isRoundTrip() {
        return this.returnDate != null;
    }

    public boolean isMultiPassenger() {
        return this.passengerCount != null && this.passengerCount > 1;
    }

    public boolean requiresDocuments() {
        return this.documentInfo == null || 
               !Boolean.TRUE.equals(this.documentInfo.getDocumentsGenerated());
    }

    public String getDisplayStatus() {
        if (this.statusInfo != null && this.statusInfo.getStatusDescription() != null) {
            return this.statusInfo.getStatusDescription();
        }
        
        return switch (this.bookingStatus) {
            case PENDING -> "Booking is being processed";
            case CONFIRMED -> "Booking confirmed successfully";
            case CANCELLED -> "Booking has been cancelled";
            case EXPIRED -> "Booking has expired";
            case CHECKED_IN -> "Checked in successfully";
            case COMPLETED -> "Travel completed";
            case REFUNDED -> "Booking refunded";
            case PARTIALLY_REFUNDED -> "Booking partially refunded";
            case NO_SHOW -> "No show recorded";
        };
    }

    public String getDisplayAmount() {
        if (this.totalAmount == null || this.currency == null) {
            return "N/A";
        }
        return String.format("%.2f %s", this.totalAmount, this.currency);
    }

    public LocalDateTime getNextDeadline() {
        if (this.statusInfo == null) return null;
        
        LocalDateTime now = LocalDateTime.now();
        
        // Check cancellation deadline
        if (this.statusInfo.getCancellationDeadline() != null && 
            this.statusInfo.getCancellationDeadline().isAfter(now)) {
            return this.statusInfo.getCancellationDeadline();
        }
        
        // Check modification deadline
        if (this.statusInfo.getModificationDeadline() != null && 
            this.statusInfo.getModificationDeadline().isAfter(now)) {
            return this.statusInfo.getModificationDeadline();
        }
        
        // Check check-in availability
        if (this.statusInfo.getCheckInAvailableFrom() != null && 
            this.statusInfo.getCheckInAvailableFrom().isAfter(now)) {
            return this.statusInfo.getCheckInAvailableFrom();
        }
        
        return null;
    }

    public String getNextDeadlineType() {
        LocalDateTime nextDeadline = getNextDeadline();
        if (nextDeadline == null) return null;
        
        if (this.statusInfo.getCancellationDeadline() != null && 
            this.statusInfo.getCancellationDeadline().equals(nextDeadline)) {
            return "Cancellation Deadline";
        }
        
        if (this.statusInfo.getModificationDeadline() != null && 
            this.statusInfo.getModificationDeadline().equals(nextDeadline)) {
            return "Modification Deadline";
        }
        
        if (this.statusInfo.getCheckInAvailableFrom() != null && 
            this.statusInfo.getCheckInAvailableFrom().equals(nextDeadline)) {
            return "Check-in Opens";
        }
        
        return "Important Deadline";
    }

    // Factory methods for common responses
    public static TravelBookingResponse success(TravelBooking booking) {
        return TravelBookingResponse.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .providerBookingId(booking.getProviderBookingId())
                .userId(booking.getUserId())
                .bookingType(booking.getBookingType())
                .travelProvider(booking.getTravelProvider())
                .bookingStatus(booking.getBookingStatus())
                .origin(booking.getOrigin())
                .destination(booking.getDestination())
                .travelDate(booking.getTravelDate())
                .returnDate(booking.getReturnDate())
                .passengerCount(booking.getPassengerCount())
                .totalAmount(booking.getTotalAmount())
                .currency(booking.getCurrency())
                .commissionAmount(booking.getCommissionAmount())
                .confirmationCode(booking.getConfirmationCode())
                .ticketNumber(booking.getTicketNumber())
                .createdAt(booking.getCreatedAt())
                .bookingConfirmedAt(booking.getBookingConfirmedAt())
                .version(booking.getVersion())
                .build();
    }

    public static TravelBookingResponse pending(UUID bookingId, String bookingReference, String message) {
        return TravelBookingResponse.builder()
                .bookingId(bookingId)
                .bookingReference(bookingReference)
                .bookingStatus(TravelBooking.BookingStatus.PENDING)
                .statusInfo(StatusInfo.builder()
                        .currentStatus("PENDING")
                        .statusDescription(message)
                        .statusUpdatedAt(LocalDateTime.now())
                        .build())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static TravelBookingResponse failed(UUID bookingId, String bookingReference, String errorMessage) {
        return TravelBookingResponse.builder()
                .bookingId(bookingId)
                .bookingReference(bookingReference)
                .bookingStatus(TravelBooking.BookingStatus.CANCELLED)
                .statusInfo(StatusInfo.builder()
                        .currentStatus("FAILED")
                        .statusDescription(errorMessage)
                        .statusUpdatedAt(LocalDateTime.now())
                        .build())
                .createdAt(LocalDateTime.now())
                .build();
    }
}