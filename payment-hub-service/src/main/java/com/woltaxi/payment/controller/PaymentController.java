package com.woltaxi.payment.controller;

import com.woltaxi.payment.dto.request.PaymentRequest;
import com.woltaxi.payment.dto.response.PaymentResponse;
import com.woltaxi.payment.entity.PaymentTransaction;
import com.woltaxi.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
 * Payment Hub Controller
 * 
 * REST API endpoints for payment processing operations across all
 * supported payment providers and methods.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Payment Processing", description = "Advanced Multi-Platform Payment Processing API")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
        summary = "Process Payment",
        description = "Process a payment using the specified payment provider and method"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment processed successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payment request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "422", description = "Payment validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        
        log.info("Processing payment request for user: {} with provider: {}", 
                request.getUserId(), request.getPaymentProvider());
        
        PaymentResponse response = paymentService.processPayment(request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Process Subscription Payment",
        description = "Process a payment for subscription billing"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription payment processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid subscription payment request")
    })
    @PostMapping("/subscription")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<PaymentResponse> processSubscriptionPayment(
            @Valid @RequestBody PaymentRequest request) {
        
        log.info("Processing subscription payment for user: {} subscription: {}", 
                request.getUserId(), request.getSubscriptionId());
        
        PaymentResponse response = paymentService.processSubscriptionPayment(request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Process Refund",
        description = "Process a refund for a successful payment"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid refund request"),
        @ApiResponse(responseCode = "404", description = "Original transaction not found")
    })
    @PostMapping("/{transactionId}/refund")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<PaymentResponse> processRefund(
            @Parameter(description = "Original transaction ID") 
            @PathVariable UUID transactionId,
            
            @Parameter(description = "Refund amount") 
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal amount,
            
            @Parameter(description = "Refund reason") 
            @RequestParam String reason) {
        
        log.info("Processing refund for transaction: {} amount: {}", transactionId, amount);
        
        PaymentResponse response = paymentService.processRefund(transactionId, amount, reason);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Capture Payment",
        description = "Capture an authorized payment"
    )
    @PostMapping("/{transactionId}/capture")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<PaymentResponse> capturePayment(
            @Parameter(description = "Transaction ID to capture") 
            @PathVariable UUID transactionId,
            
            @Parameter(description = "Amount to capture") 
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal amount) {
        
        log.info("Capturing payment for transaction: {} amount: {}", transactionId, amount);
        
        PaymentResponse response = paymentService.capturePayment(transactionId, amount);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Cancel Payment",
        description = "Cancel an authorized payment"
    )
    @PostMapping("/{transactionId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @Parameter(description = "Transaction ID to cancel") 
            @PathVariable UUID transactionId) {
        
        log.info("Cancelling payment for transaction: {}", transactionId);
        
        PaymentResponse response = paymentService.cancelPayment(transactionId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Retry Failed Payment",
        description = "Retry a failed payment transaction"
    )
    @PostMapping("/{transactionId}/retry")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<PaymentResponse> retryPayment(
            @Parameter(description = "Failed transaction ID") 
            @PathVariable UUID transactionId) {
        
        log.info("Retrying payment for transaction: {}", transactionId);
        
        PaymentResponse response = paymentService.retryPayment(transactionId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get Payment by ID",
        description = "Retrieve payment transaction details by ID"
    )
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentTransaction> getPayment(
            @Parameter(description = "Transaction ID") 
            @PathVariable UUID transactionId) {
        
        return paymentService.getPaymentById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Get Payment by External ID",
        description = "Retrieve payment transaction details by external transaction ID"
    )
    @GetMapping("/external/{externalTransactionId}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentTransaction> getPaymentByExternalId(
            @Parameter(description = "External transaction ID") 
            @PathVariable String externalTransactionId) {
        
        return paymentService.getPaymentByExternalId(externalTransactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Get User Payments",
        description = "Retrieve payment transactions for a specific user"
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentTransaction>> getUserPayments(
            @Parameter(description = "User ID") 
            @PathVariable UUID userId,
            
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        List<PaymentTransaction> transactions = paymentService.getPaymentsByUser(userId, page, size);
        Page<PaymentTransaction> result = new PageImpl<>(transactions, PageRequest.of(page, size), transactions.size());
        
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Get Subscription Payments",
        description = "Retrieve payment transactions for a specific subscription"
    )
    @GetMapping("/subscription/{subscriptionId}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentTransaction>> getSubscriptionPayments(
            @Parameter(description = "Subscription ID") 
            @PathVariable UUID subscriptionId,
            
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        List<PaymentTransaction> transactions = paymentService.getPaymentsBySubscription(subscriptionId, page, size);
        Page<PaymentTransaction> result = new PageImpl<>(transactions, PageRequest.of(page, size), transactions.size());
        
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Get Payments by Status",
        description = "Retrieve payment transactions by status"
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<Page<PaymentTransaction>> getPaymentsByStatus(
            @Parameter(description = "Payment status") 
            @PathVariable PaymentTransaction.PaymentStatus status,
            
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        List<PaymentTransaction> transactions = paymentService.getPaymentsByStatus(status, page, size);
        Page<PaymentTransaction> result = new PageImpl<>(transactions, PageRequest.of(page, size), transactions.size());
        
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Get Payment Statistics",
        description = "Retrieve payment statistics for a user"
    )
    @GetMapping("/user/{userId}/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentService.PaymentStatistics> getPaymentStatistics(
            @Parameter(description = "User ID") 
            @PathVariable UUID userId) {
        
        PaymentService.PaymentStatistics statistics = paymentService.getPaymentStatistics(userId);
        
        return ResponseEntity.ok(statistics);
    }

    @Operation(
        summary = "Check Payment Method Support",
        description = "Check if a payment method is supported by a provider for a currency"
    )
    @GetMapping("/support/check")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<Boolean> checkPaymentMethodSupport(
            @Parameter(description = "Payment provider") 
            @RequestParam PaymentTransaction.PaymentProvider provider,
            
            @Parameter(description = "Payment method") 
            @RequestParam PaymentTransaction.PaymentMethod method,
            
            @Parameter(description = "Currency") 
            @RequestParam String currency) {
        
        boolean supported = paymentService.isPaymentMethodSupported(provider, method, currency);
        
        return ResponseEntity.ok(supported);
    }

    @Operation(
        summary = "Get Supported Payment Methods",
        description = "Get supported payment methods for a provider"
    )
    @GetMapping("/support/methods/{provider}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<List<PaymentTransaction.PaymentMethod>> getSupportedPaymentMethods(
            @Parameter(description = "Payment provider") 
            @PathVariable PaymentTransaction.PaymentProvider provider) {
        
        List<PaymentTransaction.PaymentMethod> methods = paymentService.getSupportedPaymentMethods(provider);
        
        return ResponseEntity.ok(methods);
    }

    @Operation(
        summary = "Get Supported Currencies",
        description = "Get supported currencies for a provider"
    )
    @GetMapping("/support/currencies/{provider}")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<List<String>> getSupportedCurrencies(
            @Parameter(description = "Payment provider") 
            @PathVariable PaymentTransaction.PaymentProvider provider) {
        
        List<String> currencies = paymentService.getSupportedCurrencies(provider);
        
        return ResponseEntity.ok(currencies);
    }

    @Operation(
        summary = "Calculate Processing Fee",
        description = "Calculate processing fee for a payment"
    )
    @GetMapping("/fee/calculate")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<BigDecimal> calculateProcessingFee(
            @Parameter(description = "Payment amount") 
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal amount,
            
            @Parameter(description = "Currency") 
            @RequestParam String currency,
            
            @Parameter(description = "Payment provider") 
            @RequestParam PaymentTransaction.PaymentProvider provider,
            
            @Parameter(description = "Payment method") 
            @RequestParam PaymentTransaction.PaymentMethod method) {
        
        BigDecimal fee = paymentService.calculateProcessingFee(amount, currency, provider, method);
        
        return ResponseEntity.ok(fee);
    }

    @Operation(
        summary = "Update Payment Status",
        description = "Update payment transaction status (Admin only)"
    )
    @PutMapping("/{transactionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentTransaction> updatePaymentStatus(
            @Parameter(description = "Transaction ID") 
            @PathVariable UUID transactionId,
            
            @Parameter(description = "New status") 
            @RequestParam PaymentTransaction.PaymentStatus status,
            
            @Parameter(description = "Reason for status change") 
            @RequestParam String reason) {
        
        log.info("Updating payment status for transaction: {} to: {}", transactionId, status);
        
        PaymentTransaction updatedTransaction = paymentService.updatePaymentStatus(transactionId, status, reason);
        
        return ResponseEntity.ok(updatedTransaction);
    }

    @Operation(
        summary = "Validate Payment Request",
        description = "Validate a payment request without processing"
    )
    @PostMapping("/validate")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER')")
    public ResponseEntity<PaymentService.ValidationResult> validatePaymentRequest(
            @Valid @RequestBody PaymentRequest request) {
        
        PaymentService.ValidationResult result = paymentService.validatePaymentRequest(request);
        
        return ResponseEntity.ok(result);
    }

    // Exception handlers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Payment controller error", e);
        
        ErrorResponse error = ErrorResponse.builder()
                .error("PAYMENT_ERROR")
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