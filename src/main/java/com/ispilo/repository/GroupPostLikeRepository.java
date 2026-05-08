package com.ispilo.repository;

import com.ispilo.model.entity.GroupPostLike;
import com.ispilo.model.entity.GroupPost;
import com.ispilo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupPostLikeRepository extends JpaRepository<GroupPostLike, String> {
    long countByPost(GroupPost post);
    Optional<GroupPostLike> findByPostAndUser(GroupPost post, User user);
}
