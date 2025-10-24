package com.woltaxi.payment.service;

import com.woltaxi.payment.dto.request.PaymentRequest;
import com.woltaxi.payment.dto.response.PaymentResponse;
import com.woltaxi.payment.entity.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Processing Service Interface
 * 
 * Defines the contract for payment processing operations across all
 * supported payment providers and methods.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
public interface PaymentService {

    /**
     * Process a payment request
     * 
     * @param request Payment request with all necessary details
     * @return Payment response with transaction status and details
     */
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * Process a subscription payment
     * 
     * @param request Payment request for subscription
     * @return Payment response with subscription details
     */
    PaymentResponse processSubscriptionPayment(PaymentRequest request);

    /**
     * Process a refund for a successful payment
     * 
     * @param transactionId Original transaction ID
     * @param amount Refund amount (partial or full)
     * @param reason Refund reason
     * @return Payment response for refund transaction
     */
    PaymentResponse processRefund(UUID transactionId, BigDecimal amount, String reason);

    /**
     * Capture an authorized payment
     * 
     * @param transactionId Transaction ID to capture
     * @param amount Amount to capture (can be less than authorized amount)
     * @return Payment response with capture details
     */
    PaymentResponse capturePayment(UUID transactionId, BigDecimal amount);

    /**
     * Cancel an authorized payment
     * 
     * @param transactionId Transaction ID to cancel
     * @return Payment response with cancellation details
     */
    PaymentResponse cancelPayment(UUID transactionId);

    /**
     * Get payment transaction by ID
     * 
     * @param transactionId Transaction ID
     * @return Optional payment transaction
     */
    Optional<PaymentTransaction> getPaymentById(UUID transactionId);

    /**
     * Get payment transaction by external ID
     * 
     * @param externalTransactionId External transaction ID
     * @return Optional payment transaction
     */
    Optional<PaymentTransaction> getPaymentByExternalId(String externalTransactionId);

    /**
     * Get payment transactions for a user
     * 
     * @param userId User ID
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of payment transactions
     */
    List<PaymentTransaction> getPaymentsByUser(UUID userId, int page, int size);

    /**
     * Get payment transactions for a subscription
     * 
     * @param subscriptionId Subscription ID
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of payment transactions
     */
    List<PaymentTransaction> getPaymentsBySubscription(UUID subscriptionId, int page, int size);

    /**
     * Get payment transactions by status
     * 
     * @param status Payment status
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of payment transactions
     */
    List<PaymentTransaction> getPaymentsByStatus(PaymentTransaction.PaymentStatus status, int page, int size);

    /**
     * Retry a failed payment
     * 
     * @param transactionId Failed transaction ID
     * @return Payment response with retry results
     */
    PaymentResponse retryPayment(UUID transactionId);

    /**
     * Validate payment request
     * 
     * @param request Payment request to validate
     * @return Validation result with any errors
     */
    ValidationResult validatePaymentRequest(PaymentRequest request);

    /**
     * Check if payment method is supported
     * 
     * @param provider Payment provider
     * @param method Payment method
     * @param currency Currency
     * @return True if supported, false otherwise
     */
    boolean isPaymentMethodSupported(PaymentTransaction.PaymentProvider provider, 
                                   PaymentTransaction.PaymentMethod method, 
                                   String currency);

    /**
     * Get supported payment methods for a provider
     * 
     * @param provider Payment provider
     * @return List of supported payment methods
     */
    List<PaymentTransaction.PaymentMethod> getSupportedPaymentMethods(PaymentTransaction.PaymentProvider provider);

    /**
     * Get supported currencies for a provider
     * 
     * @param provider Payment provider
     * @return List of supported currencies
     */
    List<String> getSupportedCurrencies(PaymentTransaction.PaymentProvider provider);

    /**
     * Calculate processing fees for a payment
     * 
     * @param amount Payment amount
     * @param currency Currency
     * @param provider Payment provider
     * @param method Payment method
     * @return Processing fee amount
     */
    BigDecimal calculateProcessingFee(BigDecimal amount, String currency, 
                                    PaymentTransaction.PaymentProvider provider,
                                    PaymentTransaction.PaymentMethod method);

    /**
     * Get payment statistics for a user
     * 
     * @param userId User ID
     * @return Payment statistics
     */
    PaymentStatistics getPaymentStatistics(UUID userId);

    /**
     * Update payment transaction status
     * 
     * @param transactionId Transaction ID
     * @param status New status
     * @param reason Reason for status change
     * @return Updated payment transaction
     */
    PaymentTransaction updatePaymentStatus(UUID transactionId, 
                                         PaymentTransaction.PaymentStatus status, 
                                         String reason);

    /**
     * Handle webhook from payment provider
     * 
     * @param provider Payment provider
     * @param payload Webhook payload
     * @param signature Webhook signature for verification
     * @return Processing result
     */
    WebhookResult handleWebhook(PaymentTransaction.PaymentProvider provider, 
                              String payload, 
                              String signature);

    /**
     * Validation Result
     */
    record ValidationResult(
            boolean isValid,
            List<String> errors,
            List<String> warnings
    ) {}

    /**
     * Payment Statistics
     */
    record PaymentStatistics(
            UUID userId,
            Long totalTransactions,
            Long successfulTransactions,
            Long failedTransactions,
            Long pendingTransactions,
            BigDecimal totalAmount,
            BigDecimal totalFees,
            BigDecimal averageAmount,
            String preferredProvider,
            String preferredMethod,
            String preferredCurrency
    ) {}

    /**
     * Webhook Processing Result
     */
    record WebhookResult(
            boolean processed,
            String transactionId,
            PaymentTransaction.PaymentStatus status,
            String message,
            List<String> errors
    ) {}
}