package com.woltaxi.emergency.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Acil Durum Olayları
 * Her acil durum çağrısı ve SOS tetiklemesi için kayıt
 */
@Entity
@Table(name = "emergency_incidents",
       indexes = {
           @Index(name = "idx_emergency_incidents_status", columnList = "status"),
           @Index(name = "idx_emergency_incidents_severity", columnList = "severity_assessment"),
           @Index(name = "idx_emergency_incidents_location", columnList = "incident_latitude, incident_longitude"),
           @Index(name = "idx_emergency_incidents_time", columnList = "reported_at DESC"),
           @Index(name = "idx_emergency_incidents_ride", columnList = "ride_id"),
           @Index(name = "idx_emergency_incidents_type", columnList = "incident_type_id")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique Identifiers
    @Builder.Default
    @Column(name = "incident_uuid", unique = true, nullable = false)
    private UUID incidentUuid = UUID.randomUUID();

    @Column(name = "incident_number", unique = true, nullable = false, length = 20)
    private String incidentNumber; // Auto-generated: EMG-2025-001234

    // References
    @NotNull
    @Column(name = "incident_type_id", nullable = false)
    private Long incidentTypeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "ride_id")
    private Long rideId;

    // Incident Details
    @Size(max = 200)
    @Column(name = "incident_title", length = 200)
    private String incidentTitle;

    @Column(name = "incident_description", columnDefinition = "TEXT")
    private String incidentDescription;

    @Min(1)
    @Max(5)
    @Column(name = "severity_assessment")
    private Integer severityAssessment;

    // Location Information
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    @Column(name = "incident_latitude", precision = 10, scale = 8)
    private BigDecimal incidentLatitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    @Column(name = "incident_longitude", precision = 11, scale = 8)
    private BigDecimal incidentLongitude;

    @Column(name = "incident_address", columnDefinition = "TEXT")
    private String incidentAddress;

    @Size(max = 3)
    @Column(name = "incident_country_code", length = 3)
    private String incidentCountryCode;

    @Size(max = 100)
    @Column(name = "incident_city", length = 100)
    private String incidentCity;

    @Size(max = 200)
    @Column(name = "nearest_landmark", length = 200)
    private String nearestLandmark;

    @DecimalMin("0.0")
    @Column(name = "location_accuracy_meters", precision = 8, scale = 2)
    private BigDecimal locationAccuracyMeters;

    // Timing
    @NotNull
    @Builder.Default
    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "occurred_at")
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Column(name = "response_started_at")
    private LocalDateTime responseStartedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Reporter Information
    @Column(name = "reported_by_user_id")
    private Long reportedByUserId;

    @Column(name = "reported_by_driver_id")
    private Long reportedByDriverId;

    @Builder.Default
    @Column(name = "reporting_method", length = 20)
    private String reportingMethod = "APP"; // APP, CALL, SMS, WEBSITE

    @Size(max = 20)
    @Column(name = "reporter_phone", length = 20)
    private String reporterPhone;

    @Size(max = 50)
    @Column(name = "reporter_relationship", length = 50)
    private String reporterRelationship; // SELF, FAMILY, WITNESS, etc.

    // Status & Progress
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private IncidentStatus status = IncidentStatus.REPORTED;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Builder.Default
    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;

    // Response Actions Taken
    @Builder.Default
    @Column(name = "police_notified")
    private Boolean policeNotified = false;

    @Column(name = "police_notification_time")
    private LocalDateTime policeNotificationTime;

    @Builder.Default
    @Column(name = "ambulance_called")
    private Boolean ambulanceCalled = false;

    @Column(name = "ambulance_call_time")
    private LocalDateTime ambulanceCallTime;

    @Builder.Default
    @Column(name = "family_notified")
    private Boolean familyNotified = false;

    @Column(name = "family_notification_time")
    private LocalDateTime familyNotificationTime;

    @Builder.Default
    @Column(name = "security_dispatched")
    private Boolean securityDispatched = false;

    @Column(name = "security_dispatch_time")
    private LocalDateTime securityDispatchTime;

    // Media & Evidence
    @Size(max = 500)
    @Column(name = "audio_recording_url", length = 500)
    private String audioRecordingUrl;

    @Size(max = 500)
    @Column(name = "video_recording_url", length = 500)
    private String videoRecordingUrl;

    @ElementCollection
    @CollectionTable(name = "incident_photo_urls", joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private List<String> photoUrls;

    @Size(max = 500)
    @Column(name = "voice_message_url", length = 500)
    private String voiceMessageUrl;

    // Environmental Context
    @Size(max = 100)
    @Column(name = "weather_conditions", length = 100)
    private String weatherConditions;

    @Size(max = 100)
    @Column(name = "traffic_conditions", length = 100)
    private String trafficConditions;

    @Min(0)
    @Builder.Default
    @Column(name = "witnesses_count")
    private Integer witnessesCount = 0;

    // Additional Metadata (JSON)
    @Column(name = "additional_metadata", columnDefinition = "jsonb")
    private String additionalMetadata;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum IncidentStatus {
        REPORTED,           // İlk rapor edildi
        ACKNOWLEDGED,       // Sistem tarafından onaylandı
        DISPATCHED,         // Yardım gönderildi
        IN_PROGRESS,        // Müdahale devam ediyor
        RESOLVED,           // Çözüldü
        FALSE_ALARM,        // Yanlış alarm
        CANCELLED           // İptal edildi
    }

    // Helper Methods
    public boolean isActive() {
        return status == IncidentStatus.REPORTED || 
               status == IncidentStatus.ACKNOWLEDGED || 
               status == IncidentStatus.DISPATCHED || 
               status == IncidentStatus.IN_PROGRESS;
    }

    public boolean isCritical() {
        return severityAssessment != null && severityAssessment <= 2;
    }

    public boolean hasLocation() {
        return incidentLatitude != null && incidentLongitude != null;
    }

    public Long getMinutesSinceReported() {
        if (reportedAt == null) return 0L;
        return java.time.Duration.between(reportedAt, LocalDateTime.now()).toMinutes();
    }

    public String getStatusDescription() {
        return switch (status) {
            case REPORTED -> "Acil durum bildirildi, değerlendiriliyor";
            case ACKNOWLEDGED -> "Olay onaylandı, yardım organize ediliyor";
            case DISPATCHED -> "Yardım ekipleri gönderildi";
            case IN_PROGRESS -> "Müdahale devam ediyor";
            case RESOLVED -> "Olay başarıyla çözüldü";
            case FALSE_ALARM -> "Yanlış alarm tespit edildi";
            case CANCELLED -> "Olay iptal edildi";
        };
    }

    public boolean requiresImmediateResponse() {
        return isCritical() && isActive() && getMinutesSinceReported() > 5;
    }

    public String getSeverityDescription() {
        if (severityAssessment == null) return "Belirlenmedi";
        return switch (severityAssessment) {
            case 1 -> "Kritik - Hayati Tehlike";
            case 2 -> "Çok Yüksek - Acil Müdahale";
            case 3 -> "Yüksek - Hızlı Müdahale";
            case 4 -> "Orta - Normal Müdahale";
            case 5 -> "Düşük - Rutin Takip";
            default -> "Bilinmeyen Seviye";
        };
    }

    public boolean hasEvidence() {
        return (audioRecordingUrl != null && !audioRecordingUrl.isEmpty()) ||
               (videoRecordingUrl != null && !videoRecordingUrl.isEmpty()) ||
               (photoUrls != null && !photoUrls.isEmpty()) ||
               (voiceMessageUrl != null && !voiceMessageUrl.isEmpty());
    }

    public boolean isUserIncident() {
        return userId != null;
    }

    public boolean isDriverIncident() {
        return driverId != null;
    }

    public boolean isRideRelated() {
        return rideId != null;
    }
}