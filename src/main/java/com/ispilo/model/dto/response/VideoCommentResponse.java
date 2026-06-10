package com.ispilo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCommentResponse {
    private String id;
    private String videoId;
    private UserProfileResponse user;
    private String content;
    private Integer likesCount;
    private Boolean isLiked;
    private String parentCommentId;
    private LocalDateTime createdAt;
}
