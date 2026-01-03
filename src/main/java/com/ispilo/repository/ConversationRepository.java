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

    Page<Conversation> findByParticipantsId(String userId, Pageable pageable);

    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 WHERE c.type = com.ispilo.model.enums.ConversationType.DIRECT AND p1.id = :userId1 AND p2.id = :userId2")
    Conversation findDirectConversationBetweenUsers(@Param("userId1") String userId1, @Param("userId2") String userId2);
}
