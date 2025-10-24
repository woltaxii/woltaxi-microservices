package com.woltaxi.emergency.service.external;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Firebase Service - Push Notification ve Real-time Communication
 * Bu sınıf, Firebase FCM kullanarak push notification ve gerçek zamanlı iletişim sağlar
 */
@Slf4j
@Service
public class FirebaseService {

    @Value("${woltaxi.emergency.integrations.firebase.project-id}")
    private String projectId;

    @Value("${woltaxi.emergency.integrations.firebase.service-account-key}")
    private String serviceAccountKeyPath;

    private FirebaseMessaging firebaseMessaging;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath);
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
            
            this.firebaseMessaging = FirebaseMessaging.getInstance();
            
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
        }
    }

    /**
     * Acil durum push notification gönder
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean sendEmergencyNotification(String userId, String title, String body, String incidentId) {
        try {
            log.info("Sending emergency push notification to user: {} for incident: {}", userId, incidentId);
            
            // Kullanıcının FCM token'ını al (gerçek implementasyonda database'den alınmalı)
            String fcmToken = getUserFcmToken(userId);
            if (fcmToken == null) {
                log.warn("No FCM token found for user: {}", userId);
                return false;
            }
            
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("type", "EMERGENCY")
                    .putData("incidentId", incidentId)
                    .putData("priority", "CRITICAL")
                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setIcon("ic_emergency")
                                    .setColor("#FF0000")
                                    .setSound("emergency_alarm")
                                    .setPriority(AndroidNotification.Priority.MAX)
                                    .setChannelId("emergency_channel")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setAlert(ApsAlert.builder()
                                            .setTitle(title)
                                            .setBody(body)
                                            .build())
                                    .setSound("emergency_alarm.wav")
                                    .setBadge(1)
                                    .setCategory("EMERGENCY")
                                    .setThreadId("emergency_" + incidentId)
                                    .build())
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Emergency notification sent successfully. Message ID: {} for incident: {}", 
                    response, incidentId);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send emergency notification to user: {} for incident: {}", 
                    userId, incidentId, e);
            return false;
        }
    }

    /**
     * Toplu acil durum bildirim gönder
     */
    public void sendBulkEmergencyNotifications(List<String> userIds, 
                                             String title, 
                                             String body, 
                                             String incidentId) {
        log.info("Sending bulk emergency notifications to {} users for incident: {}", 
                userIds.size(), incidentId);
        
        List<CompletableFuture<Boolean>> futures = userIds.stream()
                .map(userId -> CompletableFuture.supplyAsync(() -> 
                        sendEmergencyNotification(userId, title, body, incidentId)))
                .toList();
        
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
                    
                    log.info("Bulk notifications completed for incident: {}. Success: {}/{}", 
                            incidentId, successCount, userIds.size());
                });
    }

    /**
     * Konum paylaşım bildirimi gönder
     */
    public boolean sendLocationSharingNotification(String userId, 
                                                 String sharerName, 
                                                 String trackingUrl, 
                                                 int durationMinutes) {
        try {
            log.info("Sending location sharing notification to user: {}", userId);
            
            String fcmToken = getUserFcmToken(userId);
            if (fcmToken == null) {
                return false;
            }
            
            String title = "Konum Paylaşımı";
            String body = String.format("%s konumunu sizinle paylaşıyor (%d dakika)", 
                    sharerName, durationMinutes);
            
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("type", "LOCATION_SHARING")
                    .putData("trackingUrl", trackingUrl)
                    .putData("duration", String.valueOf(durationMinutes))
                    .putData("sharerName", sharerName)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setIcon("ic_location_sharing")
                                    .setColor("#2196F3")
                                    .setChannelId("location_sharing_channel")
                                    .build())
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Location sharing notification sent successfully. Message ID: {}", response);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send location sharing notification to user: {}", userId, e);
            return false;
        }
    }

    /**
     * SOS iptal bildirimi gönder
     */
    public boolean sendSosCancelNotification(String userId, String incidentNumber) {
        try {
            log.info("Sending SOS cancel notification to user: {}", userId);
            
            String fcmToken = getUserFcmToken(userId);
            if (fcmToken == null) {
                return false;
            }
            
            String title = "SOS İptal Edildi";
            String body = String.format("Acil durum (%s) iptal edildi. Güvendesiniz.", incidentNumber);
            
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("type", "SOS_CANCELLED")
                    .putData("incidentNumber", incidentNumber)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setIcon("ic_check_circle")
                                    .setColor("#4CAF50")
                                    .setChannelId("emergency_channel")
                                    .build())
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("SOS cancel notification sent successfully. Message ID: {}", response);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send SOS cancel notification to user: {}", userId, e);
            return false;
        }
    }

    /**
     * Ses kayıt komutu gönder
     */
    public boolean sendRecordingCommand(String userId, String recordingId, String command) {
        try {
            log.info("Sending recording command: {} to user: {}", command, userId);
            
            String fcmToken = getUserFcmToken(userId);
            if (fcmToken == null) {
                return false;
            }
            
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .putData("type", "RECORDING_COMMAND")
                    .putData("command", command)
                    .putData("recordingId", recordingId)
                    .putData("priority", "HIGH")
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Recording command sent successfully. Message ID: {}", response);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send recording command to user: {}", userId, e);
            return false;
        }
    }

    /**
     * Acil durum durumu güncelleme bildirimi
     */
    public boolean sendEmergencyStatusUpdate(String userId, 
                                           String incidentNumber, 
                                           String status, 
                                           String message) {
        try {
            log.info("Sending emergency status update to user: {} for incident: {}", userId, incidentNumber);
            
            String fcmToken = getUserFcmToken(userId);
            if (fcmToken == null) {
                return false;
            }
            
            String title = "Acil Durum Güncelleme";
            String body = String.format("Olay %s: %s", incidentNumber, message);
            
            Message fcmMessage = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("type", "EMERGENCY_STATUS_UPDATE")
                    .putData("incidentNumber", incidentNumber)
                    .putData("status", status)
                    .putData("message", message)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setIcon("ic_info")
                                    .setColor("#FF9800")
                                    .setChannelId("emergency_updates_channel")
                                    .build())
                            .build())
                    .build();

            String response = firebaseMessaging.send(fcmMessage);
            log.info("Emergency status update sent successfully. Message ID: {}", response);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send emergency status update to user: {}", userId, e);
            return false;
        }
    }

    /**
     * Topic'e acil durum bildirimi gönder
     */
    public boolean sendEmergencyTopicNotification(String topic, 
                                                String title, 
                                                String body, 
                                                Map<String, String> data) {
        try {
            log.info("Sending emergency notification to topic: {}", topic);
            
            Message.Builder messageBuilder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setIcon("ic_emergency")
                                    .setColor("#FF0000")
                                    .setChannelId("emergency_channel")
                                    .build())
                            .build());
            
            if (data != null && !data.isEmpty()) {
                data.forEach(messageBuilder::putData);
            }
            
            Message message = messageBuilder.build();
            String response = firebaseMessaging.send(message);
            
            log.info("Emergency topic notification sent successfully. Message ID: {}", response);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send emergency topic notification to topic: {}", topic, e);
            return false;
        }
    }

    /**
     * Kullanıcıyı topic'e subscribe et
     */
    public boolean subscribeToTopic(String fcmToken, String topic) {
        try {
            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(
                    Collections.singletonList(fcmToken), topic);
            
            log.info("Subscribed to topic: {}. Success count: {}", topic, response.getSuccessCount());
            return response.getSuccessCount() > 0;
            
        } catch (Exception e) {
            log.error("Failed to subscribe to topic: {}", topic, e);
            return false;
        }
    }

    /**
     * Kullanıcının topic subscription'ını kaldır
     */
    public boolean unsubscribeFromTopic(String fcmToken, String topic) {
        try {
            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(
                    Collections.singletonList(fcmToken), topic);
            
            log.info("Unsubscribed from topic: {}. Success count: {}", topic, response.getSuccessCount());
            return response.getSuccessCount() > 0;
            
        } catch (Exception e) {
            log.error("Failed to unsubscribe from topic: {}", topic, e);
            return false;
        }
    }

    // Yardımcı metodlar
    private String getUserFcmToken(String userId) {
        // Gerçek implementasyonda, bu method database'den kullanıcının FCM token'ını almalı
        // Şimdilik mock implementation
        log.debug("Getting FCM token for user: {}", userId);
        
        // Bu bilgi Redis cache'den veya database'den alınmalı
        // Redis'ten al: redisTemplate.opsForValue().get("fcm_token:" + userId)
        
        return "mock_fcm_token_" + userId; // Mock token
    }

    /**
     * FCM token kaydet/güncelle
     */
    public void saveUserFcmToken(String userId, String fcmToken) {
        try {
            // Gerçek implementasyonda Redis'e ve database'e kaydet
            log.info("Saving FCM token for user: {}", userId);
            
            // Redis'e kaydet (hızlı erişim için)
            // redisTemplate.opsForValue().set("fcm_token:" + userId, fcmToken, Duration.ofDays(30));
            
            // Database'e de kaydet (persistence için)
            
        } catch (Exception e) {
            log.error("Failed to save FCM token for user: {}", userId, e);
        }
    }

    /**
     * FCM token doğrula
     */
    public boolean validateFcmToken(String fcmToken) {
        try {
            // Test mesajı göndererek token'ı doğrula
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .putData("type", "TOKEN_VALIDATION")
                    .build();
            
            firebaseMessaging.send(message);
            return true;
            
        } catch (Exception e) {
            log.warn("FCM token validation failed: {}", e.getMessage());
            return false;
        }
    }
}