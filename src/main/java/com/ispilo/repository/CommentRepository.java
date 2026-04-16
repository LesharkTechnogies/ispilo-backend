package com.ispilo.repository;

import com.ispilo.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    Page<Comment> findByPostIdOrderByCreatedAtDesc(String postId, Pageable pageable);

    Page<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(String postId, Pageable pageable);
}

