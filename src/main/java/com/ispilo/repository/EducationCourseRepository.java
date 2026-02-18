package com.ispilo.repository;

import com.ispilo.model.entity.EducationCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducationCourseRepository extends JpaRepository<EducationCourse, String> {

    /**
     * Find courses by instructor
     */
    Page<EducationCourse> findByInstructor(String instructor, Pageable pageable);

    /**
     * Find courses by category
     */
    Page<EducationCourse> findByCategory(String category, Pageable pageable);

    /**
     * Search courses by title or description
     */
    @Query("SELECT c FROM EducationCourse c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<EducationCourse> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find popular courses by enrollment count
     */
    @Query("SELECT c FROM EducationCourse c ORDER BY c.enrollmentCount DESC")
    Page<EducationCourse> findPopularCourses(Pageable pageable);

    /**
     * Find top-rated courses
     */
    @Query("SELECT c FROM EducationCourse c ORDER BY c.rating DESC, c.enrollmentCount DESC")
    Page<EducationCourse> findTopRatedCourses(Pageable pageable);

    /**
     * Get all categories
     */
    @Query("SELECT DISTINCT c.category FROM EducationCourse c WHERE c.category IS NOT NULL")
    List<String> findAllCategories();

    /**
     * Get all instructors
     */
    @Query("SELECT DISTINCT c.instructor FROM EducationCourse c")
    List<String> findAllInstructors();

    /**
     * Find courses by multiple categories
     */
    @Query("SELECT c FROM EducationCourse c WHERE c.category IN :categories")
    Page<EducationCourse> findByCategories(@Param("categories") List<String> categories, Pageable pageable);
}
