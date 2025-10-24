package com.woltaxi.wolkurye.service;

import com.woltaxi.wolkurye.entity.CourierTheme;
import com.woltaxi.wolkurye.repository.CourierThemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Kurye Tema & Branding Başlatma Servisi
 * 
 * Sistem başlangıcında WolKurye için önceden tanımlanmış 
 * tema yapılarını database'e yükler.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourierThemeInitializationService {

    private final CourierThemeRepository courierThemeRepository;

    @PostConstruct
    @Transactional
    public void initializeDefaultThemes() {
        log.info("WolKurye tema yapıları başlatılıyor...");

        // Eğer temalar zaten varsa, tekrar oluşturma
        if (courierThemeRepository.count() > 0) {
            log.info("Temalar zaten mevcut, başlatma atlanıyor.");
            return;
        }

        List<CourierTheme> defaultThemes = createDefaultThemes();
        courierThemeRepository.saveAll(defaultThemes);

        log.info("{} adet WolKurye teması başarıyla oluşturuldu.", defaultThemes.size());
    }

    private List<CourierTheme> createDefaultThemes() {
        return Arrays.asList(
            createPremiumTheme(),
            createEcoGreenTheme(),
            createSpeedRushTheme(),
            createLocalHeroTheme(),
            createCorporateEliteTheme(),
            createNightOwlTheme(),
            createFoodieExpressTheme(),
            createMedicalCareTheme(),
            createLuxuryGoldTheme(),
            createStudentBuddyTheme()
        );
    }

    /**
     * 1. PREMIUM WOLKURYE - Lüks Teslimat Teması
     */
    private CourierTheme createPremiumTheme() {
        return CourierTheme.builder()
            .themeCode("PREMIUM_WK")
            .themeName("Premium WolKurye")
            .themeNameEn("Premium WolKurye")
            .slogan("Lüks Teslimat, Mükemmel Hizmet")
            .sloganEn("Luxury Delivery, Perfect Service")
            .description("Premium müşteriler için lüks teslimat deneyimi. Beyaz eldiven hizmeti.")
            .descriptionEn("Luxury delivery experience for premium customers. White glove service.")
            .category(CourierTheme.ThemeCategory.PREMIUM)
            .targetMarket(CourierTheme.TargetMarket.PREMIUM_SEGMENT)
            
            // Görsel Kimlik
            .logoUrl("/themes/premium/logo-premium-wk.svg")
            .logoDarkUrl("/themes/premium/logo-premium-wk-dark.svg")
            .iconUrl("/themes/premium/icon-crown.svg")
            .bannerUrl("/themes/premium/banner-luxury.jpg")
            .mobileBannerUrl("/themes/premium/mobile-banner-luxury.jpg")
            
            // Renk Paleti - Altın & Siyah
            .primaryColor("#D4AF37") // Altın
            .secondaryColor("#1A1A1A") // Koyu siyah
            .accentColor("#FFFFFF") // Beyaz
            .backgroundColor("#FAFAFA")
            .textColor("#2C2C2C")
            .buttonColor("#D4AF37")
            
            // Tipografi
            .fontFamily("Playfair Display")
            .headingFont("Playfair Display")
            .fontSizeBase("16px")
            
            // Kurye Branding
            .uniformColor("Siyah & Altın Detaylar")
            .helmetDesign("Altın Logo ile Mat Siyah")
            .vehicleStickerUrl("/themes/premium/vehicle-sticker-gold.svg")
            .bagDesignUrl("/themes/premium/bag-luxury-leather.jpg")
            
            // Ses Temaları
            .notificationSound("premium-chime.mp3")
            .successSound("luxury-success.mp3")
            .alertSound("elegant-alert.mp3")
            
            // Animasyon
            .animationStyle("elegant")
            .transitionDuration("500ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(1.8)
            .serviceLevel("premium")
            .displayPriority(10)
            .isActive(true)
            .isDefault(false)
            .availableCities("İstanbul,Ankara,İzmir,Antalya")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }

    /**
     * 2. ECO GREEN WOLKURYE - Çevre Dostu Teslimat
     */
    private CourierTheme createEcoGreenTheme() {
        return CourierTheme.builder()
            .themeCode("ECO_GREEN_WK")
            .themeName("Eco Green WolKurye")
            .themeNameEn("Eco Green WolKurye")
            .slogan("Yeşil Teslimat, Temiz Gelecek")
            .sloganEn("Green Delivery, Clean Future")
            .description("Çevre dostu elektrikli araçlarla sürdürülebilir teslimat hizmeti.")
            .descriptionEn("Sustainable delivery service with eco-friendly electric vehicles.")
            .category(CourierTheme.ThemeCategory.ECO)
            .targetMarket(CourierTheme.TargetMarket.YOUNG_ADULTS)
            
            // Görsel Kimlik
            .logoUrl("/themes/eco/logo-eco-wk.svg")
            .logoDarkUrl("/themes/eco/logo-eco-wk-dark.svg")
            .iconUrl("/themes/eco/icon-leaf.svg")
            .bannerUrl("/themes/eco/banner-nature.jpg")
            .mobileBannerUrl("/themes/eco/mobile-banner-nature.jpg")
            
            // Renk Paleti - Doğa Yeşili
            .primaryColor("#4CAF50") // Yeşil
            .secondaryColor("#2E7D32") // Koyu yeşil
            .accentColor("#8BC34A") // Açık yeşil
            .backgroundColor("#F1F8E9")
            .textColor("#1B5E20")
            .buttonColor("#4CAF50")
            
            // Tipografi
            .fontFamily("Open Sans")
            .headingFont("Roboto")
            .fontSizeBase("15px")
            
            // Kurye Branding
            .uniformColor("Yeşil & Doğal Kumaş")
            .helmetDesign("Yaprak Logosu ile Yeşil")
            .vehicleStickerUrl("/themes/eco/vehicle-sticker-leaf.svg")
            .bagDesignUrl("/themes/eco/bag-recycled-material.jpg")
            
            // Ses Temaları
            .notificationSound("nature-bell.mp3")
            .successSound("bird-chirp.mp3")
            .alertSound("wind-chime.mp3")
            
            // Animasyon
            .animationStyle("smooth")
            .transitionDuration("400ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(0.9)
            .serviceLevel("eco-friendly")
            .displayPriority(20)
            .isActive(true)
            .isDefault(false)
            .availableCities("İstanbul,Ankara,İzmir")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }

    /**
     * 3. SPEED RUSH WOLKURYE - Hızlı Teslimat Teması
     */
    private CourierTheme createSpeedRushTheme() {
        return CourierTheme.builder()
            .themeCode("SPEED_RUSH_WK")
            .themeName("Speed Rush WolKurye")
            .themeNameEn("Speed Rush WolKurye")
            .slogan("Hız Sınırlarını Zorluyoruz!")
            .sloganEn("Pushing Speed Limits!")
            .description("15 dakikada teslimat garantisi ile süper hızlı kurye hizmeti.")
            .descriptionEn("Super fast courier service with 15-minute delivery guarantee.")
            .category(CourierTheme.ThemeCategory.SPEED)
            .targetMarket(CourierTheme.TargetMarket.YOUNG_ADULTS)
            
            // Görsel Kimlik
            .logoUrl("/themes/speed/logo-speed-wk.svg")
            .logoDarkUrl("/themes/speed/logo-speed-wk-dark.svg")
            .iconUrl("/themes/speed/icon-lightning.svg")
            .bannerUrl("/themes/speed/banner-racing.jpg")
            .mobileBannerUrl("/themes/speed/mobile-banner-racing.jpg")
            
            // Renk Paleti - Kırmızı & Turuncu
            .primaryColor("#FF5722") // Ateş kırmızısı
            .secondaryColor("#FF9800") // Turuncu
            .accentColor("#FFC107") // Sarı
            .backgroundColor("#FFF3E0")
            .textColor("#BF360C")
            .buttonColor("#FF5722")
            
            // Tipografi
            .fontFamily("Roboto Condensed")
            .headingFont("Racing Sans One")
            .fontSizeBase("15px")
            
            // Kurye Branding
            .uniformColor("Kırmızı & Siyah Racing")
            .helmetDesign("Şimşek Logosu ile Kırmızı")
            .vehicleStickerUrl("/themes/speed/vehicle-sticker-lightning.svg")
            .bagDesignUrl("/themes/speed/bag-racing-style.jpg")
            
            // Ses Temaları
            .notificationSound("racing-start.mp3")
            .successSound("finish-line.mp3")
            .alertSound("racing-horn.mp3")
            
            // Animasyon
            .animationStyle("fast")
            .transitionDuration("200ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(1.3)
            .serviceLevel("express")
            .displayPriority(15)
            .isActive(true)
            .isDefault(false)
            .availableCities("İstanbul,Ankara,İzmir,Bursa,Adana")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }

    /**
     * 4. LOCAL HERO WOLKURYE - Mahalle Dostu Tema
     */
    private CourierTheme createLocalHeroTheme() {
        return CourierTheme.builder()
            .themeCode("LOCAL_HERO_WK")
            .themeName("Local Hero WolKurye")
            .themeNameEn("Local Hero WolKurye")
            .slogan("Mahallenin Süper Kahramanı")
            .sloganEn("Neighborhood Superhero")
            .description("Yerel işletmeleri destekleyen, mahalle dostu kurye hizmeti.")
            .descriptionEn("Neighborhood-friendly courier service supporting local businesses.")
            .category(CourierTheme.ThemeCategory.LOCAL)
            .targetMarket(CourierTheme.TargetMarket.FAMILIES)
            
            // Görsel Kimlik
            .logoUrl("/themes/local/logo-local-wk.svg")
            .logoDarkUrl("/themes/local/logo-local-wk-dark.svg")
            .iconUrl("/themes/local/icon-house.svg")
            .bannerUrl("/themes/local/banner-neighborhood.jpg")
            .mobileBannerUrl("/themes/local/mobile-banner-neighborhood.jpg")
            
            // Renk Paleti - Mavi & Beyaz
            .primaryColor("#2196F3") // Mavi
            .secondaryColor("#1976D2") // Koyu mavi
            .accentColor("#03A9F4") // Açık mavi
            .backgroundColor("#E3F2FD")
            .textColor("#0D47A1")
            .buttonColor("#2196F3")
            
            // Tipografi
            .fontFamily("Poppins")
            .headingFont("Poppins")
            .fontSizeBase("16px")
            
            // Kurye Branding
            .uniformColor("Mavi & Beyaz Klasik")
            .helmetDesign("Ev Logosu ile Mavi")
            .vehicleStickerUrl("/themes/local/vehicle-sticker-home.svg")
            .bagDesignUrl("/themes/local/bag-friendly-design.jpg")
            
            // Ses Temaları
            .notificationSound("friendly-bell.mp3")
            .successSound("happy-tune.mp3")
            .alertSound("gentle-reminder.mp3")
            
            // Animasyon
            .animationStyle("bouncy")
            .transitionDuration("350ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(1.0)
            .serviceLevel("standard")
            .displayPriority(25)
            .isActive(true)
            .isDefault(true) // Varsayılan tema
            .availableCities("Tüm Şehirler")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }

    /**
     * 5. CORPORATE ELITE WOLKURYE - Kurumsal Tema
     */
    private CourierTheme createCorporateEliteTheme() {
        return CourierTheme.builder()
            .themeCode("CORPORATE_ELITE_WK")
            .themeName("Corporate Elite WolKurye")
            .themeNameEn("Corporate Elite WolKurye")
            .slogan("İş Dünyasının Güvenilir Ortağı")
            .sloganEn("Trusted Partner of Business World")
            .description("Kurumsal müşteriler için profesyonel teslimat çözümleri.")
            .descriptionEn("Professional delivery solutions for corporate customers.")
            .category(CourierTheme.ThemeCategory.CORPORATE)
            .targetMarket(CourierTheme.TargetMarket.BUSINESSES)
            
            // Görsel Kimlik
            .logoUrl("/themes/corporate/logo-corporate-wk.svg")
            .logoDarkUrl("/themes/corporate/logo-corporate-wk-dark.svg")
            .iconUrl("/themes/corporate/icon-briefcase.svg")
            .bannerUrl("/themes/corporate/banner-business.jpg")
            .mobileBannerUrl("/themes/corporate/mobile-banner-business.jpg")
            
            // Renk Paleti - Lacivert & Gri
            .primaryColor("#1A237E") // Lacivert
            .secondaryColor("#3F51B5") // İndigo
            .accentColor("#757575") // Gri
            .backgroundColor("#FAFAFA")
            .textColor("#212121")
            .buttonColor("#1A237E")
            
            // Tipografi
            .fontFamily("Source Sans Pro")
            .headingFont("Montserrat")
            .fontSizeBase("16px")
            
            // Kurye Branding
            .uniformColor("Lacivert & Gri Profesyonel")
            .helmetDesign("Çanta Logosu ile Lacivert")
            .vehicleStickerUrl("/themes/corporate/vehicle-sticker-briefcase.svg")
            .bagDesignUrl("/themes/corporate/bag-professional.jpg")
            
            // Ses Temaları
            .notificationSound("corporate-chime.mp3")
            .successSound("professional-success.mp3")
            .alertSound("business-alert.mp3")
            
            // Animasyon
            .animationStyle("professional")
            .transitionDuration("300ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(1.4)
            .serviceLevel("corporate")
            .displayPriority(30)
            .isActive(true)
            .isDefault(false)
            .availableCities("İstanbul,Ankara,İzmir")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }

    /**
     * 6. NIGHT OWL WOLKURYE - Gece Teslimat Teması
     */
    private CourierTheme createNightOwlTheme() {
        return CourierTheme.builder()
            .themeCode("NIGHT_OWL_WK")
            .themeName("Night Owl WolKurye")
            .themeNameEn("Night Owl WolKurye")
            .slogan("Gece Yarısı Kahramanları")
            .sloganEn("Midnight Heroes")
            .description("24 saat kesintisiz gece teslimat hizmeti.")
            .descriptionEn("24/7 non-stop night delivery service.")
            .category(CourierTheme.ThemeCategory.NIGHT)
            .targetMarket(CourierTheme.TargetMarket.YOUNG_ADULTS)
            
            // Görsel Kimlik
            .logoUrl("/themes/night/logo-night-wk.svg")
            .logoDarkUrl("/themes/night/logo-night-wk-light.svg")
            .iconUrl("/themes/night/icon-moon.svg")
            .bannerUrl("/themes/night/banner-citynight.jpg")
            .mobileBannerUrl("/themes/night/mobile-banner-citynight.jpg")
            
            // Renk Paleti - Mor & Siyah
            .primaryColor("#673AB7") // Mor
            .secondaryColor("#9C27B0") // Pembe mor
            .accentColor("#E91E63") // Pembe
            .backgroundColor("#121212")
            .textColor("#E0E0E0")
            .buttonColor("#673AB7")
            
            // Tipografi
            .fontFamily("Roboto")
            .headingFont("Orbitron")
            .fontSizeBase("15px")
            
            // Kurye Branding
            .uniformColor("Siyah & Mor Neon")
            .helmetDesign("Ay Logosu ile Mor")
            .vehicleStickerUrl("/themes/night/vehicle-sticker-moon.svg")
            .bagDesignUrl("/themes/night/bag-reflective-night.jpg")
            
            // Ses Temaları
            .notificationSound("night-owl.mp3")
            .successSound("city-night.mp3")
            .alertSound("neon-alert.mp3")
            
            // Animasyon
            .animationStyle("neon")
            .transitionDuration("400ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(1.5)
            .serviceLevel("night")
            .displayPriority(35)
            .isActive(true)
            .isDefault(false)
            .availableCities("İstanbul,Ankara,İzmir")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }

    /**
     * 7. FOODIE EXPRESS WOLKURYE - Yemek Teslimat Teması
     */
    private CourierTheme createFoodieExpressTheme() {
        return CourierTheme.builder()
            .themeCode("FOODIE_EXPRESS_WK")
            .themeName("Foodie Express WolKurye")
            .themeNameEn("Foodie Express WolKurye")
            .slogan("Lezzet Kapınızda!")
            .sloganEn("Flavor at Your Door!")
            .description("Sıcak yemek teslimatında uzman kurye hizmeti.")
            .descriptionEn("Expert courier service specializing in hot food delivery.")
            .category(CourierTheme.ThemeCategory.FOOD)
            .targetMarket(CourierTheme.TargetMarket.MASS_MARKET)
            
            // Görsel Kimlik
            .logoUrl("/themes/food/logo-foodie-wk.svg")
            .logoDarkUrl("/themes/food/logo-foodie-wk-dark.svg")
            .iconUrl("/themes/food/icon-fork-knife.svg")
            .bannerUrl("/themes/food/banner-delicious.jpg")
            .mobileBannerUrl("/themes/food/mobile-banner-delicious.jpg")
            
            // Renk Paleti - Kırmızı & Sarı
            .primaryColor("#F44336") // Kırmızı
            .secondaryColor("#FFC107") // Sarı
            .accentColor("#FF9800") // Turuncu
            .backgroundColor("#FFF8E1")
            .textColor("#BF360C")
            .buttonColor("#F44336")
            
            // Tipografi
            .fontFamily("Nunito")
            .headingFont("Pacifico")
            .fontSizeBase("16px")
            
            // Kurye Branding
            .uniformColor("Kırmızı & Sarı Şef")
            .helmetDesign("Çatal Bıçak ile Kırmızı")
            .vehicleStickerUrl("/themes/food/vehicle-sticker-chef.svg")
            .bagDesignUrl("/themes/food/bag-thermal-food.jpg")
            
            // Ses Temaları
            .notificationSound("kitchen-bell.mp3")
            .successSound("bon-appetit.mp3")
            .alertSound("oven-timer.mp3")
            
            // Animasyon
            .animationStyle("tasty")
            .transitionDuration("350ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(1.1)
            .serviceLevel("food-specialist")
            .displayPriority(40)
            .isActive(true)
            .isDefault(false)
            .availableCities("Tüm Şehirler")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }

    /**
     * 8. MEDICAL CARE WOLKURYE - Tıbbi Teslimat Teması
     */
    private CourierTheme createMedicalCareTheme() {
        return CourierTheme.builder()
            .themeCode("MEDICAL_CARE_WK")
            .themeName("Medical Care WolKurye")
            .themeNameEn("Medical Care WolKurye")
            .slogan("Sağlığınız Bizim Önceliğimiz")
            .sloganEn("Your Health is Our Priority")
            .description("İlaç ve tıbbi malzeme teslimatında uzman hizmet.")
            .descriptionEn("Expert service in medicine and medical supply delivery.")
            .category(CourierTheme.ThemeCategory.MEDICAL)
            .targetMarket(CourierTheme.TargetMarket.HEALTH_SECTOR)
            
            // Görsel Kimlik
            .logoUrl("/themes/medical/logo-medical-wk.svg")
            .logoDarkUrl("/themes/medical/logo-medical-wk-dark.svg")
            .iconUrl("/themes/medical/icon-medical-cross.svg")
            .bannerUrl("/themes/medical/banner-healthcare.jpg")
            .mobileBannerUrl("/themes/medical/mobile-banner-healthcare.jpg")
            
            // Renk Paleti - Beyaz & Mavi
            .primaryColor("#2196F3") // Tıbbi mavi
            .secondaryColor("#0277BD") // Koyu mavi
            .accentColor("#4CAF50") // Yeşil (sağlık)
            .backgroundColor("#FFFFFF")
            .textColor("#1565C0")
            .buttonColor("#2196F3")
            
            // Tipografi
            .fontFamily("Lato")
            .headingFont("Source Sans Pro")
            .fontSizeBase("16px")
            
            // Kurye Branding
            .uniformColor("Beyaz & Mavi Tıbbi")
            .helmetDesign("Tıbbi Haç ile Beyaz")
            .vehicleStickerUrl("/themes/medical/vehicle-sticker-cross.svg")
            .bagDesignUrl("/themes/medical/bag-medical-cool.jpg")
            
            // Ses Temaları
            .notificationSound("medical-beep.mp3")
            .successSound("health-chime.mp3")
            .alertSound("medical-alert.mp3")
            
            // Animasyon
            .animationStyle("clinical")
            .transitionDuration("300ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(1.2)
            .serviceLevel("medical-grade")
            .displayPriority(45)
            .isActive(true)
            .isDefault(false)
            .availableCities("İstanbul,Ankara,İzmir")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }

    /**
     * 9. LUXURY GOLD WOLKURYE - Ultra Lüks Tema
     */
    private CourierTheme createLuxuryGoldTheme() {
        return CourierTheme.builder()
            .themeCode("LUXURY_GOLD_WK")
            .themeName("Luxury Gold WolKurye")
            .themeNameEn("Luxury Gold WolKurye")
            .slogan("Altın Standardında Hizmet")
            .sloganEn("Gold Standard Service")
            .description("Ultra lüks müşteriler için özel tasarım teslimat deneyimi.")
            .descriptionEn("Exclusively designed delivery experience for ultra-luxury customers.")
            .category(CourierTheme.ThemeCategory.LUXURY)
            .targetMarket(CourierTheme.TargetMarket.PREMIUM_SEGMENT)
            
            // Görsel Kimlik
            .logoUrl("/themes/luxury/logo-luxury-wk.svg")
            .logoDarkUrl("/themes/luxury/logo-luxury-wk-dark.svg")
            .iconUrl("/themes/luxury/icon-diamond.svg")
            .bannerUrl("/themes/luxury/banner-gold.jpg")
            .mobileBannerUrl("/themes/luxury/mobile-banner-gold.jpg")
            
            // Renk Paleti - Altın & Bordo
            .primaryColor("#FFD700") // Altın
            .secondaryColor("#8B0000") // Koyu kırmızı
            .accentColor("#FFA500") // Turuncu altın
            .backgroundColor("#FFFEF7")
            .textColor("#4A4A4A")
            .buttonColor("#FFD700")
            
            // Tipografi
            .fontFamily("Cormorant Garamond")
            .headingFont("Playfair Display")
            .fontSizeBase("17px")
            
            // Kurye Branding
            .uniformColor("Bordo & Altın Lüks")
            .helmetDesign("Elmas Logosu ile Altın")
            .vehicleStickerUrl("/themes/luxury/vehicle-sticker-diamond.svg")
            .bagDesignUrl("/themes/luxury/bag-gold-leather.jpg")
            
            // Ses Temaları
            .notificationSound("luxury-gong.mp3")
            .successSound("golden-success.mp3")
            .alertSound("crystal-alert.mp3")
            
            // Animasyon
            .animationStyle("luxurious")
            .transitionDuration("600ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(2.5)
            .serviceLevel("ultra-luxury")
            .displayPriority(5)
            .isActive(true)
            .isDefault(false)
            .availableCities("İstanbul,Ankara")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }

    /**
     * 10. STUDENT BUDDY WOLKURYE - Öğrenci Dostu Tema
     */
    private CourierTheme createStudentBuddyTheme() {
        return CourierTheme.builder()
            .themeCode("STUDENT_BUDDY_WK")
            .themeName("Student Buddy WolKurye")
            .themeNameEn("Student Buddy WolKurye")
            .slogan("Öğrenci Cebine Uygun!")
            .sloganEn("Student Budget Friendly!")
            .description("Öğrenciler için bütçe dostu kurye hizmeti.")
            .descriptionEn("Budget-friendly courier service for students.")
            .category(CourierTheme.ThemeCategory.STUDENT)
            .targetMarket(CourierTheme.TargetMarket.STUDENTS)
            
            // Görsel Kimlik
            .logoUrl("/themes/student/logo-student-wk.svg")
            .logoDarkUrl("/themes/student/logo-student-wk-dark.svg")
            .iconUrl("/themes/student/icon-graduation.svg")
            .bannerUrl("/themes/student/banner-campus.jpg")
            .mobileBannerUrl("/themes/student/mobile-banner-campus.jpg")
            
            // Renk Paleti - Turuncu & Mavi
            .primaryColor("#FF9800") // Turuncu
            .secondaryColor("#2196F3") // Mavi
            .accentColor("#4CAF50") // Yeşil
            .backgroundColor("#FFF9C4")
            .textColor("#E65100")
            .buttonColor("#FF9800")
            
            // Tipografi
            .fontFamily("Comic Sans MS")
            .headingFont("Fredoka One")
            .fontSizeBase("15px")
            
            // Kurye Branding
            .uniformColor("Turuncu & Mavi Genç")
            .helmetDesign("Mezuniyet Şapkası ile Turuncu")
            .vehicleStickerUrl("/themes/student/vehicle-sticker-graduation.svg")
            .bagDesignUrl("/themes/student/bag-colorful-student.jpg")
            
            // Ses Temaları
            .notificationSound("school-bell.mp3")
            .successSound("graduation-march.mp3")
            .alertSound("study-alarm.mp3")
            
            // Animasyon
            .animationStyle("playful")
            .transitionDuration("300ms")
            
            // Hizmet Özellikleri
            .priceMultiplier(0.7)
            .serviceLevel("student-friendly")
            .displayPriority(50)
            .isActive(true)
            .isDefault(false)
            .availableCities("Ankara,İzmir,Eskişehir,Konya")
            .launchDate(LocalDateTime.now())
            .createdBy("SYSTEM")
            .build();
    }
}