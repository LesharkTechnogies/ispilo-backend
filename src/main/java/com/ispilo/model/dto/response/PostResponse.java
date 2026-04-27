
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
    private String content; // text content
    private String description; // same as content
    private String imageUrl; // single image preview
    private String shareUrl; // shareable link
    private List<String> mediaUrls;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean likedByCurrentUser;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse fromEntity(Post post) {
        return fromEntity(post, false);
    }

    public static PostResponse fromEntity(Post post, boolean likedByCurrentUser) {
        return PostResponse.builder()
                .id(post.getId())
                .user(UserResponse.fromEntity(post.getUser()))
                .content(post.getContent())
                .description(post.getDescription())
                .imageUrl(post.getImageUrl())
                .shareUrl("https://ispilo.com/post/" + post.getId())
                .mediaUrls(post.getMediaUrls())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .likedByCurrentUser(likedByCurrentUser)
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
