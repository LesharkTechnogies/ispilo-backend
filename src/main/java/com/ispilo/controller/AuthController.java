package com.ispilo.controller;

import com.ispilo.model.dto.request.LoginRequest;
import com.ispilo.model.dto.request.ForgotPasswordCodeRequest;
import com.ispilo.model.dto.request.RefreshTokenRequest;
import com.ispilo.model.dto.request.RegisterRequest;
import com.ispilo.model.dto.request.ResetPasswordWithCodeRequest;
import com.ispilo.model.dto.response.AuthResponse;
import com.ispilo.model.dto.response.RefreshTokenResponse;
import com.ispilo.service.AuthService;
import com.ispilo.service.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth", "/api/v2/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse auth = authService.register(request);
        Map<String, Object> body = Map.of(
                "success", true,
                "message", "Registration successful",
                "data", auth
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/forgot-password/request-code")
    public ResponseEntity<Map<String, Object>> requestForgotPasswordCode(@Valid @RequestBody ForgotPasswordCodeRequest request) {
        forgotPasswordService.requestCode(request.getEmail(), false);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification code sent"
        ));
    }

    @PostMapping("/forgot-password/resend-code")
    public ResponseEntity<Map<String, Object>> resendForgotPasswordCode(@Valid @RequestBody ForgotPasswordCodeRequest request) {
        forgotPasswordService.requestCode(request.getEmail(), true);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification code sent"
        ));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Map<String, Object>> resetPasswordWithCode(@Valid @RequestBody ResetPasswordWithCodeRequest request) {
        forgotPasswordService.resetPassword(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password updated successfully"
        ));
    }
}
