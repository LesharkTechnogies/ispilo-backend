package com.ispilo.model.dto.response;

import com.ispilo.model.entity.GroupPostComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPostCommentResponse {
    private String id;
    private String postId;
    private UserResponse user;
    private String content;
    private String parentCommentId;
    private List<GroupPostCommentResponse> replies;
    private LocalDateTime createdAt;

    public static GroupPostCommentResponse fromEntity(GroupPostComment comment) {
        return GroupPostCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .user(UserResponse.fromEntity(comment.getUser()))
                .content(comment.getContent())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .replies(comment.getReplies() != null ? comment.getReplies().stream().map(GroupPostCommentResponse::fromEntity).collect(Collectors.toList()) : new java.util.ArrayList<>())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
