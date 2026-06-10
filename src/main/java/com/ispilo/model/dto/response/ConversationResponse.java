package com.ispilo.model.dto.response;

import com.ispilo.model.entity.Conversation;
import com.ispilo.model.entity.User;
import com.ispilo.model.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {

    private String id;
    private String name;
    private String avatar;
    private ConversationType type;
    private List<UserResponse> participants;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConversationResponse fromEntity(Conversation conversation) {
        return fromEntity(conversation, null);
    }

    public static ConversationResponse fromEntity(Conversation conversation, String currentUserId) {
        String computedName = conversation.getName();
        String computedAvatar = null;

        if (conversation.getType() == ConversationType.DIRECT && currentUserId != null) {
            User otherUser = conversation.getParticipants().stream()
                    .filter(p -> !p.getId().equals(currentUserId))
                    .findFirst()
                    .orElse(conversation.getParticipants().isEmpty() ? null : conversation.getParticipants().iterator().next());
            
            if (otherUser != null) {
                computedName = otherUser.getName();
                computedAvatar = otherUser.getAvatar();
            }
        }

        return ConversationResponse.builder()
                .id(conversation.getId())
                .name(computedName)
                .avatar(computedAvatar)
                .type(conversation.getType())
                .participants(conversation.getParticipants().stream()
                        .map(UserResponse::fromEntity)
                        .collect(Collectors.toList()))
                .lastMessage(conversation.getLastMessage())
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
