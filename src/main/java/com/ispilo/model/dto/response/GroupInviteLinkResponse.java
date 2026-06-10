package com.ispilo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupInviteLinkResponse {
    private String groupId;
    private String groupName;
    private String deepLink;
    private String webLink;
}
