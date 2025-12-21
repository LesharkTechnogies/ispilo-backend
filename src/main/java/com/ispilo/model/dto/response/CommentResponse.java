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
    private LocalDateTime createdAt;

    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .user(UserResponse.fromEntity(comment.getUser()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
