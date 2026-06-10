package com.ispilo.repository;

import com.ispilo.model.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, String> {
    Optional<CommentLike> findByUserIdAndCommentId(String userId, String commentId);
    boolean existsByUserIdAndCommentId(String userId, String commentId);
    void deleteByCommentId(String commentId);
}
