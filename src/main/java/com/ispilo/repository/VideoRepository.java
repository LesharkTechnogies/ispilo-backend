package com.ispilo.repository;

import com.ispilo.model.entity.User;
import com.ispilo.model.entity.Video;
import com.ispilo.model.enums.VideoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Page<Video> findByStatus(VideoStatus status, Pageable pageable);
    Page<Video> findByCreatorIdAndStatus(String creatorId, VideoStatus status, Pageable pageable);
    Page<Video> findByCreatorId(String creatorId, Pageable pageable);
    Page<Video> findByCreatorInAndStatus(List<User> creators, VideoStatus status, Pageable pageable);
    Page<Video> findByStatusOrderByViewCountDesc(VideoStatus status, Pageable pageable);
    Page<Video> findByCaptionContainingIgnoreCaseAndStatus(String hashtag, VideoStatus status, Pageable pageable);
}
