package com.ispilo.service;

import com.ispilo.exception.BadRequestException;
import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.SendMessageRequest;
import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.model.entity.Conversation;
import com.ispilo.model.entity.Message;
import com.ispilo.model.entity.User;
import com.ispilo.model.enums.MessageType;
import com.ispilo.repository.ConversationRepository;
import com.ispilo.repository.MessageRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageResponse sendMessage(String userId, SendMessageRequest request) {
        log.debug("Sending message from user {} to conversation {}", userId, request.getConversationId());

        // 1. Idempotency Check: Avoid sending the same message multiple times
        Optional<Message> existingMessage = messageRepository.findByClientMsgId(request.getClientMsgId());
        if (existingMessage.isPresent()) {
            log.info("Duplicate message detected for clientMsgId: {}. Returning existing message.", request.getClientMsgId());
            return MessageResponse.fromEntity(existingMessage.get());
        }

        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Validate user is participant
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!conversation.getParticipants().contains(sender)) {
            throw new UnauthorizedException("User is not a participant in this conversation");
        }

        // Validate message content
        if (request.getType() == MessageType.TEXT &&
            (request.getContent() == null || request.getContent().trim().isEmpty())) {
            throw new BadRequestException("Text message content cannot be empty");
        }

        // Encrypt message content if it's text
        String encryptedContent = null;
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            String conversationKey = conversation.getEncryptionKey();
            if (conversationKey == null) {
                conversationKey = encryptionService.generateConversationKey();
                conversation.setEncryptionKey(conversationKey);
                conversationRepository.save(conversation);
            }
            encryptedContent = encryptionService.encrypt(request.getContent(), conversationKey);
        }

        // Create message
        Message message = Message.builder()
                .clientMsgId(request.getClientMsgId())
                .conversation(conversation)
                .sender(sender)
                .type(request.getType())
                .content(encryptedContent)
                .mediaUrl(request.getMediaUrl())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        // Update conversation last message
        String lastMsgPreview = request.getContent() != null ?
                request.getContent().substring(0, Math.min(100, request.getContent().length())) :
                "[" + request.getType().name().toLowerCase() + "]";
        conversation.setLastMessage(lastMsgPreview);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Create response
        MessageResponse response = MessageResponse.fromEntity(message);
        response.setContent(request.getContent()); // Return original content

        // Notify participants
        notifyParticipants(conversation, response, userId);

        return response;
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessages(String userId, String conversationId,
                                                         int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!conversation.getParticipants().contains(user)) {
            throw new UnauthorizedException("User is not a participant in this conversation");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messages = messageRepository.findByConversationId(conversationId, pageable);

        String conversationKey = conversation.getEncryptionKey();

        return messages.map(message -> {
            MessageResponse response = MessageResponse.fromEntity(message);
            if (message.getContent() != null && conversationKey != null) {
                try {
                    response.setContent(encryptionService.decrypt(message.getContent(), conversationKey));
                } catch (Exception e) {
                    log.error("Failed to decrypt message {}", message.getId(), e);
                    response.setContent("[Encrypted message]");
                }
            }
            return response;
        });
    }

    @Transactional
    public void markMessagesAsRead(String userId, String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!conversation.getParticipants().contains(user)) {
            throw new UnauthorizedException("User is not a participant in this conversation");
        }

        List<Message> unreadMessages = messageRepository
                .findUnreadMessagesByConversationAndNotSender(conversationId, userId);

        unreadMessages.forEach(message -> message.setIsRead(true));
        messageRepository.saveAll(unreadMessages);

        notifyReadStatus(conversation, userId);
    }

    private void notifyParticipants(Conversation conversation, MessageResponse message, String senderId) {
        conversation.getParticipants().forEach(participant -> {
            if (!participant.getId().equals(senderId)) {
                messagingTemplate.convertAndSendToUser(
                        participant.getId(),
                        "/queue/messages",
                        message
                );
            }
        });
    }

    private void notifyReadStatus(Conversation conversation, String userId) {
        conversation.getParticipants().forEach(participant -> {
            if (!participant.getId().equals(userId)) {
                messagingTemplate.convertAndSendToUser(
                        participant.getId(),
                        "/queue/read-status",
                        new ReadStatusNotification(conversation.getId(), userId)
                );
            }
        });
    }

    private record ReadStatusNotification(String conversationId, String userId) {}
}
