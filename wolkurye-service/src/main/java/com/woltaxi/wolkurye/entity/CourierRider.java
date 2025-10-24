package com.woltaxi.wolkurye.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Kurye Motosikletçi Entity
 * 
 * WOLTAXI ekosistemindeki motor kuryelerini yönetir.
 * Aynı sürücü hem yolcu taşımacılığı hem de paket teslimatı yapabilir.
 */
@Entity
@Table(name = "courier_riders", indexes = {
    @Index(name = "idx_rider_user_id", columnList = "user_id"),
    @Index(name = "idx_rider_status", columnList = "status"),
    @Index(name = "idx_rider_current_location", columnList = "current_location"),
    @Index(name = "idx_rider_rating", columnList = "average_rating"),
    @Index(name = "idx_rider_city", columnList = "operating_city"),
    @Index(name = "idx_rider_vehicle_plate", columnList = "vehicle_plate")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierRider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId; // WOLTAXI user tablosundaki ID

    @Column(name = "rider_code", unique = true, nullable = false, length = 20)
    private String riderCode;

    // Kişisel Bilgiler
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "national_id", unique = true, nullable = false, length = 11)
    private String nationalId;

    @Column(name = "birth_date", nullable = false)
    private LocalDateTime birthDate;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    // Adres Bilgileri
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "operating_city", nullable = false, length = 100)
    private String operatingCity;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    // Durumlar
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RiderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false)
    private RiderAvailability availability;

    @Column(name = "is_online")
    private Boolean isOnline;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    // Konum Bilgileri
    @Column(name = "current_location", columnDefinition = "geometry(Point,4326)")
    private Point currentLocation;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "operating_radius_km", nullable = false)
    private Integer operatingRadiusKm;

    // Araç Bilgileri
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType; // MOTORCYCLE, SCOOTER, BICYCLE, CAR

    @Column(name = "vehicle_brand", length = 50)
    private String vehicleBrand;

    @Column(name = "vehicle_model", length = 50)
    private String vehicleModel;

    @Column(name = "vehicle_year")
    private Integer vehicleYear;

    @Column(name = "vehicle_plate", nullable = false, length = 20)
    private String vehiclePlate;

    @Column(name = "vehicle_color", length = 30)
    private String vehicleColor;

    @Column(name = "vehicle_photo_url", length = 500)
    private String vehiclePhotoUrl;

    @Column(name = "max_cargo_weight_kg", nullable = false)
    private BigDecimal maxCargoWeightKg;

    @Column(name = "max_cargo_volume_m3", nullable = false)
    private BigDecimal maxCargoVolumeM3;

    @Column(name = "has_insulated_bag")
    private Boolean hasInsulatedBag;

    @Column(name = "has_refrigerated_bag")
    private Boolean hasRefrigeratedBag;

    // Belgeler
    @Column(name = "driving_license_number", nullable = false, length = 20)
    private String drivingLicenseNumber;

    @Column(name = "driving_license_expiry")
    private LocalDateTime drivingLicenseExpiry;

    @Column(name = "driving_license_photo_url", length = 500)
    private String drivingLicensePhotoUrl;

    @Column(name = "vehicle_registration_url", length = 500)
    private String vehicleRegistrationUrl;

    @Column(name = "insurance_policy_url", length = 500)
    private String insurancePolicyUrl;

    @Column(name = "background_check_url", length = 500)
    private String backgroundCheckUrl;

    @Column(name = "health_certificate_url", length = 500)
    private String healthCertificateUrl;

    // Performans Metrikleri
    @Column(name = "total_deliveries", nullable = false)
    @Builder.Default
    private Long totalDeliveries = 0L;

    @Column(name = "successful_deliveries", nullable = false)
    @Builder.Default
    private Long successfulDeliveries = 0L;

    @Column(name = "cancelled_deliveries", nullable = false)
    @Builder.Default
    private Long cancelledDeliveries = 0L;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "total_ratings", nullable = false)
    @Builder.Default
    private Long totalRatings = 0L;

    @Column(name = "completion_rate", precision = 5, scale = 4)
    private BigDecimal completionRate;

    @Column(name = "average_delivery_time_minutes")
    private Integer averageDeliveryTimeMinutes;

    @Column(name = "on_time_delivery_rate", precision = 5, scale = 4)
    private BigDecimal onTimeDeliveryRate;

    // Finansal Bilgiler
    @Column(name = "total_earnings", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "current_month_earnings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal currentMonthEarnings = BigDecimal.ZERO;

    @Column(name = "pending_earnings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pendingEarnings = BigDecimal.ZERO;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate;

    // Çalışma Saatleri
    @Column(name = "work_start_time")
    private String workStartTime; // HH:MM format

    @Column(name = "work_end_time")
    private String workEndTime; // HH:MM format

    @Column(name = "works_weekends")
    private Boolean worksWeekends;

    @Column(name = "works_holidays")
    private Boolean worksHolidays;

    // Aktif Siparişler
    @OneToMany(mappedBy = "riderId", fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeliveryOrder> activeOrders = new ArrayList<>();

    @Column(name = "current_active_orders", nullable = false)
    @Builder.Default
    private Integer currentActiveOrders = 0;

    @Column(name = "max_concurrent_orders", nullable = false)
    @Builder.Default
    private Integer maxConcurrentOrders = 3;

    // Acil Durum Bilgileri
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;

    // Özel Yetenekler
    @Column(name = "can_handle_fragile")
    private Boolean canHandleFragile;

    @Column(name = "can_handle_food")
    private Boolean canHandleFood;

    @Column(name = "can_handle_medicine")
    private Boolean canHandleMedicine;

    @Column(name = "can_handle_valuable")
    private Boolean canHandleValuable;

    @Column(name = "speaks_languages", length = 200)
    private String speaksLanguages; // Comma separated: TR,EN,AR

    // Sistem Bilgileri
    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "registration_completed")
    private Boolean registrationCompleted;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Kurye Durumu Enum
     */
    public enum RiderStatus {
        PENDING,      // Başvuru beklemede
        APPROVED,     // Onaylandı
        ACTIVE,       // Aktif
        INACTIVE,     // Pasif
        SUSPENDED,    // Askıya alındı
        REJECTED,     // Reddedildi
        BANNED        // Yasaklandı
    }

    /**
     * Kurye Müsaitliği Enum
     */
    public enum RiderAvailability {
        AVAILABLE,       // Müsait
        BUSY,           // Meşgul
        ON_BREAK,       // Mola
        OFF_DUTY,       // Nöbet dışı
        IN_DELIVERY     // Teslimat yapıyor
    }

    /**
     * Kuryenin tam adını döndürür
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Kuryenin aktif olup olmadığını kontrol eder
     */
    public boolean isActive() {
        return status == RiderStatus.ACTIVE;
    }

    /**
     * Kuryenin müsait olup olmadığını kontrol eder
     */
    public boolean isAvailable() {
        return isActive() && 
               Boolean.TRUE.equals(isOnline) && 
               availability == RiderAvailability.AVAILABLE &&
               currentActiveOrders < maxConcurrentOrders;
    }

    /**
     * Kuryenin yeni sipariş alabileceğini kontrol eder
     */
    public boolean canTakeNewOrder() {
        return isAvailable() && currentActiveOrders < maxConcurrentOrders;
    }

    /**
     * Tamamlanma oranını hesaplar
     */
    public BigDecimal calculateCompletionRate() {
        if (totalDeliveries == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(successfulDeliveries)
                .divide(BigDecimal.valueOf(totalDeliveries), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Kuryenin belirtilen paketi taşıyıp taşıyamayacağını kontrol eder
     */
    public boolean canHandlePackage(BigDecimal weight, BigDecimal volume, PackageInfo.PackageType packageType) {
        if (weight.compareTo(maxCargoWeightKg) > 0 || volume.compareTo(maxCargoVolumeM3) > 0) {
            return false;
        }

        return switch (packageType) {
            case FOOD -> Boolean.TRUE.equals(canHandleFood);
            case MEDICINE -> Boolean.TRUE.equals(canHandleMedicine);
            case JEWELRY -> Boolean.TRUE.equals(canHandleValuable);
            default -> true;
        };
    }
}