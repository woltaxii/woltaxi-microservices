package com.woltaxi.wolkurye.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kurye Tema & Branding Entity
 * 
 * WolKurye sisteminde farklı pazar segmentleri için özelleştirilmiş
 * tema yapıları, logolar, sloganlar ve görsel kimlikler yönetir.
 */
@Entity
@Table(name = "courier_themes", indexes = {
    @Index(name = "idx_theme_code", columnList = "theme_code"),
    @Index(name = "idx_theme_active", columnList = "is_active"),
    @Index(name = "idx_theme_category", columnList = "category"),
    @Index(name = "idx_theme_priority", columnList = "display_priority")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "theme_code", unique = true, nullable = false, length = 50)
    private String themeCode;

    @Column(name = "theme_name", nullable = false, length = 100)
    private String themeName;

    @Column(name = "theme_name_en", length = 100)
    private String themeNameEn;

    @Column(name = "slogan", nullable = false, length = 200)
    private String slogan;

    @Column(name = "slogan_en", length = 200)
    private String sloganEn;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ThemeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_market", nullable = false)
    private TargetMarket targetMarket;

    // Logo ve Görsel Kimlik
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "logo_dark_url", length = 500)
    private String logoDarkUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "mobile_banner_url", length = 500)
    private String mobileBannerUrl;

    // Renk Paleti
    @Column(name = "primary_color", nullable = false, length = 7)
    private String primaryColor; // #FF5722

    @Column(name = "secondary_color", nullable = false, length = 7)
    private String secondaryColor; // #FFC107

    @Column(name = "accent_color", length = 7)
    private String accentColor; // #4CAF50

    @Column(name = "background_color", length = 7)
    private String backgroundColor; // #FFFFFF

    @Column(name = "text_color", length = 7)
    private String textColor; // #212121

    @Column(name = "button_color", length = 7)
    private String buttonColor; // #FF5722

    // Tipografi
    @Column(name = "font_family", length = 100)
    private String fontFamily; // "Roboto", "Open Sans", "Poppins"

    @Column(name = "heading_font", length = 100)
    private String headingFont;

    @Column(name = "font_size_base", length = 10)
    private String fontSizeBase; // "16px", "14px"

    // CSS ve Stil Ayarları
    @Column(name = "custom_css", columnDefinition = "TEXT")
    private String customCss;

    @Column(name = "custom_js", columnDefinition = "TEXT")
    private String customJs;

    // Kurye Uniforması ve Branding
    @Column(name = "uniform_color", length = 50)
    private String uniformColor;

    @Column(name = "helmet_design", length = 100)
    private String helmetDesign;

    @Column(name = "vehicle_sticker_url", length = 500)
    private String vehicleStickerUrl;

    @Column(name = "bag_design_url", length = 500)
    private String bagDesignUrl;

    // Ses ve Bildirim Temaları
    @Column(name = "notification_sound", length = 100)
    private String notificationSound;

    @Column(name = "success_sound", length = 100)
    private String successSound;

    @Column(name = "alert_sound", length = 100)
    private String alertSound;

    // Animasyon ve Efektler
    @Column(name = "animation_style", length = 50)
    private String animationStyle; // "smooth", "bouncy", "fast", "elegant"

    @Column(name = "transition_duration", length = 10)
    private String transitionDuration; // "300ms", "500ms"

    // Özelleştirilebilir Özellikler
    @Column(name = "features", columnDefinition = "jsonb")
    private String features; // JSON formatında özellikler

    // Pazar Segmenti Özellikleri
    @Column(name = "price_multiplier", precision = 5, scale = 2)
    private Double priceMultiplier; // 1.0 = normal, 1.5 = premium, 0.8 = economy

    @Column(name = "service_level", length = 50)
    private String serviceLevel; // "standard", "premium", "express", "economy"

    // Aktiflik ve Görüntüleme
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "display_priority", nullable = false)
    @Builder.Default
    private Integer displayPriority = 100;

    @Column(name = "available_cities", columnDefinition = "TEXT")
    private String availableCities; // Comma separated city list

    @Column(name = "launch_date")
    private LocalDateTime launchDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

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

    /**
     * Tema Kategorisi Enum
     */
    public enum ThemeCategory {
        PREMIUM,      // Premium hizmet teması
        ECO,          // Çevre dostu tema
        SPEED,        // Hızlı teslimat teması
        LOCAL,        // Yerel işletme teması
        CORPORATE,    // Kurumsal tema
        NIGHT,        // Gece teslimat teması
        FOOD,         // Yemek teslimat teması
        MEDICAL,      // Tıbbi teslimat teması
        LUXURY,       // Lüks teslimat teması
        STUDENT,      // Öğrenci dostu tema
        SEASONAL,     // Mevsimlik tema
        SPECIAL       // Özel etkinlik teması
    }

    /**
     * Hedef Pazar Enum
     */
    public enum TargetMarket {
        MASS_MARKET,     // Geniş kitle
        PREMIUM_SEGMENT, // Premium segment
        BUDGET_CONSCIOUS,// Bütçe dostu
        YOUNG_ADULTS,    // Genç yetişkinler
        FAMILIES,        // Aileler
        PROFESSIONALS,   // Profesyoneller
        STUDENTS,        // Öğrenciler
        SENIORS,         // Yaşlılar
        BUSINESSES,      // İşletmeler
        RESTAURANTS,     // Restoranlar
        HEALTH_SECTOR,   // Sağlık sektörü
        RETAIL          // Perakende
    }

    /**
     * Temanın aktif olup olmadığını kontrol eder
     */
    public boolean isCurrentlyActive() {
        if (!isActive) return false;
        
        LocalDateTime now = LocalDateTime.now();
        
        if (launchDate != null && now.isBefore(launchDate)) {
            return false;
        }
        
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }
        
        return true;
    }

    /**
     * Belirtilen şehirde kullanılabilir mi kontrol eder
     */
    public boolean isAvailableInCity(String city) {
        if (availableCities == null || availableCities.trim().isEmpty()) {
            return true; // Tüm şehirlerde kullanılabilir
        }
        
        String[] cities = availableCities.toLowerCase().split(",");
        for (String availableCity : cities) {
            if (availableCity.trim().equals(city.toLowerCase().trim())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Tema için CSS değişkenlerini oluşturur
     */
    public String generateCssVariables() {
        StringBuilder css = new StringBuilder();
        css.append(":root {\n");
        css.append("  --theme-primary: ").append(primaryColor).append(";\n");
        css.append("  --theme-secondary: ").append(secondaryColor).append(";\n");
        
        if (accentColor != null) {
            css.append("  --theme-accent: ").append(accentColor).append(";\n");
        }
        
        if (backgroundColor != null) {
            css.append("  --theme-background: ").append(backgroundColor).append(";\n");
        }
        
        if (textColor != null) {
            css.append("  --theme-text: ").append(textColor).append(";\n");
        }
        
        if (buttonColor != null) {
            css.append("  --theme-button: ").append(buttonColor).append(";\n");
        }
        
        if (fontFamily != null) {
            css.append("  --theme-font: '").append(fontFamily).append("';\n");
        }
        
        if (fontSizeBase != null) {
            css.append("  --theme-font-size: ").append(fontSizeBase).append(";\n");
        }
        
        css.append("}\n");
        
        return css.toString();
    }
}