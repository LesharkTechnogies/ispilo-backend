package com.ispilo.model.dto.response;

import com.ispilo.model.enums.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private String id;
    private UserProfileResponse creator;
    private String caption;
    private String videoUrl;
    private String thumbnailUrl;
    private String previewImageUrl;
    private Integer durationSeconds;
    private VideoStatus status;
    private Integer viewCount;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer sharesCount;
    private Boolean isLiked; // Derived for the current user
    private LocalDateTime createdAt;
}
