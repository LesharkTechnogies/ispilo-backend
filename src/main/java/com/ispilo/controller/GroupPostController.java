package com.ispilo.controller;

import com.ispilo.model.dto.request.CreatePostRequest;
import com.ispilo.model.dto.response.GroupPostResponse;
import com.ispilo.service.GroupPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/posts")
@RequiredArgsConstructor
public class GroupPostController {

    private final GroupPostService groupPostService;

    @PostMapping
    public ResponseEntity<GroupPostResponse> createGroupPost(
            @PathVariable String groupId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreatePostRequest request) {
        
        GroupPostResponse newPost = groupPostService.createGroupPost(userDetails.getUsername(), groupId, request);
        return new ResponseEntity<>(newPost, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<GroupPostResponse>> getGroupPosts(
            @PathVariable String groupId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupPostResponse> posts = groupPostService.getGroupPosts(userDetails.getUsername(), groupId, pageable);
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteGroupPost(
            @PathVariable String groupId, // Though not strictly needed by the service, it keeps the URL structure consistent
            @PathVariable String postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        groupPostService.deleteGroupPost(userDetails.getUsername(), groupId, postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<GroupPostResponse> toggleLikeGroupPost(
            @PathVariable String groupId,
            @PathVariable String postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        GroupPostResponse post = groupPostService.toggleLike(userDetails.getUsername(), groupId, postId);
        return ResponseEntity.ok(post);
    }
}
