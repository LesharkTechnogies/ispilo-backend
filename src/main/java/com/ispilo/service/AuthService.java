package com.ispilo.service;

import com.ispilo.exception.ConflictException;
import com.ispilo.exception.NotFoundException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User already exists with email: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("User already exists with phone: " + request.getPhone());
        }

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

        // TODO: Send verification email and SMS here

        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // We need to find the user by phone first to get the email (username) for Spring Security
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new NotFoundException("User not found with phone: " + request.getPhone()));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtUtil.extractUsername(request.getRefreshToken());
        if (email != null && jwtUtil.isTokenValid(request.getRefreshToken(), email)) {
            String newToken = jwtUtil.generateToken(email);
            return RefreshTokenResponse.builder()
                    .token(newToken)
                    .refreshToken(request.getRefreshToken())
                    .build();
        }
        throw new RuntimeException("Invalid refresh token");
    }
}
