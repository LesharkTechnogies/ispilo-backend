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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final FeedService feedService;

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
