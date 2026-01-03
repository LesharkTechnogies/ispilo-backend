package com.ispilo.model.dto.response;

import com.ispilo.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String name;
    private String avatar;
    private String phone;
    private Boolean phonePrivacyPublic;
    private String countryCode;
    private String county;
    private String town;
    private Boolean isVerified;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private String bio;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .name(user.getName())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .phonePrivacyPublic(user.getPhonePrivacyPublic())
                .countryCode(user.getCountryCode())
                .county(user.getCounty())
                .town(user.getTown())
                .isVerified(user.getIsVerified())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsPhoneVerified())
                .bio(user.getBio())
                .location(user.getLocation())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
