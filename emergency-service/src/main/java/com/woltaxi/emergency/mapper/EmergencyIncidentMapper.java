package com.woltaxi.emergency.mapper;

import com.woltaxi.emergency.dto.EmergencyIncidentDto;
import com.woltaxi.emergency.entity.EmergencyIncident;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Emergency Incident Mapper - EmergencyIncident entity ve DTO arasında dönüşüm
 * MapStruct kullanarak otomatik mapping sağlar
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EmergencyIncidentMapper {

    EmergencyIncidentMapper INSTANCE = Mappers.getMapper(EmergencyIncidentMapper.class);

    /**
     * Entity'den DTO'ya dönüştür
     */
    @Mapping(target = "location", expression = "java(mapLocationToDto(entity))")
    @Mapping(target = "contactInfo", expression = "java(mapContactInfoToDto(entity))")
    @Mapping(target = "notifiedContacts", ignore = true) // Ayrı servis çağrısı ile doldurulacak
    @Mapping(target = "actionsTaken", ignore = true) // Ayrı servis çağrısı ile doldurulacak
    @Mapping(target = "mediaFiles", ignore = true) // Ayrı servis çağrısı ile doldurulacak
    @Mapping(target = "automaticResponses", ignore = true) // Ayrı servis çağrısı ile doldurulacak
    @Mapping(target = "performanceMetrics", ignore = true) // Ayrı hesaplama ile doldurulacak
    EmergencyIncidentDto toDto(EmergencyIncident entity);

    /**
     * DTO'dan Entity'ye dönüştür
     */
    @Mapping(target = "latitude", expression = "java(dto.getLocation() != null ? dto.getLocation().getLatitude() : null)")
    @Mapping(target = "longitude", expression = "java(dto.getLocation() != null ? dto.getLocation().getLongitude() : null)")
    @Mapping(target = "locationAccuracy", expression = "java(dto.getLocation() != null ? dto.getLocation().getAccuracy() : null)")
    @Mapping(target = "address", expression = "java(dto.getLocation() != null ? dto.getLocation().getAddress() : null)")
    @Mapping(target = "city", expression = "java(dto.getLocation() != null ? dto.getLocation().getCity() : null)")
    @Mapping(target = "country", expression = "java(dto.getLocation() != null ? dto.getLocation().getCountry() : null)")
    @Mapping(target = "countryCode", expression = "java(dto.getLocation() != null ? dto.getLocation().getCountryCode() : null)")
    @Mapping(target = "locationDetails", expression = "java(dto.getLocation() != null ? dto.getLocation().getLocationDetails() : null)")
    @Mapping(target = "contactEmail", expression = "java(dto.getContactInfo() != null ? dto.getContactInfo().getEmail() : null)")
    @Mapping(target = "contactPhone", expression = "java(dto.getContactInfo() != null ? dto.getContactInfo().getPhoneNumber() : null)")
    @Mapping(target = "alternativeContactPhone", expression = "java(dto.getContactInfo() != null ? dto.getContactInfo().getAlternativePhoneNumber() : null)")
    @Mapping(target = "preferredContactMethod", expression = "java(dto.getContactInfo() != null ? dto.getContactInfo().getPreferredContactMethod() : null)")
    EmergencyIncident toEntity(EmergencyIncidentDto dto);

    /**
     * Entity listesinden DTO listesine dönüştür
     */
    List<EmergencyIncidentDto> toDtoList(List<EmergencyIncident> entities);

    /**
     * DTO listesinden Entity listesine dönüştür
     */
    List<EmergencyIncident> toEntityList(List<EmergencyIncidentDto> dtos);

    /**
     * Mevcut entity'yi DTO ile güncelle
     */
    @Mapping(target = "id", ignore = true) // ID değiştirilmez
    @Mapping(target = "incidentNumber", ignore = true) // Incident number değiştirilmez
    @Mapping(target = "createdAt", ignore = true) // Created timestamp korunur
    @Mapping(target = "latitude", expression = "java(dto.getLocation() != null ? dto.getLocation().getLatitude() : entity.getLatitude())")
    @Mapping(target = "longitude", expression = "java(dto.getLocation() != null ? dto.getLocation().getLongitude() : entity.getLongitude())")
    @Mapping(target = "locationAccuracy", expression = "java(dto.getLocation() != null ? dto.getLocation().getAccuracy() : entity.getLocationAccuracy())")
    @Mapping(target = "address", expression = "java(dto.getLocation() != null ? dto.getLocation().getAddress() : entity.getAddress())")
    @Mapping(target = "city", expression = "java(dto.getLocation() != null ? dto.getLocation().getCity() : entity.getCity())")
    @Mapping(target = "country", expression = "java(dto.getLocation() != null ? dto.getLocation().getCountry() : entity.getCountry())")
    @Mapping(target = "countryCode", expression = "java(dto.getLocation() != null ? dto.getLocation().getCountryCode() : entity.getCountryCode())")
    void updateEntityFromDto(EmergencyIncidentDto dto, @MappingTarget EmergencyIncident entity);

    // Custom mapping methods
    default EmergencyIncidentDto.LocationDto mapLocationToDto(EmergencyIncident entity) {
        if (entity == null) return null;
        
        return EmergencyIncidentDto.LocationDto.builder()
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .accuracy(entity.getLocationAccuracy())
                .address(entity.getAddress())
                .city(entity.getCity())
                .country(entity.getCountry())
                .countryCode(entity.getCountryCode())
                .locationDetails(entity.getLocationDetails())
                .timestamp(entity.getCreatedAt())
                .build();
    }

    default EmergencyIncidentDto.ContactInfoDto mapContactInfoToDto(EmergencyIncident entity) {
        if (entity == null) return null;
        
        return EmergencyIncidentDto.ContactInfoDto.builder()
                .email(entity.getContactEmail())
                .phoneNumber(entity.getContactPhone())
                .alternativePhoneNumber(entity.getAlternativeContactPhone())
                .preferredContactMethod(entity.getPreferredContactMethod())
                .build();
    }

    /**
     * Basit DTO dönüşümü (detaylar olmadan)
     */
    @Mapping(target = "location", expression = "java(mapLocationToDto(entity))")
    @Mapping(target = "contactInfo", expression = "java(mapContactInfoToDto(entity))")
    @Mapping(target = "notifiedContacts", ignore = true)
    @Mapping(target = "actionsTaken", ignore = true)
    @Mapping(target = "mediaFiles", ignore = true)
    @Mapping(target = "automaticResponses", ignore = true)
    @Mapping(target = "performanceMetrics", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "authoritiesContactedList", ignore = true)
    @Mapping(target = "externalReferences", ignore = true)
    EmergencyIncidentDto toSimpleDto(EmergencyIncident entity);

    /**
     * Özet DTO (sadece temel bilgiler)
     */
    @Mapping(target = "location", expression = "java(mapBasicLocationToDto(entity))")
    @Mapping(target = "contactInfo", ignore = true)
    @Mapping(target = "notifiedContacts", ignore = true)
    @Mapping(target = "actionsTaken", ignore = true)
    @Mapping(target = "mediaFiles", ignore = true)
    @Mapping(target = "automaticResponses", ignore = true)
    @Mapping(target = "performanceMetrics", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "authoritiesContactedList", ignore = true)
    @Mapping(target = "externalReferences", ignore = true)
    @Mapping(target = "escalationLevel", ignore = true)
    @Mapping(target = "escalatedAt", ignore = true)
    @Mapping(target = "escalationReason", ignore = true)
    EmergencyIncidentDto toSummaryDto(EmergencyIncident entity);

    default EmergencyIncidentDto.LocationDto mapBasicLocationToDto(EmergencyIncident entity) {
        if (entity == null) return null;
        
        return EmergencyIncidentDto.LocationDto.builder()
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .address(entity.getAddress())
                .city(entity.getCity())
                .country(entity.getCountry())
                .build();
    }
}