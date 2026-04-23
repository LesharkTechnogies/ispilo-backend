package com.ispilo.model.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class GroupResponse {
    private String id;
    private String name;
    private String description;
    private boolean isPrivate;
    private Instant createdAt;
    private String createdById;
    private long memberCount;
}
