package com.ispilo.job;

import com.ispilo.model.entity.User;
import com.ispilo.repository.UserRepository;
import com.ispilo.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnverifiedAccountCleanupJob {

    private final UserRepository userRepository;
    private final FcmService fcmService;

    // Run every day at 3:00 AM
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldUnverifiedAccounts() {
        log.info("Starting cleanup of unverified accounts older than 24 hours...");
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<User> usersToDelete = userRepository.findUnverifiedUsersOlderThan(cutoff);
        
        if (!usersToDelete.isEmpty()) {
            userRepository.deleteAll(usersToDelete);
            log.info("Deleted {} unverified accounts.", usersToDelete.size());
        } else {
            log.info("No unverified accounts to delete.");
        }
    }

    // Run every 5 minutes to check for users who haven't verified within 5 minutes.
    // We check between 5 and 10 minutes ago so we don't repeatedly send the notification.
    @Scheduled(fixedRate = 300000)
    public void sendVerificationReminders() {
        log.info("Checking for unverified accounts to send FCM reminders...");
        LocalDateTime endTime = LocalDateTime.now().minusMinutes(5);
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(10);
        
        List<User> usersToRemind = userRepository.findUnverifiedUsersForReminder(startTime, endTime);
        for (User user : usersToRemind) {
            if (Boolean.TRUE.equals(user.getPrivateNotification())) {
                fcmService.sendNotification(
                    user.getFcmToken(), 
                    "Verify your phone number", 
                    "Please verify your phone number to complete your registration."
                );
                log.info("Sent verification reminder to user {}", user.getId());
            }
        }
    }
}
