package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.model.dto.request.UpdateProfileRequest;
import com.ispilo.model.dto.request.UpdateSettingsRequest;
import com.ispilo.model.dto.response.UserResponse;
import com.ispilo.model.entity.User;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final MediaService mediaService;

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Only update fields that are provided (partial update)
        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getPhonePrivacyPublic() != null) {
            user.setPhonePrivacyPublic(request.getPhonePrivacyPublic());
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public UserResponse updateAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        var uploadResponse = mediaService.uploadFile(file, "avatars", user.getId());
        user.setAvatar(uploadResponse.getMediaUrl());

        return UserResponse.fromEntity(userRepository.save(user));
    }

    /**
     * Get user statistics (posts, followers, following, connections)
     */
    public Map<String, Object> getUserStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return getUserStatsById(user.getId());
    }

    /**
     * Get user statistics by user ID
     */
    public Map<String, Object> getUserStatsById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", user.getId());
        stats.put("postCount", getPostCount(userId)); // TODO: Query from Post table
        stats.put("followers", getFollowersCount(userId)); // TODO: Query from Follow table
        stats.put("following", getFollowingCount(userId)); // TODO: Query from Follow table
        stats.put("connections", getConnectionsCount(userId)); // TODO: Query from Connection table

        log.info("Retrieved stats for user: {}", userId);
        return stats;
    }

    /**
     * Get user preferences/settings
     */
    public Map<String, Object> getUserPreferences(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("userId", user.getId());
        // TODO: Retrieve from UserPreferences entity or user settings
        preferences.put("biometricAuth", false);
        preferences.put("twoFactorAuth", false);
        preferences.put("accountVisibility", true);
        preferences.put("phonePrivacyPublic", user.getPhonePrivacyPublic() != null ? user.getPhonePrivacyPublic() : false);
        preferences.put("socialNotifications", true);
        preferences.put("messageNotifications", true);
        preferences.put("educationNotifications", false);
        preferences.put("marketplaceNotifications", true);
        preferences.put("themeMode", "SYSTEM");
        preferences.put("highContrast", false);

        return preferences;
    }

    /**
     * Update user preferences/settings
     */
    public Map<String, Object> updateUserPreferences(String email, UpdateSettingsRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Update phone privacy if provided
        if (request.getPhonePrivacyPublic() != null) {
            user.setPhonePrivacyPublic(request.getPhonePrivacyPublic());
            userRepository.save(user);
        }

        // TODO: Save other preferences to UserPreferences entity
        Map<String, Object> updatedPreferences = new HashMap<>();
        updatedPreferences.put("userId", user.getId());
        updatedPreferences.put("message", "Preferences updated successfully");
        updatedPreferences.put("biometricAuth", request.getBiometricAuth() != null ? request.getBiometricAuth() : false);
        updatedPreferences.put("twoFactorAuth", request.getTwoFactorAuth() != null ? request.getTwoFactorAuth() : false);
        updatedPreferences.put("accountVisibility", request.getAccountVisibility() != null ? request.getAccountVisibility() : true);
        updatedPreferences.put("phonePrivacyPublic", request.getPhonePrivacyPublic() != null ? request.getPhonePrivacyPublic() : false);
        updatedPreferences.put("themeMode", request.getThemeMode() != null ? request.getThemeMode() : "SYSTEM");

        log.info("Updated preferences for user: {}", email);
        return updatedPreferences;
    }

    /**
     * Get complete user profile with all details
     */
    public Map<String, Object> getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("avatar", user.getAvatar());
        profile.put("bio", user.getBio());
        profile.put("location", user.getLocation());
        profile.put("phone", user.getPhone());
        profile.put("phonePrivacyPublic", user.getPhonePrivacyPublic());
        profile.put("isVerified", user.getIsVerified());
        profile.put("createdAt", user.getCreatedAt());

        // Add stats
        Map<String, Object> stats = getUserStatsById(userId);
        profile.put("stats", stats);

        return profile;
    }

    /**
     * Delete user account
     */
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // TODO: Implement soft delete or full delete with cascade
        userRepository.delete(user);
        log.info("Deleted account for user: {}", email);
    }

    // Helper methods for stats (TODO: implement with actual database queries)
    private Integer getPostCount(String userId) {
        // TODO: Query from Post table where userId = ?
        return 0;
    }

    private Integer getFollowersCount(String userId) {
        // TODO: Query from Follow/UserFollower table where followingId = ?
        return 0;
    }

    private Integer getFollowingCount(String userId) {
        // TODO: Query from Follow/UserFollower table where followerId = ?
        return 0;
    }

    private Integer getConnectionsCount(String userId) {
        // TODO: Query from Connection table where userId = ?
        return 0;
    }
}


