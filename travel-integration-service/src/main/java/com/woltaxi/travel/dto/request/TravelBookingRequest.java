package com.woltaxi.travel.dto.request;

import com.woltaxi.travel.entity.TravelBooking;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Travel Booking Request DTO
 * 
 * Comprehensive request object for booking travel services including
 * flights, buses, hotels, and car rentals with passenger details
 * and payment information.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"passengerDetails", "paymentInfo", "specialRequests"})
public class TravelBookingRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Booking type is required")
    private TravelBooking.BookingType bookingType;

    @NotNull(message = "Travel provider is required")
    private TravelBooking.TravelProvider travelProvider;

    @NotBlank(message = "Origin is required")
    @Size(max = 100, message = "Origin must not exceed 100 characters")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(max = 100, message = "Destination must not exceed 100 characters")
    private String destination;

    @NotNull(message = "Travel date is required")
    @Future(message = "Travel date must be in the future")
    private LocalDateTime travelDate;

    private LocalDateTime returnDate;

    @NotNull(message = "Passenger count is required")
    @Min(value = 1, message = "Passenger count must be at least 1")
    @Max(value = 9, message = "Passenger count must not exceed 9")
    private Integer passengerCount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Total amount format is invalid")
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    @NotEmpty(message = "Passenger details are required")
    @Valid
    private List<PassengerDetails> passengerDetails;

    @NotNull(message = "Contact information is required")
    @Valid
    private ContactInfo contactInfo;

    @Valid
    private PaymentInfo paymentInfo;

    @Valid
    private BookingPreferences preferences;

    @Size(max = 1000, message = "Special requests must not exceed 1000 characters")
    private String specialRequests;

    private Map<String, Object> additionalInfo;

    private Boolean acceptTermsAndConditions = false;

    /**
     * Passenger Details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PassengerDetails {

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        private String lastName;

        @Size(max = 100, message = "Middle name must not exceed 100 characters")
        private String middleName;

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        private LocalDateTime dateOfBirth;

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
        private String gender;

        @NotBlank(message = "Nationality is required")
        @Size(min = 2, max = 3, message = "Nationality must be 2-3 characters")
        private String nationality;

        @Size(max = 50, message = "Passport number must not exceed 50 characters")
        private String passportNumber;

        private LocalDateTime passportExpiryDate;

        @Size(max = 50, message = "National ID must not exceed 50 characters")
        private String nationalId;

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        private String phoneNumber;

        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        private String email;

        private Boolean isChild = false;

        private Boolean isInfant = false;

        @Size(max = 20, message = "Passenger type must not exceed 20 characters")
        private String passengerType = "ADULT";

        @Size(max = 500, message = "Special needs must not exceed 500 characters")
        private String specialNeeds;

        @Size(max = 20, message = "Meal preference must not exceed 20 characters")
        private String mealPreference;

        @Size(max = 10, message = "Seat preference must not exceed 10 characters")
        private String seatPreference; // WINDOW, AISLE, ANY

        private Boolean frequentFlyerMember = false;

        @Size(max = 50, message = "Frequent flyer number must not exceed 50 characters")
        private String frequentFlyerNumber;
    }

    /**
     * Contact Information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactInfo {

        @NotBlank(message = "Contact email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Contact email must not exceed 255 characters")
        private String email;

        @NotBlank(message = "Contact phone is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        @Size(max = 20, message = "Contact phone must not exceed 20 characters")
        private String phoneNumber;

        @Size(max = 20, message = "Alternative phone must not exceed 20 characters")
        private String alternativePhone;

        @Size(max = 255, message = "Address must not exceed 255 characters")
        private String address;

        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        private String postalCode;

        @Size(max = 3, message = "Country must not exceed 3 characters")
        private String country;

        private Boolean smsNotifications = true;

        private Boolean emailNotifications = true;

        private Boolean whatsappNotifications = false;
    }

    /**
     * Payment Information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {

        @NotBlank(message = "Payment method is required")
        @Size(max = 50, message = "Payment method must not exceed 50 characters")
        private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, WALLET, BANK_TRANSFER

        private UUID paymentTransactionId;

        @Size(max = 100, message = "Payment reference must not exceed 100 characters")
        private String paymentReference;

        private Boolean useWalletBalance = false;

        private Boolean useLoyaltyPoints = false;

        @Min(value = 0, message = "Loyalty points to use must be non-negative")
        private Integer loyaltyPointsToUse = 0;

        @Size(max = 20, message = "Installment option must not exceed 20 characters")
        private String installmentOption; // NONE, 2_MONTHS, 3_MONTHS, 6_MONTHS, 12_MONTHS

        private Map<String, Object> paymentDetails;
    }

    /**
     * Booking Preferences
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingPreferences {

        @Size(max = 20, message = "Class preference must not exceed 20 characters")
        private String classPreference; // ECONOMY, BUSINESS, FIRST for flights

        @Size(max = 20, message = "Cabin preference must not exceed 20 characters")
        private String cabinPreference; // For buses: STANDARD, VIP, SLEEPER

        private Boolean flexibleDates = false;

        @Min(value = 0, message = "Date flexibility days must be non-negative")
        @Max(value = 7, message = "Date flexibility days must not exceed 7")
        private Integer dateFlexibilityDays = 0;

        private Boolean refundableTicket = false;

        private Boolean changeableTicket = false;

        private Boolean directFlightOnly = false; // For flights

        private Boolean preferredAirlines; // Use only preferred airlines

        private List<String> preferredAirlineCodes;

        private Boolean baggageIncluded = false;

        @Min(value = 0, message = "Baggage weight must be non-negative")
        private Integer baggageWeightKg = 0;

        private Boolean seatSelection = false;

        private List<String> preferredSeatNumbers;

        private Boolean mealIncluded = false;

        private Boolean priorityBoarding = false;

        private Boolean loungeAccess = false;

        private Boolean travelInsurance = false;

        @Size(max = 20, message = "Insurance type must not exceed 20 characters")
        private String insuranceType; // BASIC, PREMIUM, COMPREHENSIVE

        private Boolean carbonOffsetProgram = false;

        private Map<String, Object> additionalPreferences;
    }

    // Validation methods
    public boolean isRoundTrip() {
        return this.returnDate != null;
    }

    public boolean isMultiPassenger() {
        return this.passengerCount > 1;
    }

    public boolean hasChildren() {
        return this.passengerDetails.stream()
                .anyMatch(passenger -> Boolean.TRUE.equals(passenger.getIsChild()));
    }

    public boolean hasInfants() {
        return this.passengerDetails.stream()
                .anyMatch(passenger -> Boolean.TRUE.equals(passenger.getIsInfant()));
    }

    public boolean requiresPassportInfo() {
        return TravelBooking.BookingType.FLIGHT.equals(this.bookingType);
    }

    public boolean isInternationalTravel() {
        // Simple check based on origin/destination format
        // In real implementation, this would check against airport/city codes
        return !this.origin.substring(0, 2).equalsIgnoreCase(this.destination.substring(0, 2));
    }

    public boolean isGroupBooking() {
        return this.passengerCount >= 6;
    }

    public boolean requiresSpecialAssistance() {
        return this.passengerDetails.stream()
                .anyMatch(passenger -> passenger.getSpecialNeeds() != null && 
                                     !passenger.getSpecialNeeds().trim().isEmpty());
    }

    public boolean isFlexibleBooking() {
        return this.preferences != null && 
               Boolean.TRUE.equals(this.preferences.getFlexibleDates());
    }

    public boolean isPremiumBooking() {
        return this.preferences != null && 
               ("BUSINESS".equals(this.preferences.getClassPreference()) ||
                "FIRST".equals(this.preferences.getClassPreference()) ||
                "VIP".equals(this.preferences.getCabinPreference()));
    }

    public BigDecimal getEstimatedLoyaltyPointsEarned() {
        if (this.totalAmount == null) return BigDecimal.ZERO;
        
        // Base calculation: 1 point per 10 currency units
        BigDecimal basePoints = this.totalAmount.divide(BigDecimal.valueOf(10), 0, java.math.RoundingMode.DOWN);
        
        // Premium booking bonus
        if (isPremiumBooking()) {
            basePoints = basePoints.multiply(BigDecimal.valueOf(2));
        }
        
        // Group booking bonus
        if (isGroupBooking()) {
            basePoints = basePoints.multiply(BigDecimal.valueOf(1.5));
        }
        
        return basePoints;
    }

    public boolean hasValidPassengerCount() {
        return this.passengerDetails != null && 
               this.passengerDetails.size() == this.passengerCount;
    }

    @AssertTrue(message = "Terms and conditions must be accepted")
    public boolean isTermsAccepted() {
        return Boolean.TRUE.equals(this.acceptTermsAndConditions);
    }

    @AssertTrue(message = "Return date must be after travel date for round trips")
    public boolean isValidReturnDate() {
        if (!isRoundTrip()) return true;
        return this.returnDate.isAfter(this.travelDate);
    }

    @AssertTrue(message = "Passenger details count must match passenger count")
    public boolean isValidPassengerDetails() {
        return hasValidPassengerCount();
    }

    @AssertTrue(message = "International travel requires passport information")
    public boolean hasRequiredPassportInfo() {
        if (!requiresPassportInfo() || !isInternationalTravel()) return true;
        
        return this.passengerDetails.stream()
                .allMatch(passenger -> passenger.getPassportNumber() != null && 
                                     !passenger.getPassportNumber().trim().isEmpty() &&
                                     passenger.getPassportExpiryDate() != null &&
                                     passenger.getPassportExpiryDate().isAfter(this.travelDate.plusMonths(6)));
    }
}