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

