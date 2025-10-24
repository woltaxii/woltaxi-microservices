package com.woltaxi.payment.service.impl;

import com.woltaxi.payment.dto.request.PaymentRequest;
import com.woltaxi.payment.dto.response.PaymentResponse;
import com.woltaxi.payment.entity.PaymentTransaction;
import com.woltaxi.payment.event.PaymentEventPublisher;
import com.woltaxi.payment.repository.PaymentTransactionRepository;
import com.woltaxi.payment.service.PaymentService;
import com.woltaxi.payment.service.provider.PaymentProviderService;
import com.woltaxi.payment.service.provider.PaymentProviderFactory;
import com.woltaxi.payment.service.fraud.FraudDetectionService;
import com.woltaxi.payment.service.currency.CurrencyService;
import com.woltaxi.payment.service.wallet.WalletService;
import com.woltaxi.payment.service.validation.PaymentValidationService;
import com.woltaxi.payment.exception.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Service Implementation
 * 
 * Core implementation of payment processing with comprehensive support
 * for multiple providers, fraud detection, currency conversion, and
 * integration with subscription services.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final PaymentProviderFactory providerFactory;
    private final PaymentValidationService validationService;
    private final FraudDetectionService fraudDetectionService;
    private final CurrencyService currencyService;
    private final WalletService walletService;
    private final PaymentEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for user: {} with provider: {}", 
                request.getUserId(), request.getPaymentProvider());

        try {
            // 1. Validate payment request
            ValidationResult validation = validatePaymentRequest(request);
            if (!validation.isValid()) {
                log.warn("Payment validation failed for user: {}, errors: {}", 
                        request.getUserId(), validation.errors());
                throw new PaymentValidationException("Payment validation failed", validation.errors());
            }

            // 2. Fraud detection
            var fraudResult = fraudDetectionService.assessRisk(request);
            if (fraudResult.isBlocked()) {
                log.warn("Payment blocked due to fraud risk for user: {}, score: {}", 
                        request.getUserId(), fraudResult.getRiskScore());
                throw new FraudDetectionException("Payment blocked due to high fraud risk");
            }

            // 3. Currency conversion if needed
            PaymentRequest processedRequest = handleCurrencyConversion(request);

            // 4. Create payment transaction
            PaymentTransaction transaction = createPaymentTransaction(processedRequest, fraudResult);

            // 5. Process payment with provider
            PaymentProviderService providerService = providerFactory.getProvider(request.getPaymentProvider());
            PaymentResponse response = providerService.processPayment(processedRequest, transaction);

            // 6. Update transaction with provider response
            updateTransactionWithResponse(transaction, response);

            // 7. Handle wallet operations if successful
            if (response.isSuccessful()) {
                handleSuccessfulPayment(transaction, response);
            } else if (response.isFailed()) {
                handleFailedPayment(transaction, response);
            }

            // 8. Publish payment event
            publishPaymentEvent(transaction, response);

            log.info("Payment processing completed for transaction: {} with status: {}", 
                    transaction.getId(), response.getStatus());

            return response;

        } catch (Exception e) {
            log.error("Payment processing failed for user: {}", request.getUserId(), e);
            return handlePaymentError(request, e);
        }
    }

    @Override
    @Transactional
    public PaymentResponse processSubscriptionPayment(PaymentRequest request) {
        log.info("Processing subscription payment for user: {} subscription: {}", 
                request.getUserId(), request.getSubscriptionId());

        // Set transaction type to subscription
        request.setTransactionType(PaymentTransaction.TransactionType.SUBSCRIPTION);
        
        // Process payment with additional subscription handling
        PaymentResponse response = processPayment(request);
        
        // If successful, handle subscription-specific operations
        if (response.isSuccessful()) {
            handleSubscriptionPaymentSuccess(request, response);
        }
        
        return response;
    }

    @Override
    @Transactional
    public PaymentResponse processRefund(UUID transactionId, BigDecimal amount, String reason) {
        log.info("Processing refund for transaction: {} amount: {}", transactionId, amount);

        Optional<PaymentTransaction> originalTransaction = getPaymentById(transactionId);
        if (originalTransaction.isEmpty()) {
            throw new PaymentNotFoundException("Original transaction not found: " + transactionId);
        }

        PaymentTransaction original = originalTransaction.get();
        
        // Validate refund eligibility
        if (!original.isRefundable()) {
            throw new RefundNotAllowedException("Transaction is not refundable: " + transactionId);
        }

        // Validate refund amount
        BigDecimal refundableAmount = calculateRefundableAmount(original);
        if (amount.compareTo(refundableAmount) > 0) {
            throw new InvalidRefundAmountException("Refund amount exceeds refundable amount");
        }

        try {
            // Get payment provider service
            PaymentProviderService providerService = providerFactory.getProvider(original.getPaymentProvider());
            
            // Process refund with provider
            PaymentResponse refundResponse = providerService.processRefund(original, amount, reason);
            
            // Create refund transaction
            PaymentTransaction refundTransaction = createRefundTransaction(original, amount, reason, refundResponse);
            
            // Update original transaction
            updateOriginalTransactionForRefund(original, amount);
            
            // Handle wallet operations
            if (refundResponse.isSuccessful()) {
                handleSuccessfulRefund(original, refundTransaction);
            }
            
            // Publish refund event
            publishRefundEvent(original, refundTransaction, refundResponse);
            
            log.info("Refund processing completed for transaction: {} refund amount: {}", 
                    transactionId, amount);
            
            return refundResponse;
            
        } catch (Exception e) {
            log.error("Refund processing failed for transaction: {}", transactionId, e);
            throw new PaymentProcessingException("Refund processing failed", e);
        }
    }

    @Override
    @Transactional
    public PaymentResponse capturePayment(UUID transactionId, BigDecimal amount) {
        log.info("Capturing payment for transaction: {} amount: {}", transactionId, amount);

        Optional<PaymentTransaction> transaction = getPaymentById(transactionId);
        if (transaction.isEmpty()) {
            throw new PaymentNotFoundException("Transaction not found: " + transactionId);
        }

        PaymentTransaction paymentTransaction = transaction.get();
        
        // Validate capture eligibility
        if (!PaymentTransaction.PaymentStatus.PENDING.equals(paymentTransaction.getStatus())) {
            throw new InvalidPaymentStateException("Transaction is not in capturable state");
        }

        try {
            PaymentProviderService providerService = providerFactory.getProvider(paymentTransaction.getPaymentProvider());
            PaymentResponse response = providerService.capturePayment(paymentTransaction, amount);
            
            updateTransactionWithResponse(paymentTransaction, response);
            
            if (response.isSuccessful()) {
                handleSuccessfulPayment(paymentTransaction, response);
            }
            
            publishPaymentEvent(paymentTransaction, response);
            
            return response;
            
        } catch (Exception e) {
            log.error("Payment capture failed for transaction: {}", transactionId, e);
            throw new PaymentProcessingException("Payment capture failed", e);
        }
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(UUID transactionId) {
        log.info("Cancelling payment for transaction: {}", transactionId);

        Optional<PaymentTransaction> transaction = getPaymentById(transactionId);
        if (transaction.isEmpty()) {
            throw new PaymentNotFoundException("Transaction not found: " + transactionId);
        }

        PaymentTransaction paymentTransaction = transaction.get();
        
        try {
            PaymentProviderService providerService = providerFactory.getProvider(paymentTransaction.getPaymentProvider());
            PaymentResponse response = providerService.cancelPayment(paymentTransaction);
            
            updateTransactionWithResponse(paymentTransaction, response);
            publishPaymentEvent(paymentTransaction, response);
            
            return response;
            
        } catch (Exception e) {
            log.error("Payment cancellation failed for transaction: {}", transactionId, e);
            throw new PaymentProcessingException("Payment cancellation failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransaction> getPaymentById(UUID transactionId) {
        return paymentRepository.findById(transactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransaction> getPaymentByExternalId(String externalTransactionId) {
        return paymentRepository.findByExternalTransactionId(externalTransactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getPaymentsByUser(UUID userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return paymentRepository.findByUserId(userId, pageRequest).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getPaymentsBySubscription(UUID subscriptionId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return paymentRepository.findBySubscriptionId(subscriptionId, pageRequest).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getPaymentsByStatus(PaymentTransaction.PaymentStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return paymentRepository.findByStatus(status, pageRequest).getContent();
    }

    @Override
    @Transactional
    public PaymentResponse retryPayment(UUID transactionId) {
        log.info("Retrying payment for transaction: {}", transactionId);

        Optional<PaymentTransaction> transaction = getPaymentById(transactionId);
        if (transaction.isEmpty()) {
            throw new PaymentNotFoundException("Transaction not found: " + transactionId);
        }

        PaymentTransaction paymentTransaction = transaction.get();
        
        // Validate retry eligibility
        if (!PaymentTransaction.PaymentStatus.FAILED.equals(paymentTransaction.getStatus())) {
            throw new InvalidPaymentStateException("Transaction is not in retryable state");
        }

        // Check retry limits
        if (paymentTransaction.getAttemptCount() >= 3) {
            throw new MaxRetryAttemptsExceededException("Maximum retry attempts exceeded");
        }

        try {
            // Increment attempt count
            paymentTransaction.setAttemptCount(paymentTransaction.getAttemptCount() + 1);
            paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.PENDING);
            paymentRepository.save(paymentTransaction);
            
            // Retry with provider
            PaymentProviderService providerService = providerFactory.getProvider(paymentTransaction.getPaymentProvider());
            PaymentResponse response = providerService.retryPayment(paymentTransaction);
            
            updateTransactionWithResponse(paymentTransaction, response);
            
            if (response.isSuccessful()) {
                handleSuccessfulPayment(paymentTransaction, response);
            } else if (response.isFailed()) {
                handleFailedPayment(paymentTransaction, response);
            }
            
            publishPaymentEvent(paymentTransaction, response);
            
            return response;
            
        } catch (Exception e) {
            log.error("Payment retry failed for transaction: {}", transactionId, e);
            throw new PaymentProcessingException("Payment retry failed", e);
        }
    }

    @Override
    public ValidationResult validatePaymentRequest(PaymentRequest request) {
        return validationService.validatePaymentRequest(request);
    }

    @Override
    public boolean isPaymentMethodSupported(PaymentTransaction.PaymentProvider provider, 
                                          PaymentTransaction.PaymentMethod method, 
                                          String currency) {
        try {
            PaymentProviderService providerService = providerFactory.getProvider(provider);
            return providerService.isPaymentMethodSupported(method, currency);
        } catch (Exception e) {
            log.warn("Error checking payment method support: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<PaymentTransaction.PaymentMethod> getSupportedPaymentMethods(PaymentTransaction.PaymentProvider provider) {
        try {
            PaymentProviderService providerService = providerFactory.getProvider(provider);
            return providerService.getSupportedPaymentMethods();
        } catch (Exception e) {
            log.warn("Error getting supported payment methods: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<String> getSupportedCurrencies(PaymentTransaction.PaymentProvider provider) {
        try {
            PaymentProviderService providerService = providerFactory.getProvider(provider);
            return providerService.getSupportedCurrencies();
        } catch (Exception e) {
            log.warn("Error getting supported currencies: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public BigDecimal calculateProcessingFee(BigDecimal amount, String currency, 
                                           PaymentTransaction.PaymentProvider provider,
                                           PaymentTransaction.PaymentMethod method) {
        try {
            PaymentProviderService providerService = providerFactory.getProvider(provider);
            return providerService.calculateProcessingFee(amount, currency, method);
        } catch (Exception e) {
            log.warn("Error calculating processing fee: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatistics getPaymentStatistics(UUID userId) {
        return paymentRepository.getPaymentStatistics(userId);
    }

    @Override
    @Transactional
    public PaymentTransaction updatePaymentStatus(UUID transactionId, 
                                                PaymentTransaction.PaymentStatus status, 
                                                String reason) {
        PaymentTransaction transaction = getPaymentById(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Transaction not found: " + transactionId));
        
        PaymentTransaction.PaymentStatus oldStatus = transaction.getStatus();
        transaction.setStatus(status);
        
        // Set timestamps based on status
        switch (status) {
            case PROCESSING -> transaction.setProcessedAt(LocalDateTime.now());
            case SUCCEEDED -> {
                transaction.setProcessedAt(LocalDateTime.now());
                handleSuccessfulPayment(transaction, null);
            }
            case FAILED -> {
                transaction.setFailedAt(LocalDateTime.now());
                handleFailedPayment(transaction, null);
            }
        }
        
        PaymentTransaction savedTransaction = paymentRepository.save(transaction);
        
        // Publish status change event
        eventPublisher.publishPaymentStatusChanged(savedTransaction, oldStatus, reason);
        
        log.info("Payment status updated for transaction: {} from {} to {}", 
                transactionId, oldStatus, status);
        
        return savedTransaction;
    }

    @Override
    @Transactional
    public WebhookResult handleWebhook(PaymentTransaction.PaymentProvider provider, 
                                     String payload, 
                                     String signature) {
        log.info("Handling webhook from provider: {}", provider);
        
        try {
            PaymentProviderService providerService = providerFactory.getProvider(provider);
            return providerService.handleWebhook(payload, signature);
        } catch (Exception e) {
            log.error("Webhook handling failed for provider: {}", provider, e);
            return new WebhookResult(false, null, null, "Webhook processing failed", List.of(e.getMessage()));
        }
    }

    // Private helper methods
    private PaymentRequest handleCurrencyConversion(PaymentRequest request) {
        // Implementation for currency conversion logic
        return request;
    }

    private PaymentTransaction createPaymentTransaction(PaymentRequest request, 
                                                      FraudDetectionService.FraudResult fraudResult) {
        // Implementation for creating payment transaction
        return PaymentTransaction.builder()
                .externalTransactionId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .subscriptionId(request.getSubscriptionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentTransaction.PaymentStatus.PENDING)
                .paymentProvider(request.getPaymentProvider())
                .paymentMethod(request.getPaymentMethod())
                .transactionType(request.getTransactionType())
                .description(request.getDescription())
                .fraudScore(fraudResult.getRiskScore())
                .riskLevel(fraudResult.getRiskLevel())
                .isRecurring(request.isRecurringPayment())
                .build();
    }

    private void updateTransactionWithResponse(PaymentTransaction transaction, PaymentResponse response) {
        transaction.setStatus(response.getStatus());
        transaction.setProviderTransactionId(response.getProviderTransactionId());
        transaction.setProviderResponseCode(response.getProviderResponseCode());
        transaction.setProviderResponseMessage(response.getProviderResponseMessage());
        
        if (response.isSuccessful()) {
            transaction.setProcessedAt(LocalDateTime.now());
        } else if (response.isFailed()) {
            transaction.setFailedAt(LocalDateTime.now());
        }
        
        paymentRepository.save(transaction);
    }

    private void handleSuccessfulPayment(PaymentTransaction transaction, PaymentResponse response) {
        // Handle wallet operations, notifications, etc.
        log.info("Handling successful payment for transaction: {}", transaction.getId());
    }

    private void handleFailedPayment(PaymentTransaction transaction, PaymentResponse response) {
        // Handle failed payment operations, notifications, etc.
        log.info("Handling failed payment for transaction: {}", transaction.getId());
    }

    private void handleSubscriptionPaymentSuccess(PaymentRequest request, PaymentResponse response) {
        // Handle subscription-specific success operations
        log.info("Handling subscription payment success for subscription: {}", request.getSubscriptionId());
    }

    private PaymentResponse handlePaymentError(PaymentRequest request, Exception e) {
        log.error("Handling payment error for user: {}", request.getUserId(), e);
        return PaymentResponse.failed(UUID.randomUUID(), e.getMessage(), "PROCESSING_ERROR");
    }

    private void publishPaymentEvent(PaymentTransaction transaction, PaymentResponse response) {
        eventPublisher.publishPaymentProcessed(transaction, response);
    }

    private PaymentTransaction createRefundTransaction(PaymentTransaction original, 
                                                     BigDecimal amount, 
                                                     String reason, 
                                                     PaymentResponse refundResponse) {
        // Implementation for creating refund transaction
        return PaymentTransaction.builder()
                .externalTransactionId(UUID.randomUUID().toString())
                .userId(original.getUserId())
                .subscriptionId(original.getSubscriptionId())
                .amount(amount)
                .currency(original.getCurrency())
                .status(refundResponse.getStatus())
                .paymentProvider(original.getPaymentProvider())
                .paymentMethod(original.getPaymentMethod())
                .transactionType(PaymentTransaction.TransactionType.REFUND)
                .description("Refund: " + reason)
                .parentTransactionId(original.getId())
                .refundReason(reason)
                .build();
    }

    private BigDecimal calculateRefundableAmount(PaymentTransaction transaction) {
        BigDecimal refundedAmount = transaction.getRefundAmount() != null ? 
                transaction.getRefundAmount() : BigDecimal.ZERO;
        return transaction.getAmount().subtract(refundedAmount);
    }

    private void updateOriginalTransactionForRefund(PaymentTransaction original, BigDecimal refundAmount) {
        BigDecimal currentRefundAmount = original.getRefundAmount() != null ? 
                original.getRefundAmount() : BigDecimal.ZERO;
        original.setRefundAmount(currentRefundAmount.add(refundAmount));
        original.setRefundedAt(LocalDateTime.now());
        
        // Update status based on refund amount
        if (original.getRefundAmount().compareTo(original.getAmount()) >= 0) {
            original.setStatus(PaymentTransaction.PaymentStatus.REFUNDED);
        } else {
            original.setStatus(PaymentTransaction.PaymentStatus.PARTIALLY_REFUNDED);
        }
        
        paymentRepository.save(original);
    }

    private void handleSuccessfulRefund(PaymentTransaction original, PaymentTransaction refundTransaction) {
        // Handle successful refund operations
        log.info("Handling successful refund for original transaction: {}", original.getId());
    }

    private void publishRefundEvent(PaymentTransaction original, 
                                  PaymentTransaction refundTransaction, 
                                  PaymentResponse refundResponse) {
        eventPublisher.publishRefundProcessed(original, refundTransaction, refundResponse);
    }
}