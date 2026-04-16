package com.ispilo.model.dto.response;

import com.ispilo.model.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private String id;
    private String postId;
    private UserResponse user;
    private String content;
    private String parentCommentId;
    private java.util.List<CommentResponse> replies;
    private LocalDateTime createdAt;

    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .user(UserResponse.fromEntity(comment.getUser()))
                .content(comment.getContent())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .replies(comment.getReplies() != null ? comment.getReplies().stream().map(CommentResponse::fromEntity).collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
