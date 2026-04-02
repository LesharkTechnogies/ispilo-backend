package com.ispilo.service;

import com.ispilo.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NotificationService {

    // In a real application, you would inject FirebaseMessaging or another push notification client here.

    /**
     * Send a notification to a specific user.
     */
    @Async
    public void sendPushNotification(User user, String title, String body, String type, String targetId) {
        String fcmToken = user.getFcmToken();
        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("Cannot send push notification to user {} (no FCM token). Title: {}", user.getId(), title);
            return;
        }

        log.info("Sending Push Notification to {} [token: {}] - Title: {}, Body: {}, Type: {}, TargetId: {}", 
                user.getId(), fcmToken, title, body, type, targetId);
        
        // TODO: Implement actual push notification logic (e.g., Firebase Cloud Messaging)
        // Message message = Message.builder()
        //         .setToken(fcmToken)
        //         .setNotification(Notification.builder()
        //                 .setTitle(title)
        //                 .setBody(body)
        //                 .build())
        //         .putData("type", type)
        //         .putData("targetId", targetId)
        //         .build();
        // FirebaseMessaging.getInstance().sendAsync(message);
    }

    /**
     * Send a notification to multiple users.
     */
    @Async
    public void sendPushNotifications(List<User> users, String title, String body, String type, String targetId) {
        for (User user : users) {
            sendPushNotification(user, title, body, type, targetId);
        }
    }
}
