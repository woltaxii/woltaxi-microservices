package com.woltaxi.emergency.mapper;

import com.woltaxi.emergency.dto.EmergencyContactDto;
import com.woltaxi.emergency.entity.EmergencyContact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Emergency Contact Mapper - EmergencyContact entity ve DTO arasında dönüşüm
 * MapStruct kullanarak otomatik mapping sağlar
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EmergencyContactMapper {

    EmergencyContactMapper INSTANCE = Mappers.getMapper(EmergencyContactMapper.class);

    /**
     * Entity'den DTO'ya dönüştür
     */
    @Mapping(target = "notificationPreferences", expression = "java(mapNotificationPreferencesToDto(entity))")
    @Mapping(target = "address", expression = "java(mapAddressToDto(entity))")
    @Mapping(target = "availability", expression = "java(mapAvailabilityToDto(entity))")
    @Mapping(target = "medicalInfo", expression = "java(mapMedicalInfoToDto(entity))")
    @Mapping(target = "scenarioPreferences", expression = "java(mapScenarioPreferencesToDto(entity))")
    EmergencyContactDto toDto(EmergencyContact entity);

    /**
     * DTO'dan Entity'ye dönüştür
     */
    @Mapping(target = "smsEnabled", expression = "java(dto.getNotificationPreferences() != null ? dto.getNotificationPreferences().getSmsEnabled() : true)")
    @Mapping(target = "callEnabled", expression = "java(dto.getNotificationPreferences() != null ? dto.getNotificationPreferences().getCallEnabled() : true)")
    @Mapping(target = "emailEnabled", expression = "java(dto.getNotificationPreferences() != null ? dto.getNotificationPreferences().getEmailEnabled() : true)")
    @Mapping(target = "whatsappEnabled", expression = "java(dto.getNotificationPreferences() != null ? dto.getNotificationPreferences().getWhatsappEnabled() : false)")
    @Mapping(target = "pushEnabled", expression = "java(dto.getNotificationPreferences() != null ? dto.getNotificationPreferences().getPushEnabled() : true)")
    @Mapping(target = "preferredNotificationMethod", expression = "java(dto.getNotificationPreferences() != null ? dto.getNotificationPreferences().getPreferredMethod() : \"SMS\")")
    @Mapping(target = "maxNotificationAttempts", expression = "java(dto.getNotificationPreferences() != null ? dto.getNotificationPreferences().getMaxAttempts() : 3)")
    @Mapping(target = "retryIntervalMinutes", expression = "java(dto.getNotificationPreferences() != null ? dto.getNotificationPreferences().getRetryIntervalMinutes() : 5)")
    @Mapping(target = "addressStreet", expression = "java(dto.getAddress() != null ? dto.getAddress().getStreet() : null)")
    @Mapping(target = "addressCity", expression = "java(dto.getAddress() != null ? dto.getAddress().getCity() : null)")
    @Mapping(target = "addressState", expression = "java(dto.getAddress() != null ? dto.getAddress().getState() : null)")
    @Mapping(target = "addressPostalCode", expression = "java(dto.getAddress() != null ? dto.getAddress().getPostalCode() : null)")
    @Mapping(target = "addressCountry", expression = "java(dto.getAddress() != null ? dto.getAddress().getCountry() : null)")
    @Mapping(target = "addressCountryCode", expression = "java(dto.getAddress() != null ? dto.getAddress().getCountryCode() : null)")
    @Mapping(target = "addressLatitude", expression = "java(dto.getAddress() != null ? dto.getAddress().getLatitude() : null)")
    @Mapping(target = "addressLongitude", expression = "java(dto.getAddress() != null ? dto.getAddress().getLongitude() : null)")
    EmergencyContact toEntity(EmergencyContactDto dto);

    /**
     * Entity listesinden DTO listesine dönüştür
     */
    List<EmergencyContactDto> toDtoList(List<EmergencyContact> entities);

    /**
     * DTO listesinden Entity listesine dönüştür
     */
    List<EmergencyContact> toEntityList(List<EmergencyContactDto> dtos);

    /**
     * Mevcut entity'yi DTO ile güncelle
     */
    @Mapping(target = "id", ignore = true) // ID değiştirilmez
    @Mapping(target = "userId", ignore = true) // User ID değiştirilmez
    @Mapping(target = "createdAt", ignore = true) // Created timestamp korunur
    @Mapping(target = "notificationCount", ignore = true) // Notification count korunur
    @Mapping(target = "lastContactedAt", ignore = true) // Last contacted korunur
    void updateEntityFromDto(EmergencyContactDto dto, @MappingTarget EmergencyContact entity);

    // Custom mapping methods for nested DTOs
    default EmergencyContactDto.NotificationPreferencesDto mapNotificationPreferencesToDto(EmergencyContact entity) {
        if (entity == null) return null;
        
        return EmergencyContactDto.NotificationPreferencesDto.builder()
                .smsEnabled(entity.getSmsEnabled())
                .callEnabled(entity.getCallEnabled())
                .emailEnabled(entity.getEmailEnabled())
                .whatsappEnabled(entity.getWhatsappEnabled())
                .pushEnabled(entity.getPushEnabled())
                .preferredMethod(entity.getPreferredNotificationMethod())
                .maxAttempts(entity.getMaxNotificationAttempts())
                .retryIntervalMinutes(entity.getRetryIntervalMinutes())
                .build();
    }

    default EmergencyContactDto.AddressDto mapAddressToDto(EmergencyContact entity) {
        if (entity == null) return null;
        
        return EmergencyContactDto.AddressDto.builder()
                .street(entity.getAddressStreet())
                .city(entity.getAddressCity())
                .state(entity.getAddressState())
                .postalCode(entity.getAddressPostalCode())
                .country(entity.getAddressCountry())
                .countryCode(entity.getAddressCountryCode())
                .latitude(entity.getAddressLatitude())
                .longitude(entity.getAddressLongitude())
                .build();
    }

    default EmergencyContactDto.AvailabilityDto mapAvailabilityToDto(EmergencyContact entity) {
        if (entity == null || !entity.getAvailable24x7()) {
            return EmergencyContactDto.AvailabilityDto.builder()
                    .monday(true)
                    .tuesday(true)
                    .wednesday(true)
                    .thursday(true)
                    .friday(true)
                    .saturday(true)
                    .sunday(true)
                    .startTime("00:00")
                    .endTime("23:59")
                    .build();
        }
        
        return EmergencyContactDto.AvailabilityDto.builder()
                .monday(true)
                .tuesday(true)
                .wednesday(true)
                .thursday(true)
                .friday(true)
                .saturday(true)
                .sunday(true)
                .startTime("00:00")
                .endTime("23:59")
                .notes("24/7 erişilebilir")
                .build();
    }

    default EmergencyContactDto.MedicalInfoDto mapMedicalInfoToDto(EmergencyContact entity) {
        // Medical info şu anda entity'de ayrı field'lar halinde değil
        // Gelecekte eklenebilir veya JSON field olarak saklanabilir
        return null;
    }

    default EmergencyContactDto.EmergencyScenarioPreferencesDto mapScenarioPreferencesToDto(EmergencyContact entity) {
        if (entity == null) return null;
        
        // Varsayılan scenario preferences döndür
        return EmergencyContactDto.EmergencyScenarioPreferencesDto.builder()
                .sosIncidents(true)
                .panicButton(true)
                .medicalEmergencies(true)
                .accidents(true)
                .harassment(true)
                .vehicleBreakdowns(false)
                .safetyConcerns(true)
                .naturalDisasters(true)
                .crimeInProgress(true)
                .otherIncidents(false)
                .minimumPriority("MEDIUM")
                .build();
    }

    /**
     * Basit DTO dönüşümü (sadece temel bilgiler)
     */
    @Mapping(target = "notificationPreferences", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "availability", ignore = true)
    @Mapping(target = "medicalInfo", ignore = true)
    @Mapping(target = "scenarioPreferences", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    EmergencyContactDto toSimpleDto(EmergencyContact entity);

    /**
     * Özet DTO (liste görünümü için)
     */
    @Mapping(target = "notificationPreferences", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "availability", ignore = true)
    @Mapping(target = "medicalInfo", ignore = true)
    @Mapping(target = "scenarioPreferences", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "timeZone", ignore = true)
    @Mapping(target = "lastContactedAt", ignore = true)
    @Mapping(target = "lastIncidentId", ignore = true)
    @Mapping(target = "lastVerifiedAt", ignore = true)
    EmergencyContactDto toSummaryDto(EmergencyContact entity);

    /**
     * Bildirim için DTO (sadece iletişim bilgileri)
     */
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "availability", ignore = true)
    @Mapping(target = "medicalInfo", ignore = true)
    @Mapping(target = "scenarioPreferences", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "timeZone", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastContactedAt", ignore = true)
    @Mapping(target = "lastIncidentId", ignore = true)
    @Mapping(target = "lastVerifiedAt", ignore = true)
    @Mapping(target = "averageResponseTimeMinutes", ignore = true)
    @Mapping(target = "hasRespondedBefore", ignore = true)
    @Mapping(target = "notificationSuccessRate", ignore = true)
    @Mapping(target = "notificationPreferences", expression = "java(mapNotificationPreferencesToDto(entity))")
    EmergencyContactDto toNotificationDto(EmergencyContact entity);
}