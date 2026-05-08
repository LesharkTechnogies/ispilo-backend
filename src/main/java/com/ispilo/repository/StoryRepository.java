package com.ispilo.repository;

import com.ispilo.model.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, String> {

    @Query("SELECT s FROM Story s JOIN FETCH s.user WHERE s.deleted = false AND s.expiresAt > :now ORDER BY s.createdAt ASC")
    List<Story> findActiveStories(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Story s WHERE s.deleted = false AND s.expiresAt <= :now")
    List<Story> findExpiredStories(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Story s JOIN FETCH s.user WHERE s.user.id = :userId AND s.deleted = false AND s.expiresAt > :now ORDER BY s.createdAt ASC")
    List<Story> findActiveStoriesByUser(@Param("userId") String userId, @Param("now") LocalDateTime now);

    List<Story> findByUserId(String userId);
}
