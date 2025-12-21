package com.ispilo.controller;

import com.ispilo.model.dto.request.SendMessageRequest;
import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.security.UserPrincipal;
import com.ispilo.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
        if (auth == null) {
            log.error("Unauthorized message attempt");
            return;
        }

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        messageService.sendMessage(userPrincipal.getId(), request);
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(
            @Payload TypingNotification notification,
            SimpMessageHeaderAccessor headerAccessor) {
        // Implementation for typing notification
    }

    @MessageMapping("/chat.read")
    public void markAsRead(
            @Payload ReadReceiptRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
        if (auth == null) return;

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        messageService.markMessagesAsRead(userPrincipal.getId(), request.conversationId());
    }

    public record TypingNotification(String conversationId, boolean isTyping) {}
    public record ReadReceiptRequest(String conversationId) {}
}
