package com.woltaxi.emergency.service;

import com.woltaxi.emergency.dto.EmergencyContactDto;
import com.woltaxi.emergency.entity.EmergencyContact;
import com.woltaxi.emergency.repository.EmergencyContactRepository;
import com.woltaxi.emergency.mapper.EmergencyContactMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Emergency Contact Service - Acil Durum İletişim Kişileri Hizmet Sınıfı
 * Bu sınıf, acil durum iletişim kişilerinin yönetimini sağlar
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyContactService {

    private final EmergencyContactRepository contactRepository;
    private final EmergencyContactMapper contactMapper;

    /**
     * Yeni acil durum iletişim kişisi ekle
     */
    @Transactional
    public EmergencyContactDto addEmergencyContact(EmergencyContactDto contactDto) {
        log.info("Adding new emergency contact for user: {}", contactDto.getUserId());
        
        // Unique ID oluştur
        contactDto.setId(UUID.randomUUID().toString());
        contactDto.setCreatedAt(LocalDateTime.now());
        contactDto.setUpdatedAt(LocalDateTime.now());
        contactDto.setVerificationStatus("PENDING");
        
        // Entity'ye dönüştür ve kaydet
        EmergencyContact contact = contactMapper.toEntity(contactDto);
        EmergencyContact savedContact = contactRepository.save(contact);
        
        log.info("Emergency contact added with ID: {}", savedContact.getId());
        return contactMapper.toDto(savedContact);
    }

    /**
     * Acil durum iletişim kişisini güncelle
     */
    @Transactional
    public EmergencyContactDto updateEmergencyContact(String contactId, EmergencyContactDto contactDto) {
        log.info("Updating emergency contact: {}", contactId);
        
        EmergencyContact existingContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found: " + contactId));
        
        // Güncellenebilir alanları kopyala
        updateContactFields(existingContact, contactDto);
        existingContact.setUpdatedAt(LocalDateTime.now());
        
        EmergencyContact updatedContact = contactRepository.save(existingContact);
        
        log.info("Emergency contact updated: {}", contactId);
        return contactMapper.toDto(updatedContact);
    }

    /**
     * Acil durum iletişim kişisini sil
     */
    @Transactional
    public void deleteEmergencyContact(String contactId) {
        log.info("Deleting emergency contact: {}", contactId);
        
        if (!contactRepository.existsById(contactId)) {
            throw new RuntimeException("Emergency contact not found: " + contactId);
        }
        
        contactRepository.deleteById(contactId);
        log.info("Emergency contact deleted: {}", contactId);
    }

    /**
     * Kullanıcının acil durum iletişim kişilerini al
     */
    @Transactional(readOnly = true)
    public List<EmergencyContactDto> getUserEmergencyContacts(String userId) {
        log.debug("Getting emergency contacts for user: {}", userId);
        
        List<EmergencyContact> contacts = contactRepository.findByUserIdOrderByPriorityAsc(userId);
        return contacts.stream()
                .map(contactMapper::toDto)
                .toList();
    }

    /**
     * Kullanıcının aktif acil durum iletişim kişilerini al
     */
    @Transactional(readOnly = true)
    public List<EmergencyContact> getActiveEmergencyContacts(String userId) {
        log.debug("Getting active emergency contacts for user: {}", userId);
        
        return contactRepository.findByUserIdAndEnabledTrueOrderByPriorityAsc(userId);
    }

    /**
     * Acil durum iletişim kişisini ID ile al
     */
    @Transactional(readOnly = true)
    public Optional<EmergencyContactDto> getEmergencyContactById(String contactId) {
        log.debug("Getting emergency contact by ID: {}", contactId);
        
        return contactRepository.findById(contactId)
                .map(contactMapper::toDto);
    }

    /**
     * Kullanıcının acil durum iletişim kişilerini sayfalı al
     */
    @Transactional(readOnly = true)
    public Page<EmergencyContactDto> getUserEmergencyContactsPaged(String userId, Pageable pageable) {
        log.debug("Getting paged emergency contacts for user: {}", userId);
        
        Page<EmergencyContact> contacts = contactRepository.findByUserId(userId, pageable);
        return contacts.map(contactMapper::toDto);
    }

    /**
     * İletişim kişisi doğrulama durumunu güncelle
     */
    @Transactional
    public void updateVerificationStatus(String contactId, String status) {
        log.info("Updating verification status for contact: {} to {}", contactId, status);
        
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found: " + contactId));
        
        contact.setVerificationStatus(status);
        contact.setLastVerifiedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        
        contactRepository.save(contact);
        log.info("Verification status updated for contact: {}", contactId);
    }

    /**
     * İletişim kişisinin aktif/pasif durumunu değiştir
     */
    @Transactional
    public void toggleContactStatus(String contactId, boolean enabled) {
        log.info("Toggling contact status for: {} to {}", contactId, enabled);
        
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found: " + contactId));
        
        contact.setEnabled(enabled);
        contact.setUpdatedAt(LocalDateTime.now());
        
        contactRepository.save(contact);
        log.info("Contact status toggled for: {}", contactId);
    }

    /**
     * İletişim kişisinin önceliğini güncelle
     */
    @Transactional
    public void updateContactPriority(String contactId, int priority) {
        log.info("Updating priority for contact: {} to {}", contactId, priority);
        
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found: " + contactId));
        
        contact.setPriority(priority);
        contact.setUpdatedAt(LocalDateTime.now());
        
        contactRepository.save(contact);
        log.info("Priority updated for contact: {}", contactId);
    }

    /**
     * İletişim kişisinin son iletişim zamanını güncelle
     */
    @Transactional
    public void updateLastContactTime(String contactId) {
        log.debug("Updating last contact time for: {}", contactId);
        
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found: " + contactId));
        
        contact.setLastContactedAt(LocalDateTime.now());
        contact.setNotificationCount(contact.getNotificationCount() + 1);
        contact.setUpdatedAt(LocalDateTime.now());
        
        contactRepository.save(contact);
        log.debug("Last contact time updated for: {}", contactId);
    }

    /**
     * Belirli türdeki iletişim kişilerini al
     */
    @Transactional(readOnly = true)
    public List<EmergencyContactDto> getContactsByType(String userId, String contactType) {
        log.debug("Getting contacts by type: {} for user: {}", contactType, userId);
        
        List<EmergencyContact> contacts = contactRepository.findByUserIdAndContactTypeOrderByPriorityAsc(userId, contactType);
        return contacts.stream()
                .map(contactMapper::toDto)
                .toList();
    }

    /**
     * Yüksek öncelikli iletişim kişilerini al
     */
    @Transactional(readOnly = true)
    public List<EmergencyContactDto> getHighPriorityContacts(String userId, int maxPriority) {
        log.debug("Getting high priority contacts for user: {} with max priority: {}", userId, maxPriority);
        
        List<EmergencyContact> contacts = contactRepository.findByUserIdAndEnabledTrueAndPriorityLessThanEqualOrderByPriorityAsc(userId, maxPriority);
        return contacts.stream()
                .map(contactMapper::toDto)
                .toList();
    }

    /**
     * Kullanıcının toplam aktif iletişim kişisi sayısını al
     */
    @Transactional(readOnly = true)
    public long getActiveContactCount(String userId) {
        return contactRepository.countByUserIdAndEnabledTrue(userId);
    }

    /**
     * İletişim kişisi doğrulama kontrolü
     */
    @Transactional(readOnly = true)
    public boolean isContactVerified(String contactId) {
        return contactRepository.findById(contactId)
                .map(contact -> "VERIFIED".equals(contact.getVerificationStatus()))
                .orElse(false);
    }

    /**
     * Belirli senaryolar için uygun iletişim kişilerini al
     */
    @Transactional(readOnly = true)
    public List<EmergencyContact> getContactsForScenario(String userId, String scenario) {
        log.debug("Getting contacts for scenario: {} for user: {}", scenario, userId);
        
        // Bu method, emergency scenario preferences'a göre uygun kişileri filtreler
        List<EmergencyContact> allContacts = getActiveEmergencyContacts(userId);
        
        return allContacts.stream()
                .filter(contact -> isContactSuitableForScenario(contact, scenario))
                .toList();
    }

    /**
     * Toplu iletişim kişisi güncelleme
     */
    @Transactional
    public void bulkUpdateContacts(String userId, List<EmergencyContactDto> contacts) {
        log.info("Bulk updating {} contacts for user: {}", contacts.size(), userId);
        
        for (EmergencyContactDto contactDto : contacts) {
            if (contactDto.getId() != null) {
                try {
                    updateEmergencyContact(contactDto.getId(), contactDto);
                } catch (Exception e) {
                    log.error("Failed to update contact: {}", contactDto.getId(), e);
                }
            }
        }
        
        log.info("Bulk update completed for user: {}", userId);
    }

    // Yardımcı metodlar
    private void updateContactFields(EmergencyContact existing, EmergencyContactDto updated) {
        if (updated.getContactName() != null) {
            existing.setContactName(updated.getContactName());
        }
        if (updated.getPhoneNumber() != null) {
            existing.setPhoneNumber(updated.getPhoneNumber());
        }
        if (updated.getAlternativePhoneNumber() != null) {
            existing.setAlternativePhoneNumber(updated.getAlternativePhoneNumber());
        }
        if (updated.getEmail() != null) {
            existing.setEmail(updated.getEmail());
        }
        if (updated.getContactType() != null) {
            existing.setContactType(updated.getContactType());
        }
        if (updated.getRelationship() != null) {
            existing.setRelationship(updated.getRelationship());
        }
        if (updated.getPriority() != null) {
            existing.setPriority(updated.getPriority());
        }
        if (updated.getEnabled() != null) {
            existing.setEnabled(updated.getEnabled());
        }
        if (updated.getNotes() != null) {
            existing.setNotes(updated.getNotes());
        }
        if (updated.getPreferredLanguage() != null) {
            existing.setPreferredLanguage(updated.getPreferredLanguage());
        }
        if (updated.getTimeZone() != null) {
            existing.setTimeZone(updated.getTimeZone());
        }
        if (updated.getAutoNotify() != null) {
            existing.setAutoNotify(updated.getAutoNotify());
        }
        if (updated.getNotificationDelaySeconds() != null) {
            existing.setNotificationDelaySeconds(updated.getNotificationDelaySeconds());
        }
        if (updated.getAvailable24x7() != null) {
            existing.setAvailable24x7(updated.getAvailable24x7());
        }
    }

    private boolean isContactSuitableForScenario(EmergencyContact contact, String scenario) {
        // Bu method, contact'ın emergency scenario preferences'ına göre
        // belirli bir senaryo için uygun olup olmadığını kontrol eder
        
        // Şimdilik basit bir implementasyon
        return contact.getEnabled() && contact.getAutoNotify();
    }
}