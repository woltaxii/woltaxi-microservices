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
 * Location Share Request DTO - Konum Paylaşım İsteği Veri Transfer Nesnesi
 * Bu DTO, konum paylaşım istekleri için kullanılır
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Location Share Request - Konum paylaşım isteği")
public class LocationShareRequestDto {

    @NotNull(message = "User ID is required")
    @Schema(description = "User sharing the location", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String userId;

    @Schema(description = "Trip ID if sharing during ride", example = "770e8400-e29b-41d4-a716-446655440000")
    private String tripId;

    @NotNull(message = "Location is required")
    @Valid
    @Schema(description = "Current location to share", required = true)
    private LocationDto location;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours (1440 minutes)")
    @Schema(description = "Duration to share location in minutes", example = "60", required = true)
    private Integer durationMinutes;

    @NotEmpty(message = "At least one contact must be specified")
    @Schema(description = "Emergency contacts to share location with", required = true)
    private List<String> contactIds;

    @Schema(description = "Reason for sharing location", example = "SAFETY_PRECAUTION", 
            allowableValues = {"SAFETY_PRECAUTION", "EMERGENCY", "TRIP_MONITORING", "FAMILY_TRACKING", 
                              "BUSINESS_TRAVEL", "TOURIST_SAFETY", "LATE_NIGHT_TRAVEL", "OTHER"})
    private String shareReason = "SAFETY_PRECAUTION";

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    @Schema(description = "Optional message to include with location share", 
            example = "Eve dönerken konumumu paylaşıyorum. 60 dakika içinde ulaşacağım.")
    private String message;

    @Schema(description = "Language preference for notifications", example = "tr")
    private String languagePreference = "tr";

    @Schema(description = "Update interval in seconds", example = "30")
    @Min(value = 10, message = "Update interval must be at least 10 seconds")
    @Max(value = 300, message = "Update interval cannot exceed 5 minutes")
    private Integer updateIntervalSeconds = 30;

    @Schema(description = "Accuracy threshold in meters", example = "10.0")
    @DecimalMin(value = "1.0", message = "Accuracy threshold must be at least 1 meter")
    @DecimalMax(value = "100.0", message = "Accuracy threshold cannot exceed 100 meters")
    private Double accuracyThresholdMeters = 10.0;

    @Schema(description = "Notification preferences for location sharing")
    private NotificationPreferencesDto notificationPreferences;

    @Schema(description = "Privacy settings for location sharing")
    private PrivacySettingsDto privacySettings;

    @Schema(description = "Geofence settings for alerts")
    private List<GeofenceDto> geofences;

    @Schema(description = "Whether to include speed and heading information", example = "true")
    private Boolean includeSpeedAndHeading = true;

    @Schema(description = "Whether to include battery level information", example = "true")
    private Boolean includeBatteryLevel = true;

    @Schema(description = "Whether to send arrival notifications", example = "true")
    private Boolean sendArrivalNotifications = true;

    @Schema(description = "Whether to send departure notifications", example = "true")
    private Boolean sendDepartureNotifications = true;

    @Schema(description = "Auto-stop conditions for location sharing")
    private AutoStopConditionsDto autoStopConditions;

    @Schema(description = "Custom sharing settings")
    private CustomSharingSettingsDto customSettings;

    @Schema(description = "Additional metadata for the location share")
    private Map<String, Object> metadata;

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

        @Schema(description = "Speed in m/s", example = "15.0")
        private Double speed;

        @Schema(description = "Heading in degrees", example = "90.0")
        private Double heading;

        @Schema(description = "Address if available", example = "Taksim Square, Beyoğlu, Istanbul")
        private String address;

        @Schema(description = "Location name or landmark", example = "Taksim Meydanı")
        private String locationName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NotificationPreferencesDto {
        @Schema(description = "Send start sharing notification", example = "true")
        private Boolean sendStartNotification = true;

        @Schema(description = "Send periodic update notifications", example = "false")
        private Boolean sendPeriodicUpdates = false;

        @Schema(description = "Periodic update interval in minutes", example = "15")
        private Integer periodicUpdateIntervalMinutes = 15;

        @Schema(description = "Send stop sharing notification", example = "true")
        private Boolean sendStopNotification = true;

        @Schema(description = "Send low battery alerts", example = "true")
        private Boolean sendBatteryAlerts = true;

        @Schema(description = "Battery alert threshold percentage", example = "20")
        private Integer batteryAlertThreshold = 20;

        @Schema(description = "Send offline alerts", example = "true")
        private Boolean sendOfflineAlerts = true;

        @Schema(description = "Offline alert delay in minutes", example = "5")
        private Integer offlineAlertDelayMinutes = 5;

        @Schema(description = "Notification methods to use")
        private List<String> notificationMethods = List.of("SMS", "PUSH");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PrivacySettingsDto {
        @Schema(description = "Show exact location", example = "true")
        private Boolean showExactLocation = true;

        @Schema(description = "Location precision level", example = "HIGH", 
                allowableValues = {"HIGH", "MEDIUM", "LOW"})
        private String precisionLevel = "HIGH";

        @Schema(description = "Hide location when stationary", example = "false")
        private Boolean hideWhenStationary = false;

        @Schema(description = "Stationary threshold in minutes", example = "10")
        private Integer stationaryThresholdMinutes = 10;

        @Schema(description = "Show speed information", example = "true")
        private Boolean showSpeed = true;

        @Schema(description = "Show heading information", example = "true")
        private Boolean showHeading = true;

        @Schema(description = "Show battery level", example = "true")
        private Boolean showBatteryLevel = true;

        @Schema(description = "Allow screenshot", example = "true")
        private Boolean allowScreenshot = true;

        @Schema(description = "Show location history", example = "false")
        private Boolean showLocationHistory = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GeofenceDto {
        @Schema(description = "Geofence name", example = "Home")
        private String name;

        @NotNull(message = "Center latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        @Schema(description = "Center latitude", example = "41.0082", required = true)
        private Double centerLatitude;

        @NotNull(message = "Center longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        @Schema(description = "Center longitude", example = "28.9784", required = true)
        private Double centerLongitude;

        @NotNull(message = "Radius is required")
        @Min(value = 10, message = "Radius must be at least 10 meters")
        @Max(value = 10000, message = "Radius cannot exceed 10 kilometers")
        @Schema(description = "Radius in meters", example = "500", required = true)
        private Integer radiusMeters;

        @Schema(description = "Alert when entering geofence", example = "true")
        private Boolean alertOnEntry = true;

        @Schema(description = "Alert when exiting geofence", example = "true")
        private Boolean alertOnExit = true;

        @Schema(description = "Geofence type", example = "SAFE_ZONE", 
                allowableValues = {"SAFE_ZONE", "RESTRICTED_AREA", "DESTINATION", "WAYPOINT", "CUSTOM"})
        private String geofenceType = "SAFE_ZONE";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AutoStopConditionsDto {
        @Schema(description = "Stop when arriving at destination", example = "true")
        private Boolean stopOnArrival = false;

        @Schema(description = "Destination coordinates for auto-stop")
        private LocationDto destination;

        @Schema(description = "Arrival threshold in meters", example = "100")
        private Integer arrivalThresholdMeters = 100;

        @Schema(description = "Stop when battery is low", example = "true")
        private Boolean stopOnLowBattery = false;

        @Schema(description = "Low battery threshold percentage", example = "10")
        private Integer lowBatteryThreshold = 10;

        @Schema(description = "Stop when device goes offline", example = "false")
        private Boolean stopOnOffline = false;

        @Schema(description = "Offline threshold in minutes", example = "15")
        private Integer offlineThresholdMinutes = 15;

        @Schema(description = "Stop when stationary for extended period", example = "false")
        private Boolean stopOnExtendedStationary = false;

        @Schema(description = "Extended stationary threshold in minutes", example = "30")
        private Integer extendedStationaryMinutes = 30;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomSharingSettingsDto {
        @Schema(description = "Custom sharing name", example = "Eve dönüş yolculuğu")
        private String sharingName;

        @Schema(description = "Custom icon for sharing", example = "home")
        private String customIcon;

        @Schema(description = "Custom color for map display", example = "#FF5722")
        private String customColor;

        @Schema(description = "Show estimated arrival time", example = "true")
        private Boolean showEstimatedArrival = true;

        @Schema(description = "Show travel route", example = "true")
        private Boolean showRoute = false;

        @Schema(description = "Show traffic information", example = "false")
        private Boolean showTraffic = false;

        @Schema(description = "Custom messages for different events")
        private Map<String, String> customMessages;

        @Schema(description = "Additional features to enable")
        private List<String> additionalFeatures;
    }
}