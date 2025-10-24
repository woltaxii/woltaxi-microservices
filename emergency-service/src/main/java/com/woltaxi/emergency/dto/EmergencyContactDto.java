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
import java.util.Map;

/**
 * Emergency Contact DTO - Acil Durum İletişim Kişisi Veri Transfer Nesnesi
 * Bu DTO, acil durum iletişim kişilerinin API üzerinden transferi için kullanılır
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Emergency Contact Data Transfer Object")
public class EmergencyContactDto {

    @Schema(description = "Unique contact ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @NotNull(message = "User ID is required")
    @Schema(description = "User who owns this emergency contact", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @NotBlank(message = "Contact name is required")
    @Size(min = 2, max = 100, message = "Contact name must be between 2 and 100 characters")
    @Schema(description = "Full name of the emergency contact", example = "Ahmet Yılmaz")
    private String contactName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Schema(description = "Primary phone number", example = "+905551234567")
    private String phoneNumber;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid alternative phone number format")
    @Schema(description = "Alternative phone number", example = "+905559876543")
    private String alternativePhoneNumber;

    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "ahmet.yilmaz@example.com")
    private String email;

    @NotNull(message = "Contact type is required")
    @Schema(description = "Type of emergency contact", example = "FAMILY", 
            allowableValues = {"FAMILY", "FRIEND", "MEDICAL", "LEGAL", "WORK", "NEIGHBOR", "OTHER"})
    private String contactType;

    @NotBlank(message = "Relationship is required")
    @Size(max = 50, message = "Relationship cannot exceed 50 characters")
    @Schema(description = "Relationship to the user", example = "Eş")
    private String relationship;

    @NotNull(message = "Priority is required")
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority cannot exceed 10")
    @Schema(description = "Contact priority (1 = highest priority)", example = "1", minimum = "1", maximum = "10")
    private Integer priority;

    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether this contact is active", example = "true")
    private Boolean enabled = true;

    @Schema(description = "Notification methods for this contact")
    private NotificationPreferencesDto notificationPreferences;

    @Schema(description = "Contact's physical address")
    private AddressDto address;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Additional notes about the contact", example = "Ana acil durum iletişim kişisi - her zaman ulaşılabilir")
    private String notes;

    @Pattern(regexp = "^[a-z]{2}$", message = "Language code must be 2 characters")
    @Schema(description = "Preferred language for communication", example = "tr")
    private String preferredLanguage = "tr";

    @Schema(description = "Time zone of the contact", example = "Europe/Istanbul")
    private String timeZone;

    @Schema(description = "Availability schedule for the contact")
    private AvailabilityDto availability;

    @Schema(description = "Medical information relevant for emergencies")
    private MedicalInfoDto medicalInfo;

    @Schema(description = "Whether to auto-notify this contact", example = "true")
    private Boolean autoNotify = true;

    @Schema(description = "Notification delay in seconds", example = "0")
    private Integer notificationDelaySeconds = 0;

    @Schema(description = "Whether this contact can be reached 24/7", example = "true")
    private Boolean available24x7 = false;

    @Schema(description = "Emergency scenarios where this contact should be notified")
    private EmergencyScenarioPreferencesDto scenarioPreferences;

    @NotNull(message = "Created timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the contact was added", example = "2024-12-15T14:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the contact was last updated", example = "2024-12-15T15:45:00")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When this contact was last contacted", example = "2024-12-15T16:20:00")
    private LocalDateTime lastContactedAt;

    @Schema(description = "How many times this contact has been notified", example = "3")
    private Integer notificationCount = 0;

    @Schema(description = "Success rate of notifications to this contact", example = "95.5")
    private Double notificationSuccessRate;

    @Schema(description = "Average response time of this contact in minutes", example = "15")
    private Integer averageResponseTimeMinutes;

    @Schema(description = "Whether this contact has responded to previous emergencies", example = "true")
    private Boolean hasRespondedBefore = false;

    @Schema(description = "Last emergency incident this contact was involved in")
    private String lastIncidentId;

    @Schema(description = "Contact verification status", example = "VERIFIED", 
            allowableValues = {"PENDING", "VERIFIED", "FAILED", "EXPIRED"})
    private String verificationStatus = "PENDING";

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the contact was last verified", example = "2024-12-15T14:30:00")
    private LocalDateTime lastVerifiedAt;

    @Schema(description = "Additional metadata for the contact")
    private Map<String, Object> metadata;

    // Nested DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NotificationPreferencesDto {
        @Schema(description = "Enable SMS notifications", example = "true")
        private Boolean smsEnabled = true;

        @Schema(description = "Enable voice call notifications", example = "true")
        private Boolean callEnabled = true;

        @Schema(description = "Enable email notifications", example = "true")
        private Boolean emailEnabled = true;

        @Schema(description = "Enable WhatsApp notifications", example = "false")
        private Boolean whatsappEnabled = false;

        @Schema(description = "Enable push notifications", example = "true")
        private Boolean pushEnabled = true;

        @Schema(description = "Preferred notification method", example = "SMS", 
                allowableValues = {"SMS", "CALL", "EMAIL", "WHATSAPP", "PUSH", "ALL"})
        private String preferredMethod = "SMS";

        @Schema(description = "Maximum notification attempts", example = "3")
        private Integer maxAttempts = 3;

        @Schema(description = "Retry interval in minutes", example = "5")
        private Integer retryIntervalMinutes = 5;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddressDto {
        @Schema(description = "Street address", example = "Taksim Meydanı No: 1")
        private String street;

        @Schema(description = "City", example = "İstanbul")
        private String city;

        @Schema(description = "State or province", example = "İstanbul")
        private String state;

        @Schema(description = "Postal code", example = "34435")
        private String postalCode;

        @Schema(description = "Country", example = "Turkey")
        private String country;

        @Schema(description = "Country code", example = "TR")
        private String countryCode;

        @Schema(description = "Latitude coordinate", example = "41.0082")
        private Double latitude;

        @Schema(description = "Longitude coordinate", example = "28.9784")
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AvailabilityDto {
        @Schema(description = "Available on Monday", example = "true")
        private Boolean monday = true;

        @Schema(description = "Available on Tuesday", example = "true")
        private Boolean tuesday = true;

        @Schema(description = "Available on Wednesday", example = "true")
        private Boolean wednesday = true;

        @Schema(description = "Available on Thursday", example = "true")
        private Boolean thursday = true;

        @Schema(description = "Available on Friday", example = "true")
        private Boolean friday = true;

        @Schema(description = "Available on Saturday", example = "true")
        private Boolean saturday = true;

        @Schema(description = "Available on Sunday", example = "true")
        private Boolean sunday = true;

        @Schema(description = "Available start time", example = "09:00")
        private String startTime = "00:00";

        @Schema(description = "Available end time", example = "18:00")
        private String endTime = "23:59";

        @Schema(description = "Special availability notes", example = "Sadece iş saatlerinde arayın")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MedicalInfoDto {
        @Schema(description = "Blood type", example = "A+")
        private String bloodType;

        @Schema(description = "Known medical conditions")
        private String medicalConditions;

        @Schema(description = "Current medications")
        private String medications;

        @Schema(description = "Known allergies")
        private String allergies;

        @Schema(description = "Emergency medical contacts")
        private String emergencyMedicalContacts;

        @Schema(description = "Preferred hospital", example = "Acıbadem Taksim Hastanesi")
        private String preferredHospital;

        @Schema(description = "Medical insurance information")
        private String insuranceInfo;

        @Schema(description = "Special medical instructions")
        private String specialInstructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmergencyScenarioPreferencesDto {
        @Schema(description = "Notify for SOS incidents", example = "true")
        private Boolean sosIncidents = true;

        @Schema(description = "Notify for panic button", example = "true")
        private Boolean panicButton = true;

        @Schema(description = "Notify for medical emergencies", example = "true")
        private Boolean medicalEmergencies = true;

        @Schema(description = "Notify for accidents", example = "true")
        private Boolean accidents = true;

        @Schema(description = "Notify for harassment incidents", example = "true")
        private Boolean harassment = true;

        @Schema(description = "Notify for vehicle breakdowns", example = "false")
        private Boolean vehicleBreakdowns = false;

        @Schema(description = "Notify for safety concerns", example = "true")
        private Boolean safetyConcerns = true;

        @Schema(description = "Notify for natural disasters", example = "true")
        private Boolean naturalDisasters = true;

        @Schema(description = "Notify for crime in progress", example = "true")
        private Boolean crimeInProgress = true;

        @Schema(description = "Notify for other incidents", example = "false")
        private Boolean otherIncidents = false;

        @Schema(description = "Minimum priority level to notify", example = "HIGH", 
                allowableValues = {"CRITICAL", "HIGH", "MEDIUM", "LOW"})
        private String minimumPriority = "MEDIUM";
    }
}