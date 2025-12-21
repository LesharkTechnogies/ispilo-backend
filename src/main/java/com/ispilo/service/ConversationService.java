package com.ispilo.service;

import com.ispilo.exception.BadRequestException;
import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.CreateConversationRequest;
import com.ispilo.model.dto.response.ConversationResponse;
import com.ispilo.model.entity.Conversation;
import com.ispilo.model.entity.User;
import com.ispilo.model.enums.ConversationType;
import com.ispilo.repository.ConversationRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ConversationResponse createConversation(String userId, CreateConversationRequest request) {
        log.debug("Creating conversation of type {} by user {}", request.getType(), userId);

        // Validate user exists
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Validate participants
        if (request.getParticipantIds() == null || request.getParticipantIds().isEmpty()) {
            throw new BadRequestException("At least one participant is required");
        }

        Set<User> participants = new HashSet<>();
        participants.add(creator); // Add creator as participant

        // Add other participants
        for (String participantId : request.getParticipantIds()) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new NotFoundException("Participant not found: " + participantId));
            participants.add(participant);
        }

        // For direct conversations, ensure only 2 participants
        if (request.getType() == ConversationType.DIRECT && participants.size() != 2) {
            throw new BadRequestException("Direct conversation must have exactly 2 participants");
        }

        // Check if direct conversation already exists
        if (request.getType() == ConversationType.DIRECT) {
            List<User> participantList = participants.stream().toList();
            Conversation existing = conversationRepository
                    .findDirectConversationBetweenUsers(participantList.get(0).getId(),
                                                       participantList.get(1).getId());
            if (existing != null) {
                log.debug("Direct conversation already exists: {}", existing.getId());
                return ConversationResponse.fromEntity(existing);
            }
        }

        // Create conversation
        Conversation conversation = Conversation.builder()
                .type(request.getType())
                .participants(participants)
                .build();

        conversation = conversationRepository.save(conversation);

        log.info("Created conversation {} with {} participants",
                conversation.getId(), participants.size());

        return ConversationResponse.fromEntity(conversation);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> getUserConversations(String userId, int page, int size) {
        log.debug("Getting conversations for user {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("lastMessageAt").descending());

        Page<Conversation> conversations = conversationRepository
                .findByParticipantsId(userId, pageable);

        return conversations.map(ConversationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(String userId, String conversationId) {
        log.debug("Getting conversation {} for user {}", conversationId, userId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Validate user is participant
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!conversation.getParticipants().contains(user)) {
            throw new UnauthorizedException("User is not a participant in this conversation");
        }

        return ConversationResponse.fromEntity(conversation);
    }

    @Transactional
    public void deleteConversation(String userId, String conversationId) {
        log.debug("Deleting conversation {} for user {}", conversationId, userId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Validate user is participant
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!conversation.getParticipants().contains(user)) {
            throw new UnauthorizedException("User is not a participant in this conversation");
        }

        // Remove user from participants (soft delete)
        conversation.getParticipants().remove(user);

        // If no participants left, delete the conversation
        if (conversation.getParticipants().isEmpty()) {
            conversationRepository.delete(conversation);
            log.info("Deleted conversation {}", conversationId);
        } else {
            conversationRepository.save(conversation);
            log.info("Removed user {} from conversation {}", userId, conversationId);
        }
    }

    @Transactional
    public ConversationResponse getOrCreateDirectConversation(String userId, String otherUserId) {
        log.debug("Getting or creating direct conversation between {} and {}", userId, otherUserId);

        if (userId.equals(otherUserId)) {
            throw new BadRequestException("Cannot create conversation with yourself");
        }

        // Check if conversation exists
        Conversation existing = conversationRepository
                .findDirectConversationBetweenUsers(userId, otherUserId);

        if (existing != null) {
            log.debug("Direct conversation exists: {}", existing.getId());
            return ConversationResponse.fromEntity(existing);
        }

        // Create new conversation
        CreateConversationRequest request = new CreateConversationRequest();
        request.setType(ConversationType.DIRECT);
        request.setParticipantIds(List.of(otherUserId));

        return createConversation(userId, request);
    }
}
