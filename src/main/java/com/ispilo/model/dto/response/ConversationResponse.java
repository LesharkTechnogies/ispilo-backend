package com.ispilo.model.dto.response;

import com.ispilo.model.entity.Conversation;
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
    private ConversationType type;
    private List<UserResponse> participants;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConversationResponse fromEntity(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
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
