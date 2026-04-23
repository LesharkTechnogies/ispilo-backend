package com.ispilo.model.dto.request;

import lombok.Data;

@Data
public class CreateGroupRequest {
    private String name;
    private String description;
    private boolean isPrivateGroup;
}
