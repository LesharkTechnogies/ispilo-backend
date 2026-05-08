package com.ispilo.model.dto.response;

import com.ispilo.model.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageExportResponse {

    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private MessageType type;
    private String content;
    private String mediaUrl;
    private Boolean deletedForEveryone;
    private List<String> deletedForUsers;
    private List<String> readByUsers;
    private LocalDateTime createdAt;
}