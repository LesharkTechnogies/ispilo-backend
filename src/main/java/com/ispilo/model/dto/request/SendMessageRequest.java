package com.ispilo.model.dto.request;

import com.ispilo.model.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    @NotNull(message = "Message type is required")
    private MessageType type;

    private String content;
    private String mediaUrl;

    /**
     * Client-generated unique ID to prevent duplicate processing
     * and allow client-side tracking of message status.
     */
    @NotBlank(message = "Client message ID is required")
    private String clientMsgId;

    private String encryptionKey;
}
