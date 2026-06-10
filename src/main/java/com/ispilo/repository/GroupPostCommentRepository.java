package com.ispilo.repository;

import com.ispilo.model.entity.GroupPostComment;
import com.ispilo.model.entity.GroupPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupPostCommentRepository extends JpaRepository<GroupPostComment, String> {
    Page<GroupPostComment> findByPostAndParentCommentIsNullOrderByCreatedAtDesc(GroupPost post, Pageable pageable);
}
