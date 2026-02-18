package com.ispilo.repository;

import com.ispilo.model.entity.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, String> {

    /**
     * Find all enrollments for a user
     */
    Page<CourseEnrollment> findByUserId(String userId, Pageable pageable);

    /**
     * Find all enrollments for a course
     */
    Page<CourseEnrollment> findByCourseId(String courseId, Pageable pageable);

    /**
     * Check if user is enrolled in a course
     */
    boolean existsByUserIdAndCourseId(String userId, String courseId);

    /**
     * Find user's enrollment in a specific course
     */
    Optional<CourseEnrollment> findByUserIdAndCourseId(String userId, String courseId);

    /**
     * Get user's completed courses
     */
    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.user.id = :userId AND ce.isCompleted = true")
    List<CourseEnrollment> findCompletedCoursesByUser(@Param("userId") String userId);

    /**
     * Get user's in-progress courses
     */
    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.user.id = :userId AND ce.isCompleted = false ORDER BY ce.updatedAt DESC")
    Page<CourseEnrollment> findInProgressCoursesByUser(@Param("userId") String userId, Pageable pageable);

    /**
     * Get enrollment count for a course
     */
    long countByCourseId(String courseId);

    /**
     * Find users enrolled in a course
     */
    @Query("SELECT ce.user.id FROM CourseEnrollment ce WHERE ce.course.id = :courseId")
    List<String> findEnrolledUserIds(@Param("courseId") String courseId);
}
