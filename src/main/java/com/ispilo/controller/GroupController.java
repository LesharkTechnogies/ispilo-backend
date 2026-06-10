package com.ispilo.controller;

import com.ispilo.model.dto.request.CreateGroupRequest;
import com.ispilo.model.entity.GroupEntity;
import com.ispilo.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/groups", "/api/groups"})
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<com.ispilo.model.dto.response.GroupResponse> create(@RequestBody CreateGroupRequest r,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        GroupEntity g = groupService.createGroup(userDetails.getUsername(), r);
        return ResponseEntity.ok(groupService.toGroupResponse(g));
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> join(@PathVariable String groupId,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        groupService.joinGroup(userDetails.getUsername(), groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<com.ispilo.model.dto.response.GroupResponse>> getAllGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(groupService.getAllGroups(org.springframework.data.domain.PageRequest.of(page, size)));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<com.ispilo.model.dto.response.GroupDetailsResponse> getGroupDetails(
            @PathVariable String groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(groupService.getGroupDetails(groupId, email));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<java.util.List<com.ispilo.model.dto.response.GroupUserSummaryResponse>> getGroupMembers(
            @PathVariable String groupId) {
        return ResponseEntity.ok(groupService.getGroupMembers(groupId));
    }

    @GetMapping("/{groupId}/invite-link")
    public ResponseEntity<com.ispilo.model.dto.response.GroupInviteLinkResponse> getInviteLink(
            @PathVariable String groupId) {
        return ResponseEntity.ok(groupService.getInviteLink(groupId));
    }

    @PostMapping("/{groupId}/members/{memberId}/promote")
    public ResponseEntity<?> promote(@PathVariable String groupId,
                                     @PathVariable String memberId,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        groupService.promoteToAdmin(userDetails.getUsername(), groupId, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable String groupId,
                                          @PathVariable String memberId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        groupService.removeMember(userDetails.getUsername(), groupId, memberId);
        return ResponseEntity.noContent().build();
    }
}

