package com.woltaxi.driver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Driver Entity - Sürücü Varlık Sınıfı
 * 
 * WOLTAXI platformundaki sürücülerin tüm bilgilerini tutan ana entity.
 * Bu sınıf sürücü kayıt, onay, performans ve kazanç verilerini yönetir.
 */
@Entity
@Table(name = "drivers", indexes = {
    @Index(name = "idx_driver_phone", columnList = "phone"),
    @Index(name = "idx_driver_license", columnList = "license_number"),
    @Index(name = "idx_driver_status", columnList = "status"),
    @Index(name = "idx_driver_city", columnList = "city")
})
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kişisel Bilgiler
    @NotBlank(message = "Ad alanı boş olamaz")
    @Size(min = 2, max = 50, message = "Ad 2-50 karakter arasında olmalıdır")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Soyad alanı boş olamaz")
    @Size(min = 2, max = 50, message = "Soyad 2-50 karakter arasında olmalıdır")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Telefon numarası boş olamaz")
    @Pattern(regexp = "^\\+90[0-9]{10}$", message = "Geçersiz telefon numarası formatı")
    @Column(name = "phone", nullable = false, unique = true, length = 13)
    private String phone;

    @Email(message = "Geçersiz email formatı")
    @Column(name = "email", length = 100)
    private String email;

    @NotBlank(message = "TC Kimlik numarası boş olamaz")
    @Pattern(regexp = "^[0-9]{11}$", message = "TC Kimlik numarası 11 haneli olmalıdır")
    @Column(name = "national_id", nullable = false, unique = true, length = 11)
    private String nationalId;

    // Sürücü Belgesi Bilgileri
    @NotBlank(message = "Ehliyet numarası boş olamaz")
    @Column(name = "license_number", nullable = false, unique = true, length = 20)
    private String licenseNumber;

    @NotNull(message = "Ehliyet tarihi boş olamaz")
    @Column(name = "license_date", nullable = false)
    private LocalDateTime licenseDate;

    @Column(name = "license_class", length = 10)
    private String licenseClass; // B, C, D vb.

    // Araç Bilgileri
    @NotBlank(message = "Plaka numarası boş olamaz")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{1,3}[0-9]{1,4}$", message = "Geçersiz plaka formatı")
    @Column(name = "vehicle_plate", nullable = false, unique = true, length = 10)
    private String vehiclePlate;

    @NotBlank(message = "Araç markası boş olamaz")
    @Column(name = "vehicle_brand", nullable = false, length = 30)
    private String vehicleBrand;

    @NotBlank(message = "Araç modeli boş olamaz")
    @Column(name = "vehicle_model", nullable = false, length = 30)
    private String vehicleModel;

    @NotNull(message = "Araç yılı boş olamaz")
    @Min(value = 2010, message = "Araç 2010 yılından yeni olmalıdır")
    @Column(name = "vehicle_year", nullable = false)
    private Integer vehicleYear;

    @NotBlank(message = "Araç rengi boş olamaz")
    @Column(name = "vehicle_color", nullable = false, length = 20)
    private String vehicleColor;

    // Konum Bilgileri
    @NotBlank(message = "Şehir bilgisi boş olamaz")
    @Column(name = "city", nullable = false, length = 30)
    private String city;

    @Column(name = "district", length = 30)
    private String district;

    @Column(name = "current_latitude", precision = 10, scale = 8)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 11, scale = 8)
    private BigDecimal currentLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    // Durum Bilgileri
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DriverStatus status = DriverStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false)
    private DriverAvailability availability = DriverAvailability.OFFLINE;

    // Performans Metrikleri
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(5.0);

    @Column(name = "total_rides", nullable = false)
    private Long totalRides = 0L;

    @Column(name = "completed_rides", nullable = false)
    private Long completedRides = 0L;

    @Column(name = "cancelled_rides", nullable = false)
    private Long cancelledRides = 0L;

    @Column(name = "total_earnings", precision = 10, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "this_month_earnings", precision = 10, scale = 2)
    private BigDecimal thisMonthEarnings = BigDecimal.ZERO;

    // Güvenlik ve Doğrulama
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "background_check_status")
    @Enumerated(EnumType.STRING)
    private BackgroundCheckStatus backgroundCheckStatus = BackgroundCheckStatus.PENDING;

    // Profil Fotoğrafı
    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(name = "license_photo_url", length = 500)
    private String licensePhotoUrl;

    @Column(name = "vehicle_photo_url", length = 500)
    private String vehiclePhotoUrl;

    // Tarih Alanları
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    // Constructors
    public Driver() {}

    public Driver(String firstName, String lastName, String phone, String nationalId, 
                 String licenseNumber, String vehiclePlate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.nationalId = nationalId;
        this.licenseNumber = licenseNumber;
        this.vehiclePlate = vehiclePlate;
    }

    // Enum Definitions
    public enum DriverStatus {
        PENDING,        // Onay bekliyor
        APPROVED,       // Onaylandı
        REJECTED,       // Reddedildi
        SUSPENDED,      // Askıya alındı
        DEACTIVATED     // Deaktif
    }

    public enum DriverAvailability {
        ONLINE,         // Aktif, müsait
        BUSY,           // Yolculukta
        OFFLINE,        // Çevrimdışı
        BREAK           // Molada
    }

    public enum BackgroundCheckStatus {
        PENDING,        // Beklemede
        APPROVED,       // Onaylandı
        REJECTED,       // Reddedildi
        EXPIRED         // Süresi doldu
    }

    // Helper Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAvailableForRide() {
        return status == DriverStatus.APPROVED && 
               availability == DriverAvailability.ONLINE &&
               isVerified == true;
    }

    public BigDecimal getCompletionRate() {
        if (totalRides == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(completedRides)
                .divide(BigDecimal.valueOf(totalRides), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public LocalDateTime getLicenseDate() { return licenseDate; }
    public void setLicenseDate(LocalDateTime licenseDate) { this.licenseDate = licenseDate; }

    public String getLicenseClass() { return licenseClass; }
    public void setLicenseClass(String licenseClass) { this.licenseClass = licenseClass; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public String getVehicleBrand() { return vehicleBrand; }
    public void setVehicleBrand(String vehicleBrand) { this.vehicleBrand = vehicleBrand; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public Integer getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(Integer vehicleYear) { this.vehicleYear = vehicleYear; }

    public String getVehicleColor() { return vehicleColor; }
    public void setVehicleColor(String vehicleColor) { this.vehicleColor = vehicleColor; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public BigDecimal getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(BigDecimal currentLatitude) { this.currentLatitude = currentLatitude; }

    public BigDecimal getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(BigDecimal currentLongitude) { this.currentLongitude = currentLongitude; }

    public LocalDateTime getLastLocationUpdate() { return lastLocationUpdate; }
    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }

    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }

    public DriverAvailability getAvailability() { return availability; }
    public void setAvailability(DriverAvailability availability) { this.availability = availability; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Long getTotalRides() { return totalRides; }
    public void setTotalRides(Long totalRides) { this.totalRides = totalRides; }

    public Long getCompletedRides() { return completedRides; }
    public void setCompletedRides(Long completedRides) { this.completedRides = completedRides; }

    public Long getCancelledRides() { return cancelledRides; }
    public void setCancelledRides(Long cancelledRides) { this.cancelledRides = cancelledRides; }

    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }

    public BigDecimal getThisMonthEarnings() { return thisMonthEarnings; }
    public void setThisMonthEarnings(BigDecimal thisMonthEarnings) { this.thisMonthEarnings = thisMonthEarnings; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public LocalDateTime getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; }

    public BackgroundCheckStatus getBackgroundCheckStatus() { return backgroundCheckStatus; }
    public void setBackgroundCheckStatus(BackgroundCheckStatus backgroundCheckStatus) { this.backgroundCheckStatus = backgroundCheckStatus; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getLicensePhotoUrl() { return licensePhotoUrl; }
    public void setLicensePhotoUrl(String licensePhotoUrl) { this.licensePhotoUrl = licensePhotoUrl; }

    public String getVehiclePhotoUrl() { return vehiclePhotoUrl; }
    public void setVehiclePhotoUrl(String vehiclePhotoUrl) { this.vehiclePhotoUrl = vehiclePhotoUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }
}