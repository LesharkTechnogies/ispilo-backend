package com.ispilo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String OTP_PREFIX = "otp:phone:";
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generateAndSaveOtp(String phone) {
        String code = generateCode();
        String key = OTP_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
        log.info("Generated OTP for phone: {}", phone);
        return code;
    }

    public boolean validateOtp(String phone, String code) {
        String key = OTP_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(key);
            log.info("OTP validated successfully for phone: {}", phone);
            return true;
        }
        log.warn("Invalid OTP attempt for phone: {}", phone);
        return false;
    }

    private String generateCode() {
        int number = SECURE_RANDOM.nextInt(999999);
        return String.format("%06d", number);
    }
}
