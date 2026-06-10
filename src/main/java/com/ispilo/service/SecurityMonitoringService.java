package com.ispilo.service;

import com.ispilo.model.entity.BannedDevice;
import com.ispilo.model.entity.User;
import com.ispilo.repository.BannedDeviceRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityMonitoringService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final BannedDeviceRepository bannedDeviceRepository;
    private final BannedDeviceCacheService bannedDeviceCacheService;
    private final FcmService fcmService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String LOGIN_ATTEMPTS_PREFIX = "security:login:attempts:";
    private static final int MAX_FAILED_LOGINS = 20;

    public void recordFailedLogin(String phone, String deviceId) {
        String key = LOGIN_ATTEMPTS_PREFIX + phone;
        String attemptsStr = redisTemplate.opsForValue().get(key);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) + 1 : 1;
        
        redisTemplate.opsForValue().set(key, String.valueOf(attempts), Duration.ofHours(24));
        
        if (attempts >= MAX_FAILED_LOGINS) {
            log.warn("Phone {} reached max failed logins. Flagging account and device.", phone);
            flagAccountAndDevice(phone, deviceId, "Exceeded maximum failed login attempts");
            redisTemplate.delete(key);
        }
    }

    public void resetLoginAttempts(String phone) {
        redisTemplate.delete(LOGIN_ATTEMPTS_PREFIX + phone);
    }

    public void flagAccountAndDevice(String phone, String deviceId, String reason) {
        // Flag User
        userRepository.findByPhone(phone).ifPresent(user -> {
            user.setIsFlagged(true);
            user.setFlagReason(reason);
            user.setBlockedUntil(LocalDateTime.now().plusDays(7)); // Block for 7 days
            userRepository.save(user);

            // Send FCM
            if (Boolean.TRUE.equals(user.getPrivateNotification())) {
                fcmService.sendNotification(user.getFcmToken(), "Security Alert", "Your account has been flagged due to suspicious activity.");
            }

            // WebSocket event to log user out / block phone
            messagingTemplate.convertAndSendToUser(
                user.getId(),
                "/queue/security.flagged",
                java.util.Map.of("message", "Your account has been flagged.", "action", "LOGOUT")
            );
        });

        // Flag Device (Ban Device)
        if (deviceId != null && !deviceId.isBlank() && !bannedDeviceRepository.existsByDeviceId(deviceId)) {
            BannedDevice bannedDevice = BannedDevice.builder()
                .deviceId(deviceId)
                .reason(reason)
                .note("Automatically banned due to security monitoring")
                .build();
            bannedDeviceRepository.save(bannedDevice);
            bannedDeviceCacheService.refreshCache();
        }
    }
}
