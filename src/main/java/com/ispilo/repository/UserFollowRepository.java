package com.ispilo.repository;

import com.ispilo.model.entity.User;
import com.ispilo.model.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, String> {
    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);
    List<UserFollow> findAllByFollower(User follower);
    Integer countByFollower(User follower);
    Integer countByFollowing(User following);
    boolean existsByFollowerAndFollowing(User follower, User following);
    List<UserFollow> findByFollowing(User following);
}
