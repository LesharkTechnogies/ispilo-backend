package com.ispilo.service;

import com.ispilo.exception.BadRequestException;
import com.ispilo.model.dto.request.ResetPasswordWithCodeRequest;
import com.ispilo.model.entity.PasswordResetCode;
import com.ispilo.model.entity.User;
import com.ispilo.repository.PasswordResetCodeRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;
    private final SmsRateLimiterService smsRateLimiterService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${forgot-password.code-ttl-minutes:5}")
    private long codeTtlMinutes;

    @Value("${forgot-password.max-attempts:5}")
    private int maxAttempts;

    @Transactional
    @SuppressWarnings("null")
    public void requestCode(String phone, boolean resend) {
        // Validate user existence
        User user = userRepository.findByPhone(phone).orElse(null);
        if (user == null) {
            log.info("Password reset requested for unknown phone: {}", phone);
            throw new BadRequestException("User with this phone number is not registered.");
        }

        // Apply rate limits (10 per 30 mins, 3 resends -> 5 mins cooldown)
        smsRateLimiterService.checkAndRecordRequest(phone, resend);

        // Invalidate any previously active code
        passwordResetCodeRepository.markAllUnusedAsUsed(phone);

        String rawCode = generateCode();

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .phone(phone)
                .codeHash(passwordEncoder.encode(rawCode))
                .expiresAt(LocalDateTime.now().plusMinutes(codeTtlMinutes))
                .used(false)
                .attempts(0)
                .build();

        passwordResetCodeRepository.save(resetCode);

        // Send SMS
        String message = "Your ISPilo verification code is: " + rawCode + ". It expires in " + codeTtlMinutes + " minutes.";
        smsService.sendSms(phone, message);
        log.info("Password reset code sent to {}", phone);
    }

    @Transactional
    public void resetPassword(ResetPasswordWithCodeRequest request) {
        String phone = request.getPhone();

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        PasswordResetCode savedCode = passwordResetCodeRepository
                .findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification code"));

        if (savedCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            savedCode.setUsed(true);
            passwordResetCodeRepository.save(savedCode);
            throw new BadRequestException("Verification code has expired");
        }

        if (savedCode.getAttempts() >= maxAttempts) {
            savedCode.setUsed(true);
            passwordResetCodeRepository.save(savedCode);
            throw new BadRequestException("Verification code is no longer valid");
        }

        if (!passwordEncoder.matches(request.getCode(), savedCode.getCodeHash())) {
            savedCode.setAttempts(savedCode.getAttempts() + 1);
            if (savedCode.getAttempts() >= maxAttempts) {
                savedCode.setUsed(true);
            }
            passwordResetCodeRepository.save(savedCode);
            throw new BadRequestException("Invalid verification code");
        }

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BadRequestException("Invalid reset request"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        
        // Also verify the phone number if they reset their password successfully.
        if (!user.getIsPhoneVerified()) {
            user.setIsPhoneVerified(true);
        }
        
        userRepository.save(user);

        savedCode.setUsed(true);
        passwordResetCodeRepository.save(savedCode);
        passwordResetCodeRepository.markAllUnusedAsUsed(phone);

        log.info("Password reset successful for {}", phone);
    }

    private String generateCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}
