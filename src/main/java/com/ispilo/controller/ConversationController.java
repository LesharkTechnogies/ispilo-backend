package com.ispilo.controller;

import com.ispilo.model.dto.request.CreateConversationRequest;
import com.ispilo.model.dto.response.ConversationResponse;
import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.security.UserPrincipal;
import com.ispilo.service.ConversationService;
import com.ispilo.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/conversations", "/api/conversations", "/api/v2/conversations"})
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    /**
     * Send a message to a conversation via REST
     */
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String conversationId,
            @RequestBody java.util.Map<String, Object> payload) {

        log.info("Sending message via REST to conversation {} by user {}",
                conversationId, userPrincipal.getId());

        com.ispilo.model.dto.request.SendMessageRequest request = new com.ispilo.model.dto.request.SendMessageRequest();
        request.setConversationId(conversationId);
        
        // Handle varying message types
        String typeStr = (String) payload.getOrDefault("type", "TEXT");
        try {
            request.setType(com.ispilo.model.enums.MessageType.valueOf(typeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            request.setType(com.ispilo.model.enums.MessageType.TEXT);
        }

        // Handle varying text/content fields
        if (payload.containsKey("text")) {
            request.setContent((String) payload.get("text"));
        } else if (payload.containsKey("content")) {
            request.setContent((String) payload.get("content"));
        } else if (payload.containsKey("payload")) {
            java.util.Map<String, Object> nested = (java.util.Map<String, Object>) payload.get("payload");
            if (nested != null && nested.containsKey("text")) {
                request.setContent((String) nested.get("text"));
            }
        }

        // Auto-generate client Msg ID if not present
        String clientMsgId = (String) payload.get("clientMsgId");
        if (clientMsgId == null || clientMsgId.isEmpty()) {
            clientMsgId = java.util.UUID.randomUUID().toString();
        }
        request.setClientMsgId(clientMsgId);

        MessageResponse response = messageService.sendMessage(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Create a new conversation
     */
    @PostMapping
    public ResponseEntity<ConversationResponse> createConversation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateConversationRequest request) {

        log.info("Creating conversation for user {}", userPrincipal.getId());
        ConversationResponse response = conversationService.createConversation(
                userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all conversations for current user (paginated)
     */
    @GetMapping
    public ResponseEntity<?> getUserConversations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String targetId = userId != null ? userId : sellerId;
        if (targetId != null) {
            log.info("Getting/creating direct conversation via param between {} and {}",
                    userPrincipal.getId(), targetId);
            ConversationResponse response = conversationService.getOrCreateDirectConversation(
                    userPrincipal.getId(), targetId);
            return ResponseEntity.ok(response);
        }

        log.info("Getting conversations for user {}", userPrincipal.getId());
        Page<ConversationResponse> conversations = conversationService.getUserConversations(
                userPrincipal.getId(), page, size);

        PageResponse<ConversationResponse> response = new PageResponse<>(
                conversations.getContent(),
                conversations.getNumber(),
                conversations.getSize(),
                conversations.getTotalElements(),
                conversations.getTotalPages(),
                conversations.isLast()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific conversation by ID
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String conversationId) {

        log.info("Getting conversation {} for user {}", conversationId, userPrincipal.getId());
        ConversationResponse response = conversationService.getConversation(
                userPrincipal.getId(), conversationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get messages in a conversation (paginated)
     */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getConversationMessages(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("Getting messages for conversation {} by user {}",
                conversationId, userPrincipal.getId());

        Page<MessageResponse> messages = messageService.getConversationMessages(
                userPrincipal.getId(), conversationId, page, size);

        PageResponse<MessageResponse> response = new PageResponse<>(
                messages.getContent(),
                messages.getNumber(),
                messages.getSize(),
                messages.getTotalElements(),
                messages.getTotalPages(),
                messages.isLast()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Mark messages as read
     */
    @PutMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String conversationId) {

        log.info("Marking messages as read in conversation {} for user {}",
                conversationId, userPrincipal.getId());

        messageService.markMessagesAsRead(userPrincipal.getId(), conversationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a conversation
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String conversationId) {

        log.info("Deleting conversation {} for user {}", conversationId, userPrincipal.getId());
        conversationService.deleteConversation(userPrincipal.getId(), conversationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a specific message
     */
    @DeleteMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String conversationId,
            @PathVariable String messageId) {

        log.info("Deleting message {} in conversation {} by user {}",
                messageId, conversationId, userPrincipal.getId());

        messageService.deleteMessage(userPrincipal.getId(), messageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get or create direct conversation with another user
     */
    @GetMapping("/direct/{otherUserId}")
    public ResponseEntity<ConversationResponse> getOrCreateDirectConversation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String otherUserId) {

        log.info("Getting/creating direct conversation between {} and {}",
                userPrincipal.getId(), otherUserId);

        ConversationResponse response = conversationService.getOrCreateDirectConversation(
                userPrincipal.getId(), otherUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get or create direct conversation with another user via query param 
     */
    @GetMapping("/direct")
    public ResponseEntity<ConversationResponse> getOrCreateDirectConversationParam(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String participantId,
            @RequestParam(required = false) String userId) {

        String targetId = participantId != null ? participantId : userId;
        if (targetId == null) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Getting/creating direct conversation via param between {} and {}",
                userPrincipal.getId(), targetId);

        ConversationResponse response = conversationService.getOrCreateDirectConversation(
                userPrincipal.getId(), targetId);
        return ResponseEntity.ok(response);
    }
}
