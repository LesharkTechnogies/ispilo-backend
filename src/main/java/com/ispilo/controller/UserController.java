package com.ispilo.controller;

import com.ispilo.model.dto.request.UpdateProfileRequest;
import com.ispilo.model.dto.request.UpdateSettingsRequest;
import com.ispilo.model.dto.response.UserResponse;
import com.ispilo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserByEmail(userDetails.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("avatar") MultipartFile file) {
        return ResponseEntity.ok(userService.updateAvatar(userDetails.getUsername(), file));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserStats(userDetails.getUsername()));
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserStatsByIdById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserStatsById(userId));
    }

    @GetMapping("/me/preferences")
    public ResponseEntity<Map<String, Object>> getUserPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserPreferences(userDetails.getUsername()));
    }

    @PutMapping("/me/preferences")
    public ResponseEntity<Map<String, Object>> updateUserPreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateSettingsRequest request) {
        return ResponseEntity.ok(userService.updateUserPreferences(userDetails.getUsername(), request));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @DeleteMapping("/me/account")
    public ResponseEntity<Map<String, String>> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }
}


