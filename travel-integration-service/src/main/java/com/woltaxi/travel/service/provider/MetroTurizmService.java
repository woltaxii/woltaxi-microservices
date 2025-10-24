package com.woltaxi.travel.service.provider;

import com.woltaxi.travel.dto.request.TravelBookingRequest;
import com.woltaxi.travel.service.TravelBookingService.TravelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Metro Turizm Service
 * 
 * Integration with Metro Turizm - Turkey's leading bus company
 * for intercity bus booking and management.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetroTurizmService implements TravelProviderService {

    private final RestTemplate restTemplate;

    @Value("${travel.providers.metro-turizm.api-url}")
    private String apiUrl;

    @Value("${travel.providers.metro-turizm.api-key}")
    private String apiKey;

    @Value("${travel.providers.metro-turizm.username}")
    private String username;

    @Value("${travel.providers.metro-turizm.password}")
    private String password;

    private String sessionToken;
    private LocalDateTime tokenExpiryTime;

    @Override
    public List<TravelOption> searchOptions(TravelBookingRequest request) {
        log.info("Searching Metro Turizm buses: {} to {} on {}", 
                request.getOrigin(), request.getDestination(), request.getTravelDate());

        try {
            ensureSessionToken();

            String endpoint = "/api/v1/seferler/ara";
            Map<String, Object> searchParams = buildMetroTurizmSearchParams(request);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(searchParams, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseMetroTurizmOffers(response.getBody(), request);
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error searching Metro Turizm buses", e);
            return Collections.emptyList();
        }
    }

    @Override
    public TravelOption checkAvailability(TravelBookingRequest request) {
        List<TravelOption> options = searchOptions(request);
        return options.isEmpty() ? null : options.get(0);
    }

    @Override
    public BookingResult makeBooking(TravelBookingRequest request, TravelOption option) {
        log.info("Making Metro Turizm booking for option: {}", option.getProviderId());

        try {
            ensureSessionToken();

            String endpoint = "/api/v1/rezervasyon/yap";
            Map<String, Object> bookingData = buildMetroTurizmBookingData(request, option);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bookingData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                
                return BookingResult.builder()
                        .success(true)
                        .bookingReference(extractValue(responseBody, "pnrNo"))
                        .providerBookingId(extractValue(responseBody, "rezervasyonId"))
                        .confirmationData(responseBody.toString())
                        .additionalData(responseBody)
                        .build();
            }

            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Metro Turizm booking failed: " + response.getStatusCode())
                    .build();

        } catch (Exception e) {
            log.error("Error making Metro Turizm booking", e);
            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Metro Turizm booking error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public BookingResult confirmBooking(String providerBookingId) {
        try {
            ensureSessionToken();

            String endpoint = "/api/v1/rezervasyon/" + providerBookingId + "/onayla";
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            return BookingResult.builder()
                    .success(response.getStatusCode() == HttpStatus.OK)
                    .bookingReference(providerBookingId)
                    .providerBookingId(providerBookingId)
                    .build();

        } catch (Exception e) {
            log.error("Error confirming Metro Turizm booking", e);
            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Confirmation error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public CancellationResult cancelBooking(String providerBookingId, String reason) {
        try {
            ensureSessionToken();

            String endpoint = "/api/v1/rezervasyon/" + providerBookingId + "/iptal";
            HttpHeaders headers = createHeaders();
            
            Map<String, Object> cancellationData = Map.of("iptalNedeni", reason);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(cancellationData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                
                return CancellationResult.builder()
                        .success(true)
                        .cancellationFee(BigDecimal.valueOf(getDoubleValue(responseBody, "iptalUcreti", 0.0)))
                        .refundAmount(BigDecimal.valueOf(getDoubleValue(responseBody, "iadeTutari", 0.0)))
                        .cancellationReference(extractValue(responseBody, "iptalNo"))
                        .cancellationDate(LocalDateTime.now())
                        .build();
            }

            return CancellationResult.builder()
                    .success(false)
                    .errorMessage("Metro Turizm cancellation failed")
                    .build();

        } catch (Exception e) {
            log.error("Error cancelling Metro Turizm booking", e);
            return CancellationResult.builder()
                    .success(false)
                    .errorMessage("Cancellation error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ModificationResult modifyBooking(String providerBookingId, TravelBookingRequest modificationRequest) {
        try {
            ensureSessionToken();

            String endpoint = "/api/v1/rezervasyon/" + providerBookingId + "/degistir";
            Map<String, Object> modificationData = buildMetroTurizmModificationData(modificationRequest);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(modificationData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.PUT, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                
                return ModificationResult.builder()
                        .success(true)
                        .priceDifference(BigDecimal.valueOf(getDoubleValue(responseBody, "fiyatFarki", 0.0)))
                        .modificationReference(extractValue(responseBody, "degisiklikNo"))
                        .updatedBookingData(responseBody)
                        .build();
            }

            return ModificationResult.builder()
                    .success(false)
                    .errorMessage("Metro Turizm modification failed")
                    .build();

        } catch (Exception e) {
            log.error("Error modifying Metro Turizm booking", e);
            return ModificationResult.builder()
                    .success(false)
                    .errorMessage("Modification error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public CheckInResult checkIn(String providerBookingId) {
        // Metro Turizm buses don't typically require check-in
        return CheckInResult.builder()
                .success(true)
                .boardingPasses(Collections.singletonList("E-Ticket ready for boarding"))
                .seatAssignments(Collections.emptyMap())
                .build();
    }

    @Override
    public BookingStatus getBookingStatus(String providerBookingId) {
        try {
            ensureSessionToken();

            String endpoint = "/api/v1/rezervasyon/" + providerBookingId + "/durum";
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String status = extractValue(responseBody, "durum");

                return BookingStatus.builder()
                        .status(mapMetroTurizmStatus(status))
                        .providerStatus(status)
                        .lastUpdated(LocalDateTime.now())
                        .statusDetails(responseBody)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error getting Metro Turizm booking status", e);
        }

        return BookingStatus.builder()
                .status("UNKNOWN")
                .providerStatus("ERROR")
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Override
    public HealthCheckResult healthCheck() {
        long startTime = System.currentTimeMillis();
        
        try {
            ensureSessionToken();
            
            String endpoint = "/api/v1/sistem/durum";
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.GET, entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;

            return HealthCheckResult.builder()
                    .healthy(response.getStatusCode() == HttpStatus.OK)
                    .status("Metro Turizm API accessible")
                    .responseTime(responseTime)
                    .checkTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            return HealthCheckResult.builder()
                    .healthy(false)
                    .status("Metro Turizm API error")
                    .responseTime(responseTime)
                    .errorMessage(e.getMessage())
                    .checkTime(LocalDateTime.now())
                    .build();
        }
    }

    // Private helper methods
    
    private void ensureSessionToken() {
        if (sessionToken == null || tokenExpiryTime == null || LocalDateTime.now().isAfter(tokenExpiryTime)) {
            refreshSessionToken();
        }
    }

    private void refreshSessionToken() {
        try {
            String loginEndpoint = "/api/v1/auth/giris";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", apiKey);
            
            Map<String, Object> loginData = Map.of(
                    "kullaniciAdi", username,
                    "sifre", password
            );
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(loginData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + loginEndpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenData = response.getBody();
                sessionToken = (String) tokenData.get("sessionToken");
                Integer expiresIn = (Integer) tokenData.get("expiresIn");
                tokenExpiryTime = LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn - 60 : 3540);
                
                log.info("Metro Turizm session token refreshed");
            }

        } catch (Exception e) {
            log.error("Error refreshing Metro Turizm session token", e);
            throw new RuntimeException("Failed to authenticate with Metro Turizm", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);
        headers.set("Authorization", "Bearer " + sessionToken);
        return headers;
    }

    private Map<String, Object> buildMetroTurizmSearchParams(TravelBookingRequest request) {
        Map<String, Object> params = new HashMap<>();
        
        params.put("kalkisYeri", request.getOrigin());
        params.put("varisYeri", request.getDestination());
        params.put("tarih", request.getTravelDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        params.put("yolcuSayisi", request.getPassengerCount());
        
        return params;
    }

    private Map<String, Object> buildMetroTurizmBookingData(TravelBookingRequest request, TravelOption option) {
        Map<String, Object> bookingData = new HashMap<>();
        
        bookingData.put("seferKodu", option.getProviderId());
        
        // Passenger information
        List<Map<String, Object>> yolcular = new ArrayList<>();
        for (int i = 0; i < request.getPassengerCount(); i++) {
            Map<String, Object> yolcu = new HashMap<>();
            yolcu.put("ad", "John");
            yolcu.put("soyad", "Doe");
            yolcu.put("tcKimlikNo", "12345678901");
            yolcu.put("telefon", request.getContactPhone());
            yolcu.put("cinsiyet", "E"); // E=Erkek, K=Kadın
            yolcular.add(yolcu);
        }
        bookingData.put("yolcular", yolcular);
        
        // Contact information
        bookingData.put("iletisim", Map.of(
                "email", request.getContactEmail(),
                "telefon", request.getContactPhone()
        ));

        return bookingData;
    }

    private Map<String, Object> buildMetroTurizmModificationData(TravelBookingRequest request) {
        Map<String, Object> modificationData = new HashMap<>();
        
        if (request.getTravelDate() != null) {
            modificationData.put("yeniTarih", request.getTravelDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        return modificationData;
    }

    private List<TravelOption> parseMetroTurizmOffers(Map<String, Object> response, TravelBookingRequest request) {
        List<TravelOption> options = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> seferler = (List<Map<String, Object>>) response.get("seferler");

            if (seferler == null) return options;

            for (Map<String, Object> sefer : seferler) {
                try {
                    TravelOption option = parseMetroTurizmSefer(sefer, request);
                    if (option != null) {
                        options.add(option);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing Metro Turizm sefer", e);
                }
            }

        } catch (Exception e) {
            log.error("Error parsing Metro Turizm response", e);
        }

        return options;
    }

    private TravelOption parseMetroTurizmSefer(Map<String, Object> sefer, TravelBookingRequest request) {
        try {
            String seferKodu = (String) sefer.get("seferKodu");
            
            BigDecimal fiyat = new BigDecimal(sefer.get("fiyat").toString());
            String parabirimi = (String) sefer.get("parabirimi");

            String kalkisSaati = (String) sefer.get("kalkisSaati");
            String varisSaati = (String) sefer.get("varisSaati");
            
            LocalDateTime departureTime = LocalDateTime.parse(request.getTravelDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T" + kalkisSaati);
            LocalDateTime arrivalTime = LocalDateTime.parse(request.getTravelDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T" + varisSaati);
            
            // If arrival is next day
            if (arrivalTime.isBefore(departureTime)) {
                arrivalTime = arrivalTime.plusDays(1);
            }

            Integer bosKoltukSayisi = (Integer) sefer.get("bosKoltukSayisi");
            String otobusAdi = (String) sefer.get("otobusAdi");

            return TravelOption.builder()
                    .providerId(seferKodu)
                    .providerName("Metro Turizm")
                    .origin(request.getOrigin())
                    .destination(request.getDestination())
                    .departureTime(departureTime)
                    .arrivalTime(arrivalTime)
                    .price(fiyat)
                    .currency(parabirimi != null ? parabirimi : "TRY")
                    .availableSeats(bosKoltukSayisi != null ? bosKoltukSayisi : 0)
                    .serviceClass(otobusAdi != null ? otobusAdi : "Standard")
                    .amenities(Arrays.asList("Klimali", "WiFi", "Televizyon", "Ikram"))
                    .refundable(true)
                    .changeable(true)
                    .duration(calculateDuration(departureTime, arrivalTime))
                    .additionalData(sefer)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing Metro Turizm sefer", e);
            return null;
        }
    }

    private String calculateDuration(LocalDateTime departure, LocalDateTime arrival) {
        long minutes = java.time.Duration.between(departure, arrival).toMinutes();
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return String.format("%d saat %d dakika", hours, remainingMinutes);
    }

    private String extractValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    private double getDoubleValue(Map<String, Object> data, String key, double defaultValue) {
        Object value = data.get(key);
        try {
            return value != null ? Double.parseDouble(value.toString()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String mapMetroTurizmStatus(String status) {
        if (status == null) return "UNKNOWN";
        
        return switch (status.toLowerCase()) {
            case "onaylandi", "bilet_kesildi" -> "CONFIRMED";
            case "iptal_edildi" -> "CANCELLED";
            case "beklemede" -> "PENDING";
            case "tamamlandi" -> "COMPLETED";
            default -> "UNKNOWN";
        };
    }
}