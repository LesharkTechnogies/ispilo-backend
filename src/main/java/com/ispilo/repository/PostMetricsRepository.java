package com.ispilo.repository;

import com.ispilo.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostMetricsRepository extends JpaRepository<Post, String> {

    // Atomic increment - Concurrency Safe! No lost updates.
    @Modifying
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.id = :postId")
    void incrementLikes(@Param("postId") String postId);

    // Atomic decrement
    @Modifying
    @Query("UPDATE Post p SET p.likesCount = p.likesCount - 1 WHERE p.id = :postId AND p.likesCount > 0")
    void decrementLikes(@Param("postId") String postId);

    // Atomic increment for shares - Concurrency Safe! No lost updates during virality.
    @Modifying
    @Query("UPDATE Post p SET p.sharesCount = p.sharesCount + 1 WHERE p.id = :postId")
    void incrementShares(@Param("postId") String postId);

}