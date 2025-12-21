package com.ispilo.model.dto.response;

import com.ispilo.model.entity.Message;
import com.ispilo.model.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private String id;
    private String clientMsgId;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private MessageType type;
    private String content;
    private String mediaUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static MessageResponse fromEntity(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .clientMsgId(message.getClientMsgId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .senderAvatar(message.getSender().getAvatar())
                .type(message.getType())
                .content(message.getContent())
                .mediaUrl(message.getMediaUrl())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
