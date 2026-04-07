package com.ispilo.controller;

import com.ispilo.model.dto.request.CreateConversationRequest;
import com.ispilo.model.dto.request.SendMessageRequest;
import com.ispilo.model.dto.response.ConversationResponse;
import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.security.UserPrincipal;
import com.ispilo.service.ConversationService;
import com.ispilo.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final MessageService messageService;
    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create a new conversation (private or group)
     * Path: /app/conversation.create
     */
    @MessageMapping("/conversation.create")
    public void createConversation(
            @Payload CreateConversationRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
            if (auth == null) {
                log.error("Unauthorized attempt to create conversation");
                sendError(headerAccessor, "Unauthorized");
                return;
            }

            String userId = resolveAuthenticatedUserId(auth);
            ConversationResponse response = conversationService.createConversation(userId, request);

            // Notify the creator that the conversation is created
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/conversation.created",
                    response
            );

            log.info("Conversation created successfully: {}", response.getId());

        } catch (Exception e) {
            log.error("Error creating conversation", e);
            sendError(headerAccessor, "Failed to create conversation: " + e.getMessage());
        }
    }

    /**
     * Join a conversation to start receiving messages
     * Path: /app/conversation.join
     */
    @MessageMapping("/conversation.join")
    public void joinConversation(
            @Payload String conversationId,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
            if (auth == null) {
                log.error("Unauthorized attempt to join conversation");
                sendError(headerAccessor, "Unauthorized");
                return;
            }

            String userId = resolveAuthenticatedUserId(auth);
            // Optional: You can add logic here to verify if the user is a participant
            // of the conversation before allowing them to "join" the topic.
            // For now, we just log it.

            log.info("User {} joined conversation {}", userId, conversationId);

        } catch (Exception e) {
            log.error("Error joining conversation", e);
            sendError(headerAccessor, "Failed to join conversation: " + e.getMessage());
        }
    }


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

            String userId = resolveAuthenticatedUserId(auth);

        // Send message through service (service layer handles validation + encryption)
        MessageResponse response = messageService.sendMessage(userId, request);

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

            String userId = resolveAuthenticatedUserId(auth);

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

            String userId = resolveAuthenticatedUserId(auth);

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
        String sessionId = headerAccessor.getSessionId();
        if (sessionId != null && !sessionId.isBlank()) {
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/errors",
                    new ErrorMessage(errorMessage, System.currentTimeMillis())
            );
        }
    }

    private String resolveAuthenticatedUserId(UsernamePasswordAuthenticationToken auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        return auth.getName();
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
