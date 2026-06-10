package com.ispilo.repository;

import com.ispilo.model.entity.VideoCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoCommentLikeRepository extends JpaRepository<VideoCommentLike, String> {
    Optional<VideoCommentLike> findByUserIdAndCommentId(String userId, String commentId);
    boolean existsByUserIdAndCommentId(String userId, String commentId);
    void deleteByCommentId(String commentId);
}
