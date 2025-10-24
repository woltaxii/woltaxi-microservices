package com.woltaxi.emergency.service.external;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.http.TwilioRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * Twilio Service - SMS ve Arama Hizmetleri
 * Bu sınıf, Twilio API'si kullanarak SMS gönderme ve arama yapma işlemlerini yönetir
 */
@Slf4j
@Service
public class TwilioService {

    @Value("${woltaxi.emergency.integrations.twilio.account-sid}")
    private String accountSid;

    @Value("${woltaxi.emergency.integrations.twilio.auth-token}")
    private String authToken;

    @Value("${woltaxi.emergency.integrations.twilio.emergency-number}")
    private String emergencyNumber;

    @PostConstruct
    public void init() {
        try {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Twilio", e);
        }
    }

    /**
     * Acil durum SMS gönder
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean sendEmergencySms(String toPhoneNumber, String messageBody, String incidentId) {
        try {
            log.info("Sending emergency SMS to: {} for incident: {}", toPhoneNumber, incidentId);
            
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(emergencyNumber),
                    messageBody
            ).create();

            log.info("Emergency SMS sent successfully. SID: {} for incident: {}", 
                    message.getSid(), incidentId);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send emergency SMS to: {} for incident: {}", 
                    toPhoneNumber, incidentId, e);
            return false;
        }
    }

    /**
     * Acil durum arama yap
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public String makeEmergencyCall(String toPhoneNumber, String incidentId, String callScript) {
        try {
            log.info("Making emergency call to: {} for incident: {}", toPhoneNumber, incidentId);
            
            // TwiML URL'i oluştur (call script için)
            String twimlUrl = createTwimlUrl(callScript, incidentId);
            
            Call call = Call.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(emergencyNumber),
                    URI.create(twimlUrl)
            ).create();

            log.info("Emergency call initiated successfully. Call SID: {} for incident: {}", 
                    call.getSid(), incidentId);
            return call.getSid();
            
        } catch (Exception e) {
            log.error("Failed to make emergency call to: {} for incident: {}", 
                    toPhoneNumber, incidentId, e);
            throw new RuntimeException("Emergency call failed", e);
        }
    }

    /**
     * Asenkron SMS gönderimi
     */
    public CompletableFuture<Boolean> sendEmergencySmsAsync(String toPhoneNumber, 
                                                           String messageBody, 
                                                           String incidentId) {
        return CompletableFuture.supplyAsync(() -> 
                sendEmergencySms(toPhoneNumber, messageBody, incidentId));
    }

    /**
     * Asenkron arama yapma
     */
    public CompletableFuture<String> makeEmergencyCallAsync(String toPhoneNumber, 
                                                           String incidentId, 
                                                           String callScript) {
        return CompletableFuture.supplyAsync(() -> 
                makeEmergencyCall(toPhoneNumber, incidentId, callScript));
    }

    /**
     * Toplu SMS gönderimi
     */
    public void sendBulkEmergencySms(java.util.List<String> phoneNumbers, 
                                   String messageBody, 
                                   String incidentId) {
        log.info("Sending bulk emergency SMS to {} recipients for incident: {}", 
                phoneNumbers.size(), incidentId);
        
        java.util.List<CompletableFuture<Boolean>> futures = phoneNumbers.stream()
                .map(phone -> sendEmergencySmsAsync(phone, messageBody, incidentId))
                .toList();
        
        // Tüm SMS'lerin gönderilmesini bekle
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    long successCount = futures.stream()
                            .mapToLong(future -> {
                                try {
                                    return future.get() ? 1L : 0L;
                                } catch (Exception e) {
                                    return 0L;
                                }
                            })
                            .sum();
                    
                    log.info("Bulk SMS completed for incident: {}. Success: {}/{}", 
                            incidentId, successCount, phoneNumbers.size());
                });
    }

    /**
     * SMS durumu sorgula
     */
    public String getSmsStatus(String messageSid) {
        try {
            Message message = Message.fetcher(messageSid).fetch();
            return message.getStatus().toString();
        } catch (Exception e) {
            log.error("Failed to fetch SMS status for SID: {}", messageSid, e);
            return "UNKNOWN";
        }
    }

    /**
     * Arama durumu sorgula
     */
    public String getCallStatus(String callSid) {
        try {
            Call call = Call.fetcher(callSid).fetch();
            return call.getStatus().toString();
        } catch (Exception e) {
            log.error("Failed to fetch call status for SID: {}", callSid, e);
            return "UNKNOWN";
        }
    }

    /**
     * Acil durum SMS şablonu oluştur
     */
    public String createEmergencySmsTemplate(String incidentNumber, 
                                           String emergencyType, 
                                           String location, 
                                           String timestamp,
                                           String trackingUrl) {
        return String.format(
                "🚨 ACİL DURUM ALARMI\n\n" +
                "Olay No: %s\n" +
                "Durum: %s\n" +
                "Konum: %s\n" +
                "Zaman: %s\n\n" +
                "Durumu takip edin: %s\n\n" +
                "Bu otomatik bir mesajdır. WolTaxi Acil Durum Sistemi",
                incidentNumber,
                emergencyType,
                location,
                timestamp,
                trackingUrl
        );
    }

    /**
     * Uluslararası numara formatı kontrolü
     */
    public String formatPhoneNumber(String phoneNumber, String countryCode) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        
        phoneNumber = phoneNumber.trim();
        
        // Eğer + ile başlamıyorsa, ülke kodunu ekle
        if (!phoneNumber.startsWith("+")) {
            return "+" + getCountryDialCode(countryCode) + phoneNumber;
        }
        
        return phoneNumber;
    }

    /**
     * Numara doğrulama
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Basit regex kontrolü
        String phoneRegex = "^\\+?[1-9]\\d{1,14}$";
        return phoneNumber.matches(phoneRegex);
    }

    /**
     * Acil durum mesaj türüne göre öncelik belirleme
     */
    public int getMessagePriority(String emergencyType) {
        return switch (emergencyType != null ? emergencyType.toUpperCase() : "GENERAL") {
            case "MEDICAL_EMERGENCY" -> 1;
            case "CRIME_IN_PROGRESS" -> 1;
            case "ACCIDENT" -> 2;
            case "HARASSMENT" -> 2;
            case "SOS" -> 2;
            case "PANIC_BUTTON" -> 2;
            case "SAFETY_CONCERN" -> 3;
            case "VEHICLE_BREAKDOWN" -> 4;
            default -> 3;
        };
    }

    // Yardımcı metodlar
    private String createTwimlUrl(String callScript, String incidentId) {
        // Bu URL, Twilio'nun arama sırasında çağıracağı TwiML endpoint'i olmalı
        // Gerçek implementasyonda, bu URL'de TwiML XML dönen bir endpoint olması gerekir
        return String.format("https://api.woltaxi.com/emergency/twiml/call?incident=%s&script=%s", 
                incidentId, java.net.URLEncoder.encode(callScript, java.nio.charset.StandardCharsets.UTF_8));
    }

    private String getCountryDialCode(String countryCode) {
        return switch (countryCode != null ? countryCode.toUpperCase() : "TR") {
            case "TR" -> "90";
            case "US", "CA" -> "1";
            case "UK", "GB" -> "44";
            case "DE" -> "49";
            case "FR" -> "33";
            case "IT" -> "39";
            case "ES" -> "34";
            case "NL" -> "31";
            case "BE" -> "32";
            case "CH" -> "41";
            case "AT" -> "43";
            case "SE" -> "46";
            case "NO" -> "47";
            case "DK" -> "45";
            case "FI" -> "358";
            default -> "90"; // Default to Turkey
        };
    }
}