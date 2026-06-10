package com.ispilo.repository;

import com.ispilo.model.entity.VideoComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoCommentRepository extends JpaRepository<VideoComment, String> {
    Page<VideoComment> findByVideoIdAndParentCommentIsNull(String videoId, Pageable pageable);
    Page<VideoComment> findByParentCommentId(String parentCommentId, Pageable pageable);
    long countByVideoId(String videoId);
}
