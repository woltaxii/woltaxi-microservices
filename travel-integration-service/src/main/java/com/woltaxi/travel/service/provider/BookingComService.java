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
 * Booking.com Service
 * 
 * Integration with Booking.com for global hotel
 * reservation and accommodation booking.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingComService implements TravelProviderService {

    private final RestTemplate restTemplate;

    @Value("${travel.providers.booking-com.api-url}")
    private String apiUrl;

    @Value("${travel.providers.booking-com.api-key}")
    private String apiKey;

    @Value("${travel.providers.booking-com.affiliate-id}")
    private String affiliateId;

    @Override
    public List<TravelOption> searchOptions(TravelBookingRequest request) {
        log.info("Searching Booking.com hotels in {} from {} to {}", 
                request.getDestination(), request.getTravelDate(), request.getCheckOutDate());

        try {
            String endpoint = "/v1/hotels/search";
            Map<String, Object> searchParams = buildBookingComSearchParams(request);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(searchParams, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseBookingComOffers(response.getBody(), request);
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error searching Booking.com hotels", e);
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
        log.info("Making Booking.com reservation for hotel: {}", option.getProviderId());

        try {
            String endpoint = "/v1/bookings";
            Map<String, Object> bookingData = buildBookingComBookingData(request, option);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bookingData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                
                return BookingResult.builder()
                        .success(true)
                        .bookingReference(extractValue(responseBody, "reservation_id"))
                        .providerBookingId(extractValue(responseBody, "booking_id"))
                        .confirmationData(responseBody.toString())
                        .additionalData(responseBody)
                        .build();
            }

            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Booking.com reservation failed: " + response.getStatusCode())
                    .build();

        } catch (Exception e) {
            log.error("Error making Booking.com reservation", e);
            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Booking.com reservation error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public BookingResult confirmBooking(String providerBookingId) {
        try {
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
            log.error("Error confirming Booking.com reservation", e);
            return BookingResult.builder()
                    .success(false)
                    .errorMessage("Confirmation error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public CancellationResult cancelBooking(String providerBookingId, String reason) {
        try {
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
                        .cancellationFee(BigDecimal.valueOf(getDoubleValue(responseBody, "cancellation_fee", 0.0)))
                        .refundAmount(BigDecimal.valueOf(getDoubleValue(responseBody, "refund_amount", 0.0)))
                        .cancellationReference(extractValue(responseBody, "cancellation_id"))
                        .cancellationDate(LocalDateTime.now())
                        .build();
            }

            return CancellationResult.builder()
                    .success(false)
                    .errorMessage("Booking.com cancellation failed")
                    .build();

        } catch (Exception e) {
            log.error("Error cancelling Booking.com reservation", e);
            return CancellationResult.builder()
                    .success(false)
                    .errorMessage("Cancellation error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ModificationResult modifyBooking(String providerBookingId, TravelBookingRequest modificationRequest) {
        try {
            String endpoint = "/v1/bookings/" + providerBookingId;
            Map<String, Object> modificationData = buildBookingComModificationData(modificationRequest);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(modificationData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.PUT, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                
                return ModificationResult.builder()
                        .success(true)
                        .priceDifference(BigDecimal.valueOf(getDoubleValue(responseBody, "price_difference", 0.0)))
                        .modificationReference(extractValue(responseBody, "modification_id"))
                        .updatedBookingData(responseBody)
                        .build();
            }

            return ModificationResult.builder()
                    .success(false)
                    .errorMessage("Booking.com modification failed")
                    .build();

        } catch (Exception e) {
            log.error("Error modifying Booking.com reservation", e);
            return ModificationResult.builder()
                    .success(false)
                    .errorMessage("Modification error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public CheckInResult checkIn(String providerBookingId) {
        // Hotels don't require check-in through API
        return CheckInResult.builder()
                .success(true)
                .boardingPasses(Collections.singletonList("Hotel voucher ready for check-in"))
                .seatAssignments(Collections.emptyMap())
                .build();
    }

    @Override
    public BookingStatus getBookingStatus(String providerBookingId) {
        try {
            String endpoint = "/v1/bookings/" + providerBookingId;
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String status = extractValue(responseBody, "status");

                return BookingStatus.builder()
                        .status(mapBookingComStatus(status))
                        .providerStatus(status)
                        .lastUpdated(LocalDateTime.now())
                        .statusDetails(responseBody)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error getting Booking.com reservation status", e);
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
            String endpoint = "/v1/health";
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + endpoint, HttpMethod.GET, entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;

            return HealthCheckResult.builder()
                    .healthy(response.getStatusCode() == HttpStatus.OK)
                    .status("Booking.com API accessible")
                    .responseTime(responseTime)
                    .checkTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            return HealthCheckResult.builder()
                    .healthy(false)
                    .status("Booking.com API error")
                    .responseTime(responseTime)
                    .errorMessage(e.getMessage())
                    .checkTime(LocalDateTime.now())
                    .build();
        }
    }

    // Private helper methods

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Booking-API-Key", apiKey);
        headers.set("X-Booking-Affiliate-ID", affiliateId);
        return headers;
    }

    private Map<String, Object> buildBookingComSearchParams(TravelBookingRequest request) {
        Map<String, Object> params = new HashMap<>();
        
        params.put("destination", request.getDestination());
        params.put("checkin", request.getTravelDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        params.put("checkout", request.getCheckOutDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        params.put("adults", request.getPassengerCount());
        params.put("children", 0);
        params.put("rooms", 1);
        params.put("currency", "TRY");
        params.put("language", "tr");
        
        return params;
    }

    private Map<String, Object> buildBookingComBookingData(TravelBookingRequest request, TravelOption option) {
        Map<String, Object> bookingData = new HashMap<>();
        
        bookingData.put("hotel_id", option.getProviderId());
        bookingData.put("room_id", option.getAdditionalData().get("room_id"));
        
        // Guest information
        List<Map<String, Object>> guests = new ArrayList<>();
        for (int i = 0; i < request.getPassengerCount(); i++) {
            Map<String, Object> guest = new HashMap<>();
            guest.put("first_name", "John");
            guest.put("last_name", "Doe");
            guest.put("email", request.getContactEmail());
            guest.put("phone", request.getContactPhone());
            guests.add(guest);
        }
        bookingData.put("guests", guests);
        
        // Booking details
        bookingData.put("checkin_date", request.getTravelDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        bookingData.put("checkout_date", request.getCheckOutDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        bookingData.put("special_requests", request.getSpecialRequests());

        return bookingData;
    }

    private Map<String, Object> buildBookingComModificationData(TravelBookingRequest request) {
        Map<String, Object> modificationData = new HashMap<>();
        
        if (request.getTravelDate() != null) {
            modificationData.put("new_checkin_date", request.getTravelDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        if (request.getCheckOutDate() != null) {
            modificationData.put("new_checkout_date", request.getCheckOutDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        if (request.getSpecialRequests() != null) {
            modificationData.put("special_requests", request.getSpecialRequests());
        }

        return modificationData;
    }

    private List<TravelOption> parseBookingComOffers(Map<String, Object> response, TravelBookingRequest request) {
        List<TravelOption> options = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> hotels = (List<Map<String, Object>>) response.get("hotels");

            if (hotels == null) return options;

            for (Map<String, Object> hotel : hotels) {
                try {
                    TravelOption option = parseBookingComHotel(hotel, request);
                    if (option != null) {
                        options.add(option);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing Booking.com hotel", e);
                }
            }

        } catch (Exception e) {
            log.error("Error parsing Booking.com response", e);
        }

        return options;
    }

    private TravelOption parseBookingComHotel(Map<String, Object> hotel, TravelBookingRequest request) {
        try {
            String hotelId = (String) hotel.get("hotel_id");
            String hotelName = (String) hotel.get("hotel_name");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> pricing = (Map<String, Object>) hotel.get("pricing");
            BigDecimal totalPrice = new BigDecimal(pricing.get("total_amount").toString());
            String currency = (String) pricing.get("currency");

            @SuppressWarnings("unchecked")
            Map<String, Object> location = (Map<String, Object>) hotel.get("location");
            String address = (String) location.get("address");

            Double rating = (Double) hotel.get("review_score");
            Integer availableRooms = (Integer) hotel.get("available_rooms");

            @SuppressWarnings("unchecked")
            List<String> amenities = (List<String>) hotel.get("amenities");

            return TravelOption.builder()
                    .providerId(hotelId)
                    .providerName("Booking.com - " + hotelName)
                    .origin(address)
                    .destination(request.getDestination())
                    .departureTime(request.getTravelDate())
                    .arrivalTime(request.getCheckOutDate())
                    .price(totalPrice)
                    .currency(currency != null ? currency : "TRY")
                    .availableSeats(availableRooms != null ? availableRooms : 1)
                    .serviceClass(rating != null ? String.format("%.1f★", rating) : "Standard")
                    .amenities(amenities != null ? amenities : Arrays.asList("WiFi", "Breakfast"))
                    .refundable((Boolean) hotel.getOrDefault("free_cancellation", false))
                    .changeable(true)
                    .duration(calculateStayDuration(request.getTravelDate(), request.getCheckOutDate()))
                    .additionalData(hotel)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing Booking.com hotel", e);
            return null;
        }
    }

    private String calculateStayDuration(LocalDateTime checkin, LocalDateTime checkout) {
        long days = java.time.Duration.between(checkin, checkout).toDays();
        return days + " gece";
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

    private String mapBookingComStatus(String status) {
        if (status == null) return "UNKNOWN";
        
        return switch (status.toLowerCase()) {
            case "confirmed", "guaranteed" -> "CONFIRMED";
            case "cancelled" -> "CANCELLED";
            case "pending" -> "PENDING";
            case "completed", "checked_out" -> "COMPLETED";
            default -> "UNKNOWN";
        };
    }
}