package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.model.dto.request.UpdateProfileRequest;
import com.ispilo.model.dto.response.UserResponse;
import com.ispilo.model.entity.User;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
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

        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setLocation(request.getLocation());
        user.setPhone(request.getPhone());
        user.setPhonePrivacyPublic(request.getPhonePrivacyPublic());

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public UserResponse updateAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        var uploadResponse = mediaService.uploadFile(file, "avatars", user.getId());
        user.setAvatar(uploadResponse.getMediaUrl());

        return UserResponse.fromEntity(userRepository.save(user));
    }
}
