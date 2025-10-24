package com.woltaxi.marketing.controller;

import com.woltaxi.marketing.dto.GlobalCampaignRequest;
import com.woltaxi.marketing.dto.GlobalCampaignResponse;
import com.woltaxi.marketing.dto.CampaignPerformanceResponse;
import com.woltaxi.marketing.service.GlobalCampaignService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * Küresel Pazarlama ve Reklam Kampanyaları Yönetimi Controller
 * 
 * Tüm ülkelerde eş zamanlı çalışacak reklam kampanyalarının
 * oluşturulması, yönetimi ve performans takibi
 */
@RestController
@RequestMapping("/campaigns")
@CrossOrigin(origins = "*")
public class GlobalCampaignController {
    
    @Autowired
    private GlobalCampaignService globalCampaignService;
    
    /**
     * Yeni global kampanya oluştur
     * 
     * @param request Kampanya detayları
     * @return Oluşturulan kampanya bilgileri
     */
    @PostMapping
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalCampaignResponse> createGlobalCampaign(
            @Valid @RequestBody GlobalCampaignRequest request) {
        
        GlobalCampaignResponse response = globalCampaignService.createGlobalCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Tüm global kampanyaları listele
     * 
     * @param pageable Sayfalama bilgileri
     * @return Kampanya listesi
     */
    @GetMapping
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('MARKETING_USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<GlobalCampaignResponse>> getAllGlobalCampaigns(Pageable pageable) {
        
        Page<GlobalCampaignResponse> campaigns = globalCampaignService.getAllGlobalCampaigns(pageable);
        return ResponseEntity.ok(campaigns);
    }
    
    /**
     * ID ile global kampanya getir
     * 
     * @param campaignId Kampanya ID
     * @return Kampanya detayları
     */
    @GetMapping("/{campaignId}")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('MARKETING_USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalCampaignResponse> getGlobalCampaignById(
            @PathVariable Long campaignId) {
        
        GlobalCampaignResponse response = globalCampaignService.getGlobalCampaignById(campaignId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * UUID ile global kampanya getir
     * 
     * @param campaignUuid Kampanya UUID
     * @return Kampanya detayları
     */
    @GetMapping("/uuid/{campaignUuid}")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('MARKETING_USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalCampaignResponse> getGlobalCampaignByUuid(
            @PathVariable UUID campaignUuid) {
        
        GlobalCampaignResponse response = globalCampaignService.getGlobalCampaignByUuid(campaignUuid);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Global kampanyayı güncelle
     * 
     * @param campaignId Kampanya ID
     * @param request Güncellenecek bilgiler
     * @return Güncellenmiş kampanya bilgileri
     */
    @PutMapping("/{campaignId}")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalCampaignResponse> updateGlobalCampaign(
            @PathVariable Long campaignId,
            @Valid @RequestBody GlobalCampaignRequest request) {
        
        GlobalCampaignResponse response = globalCampaignService.updateGlobalCampaign(campaignId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Global kampanyayı sil
     * 
     * @param campaignId Kampanya ID
     * @return Başarı mesajı
     */
    @DeleteMapping("/{campaignId}")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGlobalCampaign(@PathVariable Long campaignId) {
        
        globalCampaignService.deleteGlobalCampaign(campaignId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Global kampanyayı başlat
     * 
     * @param campaignId Kampanya ID
     * @return Güncellenmiş kampanya bilgileri
     */
    @PostMapping("/{campaignId}/launch")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalCampaignResponse> launchGlobalCampaign(
            @PathVariable Long campaignId) {
        
        GlobalCampaignResponse response = globalCampaignService.launchGlobalCampaign(campaignId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Global kampanyayı duraklat
     * 
     * @param campaignId Kampanya ID
     * @return Güncellenmiş kampanya bilgileri
     */
    @PostMapping("/{campaignId}/pause")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalCampaignResponse> pauseGlobalCampaign(
            @PathVariable Long campaignId) {
        
        GlobalCampaignResponse response = globalCampaignService.pauseGlobalCampaign(campaignId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Global kampanyayı yeniden başlat
     * 
     * @param campaignId Kampanya ID
     * @return Güncellenmiş kampanya bilgileri
     */
    @PostMapping("/{campaignId}/resume")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalCampaignResponse> resumeGlobalCampaign(
            @PathVariable Long campaignId) {
        
        GlobalCampaignResponse response = globalCampaignService.resumeGlobalCampaign(campaignId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Global kampanyayı tamamla
     * 
     * @param campaignId Kampanya ID
     * @return Güncellenmiş kampanya bilgileri
     */
    @PostMapping("/{campaignId}/complete")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalCampaignResponse> completeGlobalCampaign(
            @PathVariable Long campaignId) {
        
        GlobalCampaignResponse response = globalCampaignService.completeGlobalCampaign(campaignId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Global kampanya performansını getir
     * 
     * @param campaignId Kampanya ID
     * @return Performans metrikleri
     */
    @GetMapping("/{campaignId}/performance")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('MARKETING_USER') or hasRole('ADMIN')")
    public ResponseEntity<CampaignPerformanceResponse> getGlobalCampaignPerformance(
            @PathVariable Long campaignId) {
        
        CampaignPerformanceResponse performance = globalCampaignService.getGlobalCampaignPerformance(campaignId);
        return ResponseEntity.ok(performance);
    }
    
    /**
     * Aktif global kampanyaları listele
     * 
     * @param pageable Sayfalama bilgileri
     * @return Aktif kampanya listesi
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('MARKETING_USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<GlobalCampaignResponse>> getActiveGlobalCampaigns(Pageable pageable) {
        
        Page<GlobalCampaignResponse> campaigns = globalCampaignService.getActiveGlobalCampaigns(pageable);
        return ResponseEntity.ok(campaigns);
    }
    
    /**
     * Kampanya tipine göre global kampanyaları listele
     * 
     * @param campaignType Kampanya tipi
     * @param pageable Sayfalama bilgileri
     * @return Filtrelenmiş kampanya listesi
     */
    @GetMapping("/type/{campaignType}")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('MARKETING_USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<GlobalCampaignResponse>> getGlobalCampaignsByType(
            @PathVariable String campaignType,
            Pageable pageable) {
        
        Page<GlobalCampaignResponse> campaigns = globalCampaignService.getGlobalCampaignsByType(campaignType, pageable);
        return ResponseEntity.ok(campaigns);
    }
    
    /**
     * Ülke bazında kampanyaları getir
     * 
     * @param campaignId Kampanya ID
     * @param countryCode Ülke kodu (USA, TUR, vb.)
     * @return Ülke bazında kampanya detayları
     */
    @GetMapping("/{campaignId}/countries/{countryCode}")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('MARKETING_USER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getLocalizedCampaignsByCountry(
            @PathVariable Long campaignId,
            @PathVariable String countryCode) {
        
        Object localizedCampaigns = globalCampaignService.getLocalizedCampaignsByCountry(campaignId, countryCode);
        return ResponseEntity.ok(localizedCampaigns);
    }
    
    /**
     * Platform bazında kampanyaları getir
     * 
     * @param campaignId Kampanya ID
     * @param platformCode Platform kodu (FB, GOOGLE, vb.)
     * @return Platform bazında kampanya detayları
     */
    @GetMapping("/{campaignId}/platforms/{platformCode}")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('MARKETING_USER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getLocalizedCampaignsByPlatform(
            @PathVariable Long campaignId,
            @PathVariable String platformCode) {
        
        Object localizedCampaigns = globalCampaignService.getLocalizedCampaignsByPlatform(campaignId, platformCode);
        return ResponseEntity.ok(localizedCampaigns);
    }
    
    /**
     * Global kampanya dashboard özeti
     * 
     * @return Dashboard özet bilgileri
     */
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('MARKETING_MANAGER') or hasRole('MARKETING_USER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getGlobalCampaignDashboard() {
        
        Object dashboardSummary = globalCampaignService.getGlobalCampaignDashboard();
        return ResponseEntity.ok(dashboardSummary);
    }
}