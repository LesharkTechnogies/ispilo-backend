package com.ispilo.repository;

import com.ispilo.model.entity.Conversation;
import com.ispilo.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    List<Conversation> findByParticipantsContainingOrderByLastMessageAtDesc(User user);

    @Query("SELECT DISTINCT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId")
    Page<Conversation> findByParticipantsId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE c.type = com.ispilo.model.enums.ConversationType.DIRECT AND EXISTS (SELECT 1 FROM c.participants p WHERE p.id = :userId1) AND EXISTS (SELECT 1 FROM c.participants p WHERE p.id = :userId2) AND SIZE(c.participants) = 2")
    List<Conversation> findDirectConversationBetweenUsers(@Param("userId1") String userId1, @Param("userId2") String userId2);

    @Query("SELECT c FROM Conversation c WHERE c.type = com.ispilo.model.enums.ConversationType.DIRECT AND EXISTS (SELECT 1 FROM c.participants p WHERE p.id = :userId) AND SIZE(c.participants) = 1")
    List<Conversation> findSelfDirectConversation(@Param("userId") String userId);
}
