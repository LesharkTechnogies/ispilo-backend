package com.ispilo.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVideoRequest {
    private String caption;
    
    @NotBlank(message = "Video URL is required")
    private String videoUrl;
    
    private String thumbnailUrl;
    private String previewImageUrl;
    private Integer durationSeconds;
    
    private List<String> hashtags;
}
