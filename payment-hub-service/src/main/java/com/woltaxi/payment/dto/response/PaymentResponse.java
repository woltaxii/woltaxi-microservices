package com.woltaxi.payment.dto.response;

import com.woltaxi.payment.entity.PaymentTransaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Processing Response DTO
 * 
 * Response object for payment processing operations with comprehensive
 * status information and security-compliant data exposure.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"providerMetadata", "securityInfo"})
public class PaymentResponse {

    private UUID transactionId;

    private String externalTransactionId;

    private UUID userId;

    private UUID subscriptionId;

    private BigDecimal amount;

    private String currency;

    private BigDecimal originalAmount;

    private String originalCurrency;

    private BigDecimal exchangeRate;

    private PaymentTransaction.PaymentStatus status;

    private PaymentTransaction.PaymentProvider paymentProvider;

    private PaymentTransaction.PaymentMethod paymentMethod;

    private PaymentTransaction.TransactionType transactionType;

    private String description;

    private String providerTransactionId;

    private String providerResponseCode;

    private String providerResponseMessage;

    private CardInfo cardInfo;

    private BillingInfo billingInfo;

    private SecurityInfo securityInfo;

    private ProcessingInfo processingInfo;

    private String nextAction;

    private String redirectUrl;

    private Map<String, Object> clientSecret;

    private Map<String, Object> providerMetadata;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private Long version;

    /**
     * Masked card information for security compliance
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CardInfo {

        private String lastFour;

        private String brand;

        private Integer expMonth;

        private Integer expYear;

        private String cardholderName;

        private String cardType; // DEBIT, CREDIT, PREPAID

        private String issuingBank;

        private String issuingCountry;

        private Boolean is3DSecureUsed;
    }

    /**
     * Billing information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillingInfo {

        private String firstName;

        private String lastName;

        private String company;

        private String addressLine1;

        private String addressLine2;

        private String city;

        private String state;

        private String postalCode;

        private String country;

        private String phoneNumber;

        private String email;
    }

    /**
     * Security and fraud information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SecurityInfo {

        private BigDecimal fraudScore;

        private String riskLevel;

        private Boolean avsCheck;

        private String avsResult;

        private Boolean cvvCheck;

        private String cvvResult;

        private Boolean is3DSecureAuthenticated;

        private String authenticationStatus;

        private String authenticationId;

        private Boolean isTestTransaction;
    }

    /**
     * Processing information and timing
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProcessingInfo {

        private Integer attemptCount;

        private LocalDateTime nextRetryAfter;

        private String processingMethod;

        private Long processingTimeMs;

        private String gatewayUsed;

        private String acquirerResponseCode;

        private String networkTransactionId;

        private Boolean isRecurring;

        private String subscriptionPeriod;
    }

    // Helper methods for status checking
    public boolean isSuccessful() {
        return PaymentTransaction.PaymentStatus.SUCCEEDED.equals(this.status);
    }

    public boolean isPending() {
        return PaymentTransaction.PaymentStatus.PENDING.equals(this.status) ||
               PaymentTransaction.PaymentStatus.PROCESSING.equals(this.status);
    }

    public boolean isFailed() {
        return PaymentTransaction.PaymentStatus.FAILED.equals(this.status) ||
               PaymentTransaction.PaymentStatus.CANCELLED.equals(this.status) ||
               PaymentTransaction.PaymentStatus.EXPIRED.equals(this.status);
    }

    public boolean requiresAction() {
        return this.nextAction != null && !this.nextAction.isEmpty();
    }

    public boolean requiresRedirect() {
        return this.redirectUrl != null && !this.redirectUrl.isEmpty();
    }

    public boolean hasHighRisk() {
        return this.securityInfo != null && 
               this.securityInfo.getFraudScore() != null &&
               this.securityInfo.getFraudScore().compareTo(BigDecimal.valueOf(75)) >= 0;
    }

    public boolean isRefundable() {
        return isSuccessful() && 
               PaymentTransaction.TransactionType.PAYMENT.equals(this.transactionType);
    }

    public String getStatusMessage() {
        if (isSuccessful()) {
            return "Payment completed successfully";
        } else if (isPending()) {
            return "Payment is being processed";
        } else if (isFailed()) {
            return "Payment failed: " + (providerResponseMessage != null ? 
                   providerResponseMessage : "Unknown error");
        }
        return "Payment status unknown";
    }

    public String getDisplayAmount() {
        if (this.amount == null || this.currency == null) {
            return "N/A";
        }
        return String.format("%.2f %s", this.amount, this.currency);
    }

    public boolean wasConverted() {
        return this.originalCurrency != null && 
               !this.originalCurrency.equals(this.currency);
    }

    public String getConversionInfo() {
        if (!wasConverted()) {
            return null;
        }
        return String.format("Converted from %.2f %s to %.2f %s (Rate: %.6f)",
                this.originalAmount, this.originalCurrency,
                this.amount, this.currency,
                this.exchangeRate);
    }

    // Factory methods for common responses
    public static PaymentResponse success(PaymentTransaction transaction) {
        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .externalTransactionId(transaction.getExternalTransactionId())
                .userId(transaction.getUserId())
                .subscriptionId(transaction.getSubscriptionId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .originalAmount(transaction.getOriginalAmount())
                .originalCurrency(transaction.getOriginalCurrency())
                .exchangeRate(transaction.getExchangeRate())
                .status(transaction.getStatus())
                .paymentProvider(transaction.getPaymentProvider())
                .paymentMethod(transaction.getPaymentMethod())
                .transactionType(transaction.getTransactionType())
                .description(transaction.getDescription())
                .providerTransactionId(transaction.getProviderTransactionId())
                .providerResponseCode(transaction.getProviderResponseCode())
                .providerResponseMessage(transaction.getProviderResponseMessage())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .version(transaction.getVersion())
                .build();
    }

    public static PaymentResponse pending(UUID transactionId, String message) {
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status(PaymentTransaction.PaymentStatus.PENDING)
                .providerResponseMessage(message)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PaymentResponse failed(UUID transactionId, String errorMessage, String errorCode) {
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status(PaymentTransaction.PaymentStatus.FAILED)
                .providerResponseMessage(errorMessage)
                .providerResponseCode(errorCode)
                .createdAt(LocalDateTime.now())
                .build();
    }
}