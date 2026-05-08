package com.ispilo.model.dto.response;

import com.ispilo.model.entity.Story;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StoryResponse {
    private String id;
    private String mediaUrl;
    private String publicId; // Could omit, but kept for frontend if needed
    private String mediaType;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public static StoryResponse fromEntity(Story story) {
        return StoryResponse.builder()
                .id(story.getId())
                .mediaUrl(story.getMediaUrl())
                .publicId(story.getPublicId())
                .mediaType(story.getMediaType())
                .createdAt(story.getCreatedAt())
                .expiresAt(story.getExpiresAt())
                .build();
    }
}
