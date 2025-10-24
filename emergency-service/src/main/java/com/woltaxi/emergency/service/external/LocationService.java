package com.woltaxi.emergency.service.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Location Service - Konum ve Coğrafi Bilgi Hizmetleri
 * Bu sınıf, konum tracking, geocoding ve ülke bilgisi sağlar
 */
@Slf4j
@Service
public class LocationService {

    @Value("${woltaxi.emergency.integrations.google-maps.api-key}")
    private String googleMapsApiKey;

    private final RestTemplate restTemplate;
    private final Map<String, LocationSession> activeSessions = new ConcurrentHashMap<>();

    public LocationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Acil durum konum takibi başlat
     */
    public String startEmergencyTracking(String incidentId, 
                                       String userId, 
                                       int durationMinutes, 
                                       int updateIntervalSeconds) {
        try {
            log.info("Starting emergency location tracking for incident: {} user: {}", incidentId, userId);
            
            String sessionId = UUID.randomUUID().toString();
            LocationSession session = new LocationSession(
                    sessionId,
                    incidentId,
                    userId,
                    System.currentTimeMillis(),
                    durationMinutes * 60 * 1000L, // Convert to milliseconds
                    updateIntervalSeconds * 1000L,
                    true
            );
            
            activeSessions.put(sessionId, session);
            
            log.info("Emergency location tracking started. Session ID: {}", sessionId);
            return sessionId;
            
        } catch (Exception e) {
            log.error("Failed to start emergency location tracking for incident: {}", incidentId, e);
            throw new RuntimeException("Failed to start location tracking", e);
        }
    }

    /**
     * Konum takibi durdur
     */
    public boolean stopLocationTracking(String sessionId) {
        try {
            log.info("Stopping location tracking for session: {}", sessionId);
            
            LocationSession session = activeSessions.get(sessionId);
            if (session != null) {
                session.setActive(false);
                activeSessions.remove(sessionId);
                log.info("Location tracking stopped for session: {}", sessionId);
                return true;
            } else {
                log.warn("Location tracking session not found: {}", sessionId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Failed to stop location tracking for session: {}", sessionId, e);
            return false;
        }
    }

    /**
     * Konum güncelle
     */
    public boolean updateLocation(String sessionId, double latitude, double longitude, double accuracy) {
        try {
            LocationSession session = activeSessions.get(sessionId);
            if (session == null || !session.isActive()) {
                log.warn("Invalid or inactive location session: {}", sessionId);
                return false;
            }
            
            // Session süresini kontrol et
            if (System.currentTimeMillis() - session.getStartTime() > session.getDurationMs()) {
                log.info("Location session expired: {}", sessionId);
                stopLocationTracking(sessionId);
                return false;
            }
            
            session.setLastLatitude(latitude);
            session.setLastLongitude(longitude);
            session.setLastAccuracy(accuracy);
            session.setLastUpdateTime(System.currentTimeMillis());
            
            // Konum verilerini gerçek zamanlı olarak yayınla (WebSocket, Kafka etc.)
            publishLocationUpdate(session, latitude, longitude, accuracy);
            
            log.debug("Location updated for session: {} - Lat: {}, Lng: {}, Accuracy: {}", 
                    sessionId, latitude, longitude, accuracy);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to update location for session: {}", sessionId, e);
            return false;
        }
    }

    /**
     * Koordinatlardan ülke kodu al
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String getCountryCode(double latitude, double longitude) {
        try {
            log.debug("Getting country code for coordinates: {}, {}", latitude, longitude);
            
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s&result_type=country",
                    latitude, longitude, googleMapsApiKey
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "OK".equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                var results = (java.util.List<Map<String, Object>>) response.get("results");
                
                if (!results.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    var components = (java.util.List<Map<String, Object>>) results.get(0).get("address_components");
                    
                    for (Map<String, Object> component : components) {
                        @SuppressWarnings("unchecked")
                        var types = (java.util.List<String>) component.get("types");
                        if (types.contains("country")) {
                            String countryCode = (String) component.get("short_name");
                            log.debug("Country code found: {} for coordinates: {}, {}", 
                                    countryCode, latitude, longitude);
                            return countryCode;
                        }
                    }
                }
            }
            
            log.warn("Could not determine country code for coordinates: {}, {}", latitude, longitude);
            return "TR"; // Default to Turkey
            
        } catch (Exception e) {
            log.error("Failed to get country code for coordinates: {}, {}", latitude, longitude, e);
            return "TR"; // Default to Turkey
        }
    }

    /**
     * Koordinatlardan adres al (Reverse Geocoding)
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String getAddressFromCoordinates(double latitude, double longitude, String language) {
        try {
            log.debug("Getting address for coordinates: {}, {} in language: {}", latitude, longitude, language);
            
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s&language=%s",
                    latitude, longitude, googleMapsApiKey, language != null ? language : "tr"
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "OK".equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                var results = (java.util.List<Map<String, Object>>) response.get("results");
                
                if (!results.isEmpty()) {
                    String address = (String) results.get(0).get("formatted_address");
                    log.debug("Address found: {} for coordinates: {}, {}", address, latitude, longitude);
                    return address;
                }
            }
            
            log.warn("Could not get address for coordinates: {}, {}", latitude, longitude);
            return String.format("Lat: %.4f, Lng: %.4f", latitude, longitude);
            
        } catch (Exception e) {
            log.error("Failed to get address for coordinates: {}, {}", latitude, longitude, e);
            return String.format("Lat: %.4f, Lng: %.4f", latitude, longitude);
        }
    }

    /**
     * İki nokta arasındaki mesafeyi hesapla (metre)
     */
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Dünya'nın yarıçapı (km)
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // Convert to meters
        
        return distance;
    }

    /**
     * Geofence kontrolü yap
     */
    public boolean isWithinGeofence(double latitude, double longitude, 
                                  double centerLat, double centerLng, double radiusMeters) {
        double distance = calculateDistance(latitude, longitude, centerLat, centerLng);
        return distance <= radiusMeters;
    }

    /**
     * Aktif konum takip session'ları al
     */
    public Map<String, LocationSession> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }

    /**
     * Belirli bir session bilgilerini al
     */
    public LocationSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Kullanıcının aktif session'ını al
     */
    public LocationSession getUserActiveSession(String userId) {
        return activeSessions.values().stream()
                .filter(session -> session.getUserId().equals(userId) && session.isActive())
                .findFirst()
                .orElse(null);
    }

    /**
     * Süresi dolmuş session'ları temizle
     */
    public void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        
        activeSessions.entrySet().removeIf(entry -> {
            LocationSession session = entry.getValue();
            boolean expired = (currentTime - session.getStartTime()) > session.getDurationMs();
            
            if (expired) {
                log.info("Cleaning up expired location session: {}", entry.getKey());
                session.setActive(false);
            }
            
            return expired;
        });
    }

    /**
     * Acil durum için en yakın hastane/polis karakolu bul
     */
    public java.util.List<NearbyPlace> findNearbyEmergencyServices(double latitude, 
                                                                   double longitude, 
                                                                   String serviceType, 
                                                                   int radiusMeters) {
        try {
            log.info("Finding nearby {} services for location: {}, {} within {} meters", 
                    serviceType, latitude, longitude, radiusMeters);
            
            String placeType = mapServiceTypeToGoogleType(serviceType);
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&type=%s&key=%s",
                    latitude, longitude, radiusMeters, placeType, googleMapsApiKey
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            java.util.List<NearbyPlace> places = new java.util.ArrayList<>();
            
            if (response != null && "OK".equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                var results = (java.util.List<Map<String, Object>>) response.get("results");
                
                for (Map<String, Object> result : results) {
                    @SuppressWarnings("unchecked")
                    var location = (Map<String, Object>) ((Map<String, Object>) result.get("geometry")).get("location");
                    
                    places.add(new NearbyPlace(
                            (String) result.get("place_id"),
                            (String) result.get("name"),
                            (String) result.get("vicinity"),
                            (Double) location.get("lat"),
                            (Double) location.get("lng"),
                            (Double) result.get("rating"),
                            serviceType
                    ));
                }
                
                // Mesafeye göre sırala
                places.sort((a, b) -> {
                    double distA = calculateDistance(latitude, longitude, a.getLatitude(), a.getLongitude());
                    double distB = calculateDistance(latitude, longitude, b.getLatitude(), b.getLongitude());
                    return Double.compare(distA, distB);
                });
            }
            
            log.info("Found {} nearby {} services", places.size(), serviceType);
            return places;
            
        } catch (Exception e) {
            log.error("Failed to find nearby emergency services", e);
            return new java.util.ArrayList<>();
        }
    }

    // Yardımcı metodlar
    private void publishLocationUpdate(LocationSession session, double latitude, double longitude, double accuracy) {
        try {
            // Gerçek implementasyonda WebSocket veya Kafka ile publish edilmeli
            log.debug("Publishing location update for session: {} - Incident: {}", 
                    session.getSessionId(), session.getIncidentId());
            
            // WebSocket publish:
            // webSocketService.publishLocationUpdate(session.getIncidentId(), latitude, longitude, accuracy);
            
            // Kafka publish:
            // kafkaTemplate.send("location-updates", session.getIncidentId(), locationUpdateEvent);
            
        } catch (Exception e) {
            log.error("Failed to publish location update", e);
        }
    }

    private String mapServiceTypeToGoogleType(String serviceType) {
        return switch (serviceType.toUpperCase()) {
            case "HOSPITAL" -> "hospital";
            case "POLICE" -> "police";
            case "FIRE_STATION" -> "fire_station";
            case "PHARMACY" -> "pharmacy";
            case "GAS_STATION" -> "gas_station";
            default -> "hospital";
        };
    }

    // Inner Classes
    public static class LocationSession {
        private final String sessionId;
        private final String incidentId;
        private final String userId;
        private final long startTime;
        private final long durationMs;
        private final long updateIntervalMs;
        private boolean active;
        private double lastLatitude;
        private double lastLongitude;
        private double lastAccuracy;
        private long lastUpdateTime;

        public LocationSession(String sessionId, String incidentId, String userId, 
                             long startTime, long durationMs, long updateIntervalMs, boolean active) {
            this.sessionId = sessionId;
            this.incidentId = incidentId;
            this.userId = userId;
            this.startTime = startTime;
            this.durationMs = durationMs;
            this.updateIntervalMs = updateIntervalMs;
            this.active = active;
        }

        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public String getIncidentId() { return incidentId; }
        public String getUserId() { return userId; }
        public long getStartTime() { return startTime; }
        public long getDurationMs() { return durationMs; }
        public long getUpdateIntervalMs() { return updateIntervalMs; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public double getLastLatitude() { return lastLatitude; }
        public void setLastLatitude(double lastLatitude) { this.lastLatitude = lastLatitude; }
        
        public double getLastLongitude() { return lastLongitude; }
        public void setLastLongitude(double lastLongitude) { this.lastLongitude = lastLongitude; }
        
        public double getLastAccuracy() { return lastAccuracy; }
        public void setLastAccuracy(double lastAccuracy) { this.lastAccuracy = lastAccuracy; }
        
        public long getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }

    public static class NearbyPlace {
        private final String placeId;
        private final String name;
        private final String address;
        private final double latitude;
        private final double longitude;
        private final Double rating;
        private final String serviceType;

        public NearbyPlace(String placeId, String name, String address, 
                          double latitude, double longitude, Double rating, String serviceType) {
            this.placeId = placeId;
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.rating = rating;
            this.serviceType = serviceType;
        }

        // Getters
        public String getPlaceId() { return placeId; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public Double getRating() { return rating; }
        public String getServiceType() { return serviceType; }
    }
}