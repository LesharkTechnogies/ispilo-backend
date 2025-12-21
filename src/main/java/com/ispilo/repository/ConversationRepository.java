package com.ispilo.repository;

import com.ispilo.model.entity.Conversation;
import com.ispilo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    List<Conversation> findByParticipantsContainingOrderByLastMessageAtDesc(User user);
}
