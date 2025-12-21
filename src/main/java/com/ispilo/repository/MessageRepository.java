package com.ispilo.repository;

import com.ispilo.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    Optional<Message> findByClientMsgId(String clientMsgId);

    Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    Page<Message> findByConversationId(String conversationId, Pageable pageable);

    long countByConversationIdAndIsReadFalse(String conversationId);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isRead = false AND m.sender.id != :userId")
    List<Message> findUnreadMessagesByConversationAndNotSender(
            @Param("conversationId") String conversationId,
            @Param("userId") String userId
    );
}
