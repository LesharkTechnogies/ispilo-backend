package com.ispilo.repository;

import com.ispilo.model.entity.EducationVideo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducationVideoRepository extends JpaRepository<EducationVideo, String> {

    /**
     * Find videos by channel
     */
    Page<EducationVideo> findByChannel(String channel, Pageable pageable);

    /**
     * Find videos by category
     */
    Page<EducationVideo> findByCategory(String category, Pageable pageable);

    /**
     * Search videos by title or description
     */
    @Query("SELECT v FROM EducationVideo v WHERE LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<EducationVideo> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find trending videos by view count
     */
    @Query("SELECT v FROM EducationVideo v ORDER BY v.views DESC")
    Page<EducationVideo> findTrendingVideos(Pageable pageable);

    /**
     * Find top-rated videos
     */
    @Query("SELECT v FROM EducationVideo v ORDER BY v.rating DESC, v.views DESC")
    Page<EducationVideo> findTopRatedVideos(Pageable pageable);

    /**
     * Get all unique channels
     */
    @Query("SELECT DISTINCT v.channel FROM EducationVideo v")
    List<String> findAllChannels();

    /**
     * Get all categories
     */
    @Query("SELECT DISTINCT v.category FROM EducationVideo v WHERE v.category IS NOT NULL")
    List<String> findAllCategories();
}
