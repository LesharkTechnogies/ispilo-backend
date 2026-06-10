package com.ispilo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {

    // Placeholder for FCM Integration. Once keys are configured, this will initialize FirebaseApp
    // and use FirebaseMessaging.getInstance().send(message);

    @Async
    public void sendNotification(String targetToken, String title, String body) {
        if (targetToken == null || targetToken.isBlank()) {
            log.warn("Cannot send FCM notification, target token is empty. Title: {}", title);
            return;
        }

        log.info("Mock FCM Notification sent to token {}. Title: {}, Body: {}", targetToken, title, body);
        
        // TODO: Implement actual FirebaseMessaging logic here when keys are provided.
        /*
        Message message = Message.builder()
            .setToken(targetToken)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .build();
        FirebaseMessaging.getInstance().send(message);
        */
    }
}
