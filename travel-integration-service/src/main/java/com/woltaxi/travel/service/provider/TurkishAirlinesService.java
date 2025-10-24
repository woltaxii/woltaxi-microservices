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
 * Turkish Airlines Service
 * 
 * Direct integration with Turkish Airlines API
 * for flight booking and management.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TurkishAirlinesService implements TravelProviderService {

    private final RestTemplate restTemplate;

    @Value("${travel.providers.turkish-airlines.api-url}")
    private String apiUrl;

    @Value("${travel.providers.turkish-airlines.api-key}")
    private String apiKey;

    @Value("${travel.providers.turkish-airlines.client-id}")
    private String clientId;

    @Value("${travel.providers.turkish-airlines.client-secret}")
    private String clientSecret;

    private String accessToken;
    private LocalDateTime tokenExpiryTime;

    @Override
    public List<TravelOption> searchOptions(TravelBookingRequest request) {
        log.info("Searching Turkish Airlines flights: {} to {} on {}", 
                request.getOrigin(), request.getDestination(), request.getTravelDate());

        try {
            ensureAccessToken();

            String endpoint = "/v1/flight-search";
            Map<String, Object> searchParams = buildTurkishAirlinesSearchParams(request);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(searchParams, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseTurkishAirlinesOffers(response.getBody(), request);
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error searching Turkish Airlines flights", e);
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
        log.info("Making Turkish Airlines booking for option: {}", option.getProviderId());

        try {
            ensureAccessToken();

            String endpoint = "/v1/bookings";
            Map<String, Object> bookingData = buildTurkishAirlinesBookingData(request, option);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bookingData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                
                return BookingResult.builder()
                        .success(true)
                        .bookingReference(extractValue(responseBody, "booking.pnr"))
                        .providerBookingId(extractValue(responseBody, "booking.id"))
                        .confirmationData(responseBody.toString())
                        .additionalData(responseBody)
                        .build();
            }

            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Turkish Airlines booking failed: " + response.getStatusCode())
                    .build();

        } catch (Exception e) {
            log.error("Error making Turkish Airlines booking", e);
            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Turkish Airlines booking error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public BookingResult confirmBooking(String providerBookingId) {
        try {
            ensureAccessToken();

            String endpoint = "/v1/bookings/" + providerBookingId + "/confirm";
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
            log.error("Error confirming Turkish Airlines booking", e);
            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Confirmation error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public CancellationResult cancelBooking(String providerBookingId, String reason) {
        try {
            ensureAccessToken();

            String endpoint = "/v1/bookings/" + providerBookingId + "/cancel";
            HttpHeaders headers = createHeaders();
            
            Map<String, Object> cancellationData = Map.of("reason", reason);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(cancellationData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                
                return CancellationResult.builder()
                        .success(true)
                        .cancellationFee(BigDecimal.valueOf(getDoubleValue(responseBody, "cancellationFee", 0.0)))
                        .refundAmount(BigDecimal.valueOf(getDoubleValue(responseBody, "refundAmount", 0.0)))
                        .cancellationReference(extractValue(responseBody, "cancellationId"))
                        .cancellationDate(LocalDateTime.now())
                        .build();
            }

            return CancellationResult.builder()
                    .success(false)
                    .errorMessage("Turkish Airlines cancellation failed")
                    .build();

        } catch (Exception e) {
            log.error("Error cancelling Turkish Airlines booking", e);
            return CancellationResult.builder()
                    .success(false)
                    .errorMessage("Cancellation error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ModificationResult modifyBooking(String providerBookingId, TravelBookingRequest modificationRequest) {
        try {
            ensureAccessToken();

            String endpoint = "/v1/bookings/" + providerBookingId + "/modify";
            Map<String, Object> modificationData = buildTurkishAirlinesModificationData(modificationRequest);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(modificationData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.PUT, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                
                return ModificationResult.builder()
                        .success(true)
                        .priceDifference(BigDecimal.valueOf(getDoubleValue(responseBody, "priceDifference", 0.0)))
                        .modificationReference(extractValue(responseBody, "modificationId"))
                        .updatedBookingData(responseBody)
                        .build();
            }

            return ModificationResult.builder()
                    .success(false)
                    .errorMessage("Turkish Airlines modification failed")
                    .build();

        } catch (Exception e) {
            log.error("Error modifying Turkish Airlines booking", e);
            return ModificationResult.builder()
                    .success(false)
                    .errorMessage("Modification error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public CheckInResult checkIn(String providerBookingId) {
        try {
            ensureAccessToken();

            String endpoint = "/v1/bookings/" + providerBookingId + "/checkin";
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                
                @SuppressWarnings("unchecked")
                List<String> boardingPasses = (List<String>) responseBody.get("boardingPasses");
                @SuppressWarnings("unchecked")
                Map<String, String> seatAssignments = (Map<String, String>) responseBody.get("seatAssignments");

                return CheckInResult.builder()
                        .success(true)
                        .boardingPasses(boardingPasses != null ? boardingPasses : Collections.emptyList())
                        .seatAssignments(seatAssignments != null ? seatAssignments : Collections.emptyMap())
                        .build();
            }

            return CheckInResult.builder()
                    .success(false)
                    .errorMessage("Turkish Airlines check-in failed")
                    .build();

        } catch (Exception e) {
            log.error("Error checking in Turkish Airlines booking", e);
            return CheckInResult.builder()
                    .success(false)
                    .errorMessage("Check-in error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public BookingStatus getBookingStatus(String providerBookingId) {
        try {
            ensureAccessToken();

            String endpoint = "/v1/bookings/" + providerBookingId + "/status";
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String status = extractValue(responseBody, "status");

                return BookingStatus.builder()
                        .status(mapTurkishAirlinesStatus(status))
                        .providerStatus(status)
                        .lastUpdated(LocalDateTime.now())
                        .statusDetails(responseBody)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error getting Turkish Airlines booking status", e);
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
            ensureAccessToken();
            
            String endpoint = "/v1/health";
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.GET, entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;

            return HealthCheckResult.builder()
                    .healthy(response.getStatusCode() == HttpStatus.OK)
                    .status("Turkish Airlines API accessible")
                    .responseTime(responseTime)
                    .checkTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            return HealthCheckResult.builder()
                    .healthy(false)
                    .status("Turkish Airlines API error")
                    .responseTime(responseTime)
                    .errorMessage(e.getMessage())
                    .checkTime(LocalDateTime.now())
                    .build();
        }
    }

    // Private helper methods
    
    private void ensureAccessToken() {
        if (accessToken == null || tokenExpiryTime == null || LocalDateTime.now().isAfter(tokenExpiryTime)) {
            refreshAccessToken();
        }
    }

    private void refreshAccessToken() {
        try {
            String tokenEndpoint = "/v1/oauth/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret);
            
            String body = "grant_type=client_credentials&scope=flight-api";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + tokenEndpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenData = response.getBody();
                accessToken = (String) tokenData.get("access_token");
                Integer expiresIn = (Integer) tokenData.get("expires_in");
                tokenExpiryTime = LocalDateTime.now().plusSeconds(expiresIn - 60);
                
                log.info("Turkish Airlines access token refreshed");
            }

        } catch (Exception e) {
            log.error("Error refreshing Turkish Airlines access token", e);
            throw new RuntimeException("Failed to authenticate with Turkish Airlines", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set("X-API-Key", apiKey);
        return headers;
    }

    private Map<String, Object> buildTurkishAirlinesSearchParams(TravelBookingRequest request) {
        Map<String, Object> params = new HashMap<>();
        
        params.put("origin", request.getOrigin());
        params.put("destination", request.getDestination());
        params.put("departureDate", request.getTravelDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        if (request.getReturnDate() != null) {
            params.put("returnDate", request.getReturnDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        params.put("passengers", Map.of(
                "adult", request.getPassengerCount(),
                "child", 0,
                "infant", 0
        ));
        
        params.put("cabinClass", "ECONOMY");
        params.put("directFlightOnly", false);

        return params;
    }

    private Map<String, Object> buildTurkishAirlinesBookingData(TravelBookingRequest request, TravelOption option) {
        Map<String, Object> bookingData = new HashMap<>();
        
        bookingData.put("flightOfferId", option.getProviderId());
        
        // Passenger information
        List<Map<String, Object>> passengers = new ArrayList<>();
        for (int i = 0; i < request.getPassengerCount(); i++) {
            Map<String, Object> passenger = new HashMap<>();
            passenger.put("type", "ADULT");
            passenger.put("title", "MR");
            passenger.put("firstName", "John");
            passenger.put("lastName", "Doe");
            passenger.put("dateOfBirth", "1990-01-01");
            passenger.put("nationality", "TR");
            passengers.add(passenger);
        }
        bookingData.put("passengers", passengers);
        
        // Contact information
        bookingData.put("contact", Map.of(
                "email", request.getContactEmail(),
                "phone", request.getContactPhone()
        ));

        return bookingData;
    }

    private Map<String, Object> buildTurkishAirlinesModificationData(TravelBookingRequest request) {
        Map<String, Object> modificationData = new HashMap<>();
        
        if (request.getTravelDate() != null) {
            modificationData.put("newDepartureDate", request.getTravelDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        if (request.getReturnDate() != null) {
            modificationData.put("newReturnDate", request.getReturnDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        return modificationData;
    }

    private List<TravelOption> parseTurkishAirlinesOffers(Map<String, Object> response, TravelBookingRequest request) {
        List<TravelOption> options = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> flights = (List<Map<String, Object>>) response.get("flights");

            if (flights == null) return options;

            for (Map<String, Object> flight : flights) {
                try {
                    TravelOption option = parseTurkishAirlinesFlight(flight, request);
                    if (option != null) {
                        options.add(option);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing Turkish Airlines flight", e);
                }
            }

        } catch (Exception e) {
            log.error("Error parsing Turkish Airlines response", e);
        }

        return options;
    }

    private TravelOption parseTurkishAirlinesFlight(Map<String, Object> flight, TravelBookingRequest request) {
        try {
            String flightId = (String) flight.get("id");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> pricing = (Map<String, Object>) flight.get("pricing");
            BigDecimal totalPrice = new BigDecimal(pricing.get("totalAmount").toString());
            String currency = (String) pricing.get("currency");

            @SuppressWarnings("unchecked")
            Map<String, Object> schedule = (Map<String, Object>) flight.get("schedule");
            LocalDateTime departureTime = LocalDateTime.parse((String) schedule.get("departureTime"));
            LocalDateTime arrivalTime = LocalDateTime.parse((String) schedule.get("arrivalTime"));

            String duration = (String) schedule.get("duration");
            Integer availableSeats = (Integer) flight.get("availableSeats");

            return TravelOption.builder()
                    .providerId(flightId)
                    .providerName("Turkish Airlines")
                    .origin(request.getOrigin())
                    .destination(request.getDestination())
                    .departureTime(departureTime)
                    .arrivalTime(arrivalTime)
                    .price(totalPrice)
                    .currency(currency)
                    .availableSeats(availableSeats != null ? availableSeats : 9)
                    .serviceClass("ECONOMY")
                    .amenities(Arrays.asList("Meal Service", "Entertainment", "WiFi"))
                    .refundable(true)
                    .changeable(true)
                    .duration(duration)
                    .additionalData(flight)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing Turkish Airlines flight", e);
            return null;
        }
    }

    private String extractValue(Map<String, Object> data, String path) {
        String[] keys = path.split("\\.");
        Object current = data;
        
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else {
                return null;
            }
        }
        
        return current != null ? current.toString() : null;
    }

    private double getDoubleValue(Map<String, Object> data, String path, double defaultValue) {
        String value = extractValue(data, path);
        try {
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String mapTurkishAirlinesStatus(String status) {
        if (status == null) return "UNKNOWN";
        
        return switch (status.toUpperCase()) {
            case "CONFIRMED", "TICKETED" -> "CONFIRMED";
            case "CANCELLED" -> "CANCELLED";
            case "PENDING" -> "PENDING";
            case "CHECKED_IN" -> "CONFIRMED";
            default -> "UNKNOWN";
        };
    }
}