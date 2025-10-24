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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Acil Durum İletişim Kişileri
 * Kullanıcıların acil durumda ulaşılacak kişileri
 */
@Entity
@Table(name = "emergency_contacts",
       indexes = {
           @Index(name = "idx_emergency_contacts_user", columnList = "user_id"),
           @Index(name = "idx_emergency_contacts_driver", columnList = "driver_id"),
           @Index(name = "idx_emergency_contacts_type", columnList = "contact_type_id"),
           @Index(name = "idx_emergency_contacts_active", columnList = "is_active, priority_order")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User/Driver Reference - One of them must be not null
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "driver_id")
    private Long driverId;

    // Contact Type Reference
    @NotNull
    @Column(name = "contact_type_id", nullable = false)
    private Long contactTypeId;

    // Contact Details
    @NotBlank(message = "Contact name is required")
    @Size(min = 2, max = 200)
    @Column(name = "contact_name", nullable = false, length = 200)
    private String contactName;

    @NotBlank(message = "Primary phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Column(name = "primary_phone", nullable = false, length = 20)
    private String primaryPhone;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid secondary phone number format")
    @Column(name = "secondary_phone", length = 20)
    private String secondaryPhone;

    @Email(message = "Invalid email format")
    @Column(name = "email", length = 100)
    private String email;

    // Relationship & Preferences
    @Size(max = 100)
    @Column(name = "relationship", length = 100)
    private String relationship; // 'Mother', 'Wife', 'Best Friend', etc.

    @Builder.Default
    @Size(min = 2, max = 10)
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";

    // Notification Preferences (JSON)
    @Column(name = "notification_preferences", columnDefinition = "jsonb")
    private String notificationPreferences; // SMS, Call, WhatsApp, etc.

    // Location & Availability
    @Size(max = 3)
    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 50)
    @Column(name = "timezone", length = 50)
    private String timezone;

    // Available Hours (JSON) - When they can be reached
    @Column(name = "available_hours", columnDefinition = "jsonb")
    private String availableHours;

    // Status & Verification
    @Builder.Default
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Size(max = 10)
    @Column(name = "verification_code", length = 10)
    private String verificationCode;

    @Min(0)
    @Max(5)
    @Builder.Default
    @Column(name = "verification_attempts")
    private Integer verificationAttempts = 0;

    @Column(name = "last_verification_at")
    private LocalDateTime lastVerificationAt;

    // Priority & Activation
    @Min(1)
    @Max(10)
    @Builder.Default
    @Column(name = "priority_order")
    private Integer priorityOrder = 1;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Emergency Keywords - Array of keywords that trigger notification to this contact
    @ElementCollection
    @CollectionTable(name = "emergency_contact_keywords", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "keyword")
    private List<String> emergencyKeywords;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper Methods
    public boolean isUserContact() {
        return userId != null;
    }

    public boolean isDriverContact() {
        return driverId != null;
    }

    public boolean canBeReachedNow() {
        if (!isActive || !isVerified) {
            return false;
        }
        
        // TODO: Implement timezone-based availability check
        // For now, assume always available if active and verified
        return true;
    }

    public String getFormattedPhone() {
        if (primaryPhone == null) return null;
        
        // Format phone number for display
        if (primaryPhone.startsWith("+")) {
            return primaryPhone;
        } else if (primaryPhone.length() >= 10) {
            return "+" + primaryPhone;
        }
        return primaryPhone;
    }

    public String getContactDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(contactName);
        
        if (relationship != null && !relationship.isEmpty()) {
            desc.append(" (").append(relationship).append(")");
        }
        
        return desc.toString();
    }

    public boolean hasValidEmail() {
        return email != null && !email.isEmpty() && email.contains("@");
    }

    public boolean hasSecondaryPhone() {
        return secondaryPhone != null && !secondaryPhone.isEmpty();
    }

    public String getPreferredContactMethod() {
        // Parse notification preferences JSON to determine preferred method
        // For now, return primary phone as default
        return "PHONE";
    }

    public boolean shouldNotifyForKeyword(String keyword) {
        if (emergencyKeywords == null || emergencyKeywords.isEmpty()) {
            return true; // Notify for all if no specific keywords set
        }
        
        return emergencyKeywords.stream()
                .anyMatch(k -> k.toLowerCase().contains(keyword.toLowerCase()));
    }

    public int getReliabilityScore() {
        int score = 0;
        
        // Base score for verified contact
        if (isVerified) score += 50;
        
        // Points for having secondary contact methods
        if (hasValidEmail()) score += 15;
        if (hasSecondaryPhone()) score += 15;
        
        // Points for recent verification
        if (lastVerificationAt != null) {
            long daysSinceVerification = java.time.Duration.between(
                lastVerificationAt, LocalDateTime.now()
            ).toDays();
            
            if (daysSinceVerification < 30) score += 20;
            else if (daysSinceVerification < 90) score += 10;
        }
        
        return Math.min(score, 100);
    }

    public boolean needsReverification() {
        if (!isVerified) return true;
        
        if (lastVerificationAt == null) return true;
        
        // Need reverification if not verified in last 6 months
        return java.time.Duration.between(lastVerificationAt, LocalDateTime.now()).toDays() > 180;
    }
}