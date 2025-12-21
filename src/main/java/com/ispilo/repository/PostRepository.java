package com.ispilo.repository;

import com.ispilo.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    Page<Post> findByUserId(String userId, Pageable pageable);

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE p.id NOT IN (:viewedPostIds) " +
            "ORDER BY p.created_at DESC",
            countQuery = "SELECT count(*) FROM posts p WHERE p.id NOT IN (:viewedPostIds)",
            nativeQuery = true)
    Page<Post> findPersonalizedFeed(@Param("viewedPostIds") Collection<String> viewedPostIds, Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE MATCH(p.description) AGAINST(:query IN BOOLEAN MODE) " +
            "ORDER BY MATCH(p.description) AGAINST(:query IN BOOLEAN MODE) DESC",
            countQuery = "SELECT count(*) FROM posts p WHERE MATCH(p.description) AGAINST(:query IN BOOLEAN MODE)",
            nativeQuery = true)
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT DISTINCT description FROM posts WHERE description LIKE :query LIMIT :limit", nativeQuery = true)
    List<String> findTypeaheadSuggestions(@Param("query") String query, @Param("limit") int limit);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") String postId);
}
