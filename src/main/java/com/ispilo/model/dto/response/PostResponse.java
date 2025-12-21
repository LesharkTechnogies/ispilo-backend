package com.ispilo.model.dto.response;

import com.ispilo.model.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private String id;
    private UserResponse user;
    private String description;
    private List<String> mediaUrls;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .user(UserResponse.fromEntity(post.getUser()))
                .description(post.getDescription())
                .mediaUrls(post.getMediaUrls())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
