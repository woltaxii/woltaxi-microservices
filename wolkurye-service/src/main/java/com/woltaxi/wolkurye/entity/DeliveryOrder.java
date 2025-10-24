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
 * Teslimat Siparişi Entity
 * 
 * Paket teslimat siparişlerini yönetir. Restaurant, market, pastane, çiçekçi gibi
 * işletmelerden alınacak ve müşterilere teslim edilecek paketlerin bilgilerini tutar.
 */
@Entity
@Table(name = "delivery_orders", indexes = {
    @Index(name = "idx_delivery_order_customer_id", columnList = "customer_id"),
    @Index(name = "idx_delivery_order_rider_id", columnList = "rider_id"),
    @Index(name = "idx_delivery_order_status", columnList = "status"),
    @Index(name = "idx_delivery_order_created_date", columnList = "created_date"),
    @Index(name = "idx_delivery_order_pickup_location", columnList = "pickup_location"),
    @Index(name = "idx_delivery_order_delivery_location", columnList = "delivery_location")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, length = 20)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "rider_id")
    private Long riderId;

    @Column(name = "rider_name", length = 100)
    private String riderName;

    @Column(name = "rider_phone", length = 20)
    private String riderPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private DeliveryPriority priority;

    // Alış Adresi Bilgileri
    @Column(name = "pickup_business_name", nullable = false, length = 200)
    private String pickupBusinessName;

    @Column(name = "pickup_business_type", nullable = false, length = 50)
    private String pickupBusinessType; // RESTAURANT, MARKET, BAKERY, FLORIST, PHARMACY, OTHER

    @Column(name = "pickup_contact_name", length = 100)
    private String pickupContactName;

    @Column(name = "pickup_contact_phone", length = 20)
    private String pickupContactPhone;

    @Column(name = "pickup_address", nullable = false, columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(name = "pickup_location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point pickupLocation;

    @Column(name = "pickup_district", length = 100)
    private String pickupDistrict;

    @Column(name = "pickup_city", length = 100)
    private String pickupCity;

    @Column(name = "pickup_postal_code", length = 10)
    private String pickupPostalCode;

    @Column(name = "pickup_instructions", columnDefinition = "TEXT")
    private String pickupInstructions;

    // Teslimat Adresi Bilgileri
    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point deliveryLocation;

    @Column(name = "delivery_district", length = 100)
    private String deliveryDistrict;

    @Column(name = "delivery_city", length = 100)
    private String deliveryCity;

    @Column(name = "delivery_postal_code", length = 10)
    private String deliveryPostalCode;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    @Column(name = "delivery_floor", length = 10)
    private String deliveryFloor;

    @Column(name = "delivery_apartment", length = 20)
    private String deliveryApartment;

    @Column(name = "delivery_building_name", length = 100)
    private String deliveryBuildingName;

    // Paket Bilgileri
    @OneToMany(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PackageInfo> packages = new ArrayList<>();

    @Column(name = "total_weight_kg", precision = 8, scale = 3)
    private BigDecimal totalWeightKg;

    @Column(name = "total_volume_m3", precision = 8, scale = 3)
    private BigDecimal totalVolumeM3;

    @Column(name = "package_count", nullable = false)
    private Integer packageCount;

    @Column(name = "fragile_items")
    private Boolean fragileItems;

    // Ödeme Bilgileri
    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType; // PREPAID, CASH_ON_DELIVERY, CARD_ON_DELIVERY

    @Column(name = "delivery_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal deliveryFee;

    @Column(name = "tip_amount", precision = 10, scale = 2)
    private BigDecimal tipAmount;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    // Zaman Bilgileri
    @Column(name = "requested_pickup_time")
    private LocalDateTime requestedPickupTime;

    @Column(name = "requested_delivery_time")
    private LocalDateTime requestedDeliveryTime;

    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_pickup_time")
    private LocalDateTime actualPickupTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "delivery_duration_minutes")
    private Integer deliveryDurationMinutes;

    // Takip Bilgileri
    @Column(name = "tracking_code", unique = true, length = 50)
    private String trackingCode;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    // Teslimat Onayı
    @Column(name = "delivery_proof_photo_url", length = 500)
    private String deliveryProofPhotoUrl;

    @Column(name = "recipient_signature_url", length = 500)
    private String recipientSignatureUrl;

    @Column(name = "delivery_confirmation_code", length = 10)
    private String deliveryConfirmationCode;

    // Değerlendirme
    @Column(name = "customer_rating")
    private Integer customerRating; // 1-5

    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;

    @Column(name = "rider_rating")
    private Integer riderRating; // 1-5

    @Column(name = "rider_feedback", columnDefinition = "TEXT")
    private String riderFeedback;

    // E-ticaret Entegrasyonu
    @Column(name = "external_order_id", length = 100)
    private String externalOrderId;

    @Column(name = "integration_platform", length = 50)
    private String integrationPlatform; // YEMEKSEPETI, GETIR, TRENDYOL, CUSTOM

    @Column(name = "business_integration_id", length = 100)
    private String businessIntegrationId;

    // İptal ve İade
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_by", length = 50)
    private String cancelledBy; // CUSTOMER, RIDER, BUSINESS, SYSTEM

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_processed")
    private Boolean refundProcessed;

    // Sistem Bilgileri
    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Teslimat Durumu Enum
     */
    public enum DeliveryStatus {
        PENDING,           // Beklemede
        ASSIGNED,          // Kuryeye atandı
        PICKUP_REQUESTED,  // Alım talebi
        EN_ROUTE_PICKUP,   // Alım noktasına gidiyor
        AT_PICKUP,         // Alım noktasında
        PICKED_UP,         // Alındı
        EN_ROUTE_DELIVERY, // Teslimat noktasına gidiyor
        AT_DELIVERY,       // Teslimat noktasında
        DELIVERED,         // Teslim edildi
        CANCELLED,         // İptal edildi
        RETURNED,          // İade edildi
        FAILED            // Başarısız
    }

    /**
     * Teslimat Türü Enum
     */
    public enum DeliveryType {
        STANDARD,    // Standart teslimat (2-4 saat)
        EXPRESS,     // Hızlı teslimat (30-60 dakika)
        SCHEDULED,   // Planlanmış teslimat
        SAME_DAY,    // Aynı gün teslimat
        NEXT_DAY     // Ertesi gün teslimat
    }

    /**
     * Teslimat Önceliği Enum
     */
    public enum DeliveryPriority {
        LOW,       // Düşük öncelik
        NORMAL,    // Normal öncelik
        HIGH,      // Yüksek öncelik
        URGENT     // Acil
    }

    /**
     * Toplam mesafeyi hesaplar (pickup'tan delivery'ye)
     */
    public Double calculateDistance() {
        if (pickupLocation != null && deliveryLocation != null) {
            return pickupLocation.distance(deliveryLocation) * 111.32; // Derece to KM
        }
        return 0.0;
    }

    /**
     * Siparişin aktif olup olmadığını kontrol eder
     */
    public boolean isActive() {
        return status != DeliveryStatus.DELIVERED && 
               status != DeliveryStatus.CANCELLED && 
               status != DeliveryStatus.RETURNED &&
               status != DeliveryStatus.FAILED;
    }

    /**
     * Siparişin tamamlanıp tamamlanmadığını kontrol eder
     */
    public boolean isCompleted() {
        return status == DeliveryStatus.DELIVERED;
    }

    /**
     * Siparişin iptal edilip edilmediğini kontrol eder
     */
    public boolean isCancelled() {
        return status == DeliveryStatus.CANCELLED;
    }
}