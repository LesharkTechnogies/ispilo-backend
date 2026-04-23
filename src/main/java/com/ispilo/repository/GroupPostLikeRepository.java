package com.ispilo.repository;

import com.ispilo.model.entity.GroupPostLike;
import com.ispilo.model.entity.PostEntity;
import com.ispilo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupPostLikeRepository extends JpaRepository<GroupPostLike, String> {
    long countByPost(PostEntity post);
    Optional<GroupPostLike> findByPostAndUser(PostEntity post, User user);
}
