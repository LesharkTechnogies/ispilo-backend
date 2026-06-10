package com.ispilo.repository;
import com.ispilo.model.entity.Post;
import com.ispilo.model.entity.PostLike;
import com.ispilo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, String> {
    long countByPost(Post post);
    Optional<PostLike> findByPostAndUser(Post post, User authUser);
    Optional<PostLike> findByUserAndPost(User authUser, Post post);
    boolean existsByUserAndPost(User authUser, Post post);
    boolean existsByUserIdAndPostId(String userId, String postId);
    Optional<PostLike> findByUserIdAndPostId(String userId, String postId);
}
