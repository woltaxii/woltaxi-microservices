package com.woltaxi.travel.service.notification;

import com.woltaxi.travel.entity.TravelBooking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Travel Notification Service
 * 
 * Handles notifications for travel bookings including
 * email confirmations, SMS alerts, and push notifications.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TravelNotificationService {

    private final JavaMailSender mailSender;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Send booking confirmation notification
     */
    public void sendBookingConfirmation(TravelBooking booking, String email) {
        log.info("Sending booking confirmation for: {} to {}", booking.getBookingReference(), email);

        try {
            // Send email notification
            sendConfirmationEmail(booking, email);

            // Send Kafka event for other services
            publishBookingEvent("BOOKING_CONFIRMED", booking);

            // Send SMS notification (via external service)
            sendSMSNotification(booking.getContactPhone(), buildConfirmationSMS(booking));

            // Send push notification
            sendPushNotification(booking.getUserId(), "Booking Confirmed", 
                    "Your " + booking.getBookingType().toString().toLowerCase() + " booking is confirmed! Reference: " + booking.getBookingReference());

        } catch (Exception e) {
            log.error("Failed to send booking confirmation for: {}", booking.getBookingReference(), e);
        }
    }

    /**
     * Send booking cancellation notification
     */
    public void sendBookingCancellation(TravelBooking booking, String email) {
        log.info("Sending booking cancellation for: {} to {}", booking.getBookingReference(), email);

        try {
            sendCancellationEmail(booking, email);
            publishBookingEvent("BOOKING_CANCELLED", booking);
            sendSMSNotification(booking.getContactPhone(), buildCancellationSMS(booking));
            sendPushNotification(booking.getUserId(), "Booking Cancelled", 
                    "Your booking " + booking.getBookingReference() + " has been cancelled.");

        } catch (Exception e) {
            log.error("Failed to send booking cancellation for: {}", booking.getBookingReference(), e);
        }
    }

    /**
     * Send booking modification notification
     */
    public void sendBookingModification(TravelBooking booking, String email) {
        log.info("Sending booking modification for: {} to {}", booking.getBookingReference(), email);

        try {
            sendModificationEmail(booking, email);
            publishBookingEvent("BOOKING_MODIFIED", booking);
            sendSMSNotification(booking.getContactPhone(), buildModificationSMS(booking));
            sendPushNotification(booking.getUserId(), "Booking Modified", 
                    "Your booking " + booking.getBookingReference() + " has been updated.");

        } catch (Exception e) {
            log.error("Failed to send booking modification for: {}", booking.getBookingReference(), e);
        }
    }

    /**
     * Send check-in confirmation notification
     */
    public void sendCheckInConfirmation(TravelBooking booking, String email) {
        log.info("Sending check-in confirmation for: {} to {}", booking.getBookingReference(), email);

        try {
            sendCheckInEmail(booking, email);
            publishBookingEvent("CHECKIN_COMPLETED", booking);
            sendSMSNotification(booking.getContactPhone(), buildCheckInSMS(booking));
            sendPushNotification(booking.getUserId(), "Check-in Complete", 
                    "Check-in completed for " + booking.getBookingReference() + ". Have a great trip!");

        } catch (Exception e) {
            log.error("Failed to send check-in confirmation for: {}", booking.getBookingReference(), e);
        }
    }

    /**
     * Send travel reminder notifications
     */
    public void sendTravelReminder(TravelBooking booking, String email) {
        log.info("Sending travel reminder for: {} to {}", booking.getBookingReference(), email);

        try {
            sendReminderEmail(booking, email);
            sendSMSNotification(booking.getContactPhone(), buildReminderSMS(booking));
            sendPushNotification(booking.getUserId(), "Travel Reminder", 
                    "Your trip is tomorrow! Don't forget your booking: " + booking.getBookingReference());

        } catch (Exception e) {
            log.error("Failed to send travel reminder for: {}", booking.getBookingReference(), e);
        }
    }

    // Private email methods

    private void sendConfirmationEmail(TravelBooking booking, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("WOLTAXI Travel - Booking Confirmed: " + booking.getBookingReference());
        message.setText(buildConfirmationEmailContent(booking));
        message.setFrom("noreply@woltaxi.com");

        mailSender.send(message);
        log.info("Confirmation email sent to: {}", email);
    }

    private void sendCancellationEmail(TravelBooking booking, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("WOLTAXI Travel - Booking Cancelled: " + booking.getBookingReference());
        message.setText(buildCancellationEmailContent(booking));
        message.setFrom("noreply@woltaxi.com");

        mailSender.send(message);
        log.info("Cancellation email sent to: {}", email);
    }

    private void sendModificationEmail(TravelBooking booking, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("WOLTAXI Travel - Booking Modified: " + booking.getBookingReference());
        message.setText(buildModificationEmailContent(booking));
        message.setFrom("noreply@woltaxi.com");

        mailSender.send(message);
        log.info("Modification email sent to: {}", email);
    }

    private void sendCheckInEmail(TravelBooking booking, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("WOLTAXI Travel - Check-in Complete: " + booking.getBookingReference());
        message.setText(buildCheckInEmailContent(booking));
        message.setFrom("noreply@woltaxi.com");

        mailSender.send(message);
        log.info("Check-in email sent to: {}", email);
    }

    private void sendReminderEmail(TravelBooking booking, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("WOLTAXI Travel - Reminder: " + booking.getBookingReference());
        message.setText(buildReminderEmailContent(booking));
        message.setFrom("noreply@woltaxi.com");

        mailSender.send(message);
        log.info("Reminder email sent to: {}", email);
    }

    // Email content builders

    private String buildConfirmationEmailContent(TravelBooking booking) {
        return String.format("""
                Dear Valued Customer,
                
                Your %s booking has been confirmed!
                
                Booking Reference: %s
                Confirmation Number: %s
                
                Travel Details:
                From: %s
                To: %s
                Date: %s
                Provider: %s
                Total Amount: %s %s
                
                Your e-ticket and travel documents are being prepared and will be available shortly.
                
                Thank you for choosing WOLTAXI Travel!
                
                Best regards,
                WOLTAXI Travel Team
                """,
                booking.getBookingType().toString().toLowerCase(),
                booking.getBookingReference(),
                booking.getConfirmationNumber(),
                booking.getOrigin(),
                booking.getDestination(),
                booking.getTravelDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                booking.getTravelProvider().toString(),
                booking.getTotalPrice(),
                booking.getCurrency()
        );
    }

    private String buildCancellationEmailContent(TravelBooking booking) {
        return String.format("""
                Dear Valued Customer,
                
                Your booking has been cancelled as requested.
                
                Booking Reference: %s
                Cancellation Date: %s
                Reason: %s
                
                Refund Information:
                Cancellation Fee: %s %s
                Refund Amount: %s %s
                
                The refund will be processed within 5-7 business days.
                
                Thank you for using WOLTAXI Travel.
                
                Best regards,
                WOLTAXI Travel Team
                """,
                booking.getBookingReference(),
                booking.getCancelledAt() != null ? booking.getCancelledAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "Now",
                booking.getCancellationReason() != null ? booking.getCancellationReason() : "Customer request",
                booking.getCancellationFee() != null ? booking.getCancellationFee() : "0.00",
                booking.getCurrency(),
                booking.getRefundAmount() != null ? booking.getRefundAmount() : booking.getTotalPrice(),
                booking.getCurrency()
        );
    }

    private String buildModificationEmailContent(TravelBooking booking) {
        return String.format("""
                Dear Valued Customer,
                
                Your booking has been successfully modified.
                
                Booking Reference: %s
                
                Updated Travel Details:
                From: %s
                To: %s
                Date: %s
                Provider: %s
                
                Your updated e-ticket will be sent shortly.
                
                Thank you for choosing WOLTAXI Travel!
                
                Best regards,
                WOLTAXI Travel Team
                """,
                booking.getBookingReference(),
                booking.getOrigin(),
                booking.getDestination(),
                booking.getTravelDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                booking.getTravelProvider().toString()
        );
    }

    private String buildCheckInEmailContent(TravelBooking booking) {
        return String.format("""
                Dear Valued Customer,
                
                Your check-in is complete!
                
                Booking Reference: %s
                Flight/Service: %s to %s
                Departure: %s
                
                Your boarding pass is attached. Please arrive at the gate 30 minutes before departure.
                
                Have a wonderful trip!
                
                Best regards,
                WOLTAXI Travel Team
                """,
                booking.getBookingReference(),
                booking.getOrigin(),
                booking.getDestination(),
                booking.getTravelDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
        );
    }

    private String buildReminderEmailContent(TravelBooking booking) {
        return String.format("""
                Dear Valued Customer,
                
                This is a friendly reminder about your upcoming trip!
                
                Booking Reference: %s
                From: %s
                To: %s
                Date: %s
                
                Please ensure you have all necessary documents and arrive early.
                
                Have a great trip!
                
                Best regards,
                WOLTAXI Travel Team
                """,
                booking.getBookingReference(),
                booking.getOrigin(),
                booking.getDestination(),
                booking.getTravelDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
        );
    }

    // SMS content builders

    private String buildConfirmationSMS(TravelBooking booking) {
        return String.format("WOLTAXI: Your %s booking %s is confirmed! %s to %s on %s. Total: %s %s",
                booking.getBookingType().toString().toLowerCase(),
                booking.getBookingReference(),
                booking.getOrigin(),
                booking.getDestination(),
                booking.getTravelDate().format(DateTimeFormatter.ofPattern("dd MMM")),
                booking.getTotalPrice(),
                booking.getCurrency());
    }

    private String buildCancellationSMS(TravelBooking booking) {
        return String.format("WOLTAXI: Booking %s cancelled. Refund: %s %s (Fee: %s %s). Processing time: 5-7 days.",
                booking.getBookingReference(),
                booking.getRefundAmount() != null ? booking.getRefundAmount() : booking.getTotalPrice(),
                booking.getCurrency(),
                booking.getCancellationFee() != null ? booking.getCancellationFee() : "0.00",
                booking.getCurrency());
    }

    private String buildModificationSMS(TravelBooking booking) {
        return String.format("WOLTAXI: Booking %s modified. New date: %s. Updated ticket will be sent shortly.",
                booking.getBookingReference(),
                booking.getTravelDate().format(DateTimeFormatter.ofPattern("dd MMM HH:mm")));
    }

    private String buildCheckInSMS(TravelBooking booking) {
        return String.format("WOLTAXI: Check-in complete for %s. %s to %s at %s. Have a great trip!",
                booking.getBookingReference(),
                booking.getOrigin(),
                booking.getDestination(),
                booking.getTravelDate().format(DateTimeFormatter.ofPattern("dd MMM HH:mm")));
    }

    private String buildReminderSMS(TravelBooking booking) {
        return String.format("WOLTAXI: Trip reminder! %s to %s tomorrow at %s. Ref: %s",
                booking.getOrigin(),
                booking.getDestination(),
                booking.getTravelDate().format(DateTimeFormatter.ofPattern("HH:mm")),
                booking.getBookingReference());
    }

    // Helper methods

    private void publishBookingEvent(String eventType, TravelBooking booking) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", eventType,
                    "bookingId", booking.getId(),
                    "bookingReference", booking.getBookingReference(),
                    "userId", booking.getUserId(),
                    "timestamp", java.time.Instant.now()
            );

            kafkaTemplate.send("travel-booking-events", event);
            log.info("Published event: {} for booking: {}", eventType, booking.getBookingReference());

        } catch (Exception e) {
            log.error("Failed to publish event: {} for booking: {}", eventType, booking.getBookingReference(), e);
        }
    }

    private void sendSMSNotification(String phoneNumber, String message) {
        try {
            // Mock SMS sending - integrate with Twilio, AWS SNS, or local SMS provider
            log.info("Sending SMS to {}: {}", phoneNumber, message);
            
            // In real implementation:
            // twilioService.sendSMS(phoneNumber, message);
            
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
        }
    }

    private void sendPushNotification(java.util.UUID userId, String title, String message) {
        try {
            // Mock push notification - integrate with Firebase, OneSignal, etc.
            log.info("Sending push notification to user {}: {} - {}", userId, title, message);
            
            Map<String, Object> pushEvent = Map.of(
                    "userId", userId,
                    "title", title,
                    "message", message,
                    "timestamp", java.time.Instant.now()
            );

            kafkaTemplate.send("push-notifications", pushEvent);
            
        } catch (Exception e) {
            log.error("Failed to send push notification to user: {}", userId, e);
        }
    }
}