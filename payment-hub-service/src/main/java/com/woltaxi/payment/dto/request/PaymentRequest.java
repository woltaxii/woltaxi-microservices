package com.woltaxi.payment.dto.request;

import com.woltaxi.payment.entity.PaymentTransaction;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Processing Request DTO
 * 
 * Request object for processing payments across all supported payment providers
 * with comprehensive validation and security features.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"cardDetails", "sensitiveData"})
public class PaymentRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private UUID subscriptionId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Amount format is invalid")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    @NotNull(message = "Payment provider is required")
    private PaymentTransaction.PaymentProvider paymentProvider;

    @NotNull(message = "Payment method is required")
    private PaymentTransaction.PaymentMethod paymentMethod;

    @NotNull(message = "Transaction type is required")
    private PaymentTransaction.TransactionType transactionType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Valid
    private CardDetails cardDetails;

    @Valid
    private BillingAddress billingAddress;

    @Valid
    private DeviceInfo deviceInfo;

    @Size(max = 100, message = "Return URL must not exceed 100 characters")
    private String returnUrl;

    @Size(max = 100, message = "Cancel URL must not exceed 100 characters")
    private String cancelUrl;

    @Size(max = 100, message = "Webhook URL must not exceed 100 characters")
    private String webhookUrl;

    private Boolean savePaymentMethod = false;

    private Boolean useStoredPaymentMethod = false;

    @Size(max = 100, message = "Stored payment method ID must not exceed 100 characters")
    private String storedPaymentMethodId;

    private Boolean requiresAuthentication = true;

    private Boolean autoCapture = true;

    @Size(max = 50, message = "Customer reference must not exceed 50 characters")
    private String customerReference;

    @Size(max = 50, message = "Order reference must not exceed 50 characters")
    private String orderReference;

    private Map<String, Object> metadata;

    private Map<String, Object> providerSpecificData;

    private Map<String, Object> sensitiveData;

    /**
     * Card Details for payment processing
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"cardNumber", "cvv"})
    public static class CardDetails {

        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits")
        private String cardNumber;

        @NotNull(message = "Expiry month is required")
        @Min(value = 1, message = "Expiry month must be between 1 and 12")
        @Max(value = 12, message = "Expiry month must be between 1 and 12")
        private Integer expiryMonth;

        @NotNull(message = "Expiry year is required")
        @Min(value = 2024, message = "Expiry year must be valid")
        private Integer expiryYear;

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3-4 digits")
        private String cvv;

        @NotBlank(message = "Cardholder name is required")
        @Size(max = 100, message = "Cardholder name must not exceed 100 characters")
        private String cardholderName;

        @Size(max = 20, message = "Card brand must not exceed 20 characters")
        private String cardBrand;

        private Boolean is3DSecureEnabled = true;
    }

    /**
     * Billing Address for payment processing
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillingAddress {

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        private String lastName;

        @Size(max = 100, message = "Company name must not exceed 100 characters")
        private String company;

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
        private String addressLine1;

        @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
        private String addressLine2;

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;

        @Size(max = 100, message = "State must not exceed 100 characters")
        private String state;

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        private String postalCode;

        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 3, message = "Country must be 2-3 characters")
        private String country;

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        private String phoneNumber;

        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        private String email;
    }

    /**
     * Device Information for fraud prevention
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceInfo {

        @Size(max = 45, message = "IP address must not exceed 45 characters")
        private String ipAddress;

        @Size(max = 500, message = "User agent must not exceed 500 characters")
        private String userAgent;

        @Size(max = 100, message = "Device fingerprint must not exceed 100 characters")
        private String deviceFingerprint;

        @Size(max = 50, message = "Device ID must not exceed 50 characters")
        private String deviceId;

        @Size(max = 50, message = "Session ID must not exceed 50 characters")
        private String sessionId;

        @Size(max = 10, message = "Device type must not exceed 10 characters")
        private String deviceType; // WEB, MOBILE, TABLET

        @Size(max = 50, message = "Operating system must not exceed 50 characters")
        private String operatingSystem;

        @Size(max = 50, message = "Browser must not exceed 50 characters")
        private String browser;

        @Size(max = 20, message = "Browser version must not exceed 20 characters")
        private String browserVersion;

        @Size(max = 10, message = "Screen resolution must not exceed 10 characters")
        private String screenResolution;

        @Size(max = 10, message = "Timezone must not exceed 10 characters")
        private String timezone;

        @Size(max = 5, message = "Language must not exceed 5 characters")
        private String language;

        private Boolean javaScriptEnabled;

        private Boolean cookiesEnabled;

        private Double latitude;

        private Double longitude;
    }

    // Validation methods
    public boolean isRecurringPayment() {
        return PaymentTransaction.TransactionType.SUBSCRIPTION.equals(this.transactionType);
    }

    public boolean requiresCardDetails() {
        return PaymentTransaction.PaymentMethod.CARD.equals(this.paymentMethod) && 
               !Boolean.TRUE.equals(this.useStoredPaymentMethod);
    }

    public boolean isDigitalWalletPayment() {
        return PaymentTransaction.PaymentMethod.DIGITAL_WALLET.equals(this.paymentMethod) ||
               PaymentTransaction.PaymentProvider.APPLE_PAY.equals(this.paymentProvider) ||
               PaymentTransaction.PaymentProvider.GOOGLE_PAY.equals(this.paymentProvider) ||
               PaymentTransaction.PaymentProvider.SAMSUNG_PAY.equals(this.paymentProvider);
    }

    public boolean isMobilePayment() {
        return PaymentTransaction.PaymentMethod.MOBILE_PAYMENT.equals(this.paymentMethod);
    }

    public boolean isTurkishProvider() {
        return PaymentTransaction.PaymentProvider.IYZICO.equals(this.paymentProvider) ||
               PaymentTransaction.PaymentProvider.PAYTR.equals(this.paymentProvider);
    }

    public boolean requiresBillingAddress() {
        return PaymentTransaction.PaymentMethod.CARD.equals(this.paymentMethod) &&
               !isDigitalWalletPayment();
    }

    public boolean isHighValueTransaction() {
        return this.amount != null && 
               this.amount.compareTo(new BigDecimal("1000.00")) > 0;
    }

    public boolean requiresEnhancedValidation() {
        return isHighValueTransaction() || 
               isRecurringPayment() ||
               Boolean.TRUE.equals(this.requiresAuthentication);
    }
}