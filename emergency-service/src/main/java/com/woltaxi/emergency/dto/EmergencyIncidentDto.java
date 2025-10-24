package com.woltaxi.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Emergency Incident DTO - Acil Durum Olayı Veri Transfer Nesnesi
 * Bu DTO, acil durum olaylarının API üzerinden transferi için kullanılır
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Emergency Incident Data Transfer Object")
public class EmergencyIncidentDto {

    @Schema(description = "Unique incident ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @NotBlank(message = "Incident number is required")
    @Pattern(regexp = "^EMG-\\d{8}-\\d{4}$", message = "Invalid incident number format")
    @Schema(description = "Auto-generated incident number", example = "EMG-20241215-0001")
    private String incidentNumber;

    @NotNull(message = "User ID is required")
    @Schema(description = "User who triggered the emergency", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @Schema(description = "Driver ID if emergency during ride", example = "660e8400-e29b-41d4-a716-446655440000")
    private String driverId;

    @Schema(description = "Trip ID if emergency during ride", example = "770e8400-e29b-41d4-a716-446655440000")
    private String tripId;

    @NotBlank(message = "Incident type is required")
    @Schema(description = "Type of emergency incident", example = "PANIC_BUTTON", 
            allowableValues = {"SOS", "PANIC_BUTTON", "MEDICAL_EMERGENCY", "ACCIDENT", "HARASSMENT", 
                              "VEHICLE_BREAKDOWN", "SAFETY_CONCERN", "NATURAL_DISASTER", "CRIME_IN_PROGRESS", "OTHER"})
    private String incidentType;

    @NotNull(message = "Priority is required")
    @Schema(description = "Priority level", example = "CRITICAL", 
            allowableValues = {"CRITICAL", "HIGH", "MEDIUM", "LOW"})
    private String priority;

    @NotNull(message = "Status is required")
    @Schema(description = "Current status", example = "ACTIVE", 
            allowableValues = {"ACTIVE", "ACKNOWLEDGED", "IN_PROGRESS", "RESOLVED", "CANCELLED", "ESCALATED"})
    private String status;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Detailed description of the emergency", example = "Passenger feels unsafe, driver is acting aggressively")
    private String description;

    @NotNull(message = "Location is required")
    @Valid
    @Schema(description = "Emergency location details")
    private LocationDto location;

    @Schema(description = "Contact information for follow-up")
    private ContactInfoDto contactInfo;

    @NotNull(message = "Created timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the incident was created", example = "2024-12-15T14:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the incident was acknowledged", example = "2024-12-15T14:31:30")
    private LocalDateTime acknowledgedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the incident was resolved", example = "2024-12-15T15:45:00")
    private LocalDateTime resolvedAt;

    @Schema(description = "ID of the emergency operator handling the case", example = "operator123")
    private String assignedOperatorId;

    @Size(max = 500, message = "Resolution notes cannot exceed 500 characters")
    @Schema(description = "Notes about how the incident was resolved")
    private String resolutionNotes;

    @Schema(description = "Emergency contacts that were notified")
    private List<EmergencyContactDto> notifiedContacts;

    @Schema(description = "List of actions taken during the emergency")
    private List<EmergencyActionDto> actionsTaken;

    @Schema(description = "Media files (audio/video recordings, photos)")
    private List<MediaFileDto> mediaFiles;

    @Schema(description = "Automatic responses that were triggered")
    private List<AutoResponseDto> automaticResponses;

    @Schema(description = "Additional metadata for the incident")
    private Map<String, Object> metadata;

    @Schema(description = "Language preference for communication", example = "tr")
    private String languagePreference;

    @Schema(description = "Safety mode that was active", example = "WOMEN_SAFETY", 
            allowableValues = {"WOMEN_SAFETY", "FAMILY_SAFETY", "TOURIST_SAFETY", "BUSINESS_SAFETY", "STANDARD"})
    private String safetyMode;

    @Schema(description = "Whether this is a test incident", example = "false")
    private Boolean isTestIncident = false;

    @Schema(description = "Risk assessment score (1-10)", example = "8", minimum = "1", maximum = "10")
    private Integer riskScore;

    @Schema(description = "Estimated resolution time in minutes", example = "30")
    private Integer estimatedResolutionTimeMinutes;

    @Schema(description = "Whether authorities were contacted", example = "true")
    private Boolean authoritiesContacted = false;

    @Schema(description = "Which authorities were contacted")
    private List<String> authoritiesContactedList;

    @Schema(description = "External reference numbers (police report, hospital, etc.)")
    private Map<String, String> externalReferences;

    @Schema(description = "Follow-up required after resolution", example = "true")
    private Boolean followUpRequired = false;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When follow-up is scheduled", example = "2024-12-16T10:00:00")
    private LocalDateTime followUpScheduledAt;

    @Schema(description = "Customer satisfaction rating (1-5)", example = "5", minimum = "1", maximum = "5")
    private Integer satisfactionRating;

    @Size(max = 500, message = "Customer feedback cannot exceed 500 characters")
    @Schema(description = "Customer feedback about the emergency response")
    private String customerFeedback;

    @Schema(description = "Total response time in seconds", example = "180")
    private Long totalResponseTimeSeconds;

    @Schema(description = "Performance metrics for this incident")
    private PerformanceMetricsDto performanceMetrics;

    @Schema(description = "Whether this incident was escalated", example = "false")
    private Boolean escalated = false;

    @Schema(description = "Escalation level if escalated", example = "LEVEL_1", 
            allowableValues = {"LEVEL_1", "LEVEL_2", "LEVEL_3", "EXECUTIVE"})
    private String escalationLevel;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the incident was escalated", example = "2024-12-15T14:35:00")
    private LocalDateTime escalatedAt;

    @Schema(description = "Reason for escalation")
    private String escalationReason;

    // Nested DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LocationDto {
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        @Schema(description = "Latitude coordinate", example = "41.0082")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        @Schema(description = "Longitude coordinate", example = "28.9784")
        private Double longitude;

        @Schema(description = "Location accuracy in meters", example = "5.0")
        private Double accuracy;

        @Schema(description = "Formatted address", example = "Taksim Square, Beyoğlu, Istanbul, Turkey")
        private String address;

        @Schema(description = "City name", example = "Istanbul")
        private String city;

        @Schema(description = "Country name", example = "Turkey")
        private String country;

        @Schema(description = "Country code", example = "TR")
        private String countryCode;

        @Schema(description = "Additional location details")
        private String locationDetails;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When location was captured", example = "2024-12-15T14:30:00")
        private LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContactInfoDto {
        @Email(message = "Invalid email format")
        @Schema(description = "Email address", example = "user@example.com")
        private String email;

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        @Schema(description = "Phone number", example = "+905551234567")
        private String phoneNumber;

        @Schema(description = "Alternative contact number", example = "+905559876543")
        private String alternativePhoneNumber;

        @Schema(description = "Preferred contact method", example = "SMS", 
                allowableValues = {"SMS", "CALL", "EMAIL", "WHATSAPP", "APP_NOTIFICATION"})
        private String preferredContactMethod;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmergencyActionDto {
        @Schema(description = "Action ID", example = "action123")
        private String id;

        @Schema(description = "Type of action taken", example = "CONTACT_POLICE")
        private String actionType;

        @Schema(description = "Description of the action", example = "Contacted local police department")
        private String description;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When action was taken", example = "2024-12-15T14:32:00")
        private LocalDateTime actionTimestamp;

        @Schema(description = "Who performed the action", example = "operator123")
        private String performedBy;

        @Schema(description = "Result of the action", example = "Police dispatched to location")
        private String result;

        @Schema(description = "Whether action was successful", example = "true")
        private Boolean successful;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MediaFileDto {
        @Schema(description = "File ID", example = "media123")
        private String id;

        @Schema(description = "File type", example = "AUDIO", 
                allowableValues = {"AUDIO", "VIDEO", "IMAGE", "DOCUMENT"})
        private String fileType;

        @Schema(description = "File name", example = "emergency_recording_20241215_143000.mp3")
        private String fileName;

        @Schema(description = "File URL for download", example = "https://emergency-storage.woltaxi.com/recordings/media123")
        private String fileUrl;

        @Schema(description = "File size in bytes", example = "1048576")
        private Long fileSizeBytes;

        @Schema(description = "File duration in seconds (for audio/video)", example = "120")
        private Integer durationSeconds;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When file was created", example = "2024-12-15T14:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "Whether file is automatically recorded", example = "true")
        private Boolean autoRecorded;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AutoResponseDto {
        @Schema(description = "Response ID", example = "response123")
        private String id;

        @Schema(description = "Type of automatic response", example = "SMS_SENT")
        private String responseType;

        @Schema(description = "Target of the response", example = "+905551234567")
        private String target;

        @Schema(description = "Response content/message")
        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "When response was triggered", example = "2024-12-15T14:30:30")
        private LocalDateTime triggeredAt;

        @Schema(description = "Whether response was successful", example = "true")
        private Boolean successful;

        @Schema(description = "Error message if response failed")
        private String errorMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PerformanceMetricsDto {
        @Schema(description = "Time to acknowledge in seconds", example = "25")
        private Long acknowledgeTimeSeconds;

        @Schema(description = "Time to first response in seconds", example = "45")
        private Long firstResponseTimeSeconds;

        @Schema(description = "Total resolution time in seconds", example = "1800")
        private Long resolutionTimeSeconds;

        @Schema(description = "Number of communication attempts", example = "3")
        private Integer communicationAttempts;

        @Schema(description = "Number of contacts notified", example = "2")
        private Integer contactsNotified;

        @Schema(description = "Number of authorities contacted", example = "1")
        private Integer authoritiesContacted;

        @Schema(description = "Response quality score (1-10)", example = "9")
        private Integer responseQualityScore;
    }
}