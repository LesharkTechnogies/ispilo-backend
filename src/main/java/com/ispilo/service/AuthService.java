package com.ispilo.service;

import com.ispilo.exception.ConflictException;
import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.LoginRequest;
import com.ispilo.model.dto.request.RefreshTokenRequest;
import com.ispilo.model.dto.request.RegisterRequest;
import com.ispilo.model.dto.response.AuthResponse;
import com.ispilo.model.dto.response.RefreshTokenResponse;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            throw new ConflictException("User already exists with email: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("Registration failed: Phone {} already exists", request.getPhone());
            throw new ConflictException("User already exists with phone: " + request.getPhone());
        }

        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .name(request.getFirstName() + " " + request.getLastName())
                    .phone(request.getPhone())
                    .countryCode(request.getCountryCode())
                    .county(request.getCounty())
                    .town(request.getTown())
                    .isEmailVerified(false) // Email verification pending
                    .isPhoneVerified(false) // Phone verification pending
                    .build();

            userRepository.save(user);
            log.info("User registered successfully: {}", user.getId());

            // TODO: Send verification email and SMS here (Asynchronous)

            String token = jwtUtil.generateToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            return AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .user(UserResponse.fromEntity(user))
                    .build();
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation during registration for email {}: {}", request.getEmail(), e.getMessage());
            throw new ConflictException("A user with these details already exists.");
        } catch (Exception e) {
            log.error("Unexpected error during registration for email {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Registration failed due to an internal error. Please try again.");
        }
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for phone: {}", request.getPhone());
        
        try {
            // We need to find the user by phone first to get the email (username) for Spring Security
            User user = userRepository.findByPhone(request.getPhone())
                    .orElseThrow(() -> {
                        log.warn("Login failed: User not found with phone {}", request.getPhone());
                        return new BadCredentialsException("Invalid phone number or password");
                    });

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );

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
            throw new BadCredentialsException("Invalid phone number or password");
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
