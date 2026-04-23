package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.model.dto.request.UpdatePasswordRequest;
import com.ispilo.model.dto.request.UpdateProfileRequest;
import com.ispilo.model.dto.request.UpdateSettingsRequest;
import com.ispilo.model.dto.response.UserResponse;
import com.ispilo.model.dto.response.UserProfileResponse;
import com.ispilo.model.entity.User;
import com.ispilo.model.entity.UserFollow;
import com.ispilo.repository.UserRepository;
import com.ispilo.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final MediaService mediaService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public org.springframework.data.domain.Page<UserResponse> discoverUsers(String username, org.springframework.data.domain.Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));
        
        // Find users other than the current user that they have NOT followed yet
        org.springframework.data.domain.Page<User> users = userRepository.findUsersNotFollowedBy(user.getId(), pageable);
        return users.map(u -> {
            UserResponse response = UserResponse.fromEntity(u);
            response.setIsFollowing(false); // since they are not followed by definition here
            return response;
        });
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

    public void updateFcmToken(String username, String token) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));
        user.setFcmToken(token);
        userRepository.save(user);
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
    public Map<String, Object> getUserProfile(String userId, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseGet(() -> userRepository.findByPhone(currentUsername)
                        .orElse(null));

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

        boolean isFollowing = false;
        if (currentUser != null && !currentUser.getId().equals(userId)) {
            isFollowing = userFollowRepository.existsByFollowerAndFollowing(currentUser, user);
        }
        profile.put("isFollowing", isFollowing);

        // Add stats
        Map<String, Object> stats = getUserStatsById(userId);
        profile.put("stats", stats);

        return profile;
    }

    public UserProfileResponse getUserProfileById(String userId, UserDetails userDetails) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (targetUser.getProfilePublic()) {
            return UserProfileResponse.fromUser(targetUser, true);
        }

        if (userDetails == null) {
            return UserProfileResponse.fromUser(targetUser, false);
        }

        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseGet(() -> userRepository.findByPhone(userDetails.getUsername())
                        .orElse(null));

        if (currentUser == null) {
            return UserProfileResponse.fromUser(targetUser, false);
        }

        boolean isFollowing = userFollowRepository.existsByFollowerAndFollowing(currentUser, targetUser);

        return UserProfileResponse.fromUser(targetUser, isFollowing);
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

    /**
     * Update user password
     */
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByPhone(email)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        String confirm = request.getConfirmPasswordToUse();
        if (confirm == null || !request.getNewPassword().equals(confirm)) {
            throw new com.ispilo.exception.BadRequestException("New password and confirm password do not match");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new com.ispilo.exception.BadRequestException("Incorrect old password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successfully for user: {}", email);
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> toggleFollow(String username, String targetUserId) {
        User follower = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("Follower not found")));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("Target user not found"));

        if (follower.getId().equals(targetUser.getId())) {
            throw new com.ispilo.exception.BadRequestException("You cannot follow yourself");
        }

        java.util.Optional<UserFollow> existingFollow = userFollowRepository.findByFollowerAndFollowing(follower, targetUser);
        boolean isNowFollowing;

        if (existingFollow.isPresent()) {
            userFollowRepository.delete(existingFollow.get());
            isNowFollowing = false;
        } else {
            UserFollow newFollow = UserFollow.builder()
                    .follower(follower)
                    .following(targetUser)
                    .build();
            userFollowRepository.save(newFollow);
            isNowFollowing = true;
            
            // TODO: Optional - notify target user
        }

        return Map.of(
            "following", isNowFollowing,
            "followerId", follower.getId(),
            "targetUserId", targetUser.getId()
        );
    }

    // Helper methods for stats (TODO: implement with actual database queries)
    private Integer getPostCount(String userId) {
        // TODO: Query from Post table where userId = ?
        return 0;
    }

    private Integer getFollowersCount(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return 0;
        return userFollowRepository.countByFollowing(user);
    }

    private Integer getFollowingCount(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return 0;
        return userFollowRepository.countByFollower(user);
    }

    private Integer getConnectionsCount(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return 0;
        
        List<User> followers = userFollowRepository.findByFollowing(user).stream()
                .map(UserFollow::getFollower).toList();
        List<User> following = userFollowRepository.findAllByFollower(user).stream()
                .map(UserFollow::getFollowing).toList();
        
        return (int) followers.stream().filter(following::contains).count();
    }

    /**
     * Get list of followers
     */
    public List<UserResponse> getFollowers(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userFollowRepository.findByFollowing(user).stream()
                .map(follow -> UserResponse.fromEntity(follow.getFollower()))
                .collect(Collectors.toList());
    }

    /**
     * Get list of following
     */
    public List<UserResponse> getFollowing(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userFollowRepository.findAllByFollower(user).stream()
                .map(follow -> UserResponse.fromEntity(follow.getFollowing()))
                .collect(Collectors.toList());
    }

    /**
     * Get list of connections (Mutual follows)
     */
    public List<UserResponse> getConnections(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        List<User> followers = userFollowRepository.findByFollowing(user).stream()
                .map(UserFollow::getFollower).toList();
        List<User> following = userFollowRepository.findAllByFollower(user).stream()
                .map(UserFollow::getFollowing).toList();
        
        return followers.stream()
                .filter(following::contains)
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }
}


