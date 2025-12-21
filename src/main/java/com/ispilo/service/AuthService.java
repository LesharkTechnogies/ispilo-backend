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

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .countryCode(request.getCountryCode())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

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
