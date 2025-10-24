package com.woltaxi.emergency.repository;

import com.woltaxi.emergency.entity.EmergencyContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Emergency Contact Repository - Acil Durum İletişim Kişileri Veri Erişim Katmanı
 * Bu interface, emergency_contacts tablosu için veri erişim operasyonlarını sağlar
 */
@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, String> {

    /**
     * Kullanıcının acil durum iletişim kişilerini öncelik sırasına göre al
     */
    List<EmergencyContact> findByUserIdOrderByPriorityAsc(String userId);

    /**
     * Kullanıcının aktif acil durum iletişim kişilerini öncelik sırasına göre al
     */
    List<EmergencyContact> findByUserIdAndEnabledTrueOrderByPriorityAsc(String userId);

    /**
     * Kullanıcının acil durum iletişim kişilerini sayfalı al
     */
    Page<EmergencyContact> findByUserId(String userId, Pageable pageable);

    /**
     * Kullanıcının belirli türdeki iletişim kişilerini al
     */
    List<EmergencyContact> findByUserIdAndContactTypeOrderByPriorityAsc(String userId, String contactType);

    /**
     * Kullanıcının belirli öncelik seviyesindeki aktif iletişim kişilerini al
     */
    List<EmergencyContact> findByUserIdAndEnabledTrueAndPriorityLessThanEqualOrderByPriorityAsc(String userId, Integer maxPriority);

    /**
     * Kullanıcının aktif iletişim kişi sayısını al
     */
    Long countByUserIdAndEnabledTrue(String userId);

    /**
     * Telefon numarası ile iletişim kişisini bul (tüm kullanıcılar arasında)
     */
    Optional<EmergencyContact> findByPhoneNumber(String phoneNumber);

    /**
     * Kullanıcının belirli telefon numarasına sahip iletişim kişisini bul
     */
    Optional<EmergencyContact> findByUserIdAndPhoneNumber(String userId, String phoneNumber);

    /**
     * E-posta adresi ile iletişim kişisini bul
     */
    Optional<EmergencyContact> findByEmail(String email);

    /**
     * Kullanıcının belirli e-posta adresine sahip iletişim kişisini bul
     */
    Optional<EmergencyContact> findByUserIdAndEmail(String userId, String email);

    /**
     * Doğrulama durumuna göre iletişim kişilerini al
     */
    List<EmergencyContact> findByUserIdAndVerificationStatusOrderByPriorityAsc(String userId, String verificationStatus);

    /**
     * Doğrulanmış aktif iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.verificationStatus = 'VERIFIED' ORDER BY e.priority ASC")
    List<EmergencyContact> findVerifiedActiveContacts(@Param("userId") String userId);

    /**
     * Belirli tarihten sonra oluşturulan iletişim kişilerini al
     */
    List<EmergencyContact> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(String userId, LocalDateTime afterDate);

    /**
     * Belirli tarihten sonra güncellenen iletişim kişilerini al
     */
    List<EmergencyContact> findByUserIdAndUpdatedAtAfterOrderByUpdatedAtDesc(String userId, LocalDateTime afterDate);

    /**
     * Son iletişim kurulduğu tarihe göre sırala
     */
    List<EmergencyContact> findByUserIdOrderByLastContactedAtDesc(String userId);

    /**
     * Hiç iletişim kurulmamış kişileri al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.lastContactedAt IS NULL ORDER BY e.priority ASC")
    List<EmergencyContact> findNeverContactedContacts(@Param("userId") String userId);

    /**
     * Belirli bildirim sayısının üzerinde olan kişileri al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.notificationCount >= :minCount ORDER BY e.notificationCount DESC")
    List<EmergencyContact> findContactsWithMinimumNotifications(@Param("userId") String userId, @Param("minCount") Integer minCount);

    /**
     * 24/7 erişilebilir kişileri al
     */
    List<EmergencyContact> findByUserIdAndAvailable24x7TrueOrderByPriorityAsc(String userId);

    /**
     * Otomatik bildirim aktif olan kişileri al
     */
    List<EmergencyContact> findByUserIdAndAutoNotifyTrueOrderByPriorityAsc(String userId);

    /**
     * Belirli dildeki iletişim kişilerini al
     */
    List<EmergencyContact> findByUserIdAndPreferredLanguageOrderByPriorityAsc(String userId, String language);

    /**
     * SMS aktif olan iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.smsEnabled = true ORDER BY e.priority ASC")
    List<EmergencyContact> findSmsEnabledContacts(@Param("userId") String userId);

    /**
     * Arama aktif olan iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.callEnabled = true ORDER BY e.priority ASC")
    List<EmergencyContact> findCallEnabledContacts(@Param("userId") String userId);

    /**
     * E-posta aktif olan iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.emailEnabled = true ORDER BY e.priority ASC")
    List<EmergencyContact> findEmailEnabledContacts(@Param("userId") String userId);

    /**
     * WhatsApp aktif olan iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.whatsappEnabled = true ORDER BY e.priority ASC")
    List<EmergencyContact> findWhatsAppEnabledContacts(@Param("userId") String userId);

    /**
     * Push notification aktif olan iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.pushEnabled = true ORDER BY e.priority ASC")
    List<EmergencyContact> findPushEnabledContacts(@Param("userId") String userId);

    /**
     * Belirli bildirim yöntemini tercih eden kişileri al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.preferredNotificationMethod = :method ORDER BY e.priority ASC")
    List<EmergencyContact> findByPreferredNotificationMethod(@Param("userId") String userId, @Param("method") String method);

    /**
     * Yüksek başarı oranına sahip iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.notificationSuccessRate >= :minRate ORDER BY e.notificationSuccessRate DESC, e.priority ASC")
    List<EmergencyContact> findHighSuccessRateContacts(@Param("userId") String userId, @Param("minRate") Double minRate);

    /**
     * Hızlı yanıt veren iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.averageResponseTimeMinutes IS NOT NULL AND e.averageResponseTimeMinutes <= :maxMinutes ORDER BY e.averageResponseTimeMinutes ASC")
    List<EmergencyContact> findFastRespondingContacts(@Param("userId") String userId, @Param("maxMinutes") Integer maxMinutes);

    /**
     * Daha önce yanıt vermiş iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND e.hasRespondedBefore = true ORDER BY e.priority ASC")
    List<EmergencyContact> findPreviouslyRespondedContacts(@Param("userId") String userId);

    /**
     * Belirli şehirdeki iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.addressCity = :city ORDER BY e.priority ASC")
    List<EmergencyContact> findContactsInCity(@Param("userId") String userId, @Param("city") String city);

    /**
     * Belirli ülkedeki iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.addressCountryCode = :countryCode ORDER BY e.priority ASC")
    List<EmergencyContact> findContactsInCountry(@Param("userId") String userId, @Param("countryCode") String countryCode);

    /**
     * Koordinat bazlı yakındaki iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true AND " +
           "e.addressLatitude IS NOT NULL AND e.addressLongitude IS NOT NULL AND " +
           "ABS(e.addressLatitude - :latitude) <= :latRange AND ABS(e.addressLongitude - :longitude) <= :lngRange " +
           "ORDER BY e.priority ASC")
    List<EmergencyContact> findNearbyContacts(@Param("userId") String userId,
                                            @Param("latitude") Double latitude,
                                            @Param("longitude") Double longitude,
                                            @Param("latRange") Double latRange,
                                            @Param("lngRange") Double lngRange);

    /**
     * Doğrulama süresi dolmuş iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.verificationStatus = 'VERIFIED' AND e.lastVerifiedAt < :thresholdDate")
    List<EmergencyContact> findContactsNeedingReVerification(@Param("thresholdDate") LocalDateTime thresholdDate);

    /**
     * Doğrulama bekleyen iletişim kişilerini al
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.verificationStatus = 'PENDING' AND e.createdAt < :thresholdDate")
    List<EmergencyContact> findPendingVerificationContacts(@Param("thresholdDate") LocalDateTime thresholdDate);

    /**
     * İstatistik sorguları
     */
    @Query("SELECT COUNT(e) FROM EmergencyContact e WHERE e.userId = :userId")
    Long countAllContactsByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(e) FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true")
    Long countActiveContactsByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(e) FROM EmergencyContact e WHERE e.userId = :userId AND e.verificationStatus = 'VERIFIED'")
    Long countVerifiedContactsByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(e) FROM EmergencyContact e WHERE e.userId = :userId AND e.contactType = :contactType")
    Long countContactsByTypeAndUserId(@Param("userId") String userId, @Param("contactType") String contactType);

    @Query("SELECT AVG(e.notificationSuccessRate) FROM EmergencyContact e WHERE e.userId = :userId AND e.notificationSuccessRate IS NOT NULL")
    Double getAverageSuccessRateByUserId(@Param("userId") String userId);

    @Query("SELECT AVG(e.averageResponseTimeMinutes) FROM EmergencyContact e WHERE e.userId = :userId AND e.averageResponseTimeMinutes IS NOT NULL")
    Double getAverageResponseTimeByUserId(@Param("userId") String userId);

    /**
     * Toplu işlemler için ID'leri al
     */
    @Query("SELECT e.id FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true")
    List<String> findActiveContactIdsByUserId(@Param("userId") String userId);

    @Query("SELECT e.id FROM EmergencyContact e WHERE e.verificationStatus = 'PENDING'")
    List<String> findPendingVerificationContactIds();

    /**
     * Metin tabanlı arama
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND " +
           "(LOWER(e.contactName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.phoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.relationship) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY e.priority ASC")
    List<EmergencyContact> searchContacts(@Param("userId") String userId, @Param("searchTerm") String searchTerm);

    /**
     * Son aktivite bazlı sıralama
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId ORDER BY " +
           "CASE WHEN e.lastContactedAt IS NOT NULL THEN e.lastContactedAt ELSE e.updatedAt END DESC")
    List<EmergencyContact> findByUserIdOrderByLastActivity(@Param("userId") String userId);

    /**
     * Performans bazlı sıralama
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND e.enabled = true " +
           "ORDER BY e.notificationSuccessRate DESC NULLS LAST, e.averageResponseTimeMinutes ASC NULLS LAST, e.priority ASC")
    List<EmergencyContact> findByUserIdOrderByPerformance(@Param("userId") String userId);

    /**
     * Kapsamlı filtreleme
     */
    @Query("SELECT e FROM EmergencyContact e WHERE e.userId = :userId AND " +
           "(:enabled IS NULL OR e.enabled = :enabled) AND " +
           "(:contactType IS NULL OR e.contactType = :contactType) AND " +
           "(:verificationStatus IS NULL OR e.verificationStatus = :verificationStatus) AND " +
           "(:available24x7 IS NULL OR e.available24x7 = :available24x7) " +
           "ORDER BY e.priority ASC")
    Page<EmergencyContact> findWithFilters(@Param("userId") String userId,
                                         @Param("enabled") Boolean enabled,
                                         @Param("contactType") String contactType,
                                         @Param("verificationStatus") String verificationStatus,
                                         @Param("available24x7") Boolean available24x7,
                                         Pageable pageable);
}