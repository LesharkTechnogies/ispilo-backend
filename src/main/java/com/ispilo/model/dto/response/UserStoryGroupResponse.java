package com.ispilo.model.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
@Builder
public class UserStoryGroupResponse {
    private String userId;
    private String userName;
    private String userAvatar;
    private List<StoryResponse> stories;
    private LocalDateTime latestStoryAt;
}
