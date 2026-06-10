package com.ispilo.model.dto.request;

import lombok.Data;

@Data
public class CreateGroupRequest {
    private String name;
    private String description;
    private boolean isPrivateGroup;
    private String avatarUrl;
    private String coverImageUrl;
}
