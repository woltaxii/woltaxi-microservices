package com.woltaxi.travel.service.provider;

import com.woltaxi.travel.entity.TravelBooking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

/**
 * Travel Provider Factory
 * 
 * Factory class for creating travel provider service instances
 * based on the travel provider type.
 * 
 * @author WOLTAXI Development Team
 * @version 1.0.0
 */
@Component
public class TravelProviderFactory {

    private final Map<TravelBooking.TravelProvider, TravelProviderService> providerServices;
    private final java.util.Set<TravelBooking.TravelProvider> supportedProviders;

    @Autowired
    public TravelProviderFactory(
            AmadeusFlightService amadeusFlightService,
            TurkishAirlinesService turkishAirlinesService,
            PegasusAirlinesService pegasusAirlinesService,
            SkyscannerService skyscannerService,
            MetroTurizmService metroTurizmService,
            KamilKocService kamilKocService,
            PamukkaleService pamukkaleService,
            VaranService varanService,
            BookingComService bookingComService,
            ExpediaService expediaService,
            HotelsComService hotelsComService,
            AvisService avisService,
            HertzService hertzService) {
        
        providerServices = new HashMap<>();
        
        // Flight providers
        providerServices.put(TravelBooking.TravelProvider.AMADEUS, amadeusFlightService);
        providerServices.put(TravelBooking.TravelProvider.TURKISH_AIRLINES, turkishAirlinesService);
        providerServices.put(TravelBooking.TravelProvider.PEGASUS, pegasusAirlinesService);
        providerServices.put(TravelBooking.TravelProvider.SKYSCANNER, skyscannerService);
        
        // Bus providers
        providerServices.put(TravelBooking.TravelProvider.METRO_TURIZM, metroTurizmService);
        providerServices.put(TravelBooking.TravelProvider.KAMIL_KOC, kamilKocService);
        providerServices.put(TravelBooking.TravelProvider.PAMUKKALE, pamukkaleService);
        providerServices.put(TravelBooking.TravelProvider.VARAN, varanService);
        
        // Hotel providers
        providerServices.put(TravelBooking.TravelProvider.BOOKING_COM, bookingComService);
        providerServices.put(TravelBooking.TravelProvider.EXPEDIA, expediaService);
        providerServices.put(TravelBooking.TravelProvider.HOTELS_COM, hotelsComService);
        
        // Car rental providers
        providerServices.put(TravelBooking.TravelProvider.AVIS, avisService);
        providerServices.put(TravelBooking.TravelProvider.HERTZ, hertzService);
    }

    /**
     * Get provider service instance for given provider
     */
    public TravelProviderService getProviderService(TravelBooking.TravelProvider provider) {
        TravelProviderService service = providerServices.get(provider);
        if (service == null) {
            throw new UnsupportedProviderException("Unsupported travel provider: " + provider);
        }
        return service;
    }

    /**
     * Check if provider is supported
     */
    public boolean isProviderSupported(TravelBooking.TravelProvider provider) {
        return providerServices.containsKey(provider);
    }

    /**
     * Get all supported providers
     */
    public java.util.Set<TravelBooking.TravelProvider> getSupportedProviders() {
        return providerServices.keySet();
    }

    public static class UnsupportedProviderException extends RuntimeException {
        public UnsupportedProviderException(String message) {
            super(message);
        }
    }
}