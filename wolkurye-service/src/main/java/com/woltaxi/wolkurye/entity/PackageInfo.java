package com.woltaxi.wolkurye.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Paket Bilgisi Entity
 * 
 * Her teslimat siparişindeki paketlerin detaylarını tutar.
 * Bir siparişte birden fazla paket olabilir.
 */
@Entity
@Table(name = "package_info", indexes = {
    @Index(name = "idx_package_delivery_order_id", columnList = "delivery_order_id"),
    @Index(name = "idx_package_barcode", columnList = "barcode"),
    @Index(name = "idx_package_type", columnList = "package_type")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_order_id", nullable = false)
    private DeliveryOrder deliveryOrder;

    @Column(name = "package_number", nullable = false, length = 20)
    private String packageNumber;

    @Column(name = "barcode", unique = true, length = 50)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false)
    private PackageType packageType;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "weight_kg", precision = 8, scale = 3, nullable = false)
    private BigDecimal weightKg;

    @Column(name = "length_cm", precision = 8, scale = 2)
    private BigDecimal lengthCm;

    @Column(name = "width_cm", precision = 8, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", precision = 8, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "volume_m3", precision = 8, scale = 3)
    private BigDecimal volumeM3;

    @Column(name = "value_amount", precision = 10, scale = 2)
    private BigDecimal valueAmount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "fragile")
    private Boolean fragile;

    @Column(name = "perishable")
    private Boolean perishable;

    @Column(name = "temperature_sensitive")
    private Boolean temperatureSensitive;

    @Column(name = "min_temperature_celsius")
    private Integer minTemperatureCelsius;

    @Column(name = "max_temperature_celsius")
    private Integer maxTemperatureCelsius;

    @Column(name = "special_handling_instructions", columnDefinition = "TEXT")
    private String specialHandlingInstructions;

    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "pickup_photo_url", length = 500)
    private String pickupPhotoUrl;

    @Column(name = "delivery_photo_url", length = 500)
    private String deliveryPhotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PackageStatus status;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "damaged")
    private Boolean damaged;

    @Column(name = "damage_description", columnDefinition = "TEXT")
    private String damageDescription;

    @Column(name = "damage_photo_url", length = 500)
    private String damagePhotoUrl;

    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    /**
     * Paket Türü Enum
     */
    public enum PackageType {
        FOOD,          // Yiyecek
        BEVERAGE,      // İçecek
        GROCERY,       // Market alışverişi
        MEDICINE,      // İlaç
        FLOWERS,       // Çiçek
        BAKERY,        // Pastane ürünleri
        ELECTRONICS,   // Elektronik
        CLOTHING,      // Giyim
        BOOKS,         // Kitap
        DOCUMENTS,     // Belgeler
        JEWELRY,       // Mücevher
        COSMETICS,     // Kozmetik
        TOYS,          // Oyuncak
        FURNITURE,     // Mobilya (küçük)
        OTHER          // Diğer
    }

    /**
     * Paket Durumu Enum
     */
    public enum PackageStatus {
        PENDING,       // Beklemede
        PICKED_UP,     // Alındı
        IN_TRANSIT,    // Yolda
        DELIVERED,     // Teslim edildi
        DAMAGED,       // Hasarlı
        LOST,          // Kayıp
        RETURNED       // İade edildi
    }

    /**
     * Paketin hacmini hesaplar (cm³ to m³)
     */
    public BigDecimal calculateVolume() {
        if (lengthCm != null && widthCm != null && heightCm != null) {
            BigDecimal volumeCm3 = lengthCm.multiply(widthCm).multiply(heightCm);
            return volumeCm3.divide(BigDecimal.valueOf(1000000), 6, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Paketin özel bakım gerektirip gerektirmediğini kontrol eder
     */
    public boolean requiresSpecialHandling() {
        return Boolean.TRUE.equals(fragile) || 
               Boolean.TRUE.equals(perishable) || 
               Boolean.TRUE.equals(temperatureSensitive);
    }

    /**
     * Paketin teslim edilip edilmediğini kontrol eder
     */
    public boolean isDelivered() {
        return status == PackageStatus.DELIVERED;
    }

    /**
     * Paketin hasarlı olup olmadığını kontrol eder
     */
    public boolean isDamaged() {
        return Boolean.TRUE.equals(damaged) || status == PackageStatus.DAMAGED;
    }
}