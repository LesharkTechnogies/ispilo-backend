package com.ispilo.model.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class GroupPostResponse {
    private String id;
    private String text;
    private Set<String> mediaUrls;
    private boolean anonymous;
    private Instant createdAt;

    // Visible to admins or non-anonymous posts
    private String authorId;
    private String authorName;
    private String authorAvatar;

    private long likeCount;
}
