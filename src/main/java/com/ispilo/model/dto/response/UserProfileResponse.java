package com.ispilo.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String name;
    private String avatar;
    private String town;
    private String bio;
    private String location;
    private LocalDateTime createdAt;
    private boolean isPublic;
    private String message;
    
    private Integer postCount;
    private Integer followersCount;
    private Integer followingCount;
    private Boolean isFollowing;

    public static UserProfileResponse fromUser(com.ispilo.model.entity.User user, boolean isPublic) {
        if (isPublic) {
            return UserProfileResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .name(user.getName())
                    .avatar(user.getAvatar())
                    .town(user.getTown())
                    .bio(user.getBio())
                    .location(user.getLocation())
                    .createdAt(user.getCreatedAt())
                    .isPublic(true)
                    .build();
        } else {
            return UserProfileResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .name(user.getName())
                    .avatar(user.getAvatar())
                    .town(user.getTown())
                    .isPublic(false)
                    .message("This user has a private profile.")
                    .build();
        }
    }
}
