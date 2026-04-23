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
public class CreatePostRequest {

    private String content; // text content of the post
    private String description; // alias for content
    private String imageUrl; // single image preview
    private List<String> mediaUrls;
    
    // Group related fields
    private String groupId;
    private Boolean isAnonymous;
    
    // helper to get the actual text regardless of what frontend sends
    public String getActualContent() {
        if (content != null && !content.isEmpty()) return content;
        return description != null ? description : "";
    }
}

