package com.woltaxi.emergency.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Emergency Response DTO - Acil Durum Yanıt Veri Transfer Nesnesi
 * Bu DTO, acil durum yanıtları için kullanılır
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Emergency Response - Acil durum yanıtı")
public class EmergencyResponseDto {

    @Schema(description = "Response status", example = "SUCCESS", 
            allowableValues = {"SUCCESS", "PARTIAL_SUCCESS", "FAILED", "PENDING"})
    private String status;

    @Schema(description = "Response message", example = "Emergency incident created successfully")
    private String message;

    @Schema(description = "Incident ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String incidentId;

    @Schema(description = "Auto-generated incident number", example = "EMG-20241215-0001")
    private String incidentNumber;

    @Schema(description = "Response timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Estimated response time in minutes", example = "5")
    private Integer estimatedResponseTimeMinutes;

    @Schema(description = "Actions taken automatically")
    private List<ActionTakenDto> actionsTaken;

    @Schema(description = "Contacts that were notified")
    private List<ContactNotificationDto> contactsNotified;

    @Schema(description = "Authorities that were contacted")
    private List<AuthorityContactDto> authoritiesContacted;

    @Schema(description = "Emergency services dispatched")
    private List<ServiceDispatchedDto> servicesDispatched;

    @Schema(description = "Real-time tracking information")
    private TrackingInfoDto trackingInfo;

    @Schema(description = "Next steps and instructions")
    private NextStepsDto nextSteps;

    @Schema(description = "Error details if any")
    private List<ErrorDetailDto> errors;

    @Schema(description = "Warnings or important notes")
    private List<String> warnings;

    @Schema(description = "Performance metrics")
    private PerformanceMetricsDto performanceMetrics;

    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;

    // Nested DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ActionTakenDto {
        @Schema(description = "Action type", example = "SMS_SENT")
        private String actionType;

        @Schema(description = "Action description", example = "SMS notification sent to emergency contact")
        private String description;

        @Schema(description = "Action result", example = "SUCCESS", 
                allowableValues = {"SUCCESS", "FAILED", "PENDING", "PARTIAL"})
        private String result;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When action was taken")
        private LocalDateTime timestamp;

        @Schema(description = "Target of the action", example = "+905551234567")
        private String target;

        @Schema(description = "Additional details about the action")
        private String details;

        @Schema(description = "Error message if action failed")
        private String errorMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContactNotificationDto {
        @Schema(description = "Contact ID", example = "contact123")
        private String contactId;

        @Schema(description = "Contact name", example = "Ahmet Yılmaz")
        private String contactName;

        @Schema(description = "Contact phone", example = "+905551234567")
        private String contactPhone;

        @Schema(description = "Contact relationship", example = "Eş")
        private String relationship;

        @Schema(description = "Notification method used", example = "SMS", 
                allowableValues = {"SMS", "CALL", "EMAIL", "WHATSAPP", "PUSH"})
        private String notificationMethod;

        @Schema(description = "Notification status", example = "SENT", 
                allowableValues = {"SENT", "DELIVERED", "FAILED", "PENDING"})
        private String status;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When notification was sent")
        private LocalDateTime sentAt;

        @Schema(description = "Message sent to contact")
        private String messageSent;

        @Schema(description = "Error details if notification failed")
        private String errorDetails;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuthorityContactDto {
        @Schema(description = "Authority type", example = "POLICE", 
                allowableValues = {"POLICE", "AMBULANCE", "FIRE_DEPARTMENT", "COAST_GUARD", "EMERGENCY_SERVICES"})
        private String authorityType;

        @Schema(description = "Authority name", example = "Istanbul Police Department")
        private String authorityName;

        @Schema(description = "Contact number used", example = "155")
        private String contactNumber;

        @Schema(description = "Contact method", example = "AUTOMATIC_CALL", 
                allowableValues = {"AUTOMATIC_CALL", "MANUAL_CALL", "SMS", "API"})
        private String contactMethod;

        @Schema(description = "Contact status", example = "CONTACTED", 
                allowableValues = {"CONTACTED", "DISPATCHED", "FAILED", "PENDING"})
        private String status;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When authority was contacted")
        private LocalDateTime contactedAt;

        @Schema(description = "Reference number provided by authority")
        private String referenceNumber;

        @Schema(description = "Estimated arrival time in minutes", example = "15")
        private Integer estimatedArrivalMinutes;

        @Schema(description = "Additional information from authority")
        private String additionalInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceDispatchedDto {
        @Schema(description = "Service type", example = "AMBULANCE", 
                allowableValues = {"AMBULANCE", "POLICE_CAR", "FIRE_TRUCK", "RESCUE_TEAM", "TOWING_SERVICE"})
        private String serviceType;

        @Schema(description = "Service identifier", example = "AMB-001")
        private String serviceId;

        @Schema(description = "Dispatch status", example = "DISPATCHED", 
                allowableValues = {"DISPATCHED", "EN_ROUTE", "ARRIVED", "COMPLETED"})
        private String status;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When service was dispatched")
        private LocalDateTime dispatchedAt;

        @Schema(description = "Estimated arrival time in minutes", example = "12")
        private Integer estimatedArrivalMinutes;

        @Schema(description = "Current location of service")
        private String currentLocation;

        @Schema(description = "Contact information for the service")
        private String contactInfo;

        @Schema(description = "Special instructions for the service")
        private String specialInstructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TrackingInfoDto {
        @Schema(description = "Tracking session ID", example = "track123")
        private String sessionId;

        @Schema(description = "Real-time tracking URL", example = "https://track.woltaxi.com/emergency/track123")
        private String trackingUrl;

        @Schema(description = "Tracking duration in minutes", example = "60")
        private Integer durationMinutes;

        @Schema(description = "Update interval in seconds", example = "30")
        private Integer updateIntervalSeconds;

        @Schema(description = "Contacts who can access tracking")
        private List<String> authorizedContacts;

        @Schema(description = "Tracking features enabled")
        private List<String> featuresEnabled;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When tracking will expire")
        private LocalDateTime expiresAt;

        @Schema(description = "Whether tracking is active", example = "true")
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NextStepsDto {
        @Schema(description = "Immediate actions to take")
        private List<String> immediateActions;

        @Schema(description = "What to expect next")
        private List<String> expectations;

        @Schema(description = "Important reminders")
        private List<String> reminders;

        @Schema(description = "Contact information for follow-up")
        private String followUpContact;

        @Schema(description = "Expected resolution time", example = "30 minutes")
        private String expectedResolutionTime;

        @Schema(description = "Status update frequency", example = "Every 5 minutes")
        private String updateFrequency;

        @Schema(description = "Emergency helpline number", example = "+90800123456")
        private String emergencyHelpline;

        @Schema(description = "Additional resources available")
        private List<String> additionalResources;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetailDto {
        @Schema(description = "Error code", example = "CONTACT_FAILED")
        private String errorCode;

        @Schema(description = "Error message", example = "Failed to send SMS to emergency contact")
        private String errorMessage;

        @Schema(description = "Component that failed", example = "SMS_SERVICE")
        private String component;

        @Schema(description = "Severity level", example = "HIGH", 
                allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
        private String severity;

        @Schema(description = "Whether this error is recoverable", example = "true")
        private Boolean recoverable;

        @Schema(description = "Suggested action to resolve", example = "Try alternative contact method")
        private String suggestedAction;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When error occurred")
        private LocalDateTime occurredAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PerformanceMetricsDto {
        @Schema(description = "Total processing time in milliseconds", example = "1250")
        private Long totalProcessingTimeMs;

        @Schema(description = "Time to create incident in milliseconds", example = "150")
        private Long incidentCreationTimeMs;

        @Schema(description = "Time to notify contacts in milliseconds", example = "800")
        private Long contactNotificationTimeMs;

        @Schema(description = "Time to contact authorities in milliseconds", example = "300")
        private Long authorityContactTimeMs;

        @Schema(description = "Number of successful operations", example = "8")
        private Integer successfulOperations;

        @Schema(description = "Number of failed operations", example = "1")
        private Integer failedOperations;

        @Schema(description = "Overall success rate percentage", example = "88.9")
        private Double successRate;

        @Schema(description = "Response quality score (1-10)", example = "9")
        private Integer responseQualityScore;
    }
}