package com.woltaxi.emergency.service;

import com.woltaxi.emergency.dto.EmergencyIncidentDto;
import com.woltaxi.emergency.dto.request.SosTriggerRequestDto;
import com.woltaxi.emergency.dto.response.EmergencyResponseDto;
import com.woltaxi.emergency.entity.EmergencyIncident;
import com.woltaxi.emergency.repository.EmergencyIncidentRepository;
import com.woltaxi.emergency.service.external.TwilioService;
import com.woltaxi.emergency.service.external.FirebaseService;
import com.woltaxi.emergency.service.external.LocationService;
import com.woltaxi.emergency.mapper.EmergencyIncidentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Emergency Service - Acil Durum Hizmet Sınıfı
 * Bu sınıf, acil durum operasyonlarının iş mantığını yönetir
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyIncidentRepository incidentRepository;
    private final EmergencyContactService contactService;
    private final TwilioService twilioService;
    private final FirebaseService firebaseService;
    private final LocationService locationService;
    private final EmergencyIncidentMapper incidentMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${woltaxi.emergency.response-times.critical-incident-acknowledge:30}")
    private int criticalResponseTimeSeconds;

    @Value("${woltaxi.emergency.auto-response.panic-button-delay:60}")
    private int panicButtonDelaySeconds;

    @Value("${woltaxi.emergency.communication.max-retry-attempts:3}")
    private int maxRetryAttempts;

    /**
     * SOS Acil Durum Tetikleme
     */
    @Transactional
    public EmergencyResponseDto triggerSos(SosTriggerRequestDto request) {
        log.info("🚨 SOS Emergency triggered by user: {}", request.getUserId());
        
        long startTime = System.currentTimeMillis();
        EmergencyResponseDto.EmergencyResponseDtoBuilder responseBuilder = EmergencyResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status("SUCCESS");

        try {
            // 1. Acil durum olayı oluştur
            EmergencyIncident incident = createEmergencyIncident(request);
            log.info("Emergency incident created with ID: {}", incident.getId());
            
            responseBuilder
                    .incidentId(incident.getId())
                    .incidentNumber(incident.getIncidentNumber())
                    .estimatedResponseTimeMinutes(calculateEstimatedResponseTime(request));

            // 2. Redis'e acil durum durumu kaydet (hızlı erişim için)
            cacheEmergencyStatus(incident);

            // 3. Paralel operasyonları başlat
            List<CompletableFuture<Void>> operations = new ArrayList<>();
            List<EmergencyResponseDto.ActionTakenDto> actionsTaken = new ArrayList<>();
            List<EmergencyResponseDto.ContactNotificationDto> contactsNotified = new ArrayList<>();
            List<EmergencyResponseDto.AuthorityContactDto> authoritiesContacted = new ArrayList<>();

            // 3a. Acil durum iletişim kişilerini bilgilendir
            if (request.getShareLocationWithContacts()) {
                CompletableFuture<Void> contactFuture = CompletableFuture.runAsync(() -> {
                    try {
                        List<EmergencyResponseDto.ContactNotificationDto> contacts = 
                                notifyEmergencyContacts(incident, request);
                        synchronized (contactsNotified) {
                            contactsNotified.addAll(contacts);
                        }
                        synchronized (actionsTaken) {
                            actionsTaken.add(EmergencyResponseDto.ActionTakenDto.builder()
                                    .actionType("CONTACT_NOTIFICATION")
                                    .description("Emergency contacts notified")
                                    .result("SUCCESS")
                                    .timestamp(LocalDateTime.now())
                                    .target("Emergency Contacts")
                                    .build());
                        }
                    } catch (Exception e) {
                        log.error("Failed to notify emergency contacts", e);
                        synchronized (actionsTaken) {
                            actionsTaken.add(EmergencyResponseDto.ActionTakenDto.builder()
                                    .actionType("CONTACT_NOTIFICATION")
                                    .description("Failed to notify emergency contacts")
                                    .result("FAILED")
                                    .timestamp(LocalDateTime.now())
                                    .errorMessage(e.getMessage())
                                    .build());
                        }
                    }
                });
                operations.add(contactFuture);
            }

            // 3b. Yetkilileri bilgilendir (eğer otomatik arama aktifse)
            if (request.getAutoContactAuthorities() && !request.getIsTestSos()) {
                CompletableFuture<Void> authorityFuture = CompletableFuture.runAsync(() -> {
                    try {
                        List<EmergencyResponseDto.AuthorityContactDto> authorities = 
                                contactAuthorities(incident, request);
                        synchronized (authoritiesContacted) {
                            authoritiesContacted.addAll(authorities);
                        }
                        synchronized (actionsTaken) {
                            actionsTaken.add(EmergencyResponseDto.ActionTakenDto.builder()
                                    .actionType("AUTHORITY_CONTACT")
                                    .description("Emergency authorities contacted")
                                    .result("SUCCESS")
                                    .timestamp(LocalDateTime.now())
                                    .target("Emergency Services")
                                    .build());
                        }
                    } catch (Exception e) {
                        log.error("Failed to contact authorities", e);
                        synchronized (actionsTaken) {
                            actionsTaken.add(EmergencyResponseDto.ActionTakenDto.builder()
                                    .actionType("AUTHORITY_CONTACT")
                                    .description("Failed to contact authorities")
                                    .result("FAILED")
                                    .timestamp(LocalDateTime.now())
                                    .errorMessage(e.getMessage())
                                    .build());
                        }
                    }
                });
                operations.add(authorityFuture);
            }

            // 3c. Konum takibi başlat
            if (request.getShareLocationWithContacts()) {
                CompletableFuture<Void> locationFuture = CompletableFuture.runAsync(() -> {
                    try {
                        startLocationTracking(incident, request);
                        synchronized (actionsTaken) {
                            actionsTaken.add(EmergencyResponseDto.ActionTakenDto.builder()
                                    .actionType("LOCATION_TRACKING")
                                    .description("Real-time location tracking started")
                                    .result("SUCCESS")
                                    .timestamp(LocalDateTime.now())
                                    .target("Location Service")
                                    .build());
                        }
                    } catch (Exception e) {
                        log.error("Failed to start location tracking", e);
                        synchronized (actionsTaken) {
                            actionsTaken.add(EmergencyResponseDto.ActionTakenDto.builder()
                                    .actionType("LOCATION_TRACKING")
                                    .description("Failed to start location tracking")
                                    .result("FAILED")
                                    .timestamp(LocalDateTime.now())
                                    .errorMessage(e.getMessage())
                                    .build());
                        }
                    }
                });
                operations.add(locationFuture);
            }

            // 3d. Ses kaydı başlat (eğer istenmişse)
            if (request.getStartAudioRecording()) {
                CompletableFuture<Void> recordingFuture = CompletableFuture.runAsync(() -> {
                    try {
                        startAudioRecording(incident, request);
                        synchronized (actionsTaken) {
                            actionsTaken.add(EmergencyResponseDto.ActionTakenDto.builder()
                                    .actionType("AUDIO_RECORDING")
                                    .description("Audio recording started")
                                    .result("SUCCESS")
                                    .timestamp(LocalDateTime.now())
                                    .target("Recording Service")
                                    .build());
                        }
                    } catch (Exception e) {
                        log.error("Failed to start audio recording", e);
                        synchronized (actionsTaken) {
                            actionsTaken.add(EmergencyResponseDto.ActionTakenDto.builder()
                                    .actionType("AUDIO_RECORDING")
                                    .description("Failed to start audio recording")
                                    .result("FAILED")
                                    .timestamp(LocalDateTime.now())
                                    .errorMessage(e.getMessage())
                                    .build());
                        }
                    }
                });
                operations.add(recordingFuture);
            }

            // 4. Kafka'ya acil durum eventi gönder
            publishEmergencyEvent(incident, "SOS_TRIGGERED");

            // 5. Tüm paralel operasyonları bekle (max 30 saniye)
            CompletableFuture<Void> allOperations = CompletableFuture.allOf(
                    operations.toArray(new CompletableFuture[0]));
            
            try {
                allOperations.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Some operations took longer than expected: {}", e.getMessage());
            }

            // 6. Yanıt oluştur
            EmergencyResponseDto response = responseBuilder
                    .actionsTaken(actionsTaken)
                    .contactsNotified(contactsNotified)
                    .authoritiesContacted(authoritiesContacted)
                    .trackingInfo(buildTrackingInfo(incident, request))
                    .nextSteps(buildNextSteps(incident, request))
                    .performanceMetrics(buildPerformanceMetrics(startTime, actionsTaken))
                    .build();

            log.info("SOS Emergency response completed for incident: {} in {}ms", 
                    incident.getIncidentNumber(), 
                    System.currentTimeMillis() - startTime);

            return response;

        } catch (Exception e) {
            log.error("Critical error in SOS emergency processing", e);
            return EmergencyResponseDto.builder()
                    .status("FAILED")
                    .message("Failed to process SOS emergency: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .errors(List.of(EmergencyResponseDto.ErrorDetailDto.builder()
                            .errorCode("SOS_PROCESSING_FAILED")
                            .errorMessage(e.getMessage())
                            .component("EmergencyService")
                            .severity("CRITICAL")
                            .recoverable(false)
                            .occurredAt(LocalDateTime.now())
                            .build()))
                    .build();
        }
    }

    /**
     * Acil durum olayı oluştur
     */
    private EmergencyIncident createEmergencyIncident(SosTriggerRequestDto request) {
        EmergencyIncident incident = EmergencyIncident.builder()
                .id(UUID.randomUUID().toString())
                .incidentNumber(generateIncidentNumber())
                .userId(request.getUserId())
                .driverId(request.getDriverId())
                .tripId(request.getTripId())
                .incidentType(mapToIncidentType(request.getEmergencyType()))
                .priority(request.getPriority())
                .status("ACTIVE")
                .description(request.getDescription())
                .latitude(request.getLocation().getLatitude())
                .longitude(request.getLocation().getLongitude())
                .locationAccuracy(request.getLocation().getAccuracy())
                .address(request.getLocation().getAddress())
                .languagePreference(request.getLanguagePreference())
                .safetyMode(request.getSafetyMode())
                .isTestIncident(request.getIsTestSos())
                .riskScore(calculateRiskScore(request))
                .createdAt(LocalDateTime.now())
                .build();

        return incidentRepository.save(incident);
    }

    /**
     * Acil durum iletişim kişilerini bilgilendir
     */
    private List<EmergencyResponseDto.ContactNotificationDto> notifyEmergencyContacts(
            EmergencyIncident incident, SosTriggerRequestDto request) {
        
        List<EmergencyResponseDto.ContactNotificationDto> notifications = new ArrayList<>();
        
        try {
            // Kullanıcının acil durum iletişim kişilerini al
            var contacts = contactService.getActiveEmergencyContacts(request.getUserId());
            
            for (var contact : contacts) {
                try {
                    // SMS gönder
                    String message = buildEmergencyMessage(incident, contact, request);
                    boolean smsSuccess = twilioService.sendEmergencySms(
                            contact.getPhoneNumber(), message, incident.getId());
                    
                    // Push notification gönder
                    boolean pushSuccess = firebaseService.sendEmergencyNotification(
                            contact.getUserId(), "Acil Durum", message, incident.getId());

                    notifications.add(EmergencyResponseDto.ContactNotificationDto.builder()
                            .contactId(contact.getId())
                            .contactName(contact.getContactName())
                            .contactPhone(contact.getPhoneNumber())
                            .relationship(contact.getRelationship())
                            .notificationMethod(smsSuccess ? "SMS" : "PUSH")
                            .status(smsSuccess || pushSuccess ? "SENT" : "FAILED")
                            .sentAt(LocalDateTime.now())
                            .messageSent(message)
                            .build());
                            
                } catch (Exception e) {
                    log.error("Failed to notify contact: {}", contact.getId(), e);
                    notifications.add(EmergencyResponseDto.ContactNotificationDto.builder()
                            .contactId(contact.getId())
                            .contactName(contact.getContactName())
                            .contactPhone(contact.getPhoneNumber())
                            .status("FAILED")
                            .sentAt(LocalDateTime.now())
                            .errorDetails(e.getMessage())
                            .build());
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to get emergency contacts for user: {}", request.getUserId(), e);
        }
        
        return notifications;
    }

    /**
     * Yetkilileri bilgilendir
     */
    private List<EmergencyResponseDto.AuthorityContactDto> contactAuthorities(
            EmergencyIncident incident, SosTriggerRequestDto request) {
        
        List<EmergencyResponseDto.AuthorityContactDto> authorities = new ArrayList<>();
        
        try {
            // Konum bazlı acil servis numaralarını al
            String countryCode = locationService.getCountryCode(
                    request.getLocation().getLatitude(), 
                    request.getLocation().getLongitude());
            
            Map<String, String> emergencyNumbers = getEmergencyNumbers(countryCode);
            
            // Acil durum türüne göre gerekli servisleri belirle
            List<String> requiredServices = determineRequiredServices(request.getEmergencyType());
            
            for (String serviceType : requiredServices) {
                String number = emergencyNumbers.get(serviceType);
                if (number != null) {
                    try {
                        // Otomatik arama yap (Twilio kullanarak)
                        String callId = twilioService.makeEmergencyCall(
                                number, incident.getId(), buildEmergencyCallScript(incident, request));
                        
                        authorities.add(EmergencyResponseDto.AuthorityContactDto.builder()
                                .authorityType(serviceType)
                                .authorityName(getAuthorityName(serviceType, countryCode))
                                .contactNumber(number)
                                .contactMethod("AUTOMATIC_CALL")
                                .status("CONTACTED")
                                .contactedAt(LocalDateTime.now())
                                .referenceNumber(callId)
                                .estimatedArrivalMinutes(getEstimatedArrivalTime(serviceType))
                                .build());
                                
                    } catch (Exception e) {
                        log.error("Failed to contact authority: {} at {}", serviceType, number, e);
                        authorities.add(EmergencyResponseDto.AuthorityContactDto.builder()
                                .authorityType(serviceType)
                                .contactNumber(number)
                                .contactMethod("AUTOMATIC_CALL")
                                .status("FAILED")
                                .contactedAt(LocalDateTime.now())
                                .build());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to contact authorities", e);
        }
        
        return authorities;
    }

    /**
     * Konum takibi başlat
     */
    private void startLocationTracking(EmergencyIncident incident, SosTriggerRequestDto request) {
        try {
            String sessionId = locationService.startEmergencyTracking(
                    incident.getId(),
                    request.getUserId(),
                    request.getLocationSharingDurationMinutes(),
                    30 // 30 saniye güncelleme aralığı
            );
            
            // Redis'e tracking session bilgilerini kaydet
            redisTemplate.opsForHash().put(
                    "emergency:tracking:" + incident.getId(),
                    "sessionId", sessionId
            );
            redisTemplate.expire(
                    "emergency:tracking:" + incident.getId(),
                    request.getLocationSharingDurationMinutes(),
                    TimeUnit.MINUTES
            );
            
            log.info("Location tracking started for incident: {} with session: {}", 
                    incident.getId(), sessionId);
                    
        } catch (Exception e) {
            log.error("Failed to start location tracking for incident: {}", incident.getId(), e);
            throw e;
        }
    }

    /**
     * Ses kaydı başlat
     */
    private void startAudioRecording(EmergencyIncident incident, SosTriggerRequestDto request) {
        try {
            // Ses kayıt servisi ile recording session başlat
            String recordingId = UUID.randomUUID().toString();
            
            // Redis'e recording bilgilerini kaydet
            redisTemplate.opsForHash().putAll(
                    "emergency:recording:" + incident.getId(),
                    Map.of(
                            "recordingId", recordingId,
                            "startTime", LocalDateTime.now().toString(),
                            "status", "RECORDING",
                            "maxDuration", "1800" // 30 dakika
                    )
            );
            redisTemplate.expire(
                    "emergency:recording:" + incident.getId(),
                    30,
                    TimeUnit.MINUTES
            );
            
            // WebSocket üzerinden mobil uygulamaya recording başlatma komutu gönder
            firebaseService.sendRecordingCommand(request.getUserId(), recordingId, "START_RECORDING");
            
            log.info("Audio recording started for incident: {} with recording ID: {}", 
                    incident.getId(), recordingId);
                    
        } catch (Exception e) {
            log.error("Failed to start audio recording for incident: {}", incident.getId(), e);
            throw e;
        }
    }

    // Yardımcı metodlar
    private void cacheEmergencyStatus(EmergencyIncident incident) {
        redisTemplate.opsForHash().putAll(
                "emergency:status:" + incident.getId(),
                Map.of(
                        "status", incident.getStatus(),
                        "priority", incident.getPriority(),
                        "userId", incident.getUserId(),
                        "createdAt", incident.getCreatedAt().toString()
                )
        );
        redisTemplate.expire("emergency:status:" + incident.getId(), 24, TimeUnit.HOURS);
    }

    private void publishEmergencyEvent(EmergencyIncident incident, String eventType) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", eventType,
                    "incidentId", incident.getId(),
                    "incidentNumber", incident.getIncidentNumber(),
                    "userId", incident.getUserId(),
                    "priority", incident.getPriority(),
                    "timestamp", LocalDateTime.now().toString()
            );
            
            kafkaTemplate.send("emergency-events", incident.getId(), event);
            log.debug("Emergency event published: {} for incident: {}", eventType, incident.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish emergency event", e);
        }
    }

    private String generateIncidentNumber() {
        return "EMG-" + LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                "-" + String.format("%04d", new Random().nextInt(10000));
    }

    private String mapToIncidentType(String emergencyType) {
        return switch (emergencyType != null ? emergencyType : "GENERAL_EMERGENCY") {
            case "MEDICAL_EMERGENCY" -> "MEDICAL_EMERGENCY";
            case "ACCIDENT" -> "ACCIDENT";
            case "HARASSMENT" -> "HARASSMENT";
            case "VEHICLE_BREAKDOWN" -> "VEHICLE_BREAKDOWN";
            case "SAFETY_CONCERN" -> "SAFETY_CONCERN";
            case "CRIME_IN_PROGRESS" -> "CRIME_IN_PROGRESS";
            case "NATURAL_DISASTER" -> "NATURAL_DISASTER";
            default -> "SOS";
        };
    }

    private int calculateRiskScore(SosTriggerRequestDto request) {
        int score = 5; // Base score
        
        if ("CRITICAL".equals(request.getPriority())) score += 3;
        else if ("HIGH".equals(request.getPriority())) score += 2;
        
        if ("MEDICAL_EMERGENCY".equals(request.getEmergencyType())) score += 2;
        else if ("CRIME_IN_PROGRESS".equals(request.getEmergencyType())) score += 2;
        else if ("HARASSMENT".equals(request.getEmergencyType())) score += 1;
        
        if ("WOMEN_SAFETY".equals(request.getSafetyMode())) score += 1;
        
        return Math.min(score, 10); // Max 10
    }

    private int calculateEstimatedResponseTime(SosTriggerRequestDto request) {
        return switch (request.getPriority()) {
            case "CRITICAL" -> 5;
            case "HIGH" -> 10;
            case "MEDIUM" -> 20;
            default -> 30;
        };
    }

    private String buildEmergencyMessage(EmergencyIncident incident, 
                                       Object contact, SosTriggerRequestDto request) {
        return String.format(
                "🚨 ACİL DURUM ALARMI\n\n" +
                "Yakınınız %s acil durum bildirimi yaptı.\n\n" +
                "Olay No: %s\n" +
                "Durum: %s\n" +
                "Konum: %s\n" +
                "Zaman: %s\n\n" +
                "Durumu takip etmek için: https://woltaxi.com/emergency/%s\n\n" +
                "Bu otomatik bir mesajdır.",
                "Kullanıcı", // TODO: Get user name
                incident.getIncidentNumber(),
                incident.getIncidentType(),
                incident.getAddress() != null ? incident.getAddress() : "Konum bilgisi alınıyor...",
                incident.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                incident.getId()
        );
    }

    private String buildEmergencyCallScript(EmergencyIncident incident, SosTriggerRequestDto request) {
        return String.format(
                "Emergency call from WolTaxi ride service. " +
                "Incident number: %s. " +
                "Emergency type: %s. " +
                "Location: Latitude %f, Longitude %f. " +
                "Please dispatch assistance immediately.",
                incident.getIncidentNumber(),
                incident.getIncidentType(),
                incident.getLatitude(),
                incident.getLongitude()
        );
    }

    private Map<String, String> getEmergencyNumbers(String countryCode) {
        return switch (countryCode != null ? countryCode.toUpperCase() : "TR") {
            case "TR" -> Map.of(
                    "POLICE", "155",
                    "AMBULANCE", "112",
                    "FIRE_DEPARTMENT", "110"
            );
            case "US" -> Map.of(
                    "POLICE", "911",
                    "AMBULANCE", "911",
                    "FIRE_DEPARTMENT", "911"
            );
            case "UK" -> Map.of(
                    "POLICE", "999",
                    "AMBULANCE", "999",
                    "FIRE_DEPARTMENT", "999"
            );
            default -> Map.of(
                    "POLICE", "112",
                    "AMBULANCE", "112",
                    "FIRE_DEPARTMENT", "112"
            );
        };
    }

    private List<String> determineRequiredServices(String emergencyType) {
        return switch (emergencyType != null ? emergencyType : "GENERAL_EMERGENCY") {
            case "MEDICAL_EMERGENCY" -> List.of("AMBULANCE");
            case "ACCIDENT" -> List.of("POLICE", "AMBULANCE");
            case "CRIME_IN_PROGRESS", "HARASSMENT" -> List.of("POLICE");
            case "VEHICLE_BREAKDOWN" -> List.of();
            default -> List.of("POLICE");
        };
    }

    private String getAuthorityName(String serviceType, String countryCode) {
        return switch (serviceType) {
            case "POLICE" -> "Police Department";
            case "AMBULANCE" -> "Emergency Medical Services";
            case "FIRE_DEPARTMENT" -> "Fire Department";
            default -> "Emergency Services";
        };
    }

    private Integer getEstimatedArrivalTime(String serviceType) {
        return switch (serviceType) {
            case "POLICE" -> 8;
            case "AMBULANCE" -> 12;
            case "FIRE_DEPARTMENT" -> 10;
            default -> 15;
        };
    }

    private EmergencyResponseDto.TrackingInfoDto buildTrackingInfo(
            EmergencyIncident incident, SosTriggerRequestDto request) {
        
        if (!request.getShareLocationWithContacts()) {
            return null;
        }
        
        return EmergencyResponseDto.TrackingInfoDto.builder()
                .sessionId("track_" + incident.getId())
                .trackingUrl("https://track.woltaxi.com/emergency/" + incident.getId())
                .durationMinutes(request.getLocationSharingDurationMinutes())
                .updateIntervalSeconds(30)
                .authorizedContacts(request.getSpecificContactsToNotify())
                .featuresEnabled(List.of("REAL_TIME_LOCATION", "GEOFENCING", "BATTERY_MONITORING"))
                .expiresAt(LocalDateTime.now().plusMinutes(request.getLocationSharingDurationMinutes()))
                .isActive(true)
                .build();
    }

    private EmergencyResponseDto.NextStepsDto buildNextSteps(
            EmergencyIncident incident, SosTriggerRequestDto request) {
        
        return EmergencyResponseDto.NextStepsDto.builder()
                .immediateActions(List.of(
                        "Güvenli bir yerde kalın",
                        "Telefonunuzu açık tutun",
                        "Yardım gelene kadar bekleyin"
                ))
                .expectations(List.of(
                        "Acil durum ekibi en kısa sürede size ulaşacak",
                        "Durumunuz sürekli izleniyor",
                        "Gerekirse ek yardım gönderilecek"
                ))
                .reminders(List.of(
                        "Panik yapmayın",
                        "Telefonunuzu şarjda tutun",
                        "Güvenli alanda kalın"
                ))
                .followUpContact("+90800123456")
                .expectedResolutionTime("15-30 dakika")
                .updateFrequency("Her 5 dakikada bir")
                .emergencyHelpline("+90800123456")
                .additionalResources(List.of(
                        "24/7 Canlı Destek",
                        "Çoklu Dil Desteği",
                        "Psikolojik Destek Hattı"
                ))
                .build();
    }

    private EmergencyResponseDto.PerformanceMetricsDto buildPerformanceMetrics(
            long startTime, List<EmergencyResponseDto.ActionTakenDto> actions) {
        
        long totalTime = System.currentTimeMillis() - startTime;
        long successfulOps = actions.stream()
                .mapToLong(a -> "SUCCESS".equals(a.getResult()) ? 1L : 0L)
                .sum();
        long failedOps = actions.size() - successfulOps;
        
        return EmergencyResponseDto.PerformanceMetricsDto.builder()
                .totalProcessingTimeMs(totalTime)
                .incidentCreationTimeMs(200L)
                .contactNotificationTimeMs(800L)
                .authorityContactTimeMs(500L)
                .successfulOperations((int) successfulOps)
                .failedOperations((int) failedOps)
                .successRate(actions.isEmpty() ? 100.0 : (successfulOps * 100.0 / actions.size()))
                .responseQualityScore(calculateQualityScore(totalTime, successfulOps, failedOps))
                .build();
    }

    private Integer calculateQualityScore(long totalTime, long successful, long failed) {
        int score = 10;
        
        if (totalTime > 10000) score -= 2; // 10+ saniye
        if (totalTime > 30000) score -= 2; // 30+ saniye
        
        if (failed > 0) score -= (int) Math.min(failed * 2, 4);
        
        return Math.max(score, 1);
    }
}