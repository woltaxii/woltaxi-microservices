package com.woltaxi.wolkurye.controller;

import com.woltaxi.wolkurye.dto.CourierThemeDto;
import com.woltaxi.wolkurye.entity.CourierTheme;
import com.woltaxi.wolkurye.service.CourierThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Kurye Tema & Branding REST Controller
 * 
 * WolKurye sistemindeki farklı tema yapılarının yönetimi için REST API endpoints sağlar.
 * Tema seçimi, görsel kimlik yönetimi ve branding özelleştirmeleri.
 */
@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Courier Themes", description = "WolKurye tema & branding yönetimi")
public class CourierThemeController {

    private final CourierThemeService courierThemeService;

    /**
     * Tüm aktif temaları listeler
     */
    @GetMapping
    @Operation(summary = "Aktif temaları listele", 
               description = "Sistemdeki tüm aktif kurye temalarını öncelik sırasına göre listeler")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Temalar başarıyla getirildi"),
        @ApiResponse(responseCode = "404", description = "Aktif tema bulunamadı")
    })
    public ResponseEntity<List<CourierThemeDto>> getAllActiveThemes() {
        log.debug("Tüm aktif temalar getiriliyor");
        
        List<CourierThemeDto> themes = courierThemeService.getAllActiveThemes();
        
        log.info("{} adet aktif tema bulundu", themes.size());
        return ResponseEntity.ok(themes);
    }

    /**
     * Tema detaylarını getirir
     */
    @GetMapping("/{themeId}")
    @Operation(summary = "Tema detaylarını getir", 
               description = "Belirtilen ID'ye sahip temanın tüm detaylarını getirir")
    public ResponseEntity<CourierThemeDto> getTheme(
            @Parameter(description = "Tema ID") @PathVariable Long themeId) {
        
        log.debug("Tema detayları getiriliyor - ID: {}", themeId);
        
        CourierThemeDto theme = courierThemeService.getThemeById(themeId);
        return ResponseEntity.ok(theme);
    }

    /**
     * Tema koduna göre tema getirir
     */
    @GetMapping("/code/{themeCode}")
    @Operation(summary = "Tema koduna göre tema getir", 
               description = "Tema kodu kullanarak tema bilgilerini getirir")
    public ResponseEntity<CourierThemeDto> getThemeByCode(
            @Parameter(description = "Tema kodu") @PathVariable String themeCode) {
        
        log.debug("Tema detayları getiriliyor - Tema Kodu: {}", themeCode);
        
        CourierThemeDto theme = courierThemeService.getThemeByCode(themeCode);
        return ResponseEntity.ok(theme);
    }

    /**
     * Şehir bazında kullanılabilir temaları getirir
     */
    @GetMapping("/city/{city}")
    @Operation(summary = "Şehir bazında temaları getir", 
               description = "Belirtilen şehirde kullanılabilen temaları listeler")
    public ResponseEntity<List<CourierThemeDto>> getThemesForCity(
            @Parameter(description = "Şehir adı") @PathVariable String city) {
        
        log.debug("Şehir temaları getiriliyor - Şehir: {}", city);
        
        List<CourierThemeDto> themes = courierThemeService.getThemesForCity(city);
        
        log.info("{} şehri için {} adet tema bulundu", city, themes.size());
        return ResponseEntity.ok(themes);
    }

    /**
     * Kategoriye göre temaları getirir
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Kategoriye göre temaları getir", 
               description = "Belirtilen kategorideki temaları listeler")
    public ResponseEntity<List<CourierThemeDto>> getThemesByCategory(
            @Parameter(description = "Tema kategorisi") @PathVariable CourierTheme.ThemeCategory category) {
        
        log.debug("Kategori temaları getiriliyor - Kategori: {}", category);
        
        List<CourierThemeDto> themes = courierThemeService.getThemesByCategory(category);
        
        log.info("{} kategorisi için {} adet tema bulundu", category, themes.size());
        return ResponseEntity.ok(themes);
    }

    /**
     * Hedef pazara göre temaları getirir
     */
    @GetMapping("/target-market/{targetMarket}")
    @Operation(summary = "Hedef pazara göre temaları getir", 
               description = "Belirtilen hedef pazardaki temaları listeler")
    public ResponseEntity<List<CourierThemeDto>> getThemesByTargetMarket(
            @Parameter(description = "Hedef pazar") @PathVariable CourierTheme.TargetMarket targetMarket) {
        
        log.debug("Hedef pazar temaları getiriliyor - Pazar: {}", targetMarket);
        
        List<CourierThemeDto> themes = courierThemeService.getThemesByTargetMarket(targetMarket);
        
        log.info("{} hedef pazarı için {} adet tema bulundu", targetMarket, themes.size());
        return ResponseEntity.ok(themes);
    }

    /**
     * Premium temaları getirir
     */
    @GetMapping("/premium")
    @Operation(summary = "Premium temaları getir", 
               description = "Fiyat çarpanı 1.0'dan büyük olan premium temaları listeler")
    public ResponseEntity<List<CourierThemeDto>> getPremiumThemes() {
        log.debug("Premium temalar getiriliyor");
        
        List<CourierThemeDto> themes = courierThemeService.getPremiumThemes();
        
        log.info("{} adet premium tema bulundu", themes.size());
        return ResponseEntity.ok(themes);
    }

    /**
     * Ekonomik temaları getirir
     */
    @GetMapping("/economy")
    @Operation(summary = "Ekonomik temaları getir", 
               description = "Fiyat çarpanı 1.0'dan küçük olan ekonomik temaları listeler")
    public ResponseEntity<List<CourierThemeDto>> getEconomyThemes() {
        log.debug("Ekonomik temalar getiriliyor");
        
        List<CourierThemeDto> themes = courierThemeService.getEconomyThemes();
        
        log.info("{} adet ekonomik tema bulundu", themes.size());
        return ResponseEntity.ok(themes);
    }

    /**
     * Varsayılan temayı getirir
     */
    @GetMapping("/default")
    @Operation(summary = "Varsayılan temayı getir", 
               description = "Sistem varsayılan temasını getirir")
    public ResponseEntity<CourierThemeDto> getDefaultTheme() {
        log.debug("Varsayılan tema getiriliyor");
        
        CourierThemeDto theme = courierThemeService.getDefaultTheme();
        
        log.info("Varsayılan tema: {}", theme.getThemeName());
        return ResponseEntity.ok(theme);
    }

    /**
     * Tema arama
     */
    @GetMapping("/search")
    @Operation(summary = "Tema arama", 
               description = "Tema adına göre arama yapar")
    public ResponseEntity<List<CourierThemeDto>> searchThemes(
            @Parameter(description = "Arama terimi") @RequestParam String searchTerm) {
        
        log.debug("Tema arama yapılıyor - Terim: {}", searchTerm);
        
        List<CourierThemeDto> themes = courierThemeService.searchThemes(searchTerm);
        
        log.info("'{}' araması için {} adet tema bulundu", searchTerm, themes.size());
        return ResponseEntity.ok(themes);
    }

    /**
     * Tema CSS'ini getirir
     */
    @GetMapping("/{themeId}/css")
    @Operation(summary = "Tema CSS'ini getir", 
               description = "Belirtilen temanın CSS stillerini getirir")
    public ResponseEntity<String> getThemeCss(
            @Parameter(description = "Tema ID") @PathVariable Long themeId) {
        
        log.debug("Tema CSS'i getiriliyor - ID: {}", themeId);
        
        String css = courierThemeService.generateThemeCss(themeId);
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/css")
                .body(css);
    }

    /**
     * Tema JavaScript'ini getirir
     */
    @GetMapping("/{themeId}/js")
    @Operation(summary = "Tema JavaScript'ini getir", 
               description = "Belirtilen temanın JavaScript kodlarını getirir")
    public ResponseEntity<String> getThemeJs(
            @Parameter(description = "Tema ID") @PathVariable Long themeId) {
        
        log.debug("Tema JavaScript'i getiriliyor - ID: {}", themeId);
        
        String js = courierThemeService.generateThemeJs(themeId);
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/javascript")
                .body(js);
    }

    /**
     * Tema renk paletini getirir
     */
    @GetMapping("/{themeId}/colors")
    @Operation(summary = "Tema renk paletini getir", 
               description = "Belirtilen temanın renk paletini JSON formatında getirir")
    public ResponseEntity<Map<String, String>> getThemeColorPalette(
            @Parameter(description = "Tema ID") @PathVariable Long themeId) {
        
        log.debug("Tema renk paleti getiriliyor - ID: {}", themeId);
        
        Map<String, String> colors = courierThemeService.getThemeColorPalette(themeId);
        
        return ResponseEntity.ok(colors);
    }

    /**
     * Tema branding materyallerini getirir
     */
    @GetMapping("/{themeId}/branding")
    @Operation(summary = "Tema branding materyallerini getir", 
               description = "Logo, banner, ikon gibi branding materyallerini getirir")
    public ResponseEntity<Map<String, String>> getThemeBrandingMaterials(
            @Parameter(description = "Tema ID") @PathVariable Long themeId) {
        
        log.debug("Tema branding materyalleri getiriliyor - ID: {}", themeId);
        
        Map<String, String> branding = courierThemeService.getThemeBrandingMaterials(themeId);
        
        return ResponseEntity.ok(branding);
    }

    /**
     * Kurye için tema önerisi
     */
    @GetMapping("/recommend")
    @Operation(summary = "Tema önerisi", 
               description = "Kurye profiline göre uygun tema önerir")
    public ResponseEntity<List<CourierThemeDto>> recommendThemes(
            @Parameter(description = "Kurye ID") @RequestParam(required = false) Long riderId,
            @Parameter(description = "Şehir") @RequestParam(required = false) String city,
            @Parameter(description = "Hedef pazar") @RequestParam(required = false) CourierTheme.TargetMarket targetMarket,
            @Parameter(description = "Maksimum tema sayısı") @RequestParam(required = false, defaultValue = "3") Integer maxResults) {
        
        log.debug("Tema önerisi isteniyor - Kurye: {}, Şehir: {}, Pazar: {}", riderId, city, targetMarket);
        
        List<CourierThemeDto> recommendations = courierThemeService.recommendThemes(riderId, city, targetMarket, maxResults);
        
        log.info("{} adet tema önerisi oluşturuldu", recommendations.size());
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Tema istatistikleri
     */
    @GetMapping("/statistics")
    @Operation(summary = "Tema istatistikleri", 
               description = "Tema kullanım istatistiklerini getirir")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getThemeStatistics() {
        log.debug("Tema istatistikleri getiriliyor");
        
        Map<String, Object> statistics = courierThemeService.getThemeStatistics();
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * Tema popülerlik sıralaması
     */
    @GetMapping("/popular")
    @Operation(summary = "Popüler temaları getir", 
               description = "Kullanım sıklığına göre popüler temaları listeler")
    public ResponseEntity<List<CourierThemeDto>> getPopularThemes(
            @Parameter(description = "Maksimum tema sayısı") @RequestParam(required = false, defaultValue = "5") Integer limit) {
        
        log.debug("Popüler temalar getiriliyor - Limit: {}", limit);
        
        List<CourierThemeDto> popularThemes = courierThemeService.getPopularThemes(limit);
        
        log.info("{} adet popüler tema bulundu", popularThemes.size());
        return ResponseEntity.ok(popularThemes);
    }

    /**
     * Tema önizlemesi
     */
    @GetMapping("/{themeId}/preview")
    @Operation(summary = "Tema önizlemesi", 
               description = "Temanın görsel önizlemesini getirir")
    public ResponseEntity<Map<String, Object>> getThemePreview(
            @Parameter(description = "Tema ID") @PathVariable Long themeId) {
        
        log.debug("Tema önizlemesi getiriliyor - ID: {}", themeId);
        
        Map<String, Object> preview = courierThemeService.generateThemePreview(themeId);
        
        return ResponseEntity.ok(preview);
    }
}