package com.ispilo.repository;

import com.ispilo.model.entity.MessageRead;
import com.ispilo.model.entity.Message;
import com.ispilo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageReadRepository extends JpaRepository<MessageRead, String> {
    boolean existsByMessageAndUser(Message message, User user);
    boolean existsByMessageIdAndUserId(String messageId, String userId);
    long countByMessageId(String messageId);
    List<MessageRead> findByMessageId(String messageId);
    List<MessageRead> findByMessageIdIn(List<String> messageIds);
}
