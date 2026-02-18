package com.ispilo.controller;

import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.service.EducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/education")
@Tag(name = "Education", description = "Education Hub APIs for videos and courses")
@RequiredArgsConstructor
@Slf4j
public class EducationController {

    private final EducationService educationService;

    // ==================== VIDEOS ====================

    @GetMapping("/videos")
    @Operation(summary = "Get all education videos with pagination")
    public ResponseEntity<PageResponse<?>> getAllVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(educationService.getAllVideos(pageable));
    }

    @GetMapping("/videos/search")
    @Operation(summary = "Search videos by keyword")
    public ResponseEntity<PageResponse<?>> searchVideos(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(educationService.searchVideos(keyword, pageable));
    }

    @GetMapping("/videos/trending")
    @Operation(summary = "Get trending videos")
    public ResponseEntity<PageResponse<?>> getTrendingVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(educationService.getTrendingVideos(pageable));
    }

    @GetMapping("/videos/top-rated")
    @Operation(summary = "Get top-rated videos")
    public ResponseEntity<PageResponse<?>> getTopRatedVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(educationService.getTopRatedVideos(pageable));
    }

    @GetMapping("/videos/category/{category}")
    @Operation(summary = "Get videos by category")
    public ResponseEntity<PageResponse<?>> getVideosByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(educationService.getVideosByCategory(category, pageable));
    }

    @GetMapping("/videos/channel/{channel}")
    @Operation(summary = "Get videos by channel")
    public ResponseEntity<PageResponse<?>> getVideosByChannel(
            @PathVariable String channel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(educationService.getVideosByChannel(channel, pageable));
    }

    @GetMapping("/videos/categories")
    @Operation(summary = "Get all video categories")
    public ResponseEntity<?> getVideoCategories() {
        return ResponseEntity.ok(educationService.getVideoCategories());
    }

    @GetMapping("/videos/channels")
    @Operation(summary = "Get all channels")
    public ResponseEntity<?> getChannels() {
        return ResponseEntity.ok(educationService.getChannels());
    }

    // ==================== COURSES ====================

    @GetMapping("/courses")
    @Operation(summary = "Get all courses with pagination")
    public ResponseEntity<PageResponse<?>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(educationService.getAllCourses(pageable));
    }

    @GetMapping("/courses/search")
    @Operation(summary = "Search courses by keyword")
    public ResponseEntity<PageResponse<?>> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(educationService.searchCourses(keyword, pageable));
    }

    @GetMapping("/courses/popular")
    @Operation(summary = "Get popular courses")
    public ResponseEntity<PageResponse<?>> getPopularCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(educationService.getPopularCourses(pageable));
    }

    @GetMapping("/courses/top-rated")
    @Operation(summary = "Get top-rated courses")
    public ResponseEntity<PageResponse<?>> getTopRatedCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(educationService.getTopRatedCourses(pageable));
    }

    @GetMapping("/courses/category/{category}")
    @Operation(summary = "Get courses by category")
    public ResponseEntity<PageResponse<?>> getCoursesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(educationService.getCoursesByCategory(category, pageable));
    }

    @GetMapping("/courses/categories")
    @Operation(summary = "Get all course categories")
    public ResponseEntity<?> getCourseCategories() {
        return ResponseEntity.ok(educationService.getCourseCategories());
    }

    @GetMapping("/courses/instructors")
    @Operation(summary = "Get all instructors")
    public ResponseEntity<?> getInstructors() {
        return ResponseEntity.ok(educationService.getInstructors());
    }

    // ==================== ENROLLMENTS ====================

    @PostMapping("/courses/{courseId}/enroll")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Enroll user in a course")
    public ResponseEntity<?> enrollInCourse(
            @PathVariable String courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var enrollment = educationService.enrollUserInCourse(userDetails.getUsername(), courseId);
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user's enrolled courses")
    public ResponseEntity<PageResponse<?>> getMyEnrolledCourses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        return ResponseEntity.ok(educationService.getUserEnrolledCourses(userDetails.getUsername(), pageable));
    }

    @GetMapping("/my-courses/in-progress")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user's in-progress courses")
    public ResponseEntity<PageResponse<?>> getMyInProgressCourses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(educationService.getUserInProgressCourses(userDetails.getUsername(), pageable));
    }

    @GetMapping("/my-courses/completed")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user's completed courses")
    public ResponseEntity<?> getMyCompletedCourses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(educationService.getUserCompletedCourses(userDetails.getUsername()));
    }

    @PutMapping("/enrollments/{enrollmentId}/progress")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update course progress")
    public ResponseEntity<?> updateCourseProgress(
            @PathVariable String enrollmentId,
            @RequestParam Double progress,
            @RequestParam(required = false) Integer completedLessons) {
        try {
            var updated = educationService.updateCourseProgress(
                    enrollmentId,
                    progress,
                    completedLessons != null ? completedLessons : 0
            );
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/enrolled-status")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Check if user is enrolled in a course")
    public ResponseEntity<?> checkEnrollmentStatus(
            @PathVariable String courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isEnrolled = educationService.isUserEnrolledInCourse(userDetails.getUsername(), courseId);
        return ResponseEntity.ok(new EnrollmentStatus(isEnrolled));
    }

    public record EnrollmentStatus(boolean enrolled) {}
}
