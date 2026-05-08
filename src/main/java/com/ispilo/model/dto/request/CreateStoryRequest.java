package com.ispilo.model.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateStoryRequest {
    @NotBlank
    private String mediaUrl;
    
    @NotBlank
    private String publicId;
    
    @NotBlank
    private String mediaType; // "IMAGE" or "VIDEO"
}
