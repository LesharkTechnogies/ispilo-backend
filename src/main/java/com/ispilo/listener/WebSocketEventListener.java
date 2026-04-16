package com.ispilo.listener;

import com.ispilo.model.entity.User;
import com.ispilo.repository.UserRepository;
import com.ispilo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserRepository userRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = getUserIdFromAccessor(headerAccessor);
        
        if (userId != null) {
            log.info("User {} connected to WebSocket. Marking as Online.", userId);
            updateUserStatus(userId, true);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = getUserIdFromAccessor(headerAccessor);

        if (userId != null) {
            log.info("User {} disconnected from WebSocket. Marking as Offline.", userId);
            updateUserStatus(userId, false);
        }
    }
    
    private void updateUserStatus(String userId, boolean isOnline) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsOnline(isOnline);
            user.setLastSeenAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    private String getUserIdFromAccessor(StompHeaderAccessor headerAccessor) {
        if (headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                return userPrincipal.getId();
            }
            return auth.getName(); // Fallback if using email directly
        }
        return null;
    }
}
