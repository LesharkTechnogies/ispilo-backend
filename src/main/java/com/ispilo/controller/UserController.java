package com.ispilo.controller;

import com.ispilo.model.dto.request.UpdateProfileRequest;
import com.ispilo.model.dto.request.UpdateSettingsRequest;
import com.ispilo.model.dto.response.PostResponse;
import com.ispilo.model.dto.response.UserResponse;
import com.ispilo.model.dto.response.UserProfileResponse;
import com.ispilo.service.PostService;
import com.ispilo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/users", "/api/users", "/api/v2/users"})
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @GetMapping("/discover")
    public ResponseEntity<org.springframework.data.domain.Page<UserResponse>> discoverUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(userService.discoverUsers(userDetails.getUsername(), pageable));
    }

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

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfileById(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserProfileById(userId, userDetails));
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<Page<PostResponse>> getProfilePosts(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        String viewerUsername = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(postService.getUserPosts(userId, viewerUsername, pageable));
    }

    @GetMapping("/me/posts")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getMyPosts(userDetails.getUsername(), pageable));
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Map<String, Object>> toggleFollow(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String userId) {
        return ResponseEntity.ok(userService.toggleFollow(userDetails.getUsername(), userId));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserResponse>> getFollowing(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getFollowing(userId));
    }

    @GetMapping("/{userId}/connections")
    public ResponseEntity<List<UserResponse>> getConnections(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getConnections(userId));
    }

    @DeleteMapping("/me/account")
    public ResponseEntity<Map<String, String>> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    @PostMapping("/me/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody com.ispilo.model.dto.request.UpdatePasswordRequest request) {
        userService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<Map<String, String>> updateFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        String token = request.get("fcmToken");
        userService.updateFcmToken(userDetails.getUsername(), token);
        return ResponseEntity.ok(Map.of("message", "FCM token updated successfully"));
    }
}


