package com.ispilo.controller;

import com.ispilo.model.dto.request.VideoCommentRequest;
import com.ispilo.model.dto.request.VideoUploadInitiateRequest;
import com.ispilo.model.dto.response.VideoCommentResponse;
import com.ispilo.model.dto.response.VideoResponse;
import com.ispilo.model.dto.response.UploadUrlResponse;
import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.service.VideoCommentService;
import com.ispilo.service.VideoFeedService;
import com.ispilo.service.VideoLikeService;
import com.ispilo.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.ispilo.model.dto.response.HashtagResponse;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final VideoFeedService videoFeedService;
    private final VideoLikeService videoLikeService;
    private final VideoCommentService videoCommentService;

    /**
     * Get trending videos for the scrollable feed
     */
    @GetMapping("/feed")
    public ResponseEntity<PageResponse<VideoResponse>> getFeed(
            @AuthenticationPrincipal String userEmail,
            Pageable pageable) {
        return ResponseEntity.ok(videoFeedService.getDiscoverFeed(userEmail, pageable));
    }

    @GetMapping("/feed/following")
    public ResponseEntity<PageResponse<VideoResponse>> getFollowingFeed(
            @AuthenticationPrincipal String userEmail,
            Pageable pageable) {
        return ResponseEntity.ok(videoFeedService.getFollowingFeed(userEmail, pageable));
    }

    @GetMapping("/feed/trending")
    public ResponseEntity<PageResponse<VideoResponse>> getTrendingFeed(
            @AuthenticationPrincipal String userEmail,
            Pageable pageable) {
        return ResponseEntity.ok(videoFeedService.getTrendingFeed(userEmail, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<VideoResponse>> getUserVideos(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String userId,
            Pageable pageable) {
        return ResponseEntity.ok(videoFeedService.getUserVideos(userId, userEmail, pageable));
    }

    @GetMapping("/hashtag/{hashtag}")
    public ResponseEntity<PageResponse<VideoResponse>> getVideosByHashtag(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String hashtag,
            Pageable pageable) {
        return ResponseEntity.ok(videoFeedService.getVideosByHashtag(hashtag, userEmail, pageable));
    }

    @GetMapping("/hashtags/trending")
    public ResponseEntity<PageResponse<HashtagResponse>> getTrendingHashtags(
            Pageable pageable) {
        return ResponseEntity.ok(videoFeedService.getTrendingHashtags(pageable));
    }

    /**
     * Initialize a video upload. Returns a pre-signed URL.
     */
    @PostMapping("/upload/initiate")
    public ResponseEntity<UploadUrlResponse> initiateUpload(
            @AuthenticationPrincipal String userEmail,
            @RequestBody VideoUploadInitiateRequest request) {
        return ResponseEntity.ok(videoService.initiateUpload(userEmail, request));
    }

    /**
     * Notify the system that upload to R2 is complete.
     * Triggers the async processing pipeline.
     */
    @PostMapping("/{videoId}/complete")
    public ResponseEntity<Void> completeUpload(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String videoId) {
        videoService.completeUpload(userEmail, videoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponse> getVideo(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String videoId) {
        return ResponseEntity.ok(videoService.getVideo(videoId, userEmail));
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> deleteVideo(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String videoId) {
        videoService.deleteVideo(videoId, userEmail);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{videoId}/view")
    public ResponseEntity<Void> incrementViewCount(
            @PathVariable String videoId) {
        videoService.incrementViewCount(videoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{videoId}/share")
    public ResponseEntity<Void> incrementShareCount(
            @PathVariable String videoId) {
        videoService.incrementShareCount(videoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{videoId}/like")
    public ResponseEntity<Void> toggleLike(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String videoId) {
        videoLikeService.toggleLike(videoId, userEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{videoId}/comments")
    public ResponseEntity<PageResponse<VideoCommentResponse>> getComments(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String videoId,
            Pageable pageable) {
        return ResponseEntity.ok(videoCommentService.getComments(videoId, userEmail, pageable));
    }

    @PostMapping("/{videoId}/comments")
    public ResponseEntity<VideoCommentResponse> addComment(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String videoId,
            @Valid @RequestBody VideoCommentRequest request) {
        return ResponseEntity.ok(videoCommentService.addComment(videoId, userEmail, request));
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<PageResponse<VideoCommentResponse>> getReplies(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String commentId,
            Pageable pageable) {
        return ResponseEntity.ok(videoCommentService.getReplies(commentId, userEmail, pageable));
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> toggleCommentLike(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String commentId) {
        videoCommentService.toggleCommentLike(commentId, userEmail);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal String userEmail,
            @PathVariable String commentId) {
        videoCommentService.deleteComment(commentId, userEmail);
        return ResponseEntity.ok().build();
    }
}