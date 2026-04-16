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
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final BrevoEmailService brevoEmailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${forgot-password.code-ttl-minutes:15}")
    private long codeTtlMinutes;

    @Value("${forgot-password.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${forgot-password.max-attempts:5}")
    private int maxAttempts;

    @Transactional
    @SuppressWarnings("null")
    public void requestCode(String email, boolean resend) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        // Reject account existence if not found to prevent sending unknown email codes silently.
        // Actually, the user specifically requested to reject if the email is not registered.
        if (user == null) {
            log.info("Password reset requested for unknown email: {}", normalizedEmail);
            throw new BadRequestException("User with this email is not registered.");
        }

        if (resend) {
            passwordResetCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(normalizedEmail)
                    .ifPresent(existing -> {
                        long sinceLastSendSeconds = Duration.between(existing.getCreatedAt(), LocalDateTime.now()).getSeconds();
                        if (sinceLastSendSeconds < resendCooldownSeconds) {
                            long waitSeconds = resendCooldownSeconds - sinceLastSendSeconds;
                            throw new BadRequestException("Please wait " + waitSeconds + " seconds before requesting another code");
                        }
                    });
        }

        // Invalidate any previously active code
        passwordResetCodeRepository.markAllUnusedAsUsed(normalizedEmail);

        String rawCode = generateCode();

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .email(normalizedEmail)
                .codeHash(passwordEncoder.encode(rawCode))
                .expiresAt(LocalDateTime.now().plusMinutes(codeTtlMinutes))
                .used(false)
                .attempts(0)
                .build();

    passwordResetCodeRepository.save(resetCode);

        String recipientName = user.getFirstName() != null ? user.getFirstName() : user.getName();
        brevoEmailService.sendForgotPasswordCode(normalizedEmail, recipientName, rawCode, codeTtlMinutes);
        log.info("Password reset code sent to {}", normalizedEmail);
    }

    @Transactional
    public void resetPassword(ResetPasswordWithCodeRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        PasswordResetCode savedCode = passwordResetCodeRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid reset request"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        savedCode.setUsed(true);
        passwordResetCodeRepository.save(savedCode);
        passwordResetCodeRepository.markAllUnusedAsUsed(email);

        log.info("Password reset successful for {}", email);
    }

    private String generateCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
