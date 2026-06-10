
package com.ispilo.model.dto.response;

import com.ispilo.model.entity.GroupPost;
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

    private PostResponse sharedFromPost;
    private GroupPostResponse sharedFromGroupPost;

    public static PostResponse fromEntity(Post post, String baseUrl) {
        return fromEntity(post, false, true, baseUrl);
    }

    public static PostResponse fromEntity(Post post, boolean likedByCurrentUser, String baseUrl) {
        return fromEntity(post, likedByCurrentUser, true, baseUrl);
    }

    public static PostResponse fromEntity(Post post, boolean likedByCurrentUser, boolean includeShared, String baseUrl) {
        PostResponse response = PostResponse.builder()
                .id(post.getId())
                .user(UserResponse.fromEntity(post.getUser()))
                .content(post.getContent())
                .description(post.getDescription())
                .imageUrl(post.getImageUrl())
                .shareUrl(baseUrl + "/post/" + post.getId())
                .mediaUrls(post.getMediaUrls())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .likedByCurrentUser(likedByCurrentUser)
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();

        if (includeShared && post.getSharedFromPost() != null) {
            response.setSharedFromPost(fromEntity(post.getSharedFromPost(), false, false, baseUrl));
        }

        if (includeShared && post.getSharedFromGroupPost() != null) {
            GroupPost gp = post.getSharedFromGroupPost();
            GroupPostResponse gpr = new GroupPostResponse();
            gpr.setId(gp.getId());
            gpr.setText(gp.getText());
            gpr.setMediaUrls(gp.getMediaUrls());
            gpr.setAnonymous(gp.isAnonymous());
            gpr.setCreatedAt(gp.getCreatedAt());
            if (gp.getAuthor() != null && !gp.isAnonymous()) {
                gpr.setAuthorId(gp.getAuthor().getId());
                gpr.setAuthorName(gp.getAuthor().getName());
                gpr.setAuthorAvatar(gp.getAuthor().getAvatar());
            } else if (gp.isAnonymous()) {
                gpr.setAuthorName("Anonymous");
            }
            response.setSharedFromGroupPost(gpr);
        }

        return response;
    }
}

