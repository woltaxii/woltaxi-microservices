package com.woltaxi.emergency.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * SOS Trigger Request DTO - SOS Tetikleme İsteği Veri Transfer Nesnesi
 * Bu DTO, SOS acil durum tetikleme istekleri için kullanılır
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "SOS Trigger Request - SOS acil durum tetikleme isteği")
public class SosTriggerRequestDto {

    @NotNull(message = "User ID is required")
    @Schema(description = "User triggering the SOS", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String userId;

    @Schema(description = "Driver ID if emergency during ride", example = "660e8400-e29b-41d4-a716-446655440000")
    private String driverId;

    @Schema(description = "Trip ID if emergency during ride", example = "770e8400-e29b-41d4-a716-446655440000")
    private String tripId;

    @NotNull(message = "Location is required")
    @Valid
    @Schema(description = "Current location of the emergency", required = true)
    private LocationDto location;

    @Schema(description = "Type of SOS emergency", example = "GENERAL_EMERGENCY", 
            allowableValues = {"GENERAL_EMERGENCY", "MEDICAL_EMERGENCY", "ACCIDENT", "HARASSMENT", 
                              "VEHICLE_BREAKDOWN", "SAFETY_CONCERN", "CRIME_IN_PROGRESS", "NATURAL_DISASTER"})
    private String emergencyType = "GENERAL_EMERGENCY";

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Optional description of the emergency", example = "Sürücü güvenli olmayan davranışlar sergiliyor")
    private String description;

    @Schema(description = "Priority level", example = "CRITICAL", 
            allowableValues = {"CRITICAL", "HIGH", "MEDIUM", "LOW"})
    private String priority = "CRITICAL";

    @Schema(description = "Language preference for communication", example = "tr")
    private String languagePreference = "tr";

    @Schema(description = "Active safety mode", example = "WOMEN_SAFETY", 
            allowableValues = {"WOMEN_SAFETY", "FAMILY_SAFETY", "TOURIST_SAFETY", "BUSINESS_SAFETY", "STANDARD"})
    private String safetyMode;

    @Schema(description = "Contact information for immediate response")
    private ContactInfoDto contactInfo;

    @Schema(description = "Whether to automatically contact authorities", example = "true")
    private Boolean autoContactAuthorities = true;

    @Schema(description = "Whether to start audio recording", example = "true")
    private Boolean startAudioRecording = true;

    @Schema(description = "Whether to start video recording", example = "false")
    private Boolean startVideoRecording = false;

    @Schema(description = "Whether to share location with emergency contacts", example = "true")
    private Boolean shareLocationWithContacts = true;

    @Schema(description = "Duration to share location in minutes", example = "60")
    private Integer locationSharingDurationMinutes = 60;

    @Schema(description = "Specific emergency contacts to notify (if empty, all active contacts will be notified)")
    private List<String> specificContactsToNotify;

    @Schema(description = "Whether this is a test SOS", example = "false")
    private Boolean isTestSos = false;

    @Schema(description = "Device information")
    private DeviceInfoDto deviceInfo;

    @Schema(description = "Additional metadata for the SOS request")
    private Map<String, Object> metadata;

    @Schema(description = "Silent mode - don't trigger audible alerts", example = "false")
    private Boolean silentMode = false;

    @Schema(description = "Estimated number of people in danger", example = "1")
    private Integer peopleInDanger = 1;

    @Schema(description = "Specific authorities to contact")
    private List<String> authoritiesToContact;

    @Schema(description = "Medical emergency details if applicable")
    private MedicalEmergencyDto medicalEmergency;

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
        @Schema(description = "Latitude coordinate", example = "41.0082", required = true)
        private Double latitude;

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        @Schema(description = "Longitude coordinate", example = "28.9784", required = true)
        private Double longitude;

        @Schema(description = "Location accuracy in meters", example = "5.0")
        private Double accuracy;

        @Schema(description = "Altitude in meters", example = "100.0")
        private Double altitude;

        @Schema(description = "Speed in m/s", example = "0.0")
        private Double speed;

        @Schema(description = "Heading in degrees", example = "90.0")
        private Double heading;

        @Schema(description = "Address if available", example = "Taksim Square, Beyoğlu, Istanbul")
        private String address;

        @Schema(description = "Additional location details", example = "Near the fountain")
        private String locationDetails;
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
        @Schema(description = "Primary phone number", example = "+905551234567")
        private String phoneNumber;

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid alternative phone number format")
        @Schema(description = "Alternative phone number", example = "+905559876543")
        private String alternativePhoneNumber;

        @Schema(description = "Preferred contact method", example = "SMS", 
                allowableValues = {"SMS", "CALL", "EMAIL", "WHATSAPP", "APP_NOTIFICATION"})
        private String preferredContactMethod = "SMS";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeviceInfoDto {
        @Schema(description = "Device type", example = "MOBILE", 
                allowableValues = {"MOBILE", "TABLET", "SMARTWATCH", "PANIC_BUTTON", "OTHER"})
        private String deviceType = "MOBILE";

        @Schema(description = "Operating system", example = "Android")
        private String operatingSystem;

        @Schema(description = "Device model", example = "Samsung Galaxy S23")
        private String deviceModel;

        @Schema(description = "App version", example = "1.2.3")
        private String appVersion;

        @Schema(description = "Device ID", example = "device123456")
        private String deviceId;

        @Schema(description = "Battery level percentage", example = "45")
        private Integer batteryLevel;

        @Schema(description = "Whether device is charging", example = "false")
        private Boolean isCharging;

        @Schema(description = "Network type", example = "WIFI", 
                allowableValues = {"WIFI", "4G", "5G", "3G", "2G", "OFFLINE"})
        private String networkType;

        @Schema(description = "Signal strength (0-4)", example = "3")
        private Integer signalStrength;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MedicalEmergencyDto {
        @Schema(description = "Type of medical emergency", example = "HEART_ATTACK", 
                allowableValues = {"HEART_ATTACK", "STROKE", "BREATHING_DIFFICULTY", "SEVERE_BLEEDING", 
                                  "UNCONSCIOUS", "SEVERE_PAIN", "ALLERGIC_REACTION", "OVERDOSE", 
                                  "SEIZURE", "BURN", "FRACTURE", "OTHER"})
        private String medicalEmergencyType;

        @Size(max = 500, message = "Medical details cannot exceed 500 characters")
        @Schema(description = "Description of medical condition", example = "Göğüs ağrısı ve nefes darlığı")
        private String medicalDetails;

        @Schema(description = "Is patient conscious", example = "true")
        private Boolean isConscious = true;

        @Schema(description = "Is patient breathing", example = "true")
        private Boolean isBreathing = true;

        @Schema(description = "Patient's age", example = "35")
        private Integer patientAge;

        @Schema(description = "Patient's gender", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
        private String patientGender;

        @Schema(description = "Known medical conditions", example = "Hipertansiyon")
        private String knownMedicalConditions;

        @Schema(description = "Current medications", example = "Aspirin")
        private String currentMedications;

        @Schema(description = "Known allergies", example = "Penisilin")
        private String knownAllergies;

        @Schema(description = "Blood type if known", example = "A+")
        private String bloodType;

        @Schema(description = "Emergency contact person", example = "Eşi Ayşe Yılmaz")
        private String emergencyContactPerson;

        @Schema(description = "Emergency contact phone", example = "+905551234567")
        private String emergencyContactPhone;

        @Schema(description = "Preferred hospital", example = "Acıbadem Taksim")
        private String preferredHospital;

        @Schema(description = "Medical insurance information")
        private String insuranceInfo;

        @Schema(description = "First aid being administered", example = "false")
        private Boolean firstAidBeingAdministered = false;

        @Schema(description = "Description of first aid being given")
        private String firstAidDescription;
    }
}