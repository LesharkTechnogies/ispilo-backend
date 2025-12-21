package com.ispilo.repository;

import com.ispilo.model.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {
    Optional<PostView> findByUserIdAndPostId(String userId, String postId);

    @Query("SELECT pv.postId FROM PostView pv WHERE pv.userId = :userId AND pv.lastViewedAt >= :cutoff")
    Set<String> findRecentViewedPostIds(@Param("userId") String userId, @Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query(value = "INSERT INTO post_views (user_id, post_id, view_percentage, view_duration_ms, is_viewed_completely, last_viewed_at, created_at) " +
            "VALUES (:userId, :postId, :viewPercentage, :viewDurationMs, :isViewedCompletely, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
            "ON DUPLICATE KEY UPDATE " +
            "view_percentage = GREATEST(view_percentage, :viewPercentage), " +
            "view_duration_ms = view_duration_ms + :viewDurationMs, " +
            "is_viewed_completely = (view_percentage >= :threshold), " +
            "last_viewed_at = CURRENT_TIMESTAMP", nativeQuery = true)
    void upsertView(@Param("userId") String userId, 
                    @Param("postId") String postId, 
                    @Param("viewPercentage") Double viewPercentage, 
                    @Param("viewDurationMs") Integer viewDurationMs,
                    @Param("threshold") Double threshold);
}
