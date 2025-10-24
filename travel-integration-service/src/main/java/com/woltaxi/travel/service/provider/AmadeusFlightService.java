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
 * Amadeus Flight Service
 * 
 * Integration with Amadeus Global Distribution System
 * for comprehensive flight booking capabilities.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AmadeusFlightService implements TravelProviderService {

    private final RestTemplate restTemplate;

    @Value("${travel.providers.amadeus.api-url}")
    private String apiUrl;

    @Value("${travel.providers.amadeus.api-key}")
    private String apiKey;

    @Value("${travel.providers.amadeus.api-secret}")
    private String apiSecret;

    @Value("${travel.providers.amadeus.timeout:30000}")
    private int timeout;

    private String accessToken;
    private LocalDateTime tokenExpiryTime;

    @Override
    public List<TravelOption> searchOptions(TravelBookingRequest request) {
        log.info("Searching flights with Amadeus: {} to {} on {}", 
                request.getOrigin(), request.getDestination(), request.getTravelDate());

        try {
            ensureAccessToken();

            String endpoint = "/v2/shopping/flight-offers";
            Map<String, Object> searchParams = buildSearchParams(request);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(searchParams, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseFlightOffers(response.getBody(), request);
            }

            log.warn("No flight offers returned from Amadeus");
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error searching flights with Amadeus", e);
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
        log.info("Making flight booking with Amadeus for option: {}", option.getProviderId());

        try {
            ensureAccessToken();

            String endpoint = "/v1/booking/flight-orders";
            Map<String, Object> bookingData = buildBookingData(request, option);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bookingData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                
                return BookingResult.builder()
                        .success(true)
                        .bookingReference(extractValue(responseBody, "data.id"))
                        .providerBookingId(extractValue(responseBody, "data.id"))
                        .confirmationData(responseBody.toString())
                        .additionalData(responseBody)
                        .build();
            }

            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Booking failed with status: " + response.getStatusCode())
                    .build();

        } catch (Exception e) {
            log.error("Error making booking with Amadeus", e);
            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Booking error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public BookingResult confirmBooking(String providerBookingId) {
        log.info("Confirming Amadeus booking: {}", providerBookingId);

        try {
            ensureAccessToken();

            String endpoint = "/v1/booking/flight-orders/" + providerBookingId;
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String status = extractValue(responseBody, "data.flightOffers[0].status");

                return BookingResult.builder()
                        .success("CONFIRMED".equals(status))
                        .bookingReference(providerBookingId)
                        .providerBookingId(providerBookingId)
                        .confirmationData(responseBody.toString())
                        .build();
            }

            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Failed to confirm booking")
                    .build();

        } catch (Exception e) {
            log.error("Error confirming Amadeus booking", e);
            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Confirmation error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public CancellationResult cancelBooking(String providerBookingId, String reason) {
        log.info("Cancelling Amadeus booking: {} reason: {}", providerBookingId, reason);

        try {
            ensureAccessToken();

            String endpoint = "/v1/booking/flight-orders/" + providerBookingId;
            HttpHeaders headers = createHeaders();
            
            Map<String, Object> cancellationData = new HashMap<>();
            cancellationData.put("reason", reason);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(cancellationData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.DELETE, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                
                return CancellationResult.builder()
                        .success(true)
                        .cancellationFee(BigDecimal.valueOf(getDoubleValue(responseBody, "data.cancellationFee", 0.0)))
                        .refundAmount(BigDecimal.valueOf(getDoubleValue(responseBody, "data.refundAmount", 0.0)))
                        .cancellationReference(extractValue(responseBody, "data.cancellationId"))
                        .cancellationDate(LocalDateTime.now())
                        .build();
            }

            return CancellationResult.builder()
                    .success(false)
                    .errorMessage("Cancellation failed")
                    .build();

        } catch (Exception e) {
            log.error("Error cancelling Amadeus booking", e);
            return CancellationResult.builder()
                    .success(false)
                    .errorMessage("Cancellation error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ModificationResult modifyBooking(String providerBookingId, TravelBookingRequest modificationRequest) {
        log.info("Modifying Amadeus booking: {}", providerBookingId);

        // Amadeus typically requires cancellation and rebooking for modifications
        return ModificationResult.builder()
                .success(false)
                .errorMessage("Modifications require cancellation and rebooking with Amadeus")
                .build();
    }

    @Override
    public CheckInResult checkIn(String providerBookingId) {
        log.info("Processing check-in for Amadeus booking: {}", providerBookingId);

        try {
            ensureAccessToken();

            String endpoint = "/v2/reference-data/urls/checkin-links";
            Map<String, Object> checkInData = new HashMap<>();
            checkInData.put("airlineCode", extractAirlineCode(providerBookingId));

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(checkInData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return CheckInResult.builder()
                        .success(true)
                        .boardingPasses(Collections.singletonList("Check-in URL provided"))
                        .seatAssignments(new HashMap<>())
                        .build();
            }

            return CheckInResult.builder()
                    .success(false)
                    .errorMessage("Check-in not available")
                    .build();

        } catch (Exception e) {
            log.error("Error processing check-in with Amadeus", e);
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

            String endpoint = "/v1/booking/flight-orders/" + providerBookingId;
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String status = extractValue(responseBody, "data.flightOffers[0].status");

                return BookingStatus.builder()
                        .status(mapAmadeusStatus(status))
                        .providerStatus(status)
                        .lastUpdated(LocalDateTime.now())
                        .statusDetails(responseBody)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error getting booking status from Amadeus", e);
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
            
            String endpoint = "/v1/reference-data/locations/cities";
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + endpoint + "?keyword=IST&max=1", HttpMethod.GET, entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;

            return HealthCheckResult.builder()
                    .healthy(response.getStatusCode() == HttpStatus.OK)
                    .status("Amadeus API accessible")
                    .responseTime(responseTime)
                    .checkTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            return HealthCheckResult.builder()
                    .healthy(false)
                    .status("Amadeus API error")
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
            String tokenEndpoint = "/v1/security/oauth2/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            String body = "grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + apiSecret;
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + tokenEndpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenData = response.getBody();
                accessToken = (String) tokenData.get("access_token");
                Integer expiresIn = (Integer) tokenData.get("expires_in");
                tokenExpiryTime = LocalDateTime.now().plusSeconds(expiresIn - 60); // Refresh 1 minute early
                
                log.info("Amadeus access token refreshed, expires at: {}", tokenExpiryTime);
            } else {
                throw new RuntimeException("Failed to refresh Amadeus access token");
            }

        } catch (Exception e) {
            log.error("Error refreshing Amadeus access token", e);
            throw new RuntimeException("Failed to authenticate with Amadeus", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private Map<String, Object> buildSearchParams(TravelBookingRequest request) {
        Map<String, Object> params = new HashMap<>();
        
        Map<String, Object> originDestinations = new HashMap<>();
        originDestinations.put("id", "1");
        originDestinations.put("originLocationCode", request.getOrigin());
        originDestinations.put("destinationLocationCode", request.getDestination());
        originDestinations.put("departureDateTimeRange", Map.of(
                "date", request.getTravelDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        
        params.put("originDestinations", Collections.singletonList(originDestinations));
        params.put("travelers", buildTravelersList(request.getPassengerCount()));
        params.put("sources", Collections.singletonList("GDS"));
        params.put("searchCriteria", Map.of(
                "maxFlightOffers", 50,
                "flightFilters", Map.of(
                        "cabinRestrictions", Collections.singletonList(Map.of(
                                "cabin", "ECONOMY",
                                "coverage", "MOST_SEGMENTS",
                                "originDestinationIds", Collections.singletonList("1")
                        ))
                )
        ));

        return params;
    }

    private List<Map<String, Object>> buildTravelersList(int passengerCount) {
        List<Map<String, Object>> travelers = new ArrayList<>();
        for (int i = 1; i <= passengerCount; i++) {
            Map<String, Object> traveler = new HashMap<>();
            traveler.put("id", String.valueOf(i));
            traveler.put("travelerType", "ADULT");
            travelers.add(traveler);
        }
        return travelers;
    }

    private Map<String, Object> buildBookingData(TravelBookingRequest request, TravelOption option) {
        Map<String, Object> bookingData = new HashMap<>();
        
        // Add flight offer data
        bookingData.put("flightOffers", Collections.singletonList(option.getAdditionalData()));
        
        // Add traveler information
        List<Map<String, Object>> travelers = new ArrayList<>();
        for (int i = 0; i < request.getPassengerCount(); i++) {
            Map<String, Object> traveler = new HashMap<>();
            traveler.put("id", String.valueOf(i + 1));
            traveler.put("dateOfBirth", "1990-01-01"); // Should come from passenger details
            traveler.put("name", Map.of("firstName", "John", "lastName", "Doe")); // Should come from passenger details
            traveler.put("gender", "MALE");
            traveler.put("contact", Map.of(
                    "emailAddress", request.getContactEmail(),
                    "phones", Collections.singletonList(Map.of("number", request.getContactPhone()))
            ));
            travelers.add(traveler);
        }
        bookingData.put("travelers", travelers);

        return bookingData;
    }

    private List<TravelOption> parseFlightOffers(Map<String, Object> response, TravelBookingRequest request) {
        List<TravelOption> options = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> flightOffers = (List<Map<String, Object>>) 
                    ((Map<String, Object>) response.get("data")).get("flightOffers");

            if (flightOffers == null) return options;

            for (Map<String, Object> offer : flightOffers) {
                try {
                    TravelOption option = parseFlightOffer(offer, request);
                    if (option != null) {
                        options.add(option);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing flight offer", e);
                }
            }

        } catch (Exception e) {
            log.error("Error parsing flight offers from Amadeus", e);
        }

        return options;
    }

    private TravelOption parseFlightOffer(Map<String, Object> offer, TravelBookingRequest request) {
        try {
            String offerId = (String) offer.get("id");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> price = (Map<String, Object>) offer.get("price");
            BigDecimal totalPrice = new BigDecimal((String) price.get("total"));
            String currency = (String) price.get("currency");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itineraries = (List<Map<String, Object>>) offer.get("itineraries");
            Map<String, Object> outbound = itineraries.get(0);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> segments = (List<Map<String, Object>>) outbound.get("segments");
            Map<String, Object> firstSegment = segments.get(0);
            Map<String, Object> lastSegment = segments.get(segments.size() - 1);

            String duration = (String) outbound.get("duration");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> departure = (Map<String, Object>) firstSegment.get("departure");
            @SuppressWarnings("unchecked")
            Map<String, Object> arrival = (Map<String, Object>) lastSegment.get("arrival");

            LocalDateTime departureTime = LocalDateTime.parse((String) departure.get("at"));
            LocalDateTime arrivalTime = LocalDateTime.parse((String) arrival.get("at"));

            return TravelOption.builder()
                    .providerId(offerId)
                    .providerName("Amadeus")
                    .origin(request.getOrigin())
                    .destination(request.getDestination())
                    .departureTime(departureTime)
                    .arrivalTime(arrivalTime)
                    .price(totalPrice)
                    .currency(currency)
                    .availableSeats(9) // Amadeus doesn't always provide exact seat count
                    .serviceClass("ECONOMY")
                    .amenities(Collections.singletonList("Standard Service"))
                    .refundable(true)
                    .changeable(true)
                    .duration(duration)
                    .additionalData(offer)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing individual flight offer", e);
            return null;
        }
    }

    private String extractValue(Map<String, Object> data, String path) {
        String[] keys = path.split("\\.");
        Object current = data;
        
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else if (current instanceof List && key.matches("\\d+")) {
                int index = Integer.parseInt(key);
                List<?> list = (List<?>) current;
                current = index < list.size() ? list.get(index) : null;
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

    private String extractAirlineCode(String providerBookingId) {
        // Extract airline code from booking ID or use default
        return "TK"; // Turkish Airlines as default
    }

    private String mapAmadeusStatus(String amadeusStatus) {
        if (amadeusStatus == null) return "UNKNOWN";
        
        return switch (amadeusStatus.toUpperCase()) {
            case "CONFIRMED" -> "CONFIRMED";
            case "CANCELLED" -> "CANCELLED";
            case "PENDING" -> "PENDING";
            default -> "UNKNOWN";
        };
    }
}