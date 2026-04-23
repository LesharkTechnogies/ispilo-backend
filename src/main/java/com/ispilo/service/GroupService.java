package com.ispilo.service;

import com.ispilo.model.dto.request.CreateGroupRequest;
import com.ispilo.model.entity.GroupEntity;
import com.ispilo.model.entity.GroupMembership;
import com.ispilo.model.entity.User;
import com.ispilo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepo;
    private final GroupMemberRepository membershipRepo;
    private final UserRepository userRepo;

    @Transactional
    public GroupEntity createGroup(String creatorEmail, CreateGroupRequest r) {
        User creator = userRepo.findByEmail(creatorEmail).orElseThrow();
        GroupEntity g = GroupEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(r.getName())
                .description(r.getDescription())
                .isPrivate(r.isPrivateGroup())
                .createdAt(Instant.now())
                .createdBy(creator)
                .build();
        groupRepo.save(g);

        GroupMembership m = GroupMembership.builder()
                .id(UUID.randomUUID().toString())
                .group(g)
                .user(creator)
                .role(GroupMembership.Role.ADMIN)
                .build();
        membershipRepo.save(m);
        return g;
    }

    @Transactional
    public void joinGroup(String userEmail, String groupId) {
        GroupEntity g = groupRepo.findById(groupId).orElseThrow();
        User u = userRepo.findByEmail(userEmail).orElseThrow();
        if (membershipRepo.findByGroupAndUser(g, u).isPresent()) return;
        GroupMembership m = GroupMembership.builder()
                .id(UUID.randomUUID().toString())
                .group(g)
                .user(u)
                .role(GroupMembership.Role.MEMBER)
                .build();
        membershipRepo.save(m);
    }

    @Transactional
    public void promoteToAdmin(String requesterEmail, String groupId, String memberId) {
        GroupEntity g = groupRepo.findById(groupId).orElseThrow();
        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();
        GroupMembership requesterMembership = membershipRepo.findByGroupAndUser(g, requester).orElseThrow();
        if (requesterMembership.getRole() != GroupMembership.Role.ADMIN) throw new AccessDeniedException("Only admins can promote");

        User member = userRepo.findById(memberId).orElseThrow();
        GroupMembership gm = membershipRepo.findByGroupAndUser(g, member).orElseThrow();
        gm.setRole(GroupMembership.Role.ADMIN);
        membershipRepo.save(gm);
    }

    @Transactional
    public void removeMember(String requesterEmail, String groupId, String memberId) {
        GroupEntity g = groupRepo.findById(groupId).orElseThrow();
        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();
        GroupMembership requesterMembership = membershipRepo.findByGroupAndUser(g, requester).orElseThrow();
        if (requesterMembership.getRole() != GroupMembership.Role.ADMIN) throw new AccessDeniedException("Only admins can remove members");

        User member = userRepo.findById(memberId).orElseThrow();
        membershipRepo.findByGroupAndUser(g, member).ifPresent(membershipRepo::delete);
    }

    public boolean isMember(GroupEntity g, User user) {
        return membershipRepo.findByGroupAndUser(g, user).isPresent();
    }

    public boolean isAdmin(String userEmail, String groupId) {
        GroupEntity g = groupRepo.findById(groupId).orElse(null);
        if (g == null) return false;
        User u = userRepo.findByEmail(userEmail).orElse(null);
        if (u == null) return false;
        return membershipRepo.findByGroupAndUser(g, u)
                .map(m -> m.getRole() == GroupMembership.Role.ADMIN)
                .orElse(false);
    }

    public com.ispilo.model.dto.response.GroupResponse toGroupResponse(GroupEntity g) {
        com.ispilo.model.dto.response.GroupResponse r = new com.ispilo.model.dto.response.GroupResponse();
        r.setId(g.getId());
        r.setName(g.getName());
        r.setDescription(g.getDescription());
        r.setPrivate(g.isPrivate());
        r.setCreatedAt(g.getCreatedAt());
        r.setCreatedById(g.getCreatedBy() != null ? g.getCreatedBy().getId() : null);
        r.setMemberCount(membershipRepo.countByGroup(g));
        return r;
    }
}