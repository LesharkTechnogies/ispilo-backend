package com.ispilo.repository;

import com.ispilo.model.entity.GroupEntity;
import com.ispilo.model.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface GroupPostRepository extends JpaRepository<PostEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PostEntity p WHERE p.id = :postId AND p.group.id = :groupId")
    Optional<PostEntity> findByIdAndGroupIdWithLock(@Param("postId") String postId, @Param("groupId") String groupId);

    Page<PostEntity> findByGroupOrderByCreatedAtDesc(GroupEntity group, Pageable pageable);
}
