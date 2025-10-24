package com.woltaxi.wolkurye.repository;

import com.woltaxi.wolkurye.entity.CourierTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Kurye Tema Repository
 */
@Repository
public interface CourierThemeRepository extends JpaRepository<CourierTheme, Long> {

    /**
     * Tema koduna göre tema bulur
     */
    Optional<CourierTheme> findByThemeCode(String themeCode);

    /**
     * Aktif temaları getirir
     */
    List<CourierTheme> findByIsActiveTrueOrderByDisplayPriorityAsc();

    /**
     * Kategoriye göre aktif temaları getirir
     */
    List<CourierTheme> findByCategoryAndIsActiveTrueOrderByDisplayPriorityAsc(
        CourierTheme.ThemeCategory category);

    /**
     * Hedef pazara göre aktif temaları getirir
     */
    List<CourierTheme> findByTargetMarketAndIsActiveTrueOrderByDisplayPriorityAsc(
        CourierTheme.TargetMarket targetMarket);

    /**
     * Varsayılan temayı getirir
     */
    Optional<CourierTheme> findByIsDefaultTrueAndIsActiveTrue();

    /**
     * Belirtilen şehirde kullanılabilen temaları getirir
     */
    @Query("SELECT ct FROM CourierTheme ct WHERE ct.isActive = true " +
           "AND (ct.availableCities IS NULL OR ct.availableCities = '' OR " +
           "LOWER(ct.availableCities) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "ORDER BY ct.displayPriority ASC")
    List<CourierTheme> findAvailableThemesForCity(@Param("city") String city);

    /**
     * Premium temaları getirir
     */
    @Query("SELECT ct FROM CourierTheme ct WHERE ct.isActive = true " +
           "AND ct.priceMultiplier > 1.0 " +
           "ORDER BY ct.priceMultiplier DESC")
    List<CourierTheme> findPremiumThemes();

    /**
     * Ekonomik temaları getirir
     */
    @Query("SELECT ct FROM CourierTheme ct WHERE ct.isActive = true " +
           "AND ct.priceMultiplier < 1.0 " +
           "ORDER BY ct.priceMultiplier ASC")
    List<CourierTheme> findEconomyThemes();

    /**
     * Tema adına göre arama yapar
     */
    @Query("SELECT ct FROM CourierTheme ct WHERE ct.isActive = true " +
           "AND (LOWER(ct.themeName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ct.themeNameEn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY ct.displayPriority ASC")
    List<CourierTheme> searchThemesByName(@Param("searchTerm") String searchTerm);
}