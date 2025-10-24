package com.woltaxi.travel.service.commission;

import com.woltaxi.travel.entity.TravelBooking;
import com.woltaxi.travel.entity.TravelPartner;
import com.woltaxi.travel.entity.CommissionRecord;
import com.woltaxi.travel.repository.TravelBookingRepository;
import com.woltaxi.travel.repository.CommissionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Commission Calculation Service
 * 
 * Handles commission calculation for travel partners
 * including monthly billing cycles and payment tracking.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommissionCalculationService {

    private final TravelBookingRepository bookingRepository;
    private final CommissionRecordRepository commissionRecordRepository;

    /**
     * Calculate monthly commission for a partner
     */
    public CommissionRecord calculatePartnerCommission(TravelPartner partner, YearMonth billingMonth) {
        log.info("Calculating commission for partner: {} period: {}", partner.getPartnerName(), billingMonth);

        LocalDateTime startDate = billingMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = billingMonth.atEndOfMonth().atTime(23, 59, 59);

        // Get confirmed bookings for the period
        List<TravelBooking> bookings = bookingRepository.findByTravelProviderAndBookingStatusAndCreatedAtBetween(
                partner.getProvider(), TravelBooking.BookingStatus.CONFIRMED, startDate, endDate);

        if (bookings.isEmpty()) {
            log.info("No bookings found for partner: {} in period: {}", partner.getPartnerName(), billingMonth);
            return null;
        }

        // Calculate commission amounts by booking type
        BigDecimal totalBookingAmount = BigDecimal.ZERO;
        BigDecimal totalCommissionAmount = BigDecimal.ZERO;
        int totalBookings = 0;

        for (TravelBooking booking : bookings) {
            BigDecimal bookingAmount = booking.getTotalPrice();
            BigDecimal commissionRate = getCommissionRate(partner, booking);
            BigDecimal commissionAmount = bookingAmount.multiply(commissionRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            totalBookingAmount = totalBookingAmount.add(bookingAmount);
            totalCommissionAmount = totalCommissionAmount.add(commissionAmount);
            totalBookings++;
        }

        // Apply volume discounts if applicable
        totalCommissionAmount = applyVolumeDiscounts(partner, totalCommissionAmount, totalBookings);

        // Calculate taxes
        BigDecimal taxRate = partner.getTaxRate() != null ? partner.getTaxRate() : BigDecimal.valueOf(18); // Default 18% VAT
        BigDecimal taxAmount = totalCommissionAmount.multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netCommission = totalCommissionAmount.subtract(taxAmount);

        // Create commission record
        CommissionRecord commissionRecord = CommissionRecord.builder()
                .partnerId(partner.getId())
                .billingPeriod(billingMonth.toString())
                .bookingCount(totalBookings)
                .totalBookingAmount(totalBookingAmount)
                .baseCommissionAmount(totalCommissionAmount)
                .taxAmount(taxAmount)
                .totalCommission(netCommission)
                .currency("TRY")
                .status(CommissionRecord.CommissionStatus.CALCULATED)
                .dueDate(billingMonth.atEndOfMonth().plusDays(15).atStartOfDay()) // 15 days payment term
                .createdAt(LocalDateTime.now())
                .build();

        commissionRecord = commissionRecordRepository.save(commissionRecord);

        log.info("Commission calculated for partner: {} amount: {} TRY bookings: {}", 
                partner.getPartnerName(), netCommission, totalBookings);

        return commissionRecord;
    }

    /**
     * Apply volume-based discounts to commission
     */
    public BigDecimal applyVolumeDiscounts(TravelPartner partner, BigDecimal baseCommission, int bookingCount) {
        // Example volume discount structure
        BigDecimal discountRate = BigDecimal.ZERO;

        if (bookingCount >= 1000) {
            discountRate = BigDecimal.valueOf(15); // 15% discount
        } else if (bookingCount >= 500) {
            discountRate = BigDecimal.valueOf(10); // 10% discount
        } else if (bookingCount >= 100) {
            discountRate = BigDecimal.valueOf(5); // 5% discount
        }

        if (discountRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = baseCommission.multiply(discountRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            log.info("Applied {}% volume discount: {} TRY for partner: {}", 
                    discountRate, discount, partner.getPartnerName());
            
            return baseCommission.subtract(discount);
        }

        return baseCommission;
    }

    /**
     * Calculate commission adjustments for cancellations/modifications
     */
    public BigDecimal calculateCommissionAdjustment(TravelBooking booking, BigDecimal refundAmount) {
        TravelPartner partner = getPartnerForBooking(booking);
        if (partner == null) return BigDecimal.ZERO;

        BigDecimal commissionRate = getCommissionRate(partner, booking);
        return refundAmount.multiply(commissionRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Process commission payment
     */
    public void processCommissionPayment(CommissionRecord commissionRecord, 
            BigDecimal paymentAmount, String paymentMethod, String paymentReference) {
        
        commissionRecord.setStatus(CommissionRecord.CommissionStatus.PAID);
        commissionRecord.setPaidAt(LocalDateTime.now());
        commissionRecord.setPaymentAmount(paymentAmount);
        commissionRecord.setPaymentMethod(paymentMethod);
        commissionRecord.setPaymentReference(paymentReference);
        commissionRecord.setUpdatedAt(LocalDateTime.now());

        commissionRecordRepository.save(commissionRecord);

        log.info("Commission payment processed: {} amount: {} TRY method: {}", 
                commissionRecord.getId(), paymentAmount, paymentMethod);
    }

    /**
     * Generate commission summary for period
     */
    public CommissionSummary generateCommissionSummary(YearMonth period) {
        List<CommissionRecord> records = commissionRecordRepository.findByBillingPeriod(period.toString());

        BigDecimal totalCommissions = records.stream()
                .map(CommissionRecord::getTotalCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidCommissions = records.stream()
                .filter(r -> r.getStatus() == CommissionRecord.CommissionStatus.PAID)
                .map(CommissionRecord::getTotalCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingCommissions = totalCommissions.subtract(paidCommissions);

        return CommissionSummary.builder()
                .period(period.toString())
                .totalPartners(records.size())
                .totalCommissions(totalCommissions)
                .paidCommissions(paidCommissions)
                .pendingCommissions(pendingCommissions)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // Private helper methods

    private BigDecimal getCommissionRate(TravelPartner partner, TravelBooking booking) {
        return switch (booking.getBookingType()) {
            case FLIGHT -> partner.getFlightCommissionRate();
            case BUS -> partner.getBusCommissionRate();
            case HOTEL -> partner.getHotelCommissionRate();
            case CAR_RENTAL -> partner.getCarRentalCommissionRate();
        };
    }

    private TravelPartner getPartnerForBooking(TravelBooking booking) {
        // This would typically be retrieved from the partner repository
        // For now, return null as a placeholder
        return null;
    }

    // Data classes
    @lombok.Builder
    @lombok.Data
    public static class CommissionSummary {
        private String period;
        private int totalPartners;
        private BigDecimal totalCommissions;
        private BigDecimal paidCommissions;
        private BigDecimal pendingCommissions;
        private LocalDateTime generatedAt;
    }
}