package com.ispilo.service;

import com.ispilo.exception.BadRequestException;
import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.SendMessageRequest;
import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.model.entity.Conversation;
import com.ispilo.model.entity.Message;
import com.ispilo.model.entity.MessageRead;
import com.ispilo.model.entity.User;
import com.ispilo.model.enums.MessageType;
import com.ispilo.repository.ConversationRepository;
import com.ispilo.repository.MessageRepository;
import com.ispilo.repository.MessageReadRepository;
import com.ispilo.repository.UserRepository;
import com.ispilo.security.SecurityEncryptionService;
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
    private final SecurityEncryptionService encryptionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageReadRepository messageReadRepository;
    private final AuditService auditService;

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

        Message replyTo = null;
        if (request.getReplyToMessageId() != null) {
            replyTo = messageRepository.findById(request.getReplyToMessageId()).orElse(null);
        }

        String encryptedContent = request.getContent();
        if (request.getContent() != null) {
            // Encrypt message content if it's text
            if (request.getType() == MessageType.TEXT &&
                (request.getContent() == null || request.getContent().trim().isEmpty())) {
                throw new BadRequestException("Text message content cannot be empty");
            }

            String conversationKey = conversation.getEncryptionKey();
            if (conversationKey == null) {
                // Generate a new AES key if one doesn't exist for the conversation
                conversationKey = encryptionService.aesKeyToString(encryptionService.generateAESKey());
                conversation.setEncryptionKey(conversationKey);
                conversationRepository.save(conversation);
            }
            // Use AES encryption
            encryptedContent = encryptionService.encryptWithAES(request.getContent(), conversationKey);
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
                .replyToMessage(replyTo)
                .status(com.ispilo.model.enums.MessageStatus.SENT)
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

        List<MessageResponse> responses = messages.getContent().stream()
                .filter(message -> message.getDeletedFor() == null || !message.getDeletedFor().contains(userId))
                .map(message -> {
                    MessageResponse response = MessageResponse.fromEntity(message);
                    response.setDeletedForEveryone(Boolean.TRUE.equals(message.getDeletedForEveryone()));
                    response.setDeletedForMe(false);
                    response.setReadByCount(messageReadRepository.countByMessageId(message.getId()));

                    if (Boolean.TRUE.equals(message.getDeletedForEveryone())) {
                        response.setContent("[deleted]");
                        response.setMediaUrl(null);
                        response.setReactions(new java.util.HashMap<>());
                        response.setIsRead(messageReadRepository.existsByMessageIdAndUserId(message.getId(), userId));
                        return response;
                    }

                    if (message.getContent() != null && conversationKey != null) {
                        try {
                            response.setContent(encryptionService.decryptWithAES(message.getContent(), conversationKey));
                        } catch (Exception e) {
                            log.error("Failed to decrypt message {}", message.getId(), e);
                            response.setContent("[Encrypted message]");
                        }
                    }
                    response.setIsRead(messageReadRepository.existsByMessageIdAndUserId(message.getId(), userId));
                    return response;
                })
                .toList();

        return new org.springframework.data.domain.PageImpl<>(responses, pageable, messages.getTotalElements());
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

    List<MessageRead> newReads = unreadMessages.stream()
        .filter(message -> !messageReadRepository.existsByMessageAndUser(message, user))
        .map(message -> MessageRead.builder()
            .message(message)
            .user(user)
            .build())
        .toList();

    if (!newReads.isEmpty()) {
        messageReadRepository.saveAll(newReads);
    }

        notifyReadStatus(conversation, userId);
    }

    @Transactional
    public void markMessageAsDelivered(String userId, String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (message.getStatus() == com.ispilo.model.enums.MessageStatus.SENT) {
            message.setStatus(com.ispilo.model.enums.MessageStatus.DELIVERED);
            messageRepository.save(message);

            // Notify original sender that their message was delivered
            String senderEmail = message.getSender().getEmail();
            if (senderEmail != null && !senderEmail.isBlank()) {
                messagingTemplate.convertAndSendToUser(
                        senderEmail,
                        "/queue/message-delivered",
                        new DeliveryReceipt(message.getId(), message.getConversation().getId(), System.currentTimeMillis())
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getUndeliveredMessages(String userId, String conversationId) {
        return messageRepository.findByConversationIdAndStatusNotAndSenderIdNot(
                conversationId, com.ispilo.model.enums.MessageStatus.READ, userId)
                .stream()
                .filter(m -> m.getStatus() == com.ispilo.model.enums.MessageStatus.SENT)
                .map(MessageResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteMessageForEveryone(String userId, String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own messages");
        }

        message.setDeletedForEveryone(true);
        messageRepository.save(message);

    auditService.logAction(userId, "MESSAGE_DELETE_FOR_EVERYONE", "Message", messageId,
        java.util.Map.of(
            "conversationId", message.getConversation().getId(),
            "senderId", message.getSender().getId()
        ));
    }

    @Transactional
    public void deleteMessageForMe(String userId, String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (message.getDeletedFor() == null) {
            message.setDeletedFor(new java.util.HashSet<>());
        }
        message.getDeletedFor().add(userId);
        messageRepository.save(message);

    auditService.logAction(userId, "MESSAGE_DELETE_FOR_ME", "Message", messageId,
        java.util.Map.of(
            "conversationId", message.getConversation().getId(),
            "senderId", message.getSender().getId()
        ));
    }

    @Transactional
    public MessageResponse reactToMessage(String userId, String messageId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!message.getConversation().getParticipants().contains(user)) {
            throw new UnauthorizedException("User is not a participant in this conversation");
        }

        if (emoji == null || emoji.isEmpty()) {
            message.getReactions().remove(userId);
        } else {
            message.getReactions().put(userId, emoji);
        }

        Message saved = messageRepository.save(message);

        MessageResponse response = MessageResponse.fromEntity(saved);

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + message.getConversation().getId() + "/react",
                response
        );

        return response;
    }

    private void notifyParticipants(Conversation conversation, MessageResponse message, String senderId) {
        conversation.getParticipants().forEach(participant -> {
            if (!participant.getId().equals(senderId)) {
                String participantEmail = participant.getEmail();
                if (participantEmail != null && !participantEmail.isBlank()) {
                    messagingTemplate.convertAndSendToUser(
                            participantEmail,
                            "/queue/messages",
                            message
                    );
                }
            }
        });
    }

    private void notifyReadStatus(Conversation conversation, String userId) {
        conversation.getParticipants().forEach(participant -> {
            if (!participant.getId().equals(userId)) {
                String participantEmail = participant.getEmail();
                if (participantEmail != null && !participantEmail.isBlank()) {
                    messagingTemplate.convertAndSendToUser(
                            participantEmail,
                            "/queue/read-status",
                            new ReadStatusNotification(conversation.getId(), userId)
                    );
                }
            }
        });
    }

    private record ReadStatusNotification(String conversationId, String userId) {}
    
    public record DeliveryReceipt(String messageId, String conversationId, long timestamp) {}
}
