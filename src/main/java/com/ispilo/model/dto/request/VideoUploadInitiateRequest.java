package com.ispilo.model.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class VideoUploadInitiateRequest {
    private String caption;
    private String contentType; // e.g., video/mp4
    private List<String> hashtags;
    private Long durationSeconds;
}
