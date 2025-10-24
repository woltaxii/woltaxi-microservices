package com.woltaxi.wolkurye.controller;

import com.woltaxi.wolkurye.dto.CourierRiderCreateDto;
import com.woltaxi.wolkurye.dto.CourierRiderDto;
import com.woltaxi.wolkurye.dto.CourierRiderUpdateDto;
import com.woltaxi.wolkurye.entity.CourierRider;
import com.woltaxi.wolkurye.service.CourierRiderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * Kurye Motosikletçi REST Controller
 * 
 * Bu controller, WOLTAXI ekosistemindeki motor kuryelerinin yönetimi için REST API endpoints sağlar.
 * Kurye kaydı, onay, konum takibi, performans yönetimi ve sipariş atama işlemlerini kapsar.
 */
@RestController
@RequestMapping("/riders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Courier Riders", description = "Motor kurye yönetimi")
public class CourierRiderController {

    private final CourierRiderService courierRiderService;

    /**
     * Yeni kurye kaydı oluşturur
     */
    @PostMapping
    @Operation(summary = "Yeni kurye kaydı oluştur", 
               description = "WOLTAXI sistemine yeni motor kurye kaydı oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Kurye kaydı başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek verisi"),
        @ApiResponse(responseCode = "409", description = "Kurye zaten mevcut"),
        @ApiResponse(responseCode = "422", description = "İş kuralları hatası")
    })
    public ResponseEntity<CourierRiderDto> createRider(
            @Valid @RequestBody CourierRiderCreateDto createDto) {
        
        log.info("Yeni kurye kaydı oluşturuluyor - Ad: {} {}, Telefon: {}", 
                createDto.getFirstName(), createDto.getLastName(), createDto.getPhone());
        
        CourierRiderDto riderDto = courierRiderService.createRider(createDto);
        
        log.info("Kurye kaydı oluşturuldu - Kurye Kodu: {}, ID: {}", 
                riderDto.getRiderCode(), riderDto.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(riderDto);
    }

    /**
     * Kurye detaylarını getirir
     */
    @GetMapping("/{riderId}")
    @Operation(summary = "Kurye detaylarını getir", 
               description = "Belirtilen ID'ye sahip kuryenin tüm detaylarını getirir")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RIDER') or hasRole('DISPATCHER')")
    public ResponseEntity<CourierRiderDto> getRider(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId) {
        
        log.debug("Kurye detayları getiriliyor - ID: {}", riderId);
        
        CourierRiderDto riderDto = courierRiderService.getRiderById(riderId);
        return ResponseEntity.ok(riderDto);
    }

    /**
     * Kurye koduna göre kurye getirir
     */
    @GetMapping("/code/{riderCode}")
    @Operation(summary = "Kurye koduna göre kurye getir", 
               description = "Kurye kodu kullanarak kurye bilgilerini getirir")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<CourierRiderDto> getRiderByCode(
            @Parameter(description = "Kurye kodu") @PathVariable String riderCode) {
        
        log.debug("Kurye detayları getiriliyor - Kurye Kodu: {}", riderCode);
        
        CourierRiderDto riderDto = courierRiderService.getRiderByCode(riderCode);
        return ResponseEntity.ok(riderDto);
    }

    /**
     * Şehir bazında aktif kuryeler
     */
    @GetMapping("/city/{city}/active")
    @Operation(summary = "Şehirdeki aktif kuryeler", 
               description = "Belirtilen şehirdeki aktif kuryeler listeler")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<List<CourierRiderDto>> getActiveByCityRiders(
            @Parameter(description = "Şehir adı") @PathVariable String city) {
        
        log.debug("Şehir aktif kuryeler getiriliyor - Şehir: {}", city);
        
        List<CourierRiderDto> riders = courierRiderService.getActiveRidersByCity(city);
        return ResponseEntity.ok(riders);
    }

    /**
     * Müsait kuryeler listesi
     */
    @GetMapping("/available")
    @Operation(summary = "Müsait kuryeler", 
               description = "Yeni sipariş alabilecek müsait kuryeler listeler")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<List<CourierRiderDto>> getAvailableRiders(
            @Parameter(description = "Şehir filtresi") @RequestParam(required = false) String city,
            @Parameter(description = "Maksimum mesafe (km)") @RequestParam(required = false, defaultValue = "10") Integer maxDistanceKm,
            @Parameter(description = "Pickup latitude") @RequestParam(required = false) Double pickupLat,
            @Parameter(description = "Pickup longitude") @RequestParam(required = false) Double pickupLng) {
        
        log.debug("Müsait kuryeler getiriliyor - Şehir: {}, Mesafe: {} km", city, maxDistanceKm);
        
        List<CourierRiderDto> riders = courierRiderService.getAvailableRiders(city, maxDistanceKm, pickupLat, pickupLng);
        return ResponseEntity.ok(riders);
    }

    /**
     * En yakın kuryeyi bulur
     */
    @GetMapping("/nearest")
    @Operation(summary = "En yakın kuryeyi bul", 
               description = "Belirtilen konuma en yakın müsait kuryeyi bulur")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<CourierRiderDto> getNearestRider(
            @Parameter(description = "Latitude") @RequestParam Double latitude,
            @Parameter(description = "Longitude") @RequestParam Double longitude,
            @Parameter(description = "Maksimum mesafe (km)") @RequestParam(required = false, defaultValue = "15") Integer maxDistanceKm) {
        
        log.debug("En yakın kurye aranıyor - Konum: [{}, {}], Max Mesafe: {} km", 
                latitude, longitude, maxDistanceKm);
        
        CourierRiderDto rider = courierRiderService.getNearestAvailableRider(latitude, longitude, maxDistanceKm);
        
        if (rider != null) {
            log.info("En yakın kurye bulundu - Kurye: {}, Mesafe: {} km", 
                    rider.getFullName(), rider.getDistanceFromLocation());
        } else {
            log.warn("Belirtilen konumda müsait kurye bulunamadı - Konum: [{}, {}]", latitude, longitude);
        }
        
        return ResponseEntity.ok(rider);
    }

    /**
     * Kurye bilgilerini günceller
     */
    @PutMapping("/{riderId}")
    @Operation(summary = "Kurye bilgilerini güncelle", 
               description = "Kuryenin kişisel ve çalışma bilgilerini günceller")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RIDER') and #riderId == authentication.principal.riderId)")
    public ResponseEntity<CourierRiderDto> updateRider(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Valid @RequestBody CourierRiderUpdateDto updateDto) {
        
        log.info("Kurye bilgileri güncelleniyor - ID: {}", riderId);
        
        CourierRiderDto riderDto = courierRiderService.updateRider(riderId, updateDto);
        
        log.info("Kurye bilgileri güncellendi - Kurye: {}", riderDto.getFullName());
        
        return ResponseEntity.ok(riderDto);
    }

    /**
     * Kurye durumunu değiştirir
     */
    @PutMapping("/{riderId}/status")
    @Operation(summary = "Kurye durumunu değiştir", 
               description = "Kuryenin sistem durumunu değiştirir (aktif, pasif, askıya alma)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourierRiderDto> updateRiderStatus(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Parameter(description = "Yeni durum") @RequestParam CourierRider.RiderStatus status,
            @Parameter(description = "Durum değişiklik nedeni") @RequestParam(required = false) String reason) {
        
        log.info("Kurye durumu değiştiriliyor - ID: {}, Yeni Durum: {}, Neden: {}", 
                riderId, status, reason);
        
        CourierRiderDto riderDto = courierRiderService.updateRiderStatus(riderId, status, reason);
        
        log.info("Kurye durumu değiştirildi - Kurye: {}, Durum: {}", 
                riderDto.getFullName(), riderDto.getStatus());
        
        return ResponseEntity.ok(riderDto);
    }

    /**
     * Kurye müsaitliğini değiştirir
     */
    @PutMapping("/{riderId}/availability")
    @Operation(summary = "Kurye müsaitliğini değiştir", 
               description = "Kuryenin çalışma müsaitliğini değiştirir")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RIDER') and #riderId == authentication.principal.riderId)")
    public ResponseEntity<CourierRiderDto> updateRiderAvailability(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Parameter(description = "Müsaitlik durumu") @RequestParam CourierRider.RiderAvailability availability) {
        
        log.info("Kurye müsaitliği değiştiriliyor - ID: {}, Yeni Durum: {}", riderId, availability);
        
        CourierRiderDto riderDto = courierRiderService.updateRiderAvailability(riderId, availability);
        
        log.info("Kurye müsaitliği değiştirildi - Kurye: {}, Müsaitlik: {}", 
                riderDto.getFullName(), riderDto.getAvailability());
        
        return ResponseEntity.ok(riderDto);
    }

    /**
     * Kurye konumunu günceller
     */
    @PutMapping("/{riderId}/location")
    @Operation(summary = "Kurye konumunu güncelle", 
               description = "Kuryenin güncel GPS konumunu günceller")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RIDER') and #riderId == authentication.principal.riderId)")
    public ResponseEntity<Void> updateRiderLocation(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Parameter(description = "Latitude") @RequestParam Double latitude,
            @Parameter(description = "Longitude") @RequestParam Double longitude) {
        
        log.debug("Kurye konumu güncelleniyor - ID: {}, Konum: [{}, {}]", riderId, latitude, longitude);
        
        courierRiderService.updateRiderLocation(riderId, latitude, longitude);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Kurye çevrimiçi durumunu değiştirir
     */
    @PutMapping("/{riderId}/online")
    @Operation(summary = "Kurye çevrimiçi durumunu değiştir", 
               description = "Kuryenin çevrimiçi/çevrimdışı durumunu değiştirir")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RIDER') and #riderId == authentication.principal.riderId)")
    public ResponseEntity<CourierRiderDto> updateOnlineStatus(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Parameter(description = "Çevrimiçi durumu") @RequestParam Boolean isOnline) {
        
        log.info("Kurye çevrimiçi durumu değiştiriliyor - ID: {}, Çevrimiçi: {}", riderId, isOnline);
        
        CourierRiderDto riderDto = courierRiderService.updateOnlineStatus(riderId, isOnline);
        
        log.info("Kurye çevrimiçi durumu değiştirildi - Kurye: {}, Durum: {}", 
                riderDto.getFullName(), riderDto.getIsOnline() ? "Çevrimiçi" : "Çevrimdışı");
        
        return ResponseEntity.ok(riderDto);
    }

    /**
     * Kurye belgelerini yükler
     */
    @PostMapping("/{riderId}/documents")
    @Operation(summary = "Kurye belgelerini yükle", 
               description = "Ehliyet, ruhsat, sigorta gibi kurye belgelerini yükler")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RIDER') and #riderId == authentication.principal.riderId)")
    public ResponseEntity<Map<String, String>> uploadRiderDocuments(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Parameter(description = "Ehliyet fotoğrafı") @RequestParam(required = false) MultipartFile drivingLicense,
            @Parameter(description = "Araç ruhsatı") @RequestParam(required = false) MultipartFile vehicleRegistration,
            @Parameter(description = "Sigorta poliçesi") @RequestParam(required = false) MultipartFile insurancePolicy,
            @Parameter(description = "Sabıka kaydı") @RequestParam(required = false) MultipartFile backgroundCheck) {
        
        log.info("Kurye belgeleri yükleniyor - ID: {}", riderId);
        
        Map<String, String> documentUrls = courierRiderService.uploadRiderDocuments(
                riderId, drivingLicense, vehicleRegistration, insurancePolicy, backgroundCheck);
        
        log.info("Kurye belgeleri yüklendi - ID: {}, Belgeler: {}", riderId, documentUrls.keySet());
        
        return ResponseEntity.ok(documentUrls);
    }

    /**
     * Kurye başvurusunu onaylar
     */
    @PutMapping("/{riderId}/approve")
    @Operation(summary = "Kurye başvurusunu onayla", 
               description = "Beklemedeki kurye başvurusunu onaylar ve aktif hale getirir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourierRiderDto> approveRider(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Parameter(description = "Onay notu") @RequestParam(required = false) String approvalNote) {
        
        log.info("Kurye başvurusu onaylanıyor - ID: {}, Not: {}", riderId, approvalNote);
        
        CourierRiderDto riderDto = courierRiderService.approveRider(riderId, approvalNote);
        
        log.info("Kurye başvurusu onaylandı - Kurye: {}, Kod: {}", 
                riderDto.getFullName(), riderDto.getRiderCode());
        
        return ResponseEntity.ok(riderDto);
    }

    /**
     * Kurye başvurusunu reddeder
     */
    @PutMapping("/{riderId}/reject")
    @Operation(summary = "Kurye başvurusunu reddet", 
               description = "Beklemedeki kurye başvurusunu reddeder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourierRiderDto> rejectRider(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Parameter(description = "Red nedeni") @RequestParam String rejectionReason) {
        
        log.info("Kurye başvurusu reddediliyor - ID: {}, Neden: {}", riderId, rejectionReason);
        
        CourierRiderDto riderDto = courierRiderService.rejectRider(riderId, rejectionReason);
        
        log.info("Kurye başvurusu reddedildi - Kurye: {}", riderDto.getFullName());
        
        return ResponseEntity.ok(riderDto);
    }

    /**
     * Kurye performans istatistikleri
     */
    @GetMapping("/{riderId}/performance")
    @Operation(summary = "Kurye performans istatistikleri", 
               description = "Kuryenin detaylı performans metriklerini getirir")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or (hasRole('RIDER') and #riderId == authentication.principal.riderId)")
    public ResponseEntity<Map<String, Object>> getRiderPerformance(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Parameter(description = "Başlangıç tarihi (YYYY-MM-DD)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi (YYYY-MM-DD)") @RequestParam(required = false) String endDate) {
        
        log.debug("Kurye performans istatistikleri getiriliyor - ID: {}, Tarih: {} - {}", 
                riderId, startDate, endDate);
        
        Map<String, Object> performance = courierRiderService.getRiderPerformance(riderId, startDate, endDate);
        return ResponseEntity.ok(performance);
    }

    /**
     * Kurye kazanç raporu
     */
    @GetMapping("/{riderId}/earnings")
    @Operation(summary = "Kurye kazanç raporu", 
               description = "Kuryenin belirtilen dönemdeki kazanç raporunu getirir")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or (hasRole('RIDER') and #riderId == authentication.principal.riderId)")
    public ResponseEntity<Map<String, Object>> getRiderEarnings(
            @Parameter(description = "Kurye ID") @PathVariable Long riderId,
            @Parameter(description = "Başlangıç tarihi (YYYY-MM-DD)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi (YYYY-MM-DD)") @RequestParam(required = false) String endDate) {
        
        log.debug("Kurye kazanç raporu getiriliyor - ID: {}, Tarih: {} - {}", 
                riderId, startDate, endDate);
        
        Map<String, Object> earnings = courierRiderService.getRiderEarnings(riderId, startDate, endDate);
        return ResponseEntity.ok(earnings);
    }

    /**
     * Tüm kuryeler listesi
     */
    @GetMapping
    @Operation(summary = "Kuryeler listesi", 
               description = "Sistemdeki tüm kuryeler sayfalı olarak listeler")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Page<CourierRiderDto>> getAllRiders(
            @Parameter(description = "Durum filtresi") @RequestParam(required = false) CourierRider.RiderStatus status,
            @Parameter(description = "Şehir filtresi") @RequestParam(required = false) String city,
            @Parameter(description = "Çevrimiçi filtresi") @RequestParam(required = false) Boolean isOnline,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Kuryeler listesi getiriliyor - Durum: {}, Şehir: {}, Çevrimiçi: {}", 
                status, city, isOnline);
        
        Page<CourierRiderDto> riders = courierRiderService.getAllRiders(status, city, isOnline, pageable);
        return ResponseEntity.ok(riders);
    }

    /**
     * Kurye arama
     */
    @GetMapping("/search")
    @Operation(summary = "Kurye arama", 
               description = "Çeşitli kriterlere göre kurye arar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Page<CourierRiderDto>> searchRiders(
            @Parameter(description = "Kurye kodu") @RequestParam(required = false) String riderCode,
            @Parameter(description = "Ad soyad") @RequestParam(required = false) String name,
            @Parameter(description = "Telefon numarası") @RequestParam(required = false) String phone,
            @Parameter(description = "Plaka") @RequestParam(required = false) String vehiclePlate,
            @Parameter(description = "Minimum puan") @RequestParam(required = false) BigDecimal minRating,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Kurye arama yapılıyor - Kriterler: kod={}, ad={}, telefon={}, plaka={}", 
                riderCode, name, phone, vehiclePlate);
        
        Page<CourierRiderDto> riders = courierRiderService.searchRiders(
                riderCode, name, phone, vehiclePlate, minRating, pageable);
        
        return ResponseEntity.ok(riders);
    }

    /**
     * Kurye istatistikleri
     */
    @GetMapping("/statistics")
    @Operation(summary = "Genel kurye istatistikleri", 
               description = "Sistem genelindeki kurye istatistiklerini getirir")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getRiderStatistics(
            @Parameter(description = "Şehir filtresi") @RequestParam(required = false) String city) {
        
        log.debug("Kurye istatistikleri getiriliyor - Şehir: {}", city);
        
        Map<String, Object> statistics = courierRiderService.getRiderStatistics(city);
        return ResponseEntity.ok(statistics);
    }
}