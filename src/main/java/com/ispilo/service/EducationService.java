package com.ispilo.service;

import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.model.entity.CourseEnrollment;
import com.ispilo.model.entity.EducationCourse;
import com.ispilo.model.entity.EducationVideo;
import com.ispilo.model.entity.User;
import com.ispilo.repository.CourseEnrollmentRepository;
import com.ispilo.repository.EducationCourseRepository;
import com.ispilo.repository.EducationVideoRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EducationService {

    private final EducationVideoRepository videoRepository;
    private final EducationCourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    // ==================== VIDEOS ====================

    /**
     * Get all education videos with pagination
     */
    public PageResponse<?> getAllVideos(Pageable pageable) {
        Page<EducationVideo> page = videoRepository.findAll(pageable);
        return PageResponse.of(page);
    }

    /**
     * Search videos by keyword
     */
    public PageResponse<?> searchVideos(String keyword, Pageable pageable) {
        Page<EducationVideo> page = videoRepository.searchByKeyword(keyword, pageable);
        return PageResponse.of(page);
    }

    /**
     * Get trending videos
     */
    public PageResponse<?> getTrendingVideos(Pageable pageable) {
        Page<EducationVideo> page = videoRepository.findTrendingVideos(pageable);
        return PageResponse.of(page);
    }

    /**
     * Get top-rated videos
     */
    public PageResponse<?> getTopRatedVideos(Pageable pageable) {
        Page<EducationVideo> page = videoRepository.findTopRatedVideos(pageable);
        return PageResponse.of(page);
    }

    /**
     * Get videos by category
     */
    public PageResponse<?> getVideosByCategory(String category, Pageable pageable) {
        Page<EducationVideo> page = videoRepository.findByCategory(category, pageable);
        return PageResponse.of(page);
    }

    /**
     * Get videos by channel
     */
    public PageResponse<?> getVideosByChannel(String channel, Pageable pageable) {
        Page<EducationVideo> page = videoRepository.findByChannel(channel, pageable);
        return PageResponse.of(page);
    }

    /**
     * Get all video categories
     */
    public List<String> getVideoCategories() {
        return videoRepository.findAllCategories();
    }

    /**
     * Get all channels
     */
    public List<String> getChannels() {
        return videoRepository.findAllChannels();
    }

    // ==================== COURSES ====================

    /**
     * Get all courses with pagination
     */
    public PageResponse<?> getAllCourses(Pageable pageable) {
        Page<EducationCourse> page = courseRepository.findAll(pageable);
        return PageResponse.of(page);
    }

    /**
     * Search courses by keyword
     */
    public PageResponse<?> searchCourses(String keyword, Pageable pageable) {
        Page<EducationCourse> page = courseRepository.searchByKeyword(keyword, pageable);
        return PageResponse.of(page);
    }

    /**
     * Get popular courses
     */
    public PageResponse<?> getPopularCourses(Pageable pageable) {
        Page<EducationCourse> page = courseRepository.findPopularCourses(pageable);
        return PageResponse.of(page);
    }

    /**
     * Get top-rated courses
     */
    public PageResponse<?> getTopRatedCourses(Pageable pageable) {
        Page<EducationCourse> page = courseRepository.findTopRatedCourses(pageable);
        return PageResponse.of(page);
    }

    /**
     * Get courses by category
     */
    public PageResponse<?> getCoursesByCategory(String category, Pageable pageable) {
        Page<EducationCourse> page = courseRepository.findByCategory(category, pageable);
        return PageResponse.of(page);
    }

    /**
     * Get all course categories
     */
    public List<String> getCourseCategories() {
        return courseRepository.findAllCategories();
    }

    /**
     * Get all instructors
     */
    public List<String> getInstructors() {
        return courseRepository.findAllInstructors();
    }

    // ==================== ENROLLMENTS ====================

    /**
     * Enroll user in a course
     */
    public CourseEnrollment enrollUserInCourse(String userId, String courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        EducationCourse course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new RuntimeException("User already enrolled in this course");
        }

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .user(user)
                .course(course)
                .progress(0.0)
                .completedLessons(0)
                .isCompleted(false)
                .build();

        enrollmentRepository.save(enrollment);
        course.setEnrollmentCount(course.getEnrollmentCount() + 1);
        courseRepository.save(course);

        log.info("User {} enrolled in course {}", userId, courseId);
        return enrollment;
    }

    /**
     * Get user's enrolled courses
     */
    public PageResponse<?> getUserEnrolledCourses(String userId, Pageable pageable) {
        Page<CourseEnrollment> page = enrollmentRepository.findByUserId(userId, pageable);
        return PageResponse.of(page);
    }

    /**
     * Get user's in-progress courses
     */
    public PageResponse<?> getUserInProgressCourses(String userId, Pageable pageable) {
        Page<CourseEnrollment> page = enrollmentRepository.findInProgressCoursesByUser(userId, pageable);
        return PageResponse.of(page);
    }

    /**
     * Get user's completed courses
     */
    public List<CourseEnrollment> getUserCompletedCourses(String userId) {
        return enrollmentRepository.findCompletedCoursesByUser(userId);
    }

    /**
     * Update course progress
     */
    public CourseEnrollment updateCourseProgress(String enrollmentId, Double progress, Integer completedLessons) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setProgress(progress);
        enrollment.setCompletedLessons(completedLessons);

        if (progress >= 100.0) {
            enrollment.setIsCompleted(true);
            log.info("Course completed by user {}", enrollment.getUser().getId());
        }

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Check if user is enrolled in a course
     */
    public boolean isUserEnrolledInCourse(String userId, String courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }
}
