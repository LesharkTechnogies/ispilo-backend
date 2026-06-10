package com.ispilo.service;

import com.ispilo.exception.ConflictException;
import com.ispilo.model.dto.request.CreateGroupRequest;
import com.ispilo.model.entity.GroupEntity;
import com.ispilo.model.entity.GroupMembershipEntity;
import com.ispilo.model.enums.GroupRole;
import com.ispilo.model.entity.User;
import com.ispilo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepo;
    private final GroupMemberRepository membershipRepo;
    private final UserRepository userRepo;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public GroupEntity createGroup(String creatorEmail, CreateGroupRequest r) {
        if (groupRepo.existsByNameIgnoreCase(r.getName())) {
            throw new ConflictException("A group with this name already exists.");
        }
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

        GroupMembershipEntity m = GroupMembershipEntity.builder()
                .id(UUID.randomUUID().toString())
                .group(g)
                .user(creator)
                .role(GroupRole.ADMIN)
                .build();
        membershipRepo.save(m);
        return g;
    }

    @Transactional
    public void joinGroup(String userEmail, String groupId) {
        GroupEntity g = groupRepo.findById(groupId).orElseThrow();
        User u = userRepo.findByEmail(userEmail).orElseThrow();
        if (membershipRepo.findByGroupAndUser(g, u).isPresent()) return;
        GroupMembershipEntity m = GroupMembershipEntity.builder()
                .id(UUID.randomUUID().toString())
                .group(g)
                .user(u)
                .role(GroupRole.MEMBER)
                .build();
        membershipRepo.save(m);
    }

    @Transactional
    public void promoteToAdmin(String requesterEmail, String groupId, String memberId) {
        GroupEntity g = groupRepo.findById(groupId).orElseThrow();
        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();
        GroupMembershipEntity requesterMembership = membershipRepo.findByGroupAndUser(g, requester).orElseThrow();
        if (requesterMembership.getRole() != GroupRole.ADMIN) throw new AccessDeniedException("Only admins can promote");

        User member = userRepo.findById(memberId).orElseThrow();
        GroupMembershipEntity gm = membershipRepo.findByGroupAndUser(g, member).orElseThrow();
        gm.setRole(GroupRole.ADMIN);
        membershipRepo.save(gm);
    }

    @Transactional
    public void removeMember(String requesterEmail, String groupId, String memberId) {
        GroupEntity g = groupRepo.findById(groupId).orElseThrow();
        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();
        GroupMembershipEntity requesterMembership = membershipRepo.findByGroupAndUser(g, requester).orElseThrow();
        if (requesterMembership.getRole() != GroupRole.ADMIN) throw new AccessDeniedException("Only admins can remove members");

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
                .map(m -> m.getRole() == GroupRole.ADMIN)
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

    public org.springframework.data.domain.Page<com.ispilo.model.dto.response.GroupResponse> getAllGroups(org.springframework.data.domain.Pageable pageable) {
        return groupRepo.findAll(pageable).map(this::toGroupResponse);
    }

    public com.ispilo.model.dto.response.GroupDetailsResponse getGroupDetails(String groupId, String userEmail) {
        GroupEntity g = groupRepo.findById(groupId).orElseThrow(() -> new com.ispilo.exception.NotFoundException("Group not found"));
        boolean isMember = false;
        boolean isAdmin = false;
        if (userEmail != null) {
            User u = userRepo.findByEmail(userEmail).orElse(null);
            if (u != null) {
                GroupMembershipEntity m = membershipRepo.findByGroupAndUser(g, u).orElse(null);
                if (m != null) {
                    isMember = true;
                    isAdmin = m.getRole() == GroupRole.ADMIN;
                }
            }
        }
        long memberCount = membershipRepo.countByGroup(g);
        java.util.List<GroupMembershipEntity> adminMembers = membershipRepo.findByGroupAndRole(g, GroupRole.ADMIN);
        java.util.List<com.ispilo.model.dto.response.GroupUserSummaryResponse> admins = adminMembers.stream()
            .map(m -> new com.ispilo.model.dto.response.GroupUserSummaryResponse(m.getUser().getId(), m.getUser().getName(), m.getUser().getAvatar()))
            .collect(java.util.stream.Collectors.toList());
        com.ispilo.model.dto.response.GroupUserSummaryResponse creator = null;
        if (g.getCreatedBy() != null) {
            creator = new com.ispilo.model.dto.response.GroupUserSummaryResponse(g.getCreatedBy().getId(), g.getCreatedBy().getName(), g.getCreatedBy().getAvatar());
        }
        
        String inviteLink = "ispilo://group/" + g.getId() + "/join";

        return com.ispilo.model.dto.response.GroupDetailsResponse.builder()
                .id(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .isPrivate(g.isPrivate())
                .createdAt(g.getCreatedAt())
                .createdBy(creator)
                .memberCount(memberCount)
                .adminCount(admins.size())
                .isMember(isMember)
                .isAdmin(isAdmin)
                .admins(admins)
                .inviteLink(inviteLink)
                .build();
    }

    public java.util.List<com.ispilo.model.dto.response.GroupUserSummaryResponse> getGroupMembers(String groupId) {
        GroupEntity g = groupRepo.findById(groupId).orElseThrow(() -> new com.ispilo.exception.NotFoundException("Group not found"));
        return membershipRepo.findByGroup(g).stream()
                .map(m -> new com.ispilo.model.dto.response.GroupUserSummaryResponse(m.getUser().getId(), m.getUser().getName(), m.getUser().getAvatar()))
                .collect(java.util.stream.Collectors.toList());
    }

    public com.ispilo.model.dto.response.GroupInviteLinkResponse getInviteLink(String groupId) {
        GroupEntity g = groupRepo.findById(groupId).orElseThrow(() -> new com.ispilo.exception.NotFoundException("Group not found"));
        return com.ispilo.model.dto.response.GroupInviteLinkResponse.builder()
                .groupId(g.getId())
                .groupName(g.getName())
                .deepLink("ispilo://group/" + g.getId() + "/join")
                .webLink(baseUrl + "/groups/" + g.getId() + "/join")
                .build();
    }
}