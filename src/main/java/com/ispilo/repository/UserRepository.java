package com.ispilo.repository;

import com.ispilo.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // Using JPQL with named parameters prevents SQL injection
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    // Native query using named parameters is also safe, but we should be careful with LIKE clauses
    @Query(value = "SELECT name FROM users WHERE LOWER(name) LIKE LOWER(CONCAT('%', :query, '%')) LIMIT :limit", nativeQuery = true)
    List<String> findTypeaheadSuggestions(@Param("query") String query, @Param("limit") int limit);

    @Query("SELECT u FROM User u WHERE u.id != :id ORDER BY u.createdAt DESC")
    org.springframework.data.domain.Page<User> findByIdNotOrderByCreatedAtDesc(@Param("id") String id, org.springframework.data.domain.Pageable pageable);

    // Additional endpoints for followers
    @Query("SELECT u FROM User u WHERE u.id != :userId AND u.id NOT IN (SELECT f.following.id FROM UserFollow f WHERE f.follower.id = :userId) ORDER BY u.createdAt DESC")
    Page<User> findUsersNotFollowedBy(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isPhoneVerified = false AND u.createdAt < :cutoffTime")
    List<User> findUnverifiedUsersOlderThan(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);

    @Query("SELECT u FROM User u WHERE u.isPhoneVerified = false AND u.createdAt BETWEEN :startTime AND :endTime AND u.fcmToken IS NOT NULL")
    List<User> findUnverifiedUsersForReminder(@Param("startTime") java.time.LocalDateTime startTime, @Param("endTime") java.time.LocalDateTime endTime);
}
