package com.ispilo.job;

import com.ispilo.model.entity.Conversation;
import com.ispilo.model.entity.Message;
import com.ispilo.model.enums.ConversationType;
import com.ispilo.repository.ConversationRepository;
import com.ispilo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DuplicateChatCleanupTask implements CommandLineRunner {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting Duplicate Chat Cleanup Task...");
        
        List<Conversation> directChats = conversationRepository.findAll().stream()
                .filter(c -> c.getType() == ConversationType.DIRECT)
                .collect(Collectors.toList());

        Map<Set<String>, List<Conversation>> groupedChats = new HashMap<>();

        for (Conversation chat : directChats) {
            Set<String> participantIds = chat.getParticipants().stream()
                    .map(p -> p.getId())
                    .collect(Collectors.toSet());
            groupedChats.computeIfAbsent(participantIds, k -> new ArrayList<>()).add(chat);
        }

        int mergedCount = 0;

        for (Map.Entry<Set<String>, List<Conversation>> entry : groupedChats.entrySet()) {
            List<Conversation> chats = entry.getValue();
            if (chats.size() > 1) {
                // Sort by createdAt ascending to keep the oldest as primary
                chats.sort(Comparator.comparing(Conversation::getCreatedAt));
                Conversation primary = chats.get(0);

                for (int i = 1; i < chats.size(); i++) {
                    Conversation duplicate = chats.get(i);
                    
                    // Move messages
                    List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(duplicate.getId());
                    for (Message msg : messages) {
                        msg.setConversation(primary);
                        messageRepository.save(msg);
                    }
                    
                    // Clear participants to avoid constraint violations if any before deletion
                    duplicate.getParticipants().clear();
                    conversationRepository.save(duplicate);
                    
                    // Delete duplicate
                    conversationRepository.delete(duplicate);
                    mergedCount++;
                }

                // Update primary's lastMessage
                List<Message> allMessages = messageRepository.findByConversationIdOrderByCreatedAtAsc(primary.getId());
                if (!allMessages.isEmpty()) {
                    Message lastMsg = allMessages.get(allMessages.size() - 1);
                    primary.setLastMessageAt(lastMsg.getCreatedAt());
                    String preview = lastMsg.getContent() != null ? 
                        (lastMsg.getContent().length() > 100 ? lastMsg.getContent().substring(0, 100) : lastMsg.getContent()) 
                        : "[Media]";
                    primary.setLastMessage(preview);
                    conversationRepository.save(primary);
                }
            }
        }

        log.info("Finished Duplicate Chat Cleanup. Merged {} duplicate conversations.", mergedCount);
    }
}
