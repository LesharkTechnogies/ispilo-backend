package com.ispilo.controller;

import com.ispilo.job.StoryCleanupTask;
import com.ispilo.model.dto.request.CreateStoryRequest;
import com.ispilo.model.dto.response.StoryResponse;
import com.ispilo.model.dto.response.UserStoryGroupResponse;
import com.ispilo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final StoryCleanupTask storyCleanupTask;

    @PostMapping
    public ResponseEntity<StoryResponse> createStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateStoryRequest request) {
        return ResponseEntity.ok(storyService.createStory(userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<UserStoryGroupResponse>> getActiveStories() {
        // Groups all active stories across users
        return ResponseEntity.ok(storyService.getActiveStoriesGroupedByUser());
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> deleteStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String storyId) {
        storyService.deleteStory(userDetails.getUsername(), storyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, String>> triggerBackgroundCleanup() {
        storyCleanupTask.cleanupExpiredStories();
        return ResponseEntity.ok(Map.of("message", "Background cleanup triggered successfully"));
    }
}
