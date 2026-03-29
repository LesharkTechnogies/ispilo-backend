package com.ispilo.controller;

import com.ispilo.model.dto.response.PostResponse;
import com.ispilo.service.FeedService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/posts", "/api/posts", "/api/v2/posts"})
@RequiredArgsConstructor
public class PostController {

    private final FeedService feedService;
    private final com.ispilo.service.PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody com.ispilo.model.dto.request.CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(userDetails.getUsername(), request));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable String postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId,
            @RequestBody com.ispilo.model.dto.request.CreatePostRequest request) {
        return ResponseEntity.ok(postService.updatePost(userDetails.getUsername(), postId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId) {
        postService.deletePost(userDetails.getUsername(), postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(feedService.getPersonalizedFeed(userDetails.getUsername(), pageable));
    }

    @PostMapping("/{postId}/track-view")
    public ResponseEntity<Void> trackView(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId,
            @RequestBody TrackViewRequest request) {
        feedService.trackPostView(userDetails.getUsername(), postId, request.getViewPercentage(), request.getViewDurationMs());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class TrackViewRequest {
        private Double viewPercentage;
        private Integer viewDurationMs;
    }
}
