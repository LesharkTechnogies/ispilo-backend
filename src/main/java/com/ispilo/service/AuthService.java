package com.ispilo.service;

import com.ispilo.exception.BadRequestException;
import com.ispilo.exception.ConflictException;
import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.LoginRequest;
import com.ispilo.model.dto.request.RefreshTokenRequest;
import com.ispilo.model.dto.request.RegisterRequest;
import com.ispilo.model.dto.request.VerifyPhoneRequest;
import com.ispilo.model.dto.response.AuthResponse;
import com.ispilo.model.dto.response.RefreshTokenResponse;
import com.ispilo.model.dto.response.RegistrationInitiatedResponse;
import com.ispilo.model.dto.response.UserResponse;
import com.ispilo.model.entity.User;
import com.ispilo.repository.UserRepository;
import com.ispilo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BannedDeviceCacheService bannedDeviceCacheService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final SmsService smsService;
    private final SmsRateLimiterService smsRateLimiterService;
    private final SecurityMonitoringService securityMonitoringService;

    @Transactional
    public RegistrationInitiatedResponse register(RegisterRequest request, String deviceId) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        if (deviceId == null || deviceId.isBlank()) {
            throw new BadRequestException("Device ID is required for registration");
        }
        if (bannedDeviceCacheService.isBanned(deviceId)) {
            throw new UnauthorizedException("Device is banned from registering");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            throw new ConflictException("User already exists with email: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("Registration failed: Phone {} already exists", request.getPhone());
            throw new ConflictException("User already exists with phone: " + request.getPhone());
        }

        try {
            String generatedUsername = request.getEmail().contains("@") ? 
                request.getEmail().substring(0, request.getEmail().indexOf("@")) : request.getEmail();
            
            User user = User.builder()
                    .username(generatedUsername)
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .name(request.getFirstName() + " " + request.getLastName())
                    .phone(request.getPhone())
                    .countryCode(request.getCountryCode())
                    .county(request.getCounty())
                    .town(request.getTown())
                    .isEmailVerified(false)
                    .isPhoneVerified(false) // Phone verification pending
                    .build();

            userRepository.save(user);
            log.info("User registered successfully: {}", user.getId());

            // Generate OTP and send async
            String otp = otpService.generateAndSaveOtp(request.getPhone());
            String message = "Your Ispilo verification code is: " + otp + " ispilo. It expires in 5 minutes.";
            smsService.sendSms(request.getPhone(), message);

            return RegistrationInitiatedResponse.builder()
                    .message("Verification code sent successfully")
                    .phone(request.getPhone())
                    .requiresVerification(true)
                    .build();

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation during registration for email {}: {}", request.getEmail(), e.getMessage());
            throw new ConflictException("A user with these details already exists.");
        } catch (Exception e) {
            log.error("Unexpected error during registration for email {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Registration failed due to an internal error. Please try again.");
        }
    }

    @Transactional
    public AuthResponse verifyPhone(VerifyPhoneRequest request) {
        log.info("Verifying phone number: {}", request.getPhone());
        
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new NotFoundException("User not found with phone: " + request.getPhone()));

        if (user.getIsPhoneVerified()) {
            throw new ConflictException("Phone number is already verified");
        }

        boolean isValid = otpService.validateOtp(request.getPhone(), request.getCode());
        if (!isValid) {
            throw new BadRequestException("Invalid or expired verification code");
        }

        user.setIsPhoneVerified(true);
        userRepository.save(user);
        
        log.info("Phone verified successfully for user: {}", user.getId());

        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public void resendPhoneCode(String phone) {
        log.info("Resending phone verification code for: {}", phone);
        
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new NotFoundException("User not found with phone: " + phone));

        if (user.getIsPhoneVerified()) {
            throw new ConflictException("Phone number is already verified");
        }

        // Apply rate limits (10 per 30 mins, 3 resends -> 5 mins cooldown)
        smsRateLimiterService.checkAndRecordRequest(phone, true);

        String otp = otpService.generateAndSaveOtp(phone);
        String message = "Your Ispilo verification code is: " + otp + " ispilo. It expires in 5 minutes.";
        smsService.sendSms(phone, message);
    }

    public AuthResponse login(LoginRequest request, String deviceId) {
        log.info("Attempting login for phone: {}", request.getPhone());

        if (deviceId == null || deviceId.isBlank()) {
            throw new BadRequestException("Device ID is required for login");
        }
        if (bannedDeviceCacheService.isBanned(deviceId)) {
            throw new UnauthorizedException("Device is banned from using the app");
        }
        
        try {
            User user = userRepository.findByPhone(request.getPhone())
                    .orElseThrow(() -> {
                        log.warn("Login failed: User not found with phone {}", request.getPhone());
                        securityMonitoringService.recordFailedLogin(request.getPhone(), deviceId);
                        return new BadCredentialsException("Invalid phone number or password");
                    });

            if (Boolean.TRUE.equals(user.getIsFlagged())) {
                throw new UnauthorizedException("Your account is flagged and currently blocked. Please contact support.");
            }

            if (!user.getIsPhoneVerified()) {
                throw new UnauthorizedException("Phone number not verified. Please verify your phone number to login.");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );

            securityMonitoringService.resetLoginAttempts(request.getPhone());

            String token = jwtUtil.generateToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
            
            log.info("User logged in successfully: {}", user.getId());

            return AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .user(UserResponse.fromEntity(user))
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for phone {}: {}", request.getPhone(), e.getMessage());
            securityMonitoringService.recordFailedLogin(request.getPhone(), deviceId);
            throw new BadCredentialsException("Invalid phone number or password");
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for phone {}: {}", request.getPhone(), e.getMessage());
            throw new RuntimeException("Login failed due to an internal error. Please try again.");
        }
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        try {
            String email = jwtUtil.extractUsername(request.getRefreshToken());
            if (email != null && jwtUtil.isTokenValid(request.getRefreshToken(), email)) {
                String newToken = jwtUtil.generateToken(email);
                return RefreshTokenResponse.builder()
                        .token(newToken)
                        .refreshToken(request.getRefreshToken())
                        .build();
            }
            log.warn("Invalid refresh token attempt");
            throw new UnauthorizedException("Invalid refresh token");
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new UnauthorizedException("Could not refresh token");
        }
    }
}
