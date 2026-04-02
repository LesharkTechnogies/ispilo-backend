package com.ispilo.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private String username;
    private String avatar;
    private String town;
    private String bio;
    private String location;
    private boolean isPublic;
    private String message;

    public static UserProfileResponse fromUser(com.ispilo.model.entity.User user, boolean isPublic) {
        if (isPublic) {
            return UserProfileResponse.builder()
                    .username(user.getUsername())
                    .avatar(user.getAvatar())
                    .town(user.getTown())
                    .bio(user.getBio())
                    .location(user.getLocation())
                    .isPublic(true)
                    .build();
        } else {
            return UserProfileResponse.builder()
                    .username(user.getUsername())
                    .avatar(null) // Or a default locked profile picture
                    .town(user.getTown())
                    .isPublic(false)
                    .message("This user has a private profile.")
                    .build();
        }
    }
}
