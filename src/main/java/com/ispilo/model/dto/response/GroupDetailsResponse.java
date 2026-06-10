package com.ispilo.model.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDetailsResponse {
    private String id;
    private String name;
    private String description;
    private boolean isPrivate;
    private Instant createdAt;
    private GroupUserSummaryResponse createdBy;
    private long memberCount;
    private long adminCount;
    private boolean isMember;
    private boolean isAdmin;
    private List<GroupUserSummaryResponse> admins;
    private String inviteLink;
}
