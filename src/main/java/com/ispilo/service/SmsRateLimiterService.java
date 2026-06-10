package com.ispilo.service;

import com.ispilo.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsRateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecurityMonitoringService securityMonitoringService;

    // Keys for tracking
    private static final String RATE_LIMIT_PREFIX = "sms:ratelimit:phone:";
    private static final String RESEND_ATTEMPTS_PREFIX = "sms:resends:phone:";
    private static final String RESEND_COOLDOWN_PREFIX = "sms:cooldown:phone:";

    // Limits
    private static final int MAX_REQUESTS_PER_30_MINS = 10;
    private static final int MAX_RESENDS_BEFORE_COOLDOWN = 3;
    private static final int COOLDOWN_MINUTES = 5;

    public void checkAndRecordRequest(String phone, boolean isResend) {
        String rateLimitKey = RATE_LIMIT_PREFIX + phone;
        String resendAttemptsKey = RESEND_ATTEMPTS_PREFIX + phone;
        String cooldownKey = RESEND_COOLDOWN_PREFIX + phone;

        // 1. Check strict 30-min rate limit
        String currentCountStr = redisTemplate.opsForValue().get(rateLimitKey);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
        
        if (currentCount >= MAX_REQUESTS_PER_30_MINS) {
            log.warn("Phone {} exceeded global 30 min SMS rate limit. Flagging account.", phone);
            // Flag account and device. Note: deviceId is not available here, so we flag the account and pass null for device.
            securityMonitoringService.flagAccountAndDevice(phone, null, "Exceeded maximum SMS request rate limits");
            throw new BadRequestException("Too many SMS requests. Please try again in 30 minutes.");
        }

        // 2. Check 5-min cooldown if triggered
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            Long ttlSeconds = redisTemplate.getExpire(cooldownKey);
            long minutesLeft = (ttlSeconds != null && ttlSeconds > 0) ? (ttlSeconds / 60) + 1 : COOLDOWN_MINUTES;
            log.warn("Phone {} is currently in cooldown mode for {} minutes", phone, minutesLeft);
            throw new BadRequestException("Too many resend attempts. Please wait " + minutesLeft + " minutes before requesting another code.");
        }

        // 3. Track resends & trigger cooldown
        if (isResend) {
            String resendsCountStr = redisTemplate.opsForValue().get(resendAttemptsKey);
            int resendsCount = resendsCountStr != null ? Integer.parseInt(resendsCountStr) : 0;

            if (resendsCount >= MAX_RESENDS_BEFORE_COOLDOWN) {
                // Trigger Cooldown
                redisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofMinutes(COOLDOWN_MINUTES));
                redisTemplate.delete(resendAttemptsKey); // Reset attempts after cooldown trigger
                log.warn("Phone {} triggered {} minute cooldown", phone, COOLDOWN_MINUTES);
                throw new BadRequestException("Too many resend attempts. Please wait " + COOLDOWN_MINUTES + " minutes before requesting another code.");
            } else {
                // Increment resend attempts
                if (resendsCount == 0) {
                    redisTemplate.opsForValue().set(resendAttemptsKey, "1", Duration.ofMinutes(15)); // Keep track of resends for 15 mins window
                } else {
                    redisTemplate.opsForValue().increment(resendAttemptsKey);
                }
            }
        }

        // 4. Increment global 30-min count
        if (currentCount == 0) {
            redisTemplate.opsForValue().set(rateLimitKey, "1", Duration.ofMinutes(30));
        } else {
            redisTemplate.opsForValue().increment(rateLimitKey);
        }
    }
}
