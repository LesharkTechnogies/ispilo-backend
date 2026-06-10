package com.ispilo.repository;

import com.ispilo.model.entity.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoLikeRepository extends JpaRepository<VideoLike, String> {
    Optional<VideoLike> findByUserIdAndVideoId(String userId, String videoId);
    boolean existsByUserIdAndVideoId(String userId, String videoId);
    long countByVideoId(String videoId);
}
