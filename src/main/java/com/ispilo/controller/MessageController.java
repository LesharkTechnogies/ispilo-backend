package com.ispilo.controller;

import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Page<MessageResponse>> getConversationMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(messageService.getConversationMessages(
                userDetails.getUsername(), conversationId, page, size));
    }

    @PostMapping("/conversation/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId) {
        messageService.markMessagesAsRead(userDetails.getUsername(), conversationId);
        return ResponseEntity.ok().build();
    }
}
