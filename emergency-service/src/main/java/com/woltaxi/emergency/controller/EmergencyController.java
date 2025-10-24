package com.woltaxi.emergency.controller;

import com.woltaxi.emergency.dto.*;
import com.woltaxi.emergency.entity.EmergencyIncident;
import com.woltaxi.emergency.service.EmergencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Emergency Service Controller
 * SOS, Panic Button ve Acil Durum Yönetimi API'leri
 */
@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Emergency Safety System", description = "SOS, Panic Button ve Acil Durum Yönetimi API'leri")
public class EmergencyController {

    private final EmergencyService emergencyService;

    // =============================================================================
    // SOS & PANIC BUTTON ENDPOINTS - Acil Durum Tetikleme
    // =============================================================================

    @PostMapping("/sos")
    @Operation(summary = "SOS/Panic Button Tetikleme", 
               description = "Acil durum butonu tetikleme - Kritik güvenlik endpoint'i")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<EmergencyResponseDTO> triggerSOS(
            @Valid @RequestBody SOSRequestDTO sosRequest) {
        
        log.error("🚨 SOS TRIGGERED - User/Driver: {}, Location: {},{}, Type: {}", 
                sosRequest.getReporterId(), 
                sosRequest.getLatitude(), 
                sosRequest.getLongitude(),
                sosRequest.getIncidentType());
        
        EmergencyResponseDTO response = emergencyService.triggerSOS(sosRequest);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/panic")
    @Operation(summary = "Panic Button - Sessiz Alarm", 
               description = "Sessiz panic button - Dikkat çekmeden yardım çağırma")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<EmergencyResponseDTO> triggerPanicButton(
            @Valid @RequestBody PanicButtonRequestDTO panicRequest) {
        
        log.error("🚨 PANIC BUTTON ACTIVATED - User/Driver: {}, Silent Mode: {}", 
                panicRequest.getReporterId(), 
                panicRequest.isSilentMode());
        
        EmergencyResponseDTO response = emergencyService.triggerPanicButton(panicRequest);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/emergency-call")
    @Operation(summary = "Acil Durum Çağrısı", 
               description = "Ses veya video çağrısı ile acil durum bildirimi")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<EmergencyResponseDTO> reportEmergencyCall(
            @Valid @RequestBody EmergencyCallRequestDTO callRequest) {
        
        log.warn("📞 Emergency call reported - Caller: {}, Type: {}", 
                callRequest.getCallerId(), 
                callRequest.getEmergencyType());
        
        EmergencyResponseDTO response = emergencyService.handleEmergencyCall(callRequest);
        
        return ResponseEntity.ok(response);
    }

    // =============================================================================
    // LOCATION SHARING & TRACKING - Konum Paylaşımı ve Takibi
    // =============================================================================

    @PostMapping("/share-location")
    @Operation(summary = "Acil Durum Konum Paylaşımı", 
               description = "Aile/arkadaşlarla gerçek zamanlı konum paylaşımı başlatma")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<LocationSharingResponseDTO> shareLocation(
            @Valid @RequestBody LocationSharingRequestDTO locationRequest) {
        
        log.info("📍 Emergency location sharing started - User: {}, Duration: {} min", 
                locationRequest.getUserId(), 
                locationRequest.getSharingDurationMinutes());
        
        LocationSharingResponseDTO response = emergencyService.startLocationSharing(locationRequest);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-location")
    @Operation(summary = "Konum Güncelleme", 
               description = "Aktif acil durum sırasında konum güncelleme")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<Void> updateLocation(
            @Valid @RequestBody LocationUpdateDTO locationUpdate) {
        
        emergencyService.updateEmergencyLocation(locationUpdate);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop-location-sharing/{incidentId}")
    @Operation(summary = "Konum Paylaşımını Durdur", 
               description = "Aktif konum paylaşımını sonlandırma")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<Void> stopLocationSharing(
            @Parameter(description = "Olay ID") @PathVariable UUID incidentId) {
        
        log.info("🛑 Stopping location sharing for incident: {}", incidentId);
        
        emergencyService.stopLocationSharing(incidentId);
        
        return ResponseEntity.ok().build();
    }

    // =============================================================================
    // EMERGENCY CONTACTS MANAGEMENT - Acil Durum İletişim Yönetimi
    // =============================================================================

    @GetMapping("/contacts")
    @Operation(summary = "Acil Durum İletişim Listesi", 
               description = "Kullanıcının kayıtlı acil durum iletişim kişilerini listele")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<List<EmergencyContactDTO>> getEmergencyContacts(
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Sürücü ID") @RequestParam(required = false) Long driverId) {
        
        List<EmergencyContactDTO> contacts = emergencyService.getEmergencyContacts(userId, driverId);
        
        return ResponseEntity.ok(contacts);
    }

    @PostMapping("/contacts")
    @Operation(summary = "Acil Durum İletişim Kişisi Ekle", 
               description = "Yeni acil durum iletişim kişisi ekleme")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<EmergencyContactDTO> addEmergencyContact(
            @Valid @RequestBody EmergencyContactCreateDTO contactRequest) {
        
        log.info("📋 Adding emergency contact - User: {}, Contact: {}", 
                contactRequest.getUserId(), 
                contactRequest.getContactName());
        
        EmergencyContactDTO contact = emergencyService.addEmergencyContact(contactRequest);
        
        return ResponseEntity.ok(contact);
    }

    @PutMapping("/contacts/{contactId}")
    @Operation(summary = "Acil Durum İletişim Güncelle", 
               description = "Mevcut acil durum iletişim kişisini güncelleme")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<EmergencyContactDTO> updateEmergencyContact(
            @Parameter(description = "İletişim ID") @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactUpdateDTO updateRequest) {
        
        EmergencyContactDTO contact = emergencyService.updateEmergencyContact(contactId, updateRequest);
        
        return ResponseEntity.ok(contact);
    }

    @DeleteMapping("/contacts/{contactId}")
    @Operation(summary = "Acil Durum İletişim Sil", 
               description = "Acil durum iletişim kişisini silme")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<Void> deleteEmergencyContact(
            @Parameter(description = "İletişim ID") @PathVariable Long contactId) {
        
        log.info("🗑️ Deleting emergency contact: {}", contactId);
        
        emergencyService.deleteEmergencyContact(contactId);
        
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/contacts/{contactId}/verify")
    @Operation(summary = "İletişim Doğrulama", 
               description = "Acil durum iletişim kişisini doğrulama")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<ContactVerificationResponseDTO> verifyContact(
            @Parameter(description = "İletişim ID") @PathVariable Long contactId,
            @Valid @RequestBody ContactVerificationRequestDTO verificationRequest) {
        
        ContactVerificationResponseDTO response = emergencyService.verifyEmergencyContact(
                contactId, verificationRequest);
        
        return ResponseEntity.ok(response);
    }

    // =============================================================================
    // INCIDENT MANAGEMENT - Olay Yönetimi
    // =============================================================================

    @GetMapping("/incidents")
    @Operation(summary = "Acil Durum Olayları Listesi", 
               description = "Acil durum olaylarını listele (sayfalı)")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN', 'EMERGENCY_OPERATOR')")
    public ResponseEntity<Page<EmergencyIncidentDTO>> getEmergencyIncidents(
            @Parameter(description = "Kullanıcı ID filtresi") @RequestParam(required = false) Long userId,
            @Parameter(description = "Sürücü ID filtresi") @RequestParam(required = false) Long driverId,
            @Parameter(description = "Durum filtresi") @RequestParam(required = false) EmergencyIncident.IncidentStatus status,
            @Parameter(description = "Önem seviyesi filtresi") @RequestParam(required = false) @Min(1) @Max(5) Integer severity,
            Pageable pageable) {
        
        Page<EmergencyIncidentDTO> incidents = emergencyService.getEmergencyIncidents(
                userId, driverId, status, severity, pageable);
        
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/{incidentId}")
    @Operation(summary = "Acil Durum Olay Detayı", 
               description = "Belirli bir acil durum olayının detaylı bilgileri")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN', 'EMERGENCY_OPERATOR')")
    public ResponseEntity<EmergencyIncidentDetailDTO> getEmergencyIncident(
            @Parameter(description = "Olay ID") @PathVariable UUID incidentId) {
        
        EmergencyIncidentDetailDTO incident = emergencyService.getEmergencyIncidentDetail(incidentId);
        
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/incidents/{incidentId}/status")
    @Operation(summary = "Olay Durumu Güncelle", 
               description = "Acil durum olayının durumunu güncelleme")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMERGENCY_OPERATOR')")
    public ResponseEntity<EmergencyIncidentDTO> updateIncidentStatus(
            @Parameter(description = "Olay ID") @PathVariable UUID incidentId,
            @Valid @RequestBody IncidentStatusUpdateDTO statusUpdate) {
        
        log.info("📝 Updating incident status - ID: {}, New Status: {}", 
                incidentId, statusUpdate.getNewStatus());
        
        EmergencyIncidentDTO incident = emergencyService.updateIncidentStatus(incidentId, statusUpdate);
        
        return ResponseEntity.ok(incident);
    }

    @PostMapping("/incidents/{incidentId}/response")
    @Operation(summary = "Müdahale Ekle", 
               description = "Acil durum olayına müdahale kaydı ekleme")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMERGENCY_OPERATOR')")
    public ResponseEntity<EmergencyResponseRecordDTO> addEmergencyResponse(
            @Parameter(description = "Olay ID") @PathVariable UUID incidentId,
            @Valid @RequestBody EmergencyResponseCreateDTO responseRequest) {
        
        EmergencyResponseRecordDTO response = emergencyService.addEmergencyResponse(
                incidentId, responseRequest);
        
        return ResponseEntity.ok(response);
    }

    // =============================================================================
    // SAFETY MODES - Güvenlik Modları
    // =============================================================================

    @PostMapping("/safety-modes/women")
    @Operation(summary = "Kadın Güvenlik Modu Aktif", 
               description = "Kadın yolcular için özel güvenlik modunu aktifleştir")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<SafetyModeResponseDTO> activateWomenSafetyMode(
            @Valid @RequestBody WomenSafetyModeRequestDTO safetyRequest) {
        
        log.info("👩 Women safety mode activated - User: {}", safetyRequest.getUserId());
        
        SafetyModeResponseDTO response = emergencyService.activateWomenSafetyMode(safetyRequest);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/safety-modes/family")
    @Operation(summary = "Aile Güvenlik Modu Aktif", 
               description = "Aile ile seyahat eden yolcular için güvenlik modu")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<SafetyModeResponseDTO> activateFamilySafetyMode(
            @Valid @RequestBody FamilySafetyModeRequestDTO safetyRequest) {
        
        log.info("👨‍👩‍👧‍👦 Family safety mode activated - User: {}", safetyRequest.getUserId());
        
        SafetyModeResponseDTO response = emergencyService.activateFamilySafetyMode(safetyRequest);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/safety-modes/tourist")
    @Operation(summary = "Turist Güvenlik Modu Aktif", 
               description = "Turistler için özel güvenlik ve rehberlik modu")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<SafetyModeResponseDTO> activateTouristSafetyMode(
            @Valid @RequestBody TouristSafetyModeRequestDTO safetyRequest) {
        
        log.info("🏛️ Tourist safety mode activated - User: {}, Country: {}", 
                safetyRequest.getUserId(), safetyRequest.getVisitingCountry());
        
        SafetyModeResponseDTO response = emergencyService.activateTouristSafetyMode(safetyRequest);
        
        return ResponseEntity.ok(response);
    }

    // =============================================================================
    // EMERGENCY SETTINGS - Acil Durum Ayarları
    // =============================================================================

    @GetMapping("/settings")
    @Operation(summary = "Acil Durum Ayarları", 
               description = "Kullanıcının acil durum ayarlarını getir")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<EmergencySettingsDTO> getEmergencySettings(
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Sürücü ID") @RequestParam(required = false) Long driverId) {
        
        EmergencySettingsDTO settings = emergencyService.getEmergencySettings(userId, driverId);
        
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    @Operation(summary = "Acil Durum Ayarları Güncelle", 
               description = "Kullanıcının acil durum ayarlarını güncelle")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    public ResponseEntity<EmergencySettingsDTO> updateEmergencySettings(
            @Valid @RequestBody EmergencySettingsUpdateDTO settingsUpdate) {
        
        EmergencySettingsDTO settings = emergencyService.updateEmergencySettings(settingsUpdate);
        
        return ResponseEntity.ok(settings);
    }

    // =============================================================================
    // EMERGENCY ANALYTICS & REPORTING - Acil Durum Analitik ve Raporlama
    // =============================================================================

    @GetMapping("/analytics/dashboard")
    @Operation(summary = "Acil Durum Dashboard", 
               description = "Acil durum yönetim dashboard verilerini getir")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMERGENCY_OPERATOR', 'MANAGER')")
    public ResponseEntity<EmergencyDashboardDTO> getEmergencyDashboard(
            @Parameter(description = "Ülke kodu filtresi") @RequestParam(required = false) String countryCode,
            @Parameter(description = "Şehir filtresi") @RequestParam(required = false) String city,
            @Parameter(description = "Gün sayısı") @RequestParam(defaultValue = "7") @Min(1) @Max(365) Integer days) {
        
        EmergencyDashboardDTO dashboard = emergencyService.getEmergencyDashboard(countryCode, city, days);
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/analytics/response-times")
    @Operation(summary = "Müdahale Süreleri Analizi", 
               description = "Acil durum müdahale sürelerinin analizi")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMERGENCY_OPERATOR', 'MANAGER')")
    public ResponseEntity<ResponseTimeAnalyticsDTO> getResponseTimeAnalytics(
            @Parameter(description = "Başlangıç tarihi") @RequestParam String startDate,
            @Parameter(description = "Bitiş tarihi") @RequestParam String endDate,
            @Parameter(description = "Ülke kodu") @RequestParam(required = false) String countryCode) {
        
        ResponseTimeAnalyticsDTO analytics = emergencyService.getResponseTimeAnalytics(
                startDate, endDate, countryCode);
        
        return ResponseEntity.ok(analytics);
    }

    // =============================================================================
    // HEALTH CHECK & SYSTEM STATUS - Sistem Durumu
    // =============================================================================

    @GetMapping("/health")
    @Operation(summary = "Acil Durum Sistemi Sağlık Kontrolü", 
               description = "Acil durum sisteminin durumunu kontrol et")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = emergencyService.getSystemHealth();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/active-incidents")
    @Operation(summary = "Aktif Acil Durumlar", 
               description = "Şu anda aktif olan acil durumların sayısı ve özeti")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMERGENCY_OPERATOR')")
    public ResponseEntity<ActiveIncidentsResponseDTO> getActiveIncidents() {
        ActiveIncidentsResponseDTO activeIncidents = emergencyService.getActiveIncidents();
        return ResponseEntity.ok(activeIncidents);
    }

    @PostMapping("/test-emergency")
    @Operation(summary = "Acil Durum Sistemi Test", 
               description = "Acil durum sistemini test etme (sadece test ortamı)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestEmergencyResponseDTO> testEmergencySystem(
            @Valid @RequestBody TestEmergencyRequestDTO testRequest) {
        
        log.warn("🧪 Emergency system test initiated - Test Type: {}", testRequest.getTestType());
        
        TestEmergencyResponseDTO response = emergencyService.testEmergencySystem(testRequest);
        
        return ResponseEntity.ok(response);
    }
}