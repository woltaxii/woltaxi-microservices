package com.woltaxi.wolkurye.controller;

import com.woltaxi.wolkurye.dto.DeliveryOrderCreateDto;
import com.woltaxi.wolkurye.dto.DeliveryOrderDto;
import com.woltaxi.wolkurye.dto.DeliveryOrderUpdateDto;
import com.woltaxi.wolkurye.dto.DeliveryTrackingDto;
import com.woltaxi.wolkurye.entity.DeliveryOrder;
import com.woltaxi.wolkurye.service.DeliveryOrderService;
import com.woltaxi.wolkurye.service.DeliveryTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Teslimat Siparişi REST Controller
 * 
 * Bu controller, paket teslimat siparişlerinin yönetimi için REST API endpoints sağlar.
 * Sipariş oluşturma, güncelleme, takip, iptal ve teslim işlemlerini kapsar.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Delivery Orders", description = "Paket teslimat sipariş yönetimi")
public class DeliveryOrderController {

    private final DeliveryOrderService deliveryOrderService;
    private final DeliveryTrackingService deliveryTrackingService;

    /**
     * Yeni teslimat siparişi oluşturur
     */
    @PostMapping
    @Operation(summary = "Yeni teslimat siparişi oluştur", 
               description = "Restaurant, market, pastane gibi işletmelerden paket teslimat siparişi oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sipariş başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek verisi"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulama hatası"),
        @ApiResponse(responseCode = "422", description = "İş kuralları hatası")
    })
    public ResponseEntity<DeliveryOrderDto> createOrder(
            @Valid @RequestBody DeliveryOrderCreateDto createDto) {
        
        log.info("Yeni teslimat siparişi oluşturuluyor - Müşteri: {}, İşletme: {}", 
                createDto.getCustomerName(), createDto.getPickupBusinessName());
        
        DeliveryOrderDto orderDto = deliveryOrderService.createOrder(createDto);
        
        log.info("Teslimat siparişi oluşturuldu - Sipariş No: {}, Takip Kodu: {}", 
                orderDto.getOrderNumber(), orderDto.getTrackingCode());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

    /**
     * Sipariş detaylarını getirir
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Sipariş detaylarını getir", 
               description = "Belirtilen ID'ye sahip siparişin tüm detaylarını getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sipariş detayları başarıyla getirildi"),
        @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
        @ApiResponse(responseCode = "403", description = "Bu siparişe erişim izniniz yok")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('RIDER')")
    public ResponseEntity<DeliveryOrderDto> getOrder(
            @Parameter(description = "Sipariş ID") @PathVariable Long orderId) {
        
        log.debug("Sipariş detayları getiriliyor - ID: {}", orderId);
        
        DeliveryOrderDto orderDto = deliveryOrderService.getOrderById(orderId);
        return ResponseEntity.ok(orderDto);
    }

    /**
     * Takip kodu ile sipariş sorgular
     */
    @GetMapping("/track/{trackingCode}")
    @Operation(summary = "Takip kodu ile sipariş sorgula", 
               description = "Takip kodu kullanarak sipariş durumunu ve detaylarını getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Takip bilgileri başarıyla getirildi"),
        @ApiResponse(responseCode = "404", description = "Takip kodu bulunamadı")
    })
    public ResponseEntity<DeliveryTrackingDto> trackOrder(
            @Parameter(description = "Takip kodu") @PathVariable String trackingCode) {
        
        log.debug("Sipariş takip ediliyor - Takip Kodu: {}", trackingCode);
        
        DeliveryTrackingDto trackingDto = deliveryTrackingService.trackByCode(trackingCode);
        return ResponseEntity.ok(trackingDto);
    }

    /**
     * Müşterinin siparişlerini listeler
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Müşteri siparişlerini listele", 
               description = "Belirtilen müşteriye ait tüm siparişleri sayfalı olarak listeler")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId == authentication.principal.id)")
    public ResponseEntity<Page<DeliveryOrderDto>> getCustomerOrders(
            @Parameter(description = "Müşteri ID") @PathVariable Long customerId,
            @Parameter(description = "Sayfalama bilgileri") @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Müşteri siparişleri getiriliyor - Müşteri ID: {}", customerId);
        
        Page<DeliveryOrderDto> orders = deliveryOrderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Kuryenin aktif siparişlerini listeler
     */
    @GetMapping("/rider/{riderId}/active")
    @Operation(summary = "Kurye aktif siparişlerini listele", 
               description = "Belirtilen kuryeye atanmış aktif siparişleri listeler")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RIDER')")
    public ResponseEntity<List<DeliveryOrderDto>> getRiderActiveOrders(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId) {
        
        log.debug("Kurye aktif siparişleri getiriliyor - Kurye ID: {}", riderId);
        
        List<DeliveryOrderDto> orders = deliveryOrderService.getRiderActiveOrders(riderId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Şehir bazında aktif siparişleri listeler
     */
    @GetMapping("/city/{city}/active")
    @Operation(summary = "Şehir bazında aktif siparişleri listele", 
               description = "Belirtilen şehirdeki tüm aktif siparişleri listeler")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Page<DeliveryOrderDto>> getCityActiveOrders(
            @Parameter(description = "Şehir adı") @PathVariable String city,
            @PageableDefault(size = 50) Pageable pageable) {
        
        log.debug("Şehir aktif siparişleri getiriliyor - Şehir: {}", city);
        
        Page<DeliveryOrderDto> orders = deliveryOrderService.getActiveByCityOrders(city, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Sipariş durumunu günceller
     */
    @PutMapping("/{orderId}/status")
    @Operation(summary = "Sipariş durumunu güncelle", 
               description = "Siparişin durumunu ve ilgili bilgileri günceller")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RIDER')")
    public ResponseEntity<DeliveryOrderDto> updateOrderStatus(
            @Parameter(description = "Sipariş ID") @PathVariable Long orderId,
            @Valid @RequestBody DeliveryOrderUpdateDto updateDto) {
        
        log.info("Sipariş durumu güncelleniyor - ID: {}, Yeni Durum: {}", 
                orderId, updateDto.getStatus());
        
        DeliveryOrderDto orderDto = deliveryOrderService.updateOrderStatus(orderId, updateDto);
        
        log.info("Sipariş durumu güncellendi - ID: {}, Durum: {}", 
                orderId, orderDto.getStatus());
        
        return ResponseEntity.ok(orderDto);
    }

    /**
     * Siparişe kurye atar
     */
    @PutMapping("/{orderId}/assign-rider/{riderId}")
    @Operation(summary = "Siparişe kurye ata", 
               description = "Belirtilen siparişi belirtilen kuryeye atar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<DeliveryOrderDto> assignRider(
            @Parameter(description = "Sipariş ID") @PathVariable Long orderId,
            @Parameter(description = "Kurye ID") @PathVariable Long riderId) {
        
        log.info("Siparişe kurye atanıyor - Sipariş ID: {}, Kurye ID: {}", orderId, riderId);
        
        DeliveryOrderDto orderDto = deliveryOrderService.assignRider(orderId, riderId);
        
        log.info("Kurye atandı - Sipariş: {}, Kurye: {}", 
                orderDto.getOrderNumber(), orderDto.getRiderName());
        
        return ResponseEntity.ok(orderDto);
    }

    /**
     * Otomatik kurye ataması yapar
     */
    @PutMapping("/{orderId}/auto-assign")
    @Operation(summary = "Otomatik kurye ataması", 
               description = "Sipariş için en uygun kuryeyi otomatik olarak atar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<DeliveryOrderDto> autoAssignRider(
            @Parameter(description = "Sipariş ID") @PathVariable Long orderId) {
        
        log.info("Otomatik kurye ataması yapılıyor - Sipariş ID: {}", orderId);
        
        DeliveryOrderDto orderDto = deliveryOrderService.autoAssignRider(orderId);
        
        if (orderDto.getRiderId() != null) {
            log.info("Otomatik kurye ataması başarılı - Sipariş: {}, Kurye: {}", 
                    orderDto.getOrderNumber(), orderDto.getRiderName());
        } else {
            log.warn("Otomatik kurye ataması başarısız - Uygun kurye bulunamadı - Sipariş ID: {}", orderId);
        }
        
        return ResponseEntity.ok(orderDto);
    }

    /**
     * Teslimat fotoğrafı yükler
     */
    @PostMapping("/{orderId}/delivery-proof")
    @Operation(summary = "Teslimat kanıtı fotoğrafı yükle", 
               description = "Teslimat tamamlandığında kanıt fotoğrafı yükler")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RIDER')")
    public ResponseEntity<Map<String, String>> uploadDeliveryProof(
            @Parameter(description = "Sipariş ID") @PathVariable Long orderId,
            @Parameter(description = "Teslimat fotoğrafı") @RequestParam("photo") MultipartFile photo) {
        
        log.info("Teslimat kanıt fotoğrafı yükleniyor - Sipariş ID: {}", orderId);
        
        String photoUrl = deliveryOrderService.uploadDeliveryProof(orderId, photo);
        
        log.info("Teslimat kanıt fotoğrafı yüklendi - Sipariş ID: {}, URL: {}", orderId, photoUrl);
        
        return ResponseEntity.ok(Map.of("photoUrl", photoUrl));
    }

    /**
     * Siparişi iptal eder
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Siparişi iptal et", 
               description = "Belirtilen siparişi iptal eder ve gerekli işlemleri yapar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<DeliveryOrderDto> cancelOrder(
            @Parameter(description = "Sipariş ID") @PathVariable Long orderId,
            @Parameter(description = "İptal nedeni") @RequestParam(required = false) String reason) {
        
        log.info("Sipariş iptal ediliyor - ID: {}, Neden: {}", orderId, reason);
        
        DeliveryOrderDto orderDto = deliveryOrderService.cancelOrder(orderId, reason);
        
        log.info("Sipariş iptal edildi - Sipariş No: {}", orderDto.getOrderNumber());
        
        return ResponseEntity.ok(orderDto);
    }

    /**
     * Toplu sipariş arama
     */
    @GetMapping("/search")
    @Operation(summary = "Sipariş arama", 
               description = "Çeşitli kriterlere göre sipariş arar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Page<DeliveryOrderDto>> searchOrders(
            @Parameter(description = "Sipariş numarası") @RequestParam(required = false) String orderNumber,
            @Parameter(description = "Takip kodu") @RequestParam(required = false) String trackingCode,
            @Parameter(description = "Müşteri telefonu") @RequestParam(required = false) String customerPhone,
            @Parameter(description = "Kurye ID") @RequestParam(required = false) Long riderId,
            @Parameter(description = "Sipariş durumu") @RequestParam(required = false) DeliveryOrder.DeliveryStatus status,
            @Parameter(description = "Şehir") @RequestParam(required = false) String city,
            @Parameter(description = "İşletme türü") @RequestParam(required = false) String businessType,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Sipariş arama yapılıyor - Kriterler: orderNumber={}, trackingCode={}, status={}", 
                orderNumber, trackingCode, status);
        
        Page<DeliveryOrderDto> orders = deliveryOrderService.searchOrders(
                orderNumber, trackingCode, customerPhone, riderId, status, city, businessType, pageable);
        
        return ResponseEntity.ok(orders);
    }

    /**
     * Sipariş istatistikleri
     */
    @GetMapping("/statistics")
    @Operation(summary = "Sipariş istatistikleri", 
               description = "Genel sipariş istatistiklerini getirir")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getOrderStatistics(
            @Parameter(description = "Başlangıç tarihi (YYYY-MM-DD)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi (YYYY-MM-DD)") @RequestParam(required = false) String endDate,
            @Parameter(description = "Şehir filtresi") @RequestParam(required = false) String city) {
        
        log.debug("Sipariş istatistikleri getiriliyor - Tarih: {} - {}, Şehir: {}", startDate, endDate, city);
        
        Map<String, Object> statistics = deliveryOrderService.getOrderStatistics(startDate, endDate, city);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Günlük sipariş raporu
     */
    @GetMapping("/reports/daily")
    @Operation(summary = "Günlük sipariş raporu", 
               description = "Belirtilen tarihteki günlük sipariş raporunu getirir")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getDailyReport(
            @Parameter(description = "Rapor tarihi (YYYY-MM-DD)") @RequestParam String date,
            @Parameter(description = "Şehir filtresi") @RequestParam(required = false) String city) {
        
        log.info("Günlük sipariş raporu getiriliyor - Tarih: {}, Şehir: {}", date, city);
        
        Map<String, Object> report = deliveryOrderService.getDailyReport(date, city);
        return ResponseEntity.ok(report);
    }

    /**
     * Fiyat hesaplama
     */
    @PostMapping("/calculate-price")
    @Operation(summary = "Teslimat fiyatı hesapla", 
               description = "Verilen parametrelere göre teslimat ücretini hesaplar")
    public ResponseEntity<Map<String, BigDecimal>> calculatePrice(
            @Parameter(description = "Alış konumu latitude") @RequestParam @NotNull Double pickupLat,
            @Parameter(description = "Alış konumu longitude") @RequestParam @NotNull Double pickupLng,
            @Parameter(description = "Teslimat konumu latitude") @RequestParam @NotNull Double deliveryLat,
            @Parameter(description = "Teslimat konumu longitude") @RequestParam @NotNull Double deliveryLng,
            @Parameter(description = "Teslimat türü") @RequestParam(required = false, defaultValue = "STANDARD") 
                DeliveryOrder.DeliveryType deliveryType,
            @Parameter(description = "Paket ağırlığı (kg)") @RequestParam(required = false, defaultValue = "1.0") 
                BigDecimal weight) {
        
        log.debug("Teslimat fiyatı hesaplanıyor - Pickup: [{}, {}], Delivery: [{}, {}], Tür: {}", 
                pickupLat, pickupLng, deliveryLat, deliveryLng, deliveryType);
        
        Map<String, BigDecimal> pricing = deliveryOrderService.calculateDeliveryPrice(
                pickupLat, pickupLng, deliveryLat, deliveryLng, deliveryType, weight);
        
        return ResponseEntity.ok(pricing);
    }
}