package com.ispilo.controller;

import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.service.MessageService;
import com.ispilo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/messages", "/api/messages", "/api/v2/messages"})
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Page<MessageResponse>> getConversationMessages(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(messageService.getConversationMessages(
                userPrincipal.getId(), conversationId, page, size));
    }

    @PostMapping("/conversation/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String conversationId) {
        messageService.markMessagesAsRead(userPrincipal.getId(), conversationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{messageId}/react")
    public ResponseEntity<MessageResponse> reactToMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String messageId,
            @RequestBody java.util.Map<String, String> payload) {
        String emoji = payload.get("emoji");
        return ResponseEntity.ok(messageService.reactToMessage(userPrincipal.getId(), messageId, emoji));
    }

    @PostMapping("/{messageId}/delete-for-me")
    public ResponseEntity<Void> deleteForMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String messageId) {
        messageService.deleteMessageForMe(userPrincipal.getId(), messageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{messageId}/delete-for-everyone")
    public ResponseEntity<Void> deleteForEveryone(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String messageId) {
        messageService.deleteMessageForEveryone(userPrincipal.getId(), messageId);
        return ResponseEntity.noContent().build();
    }
}
