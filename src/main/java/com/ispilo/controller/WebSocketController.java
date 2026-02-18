package com.ispilo.controller;

import com.ispilo.model.dto.request.SendMessageRequest;
import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.security.SecurityEncryptionService;
import com.ispilo.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final MessageService messageService;
    private final SecurityEncryptionService encryptionService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send encrypted message through WebSocket
     * Path: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
            if (auth == null) {
                log.error("Unauthorized message attempt");
                sendError(headerAccessor, "Unauthorized");
                return;
            }

            // Extract user ID from principal (assuming principal is UserDetails or similar)
            // Adjust this based on your actual UserPrincipal implementation
            String userId = auth.getName(); 

            // Encrypt message content before saving
            String encryptedContent = encryptionService.encryptWithAES(
                    request.getContent(),
                    request.getEncryptionKey()
            );

            // Create encrypted request
            // Since SendMessageRequest is a class with @Data (Lombok), use setters or builder
            SendMessageRequest encryptedRequest = SendMessageRequest.builder()
                    .conversationId(request.getConversationId())
                    .content(encryptedContent)
                    .mediaUrl(request.getMediaUrl())
                    .type(request.getType())
                    .clientMsgId(request.getClientMsgId())
                    .encryptionKey(request.getEncryptionKey())
                    .build();

            // Send message through service
            MessageResponse response = messageService.sendMessage(userId, encryptedRequest);

            // Broadcast to conversation participants
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + request.getConversationId(),
                    response
            );

            log.info("Message sent successfully: {}", response.getId());

        } catch (Exception e) {
            log.error("Error sending message", e);
            sendError(headerAccessor, "Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Handle typing notifications in real-time
     * Path: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(
            @Payload TypingNotification notification,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
            if (auth == null) return;

            String userId = auth.getName();

            // Broadcast typing indicator to other participants
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + notification.conversationId() + "/typing",
                    new TypingIndicator(
                            userId,
                            userId, // Or fetch username if needed
                            notification.isTyping()
                    )
            );

            log.debug("Typing notification sent: {} in {}", userId, notification.conversationId());

        } catch (Exception e) {
            log.error("Error handling typing notification", e);
        }
    }

    /**
     * Mark messages as read with real-time update
     * Path: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(
            @Payload ReadReceiptRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
            if (auth == null) return;

            String userId = auth.getName();

            // Mark messages as read in database
            messageService.markMessagesAsRead(userId, request.conversationId());

            // Broadcast read receipt to sender
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + request.conversationId() + "/read",
                    new ReadReceipt(
                            userId,
                            request.conversationId(),
                            System.currentTimeMillis()
                    )
            );

            log.debug("Messages marked as read: {}", request.conversationId());

        } catch (Exception e) {
            log.error("Error marking messages as read", e);
        }
    }

    /**
     * Send error message to client
     */
    private void sendError(SimpMessageHeaderAccessor headerAccessor, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
                headerAccessor.getSessionId(),
                "/queue/errors",
                new ErrorMessage(errorMessage, System.currentTimeMillis())
        );
    }

    // ==================== DTOs ====================

    public record TypingNotification(
            String conversationId,
            boolean isTyping
    ) {}

    public record TypingIndicator(
            String userId,
            String username,
            boolean isTyping
    ) {}

    public record ReadReceiptRequest(
            String conversationId
    ) {}

    public record ReadReceipt(
            String userId,
            String conversationId,
            long timestamp
    ) {}

    public record ErrorMessage(
            String message,
            long timestamp
    ) {}
}
