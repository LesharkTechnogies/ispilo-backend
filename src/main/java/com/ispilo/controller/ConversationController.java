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
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

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
    public ResponseEntity<PageResponse<ConversationResponse>> getUserConversations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

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
}
