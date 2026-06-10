package com.ispilo.controller;

import com.ispilo.model.dto.request.CreateGroupPostCommentRequest;
import com.ispilo.model.dto.response.GroupPostCommentResponse;
import com.ispilo.service.GroupPostCommentService;
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
@RequestMapping("/api/v1/groups/{groupId}/posts/{postId}/comments")
@RequiredArgsConstructor
public class GroupPostCommentController {

    private final GroupPostCommentService groupPostCommentService;

    @PostMapping
    public ResponseEntity<GroupPostCommentResponse> createComment(
            @PathVariable String groupId,
            @PathVariable String postId,
            @RequestBody CreateGroupPostCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        GroupPostCommentResponse response = groupPostCommentService.createComment(userDetails.getUsername(), groupId, postId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<GroupPostCommentResponse>> getComments(
            @PathVariable String groupId,
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupPostCommentResponse> response = groupPostCommentService.getComments(userDetails.getUsername(), groupId, postId, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String groupId,
            @PathVariable String postId,
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        groupPostCommentService.deleteComment(userDetails.getUsername(), groupId, postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
