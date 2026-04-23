package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.CreatePostRequest;
import com.ispilo.model.dto.response.GroupPostResponse;
import com.ispilo.model.entity.GroupEntity;
import com.ispilo.model.entity.GroupMembership;
import com.ispilo.model.entity.PostEntity;
import com.ispilo.model.entity.GroupPostLike;
import com.ispilo.model.entity.User;
import com.ispilo.repository.GroupMemberRepository;
import com.ispilo.repository.GroupPostLikeRepository;
import com.ispilo.repository.GroupPostRepository;
import com.ispilo.repository.GroupRepository;
import com.ispilo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupPostService {

    private final GroupPostRepository groupPostRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupPostLikeRepository groupPostLikeRepository;
    private final GroupPostMapper groupPostMapper;
    private final NotificationService notificationService;

    @Transactional
    public GroupPostResponse createGroupPost(String username, String groupId, CreatePostRequest request) {
        User user = findUserByUsername(username);
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        // Ensure user is a member of the group
        GroupMembership membership = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group."));

        String postContent = request.getActualContent();
        PostEntity post = PostEntity.builder()
                .id(UUID.randomUUID().toString())
                .author(user)
                .group(group)
                .text(postContent != null ? postContent : "")
                .mediaUrls(request.getMediaUrls() != null ? new HashSet<>(request.getMediaUrls()) : new HashSet<>())
                .anonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false)
                .createdAt(Instant.now())
                .build();

        post = groupPostRepository.save(post);
        
        // Notify group members about the new post
        List<User> membersToNotify = groupMemberRepository.findByGroup(group).stream()
                .map(GroupMembership::getUser)
                .filter(u -> !u.getId().equals(user.getId())) // Don't notify the author
                .collect(Collectors.toList());

        if (!membersToNotify.isEmpty()) {
            String title = "New Group Post";
            String body = post.isAnonymous() 
                    ? "Someone posted in " + group.getName()
                    : user.getName() + " posted in " + group.getName();
            
            notificationService.sendPushNotifications(
                membersToNotify, 
                title, 
                body, 
                "GROUP_POST", 
                post.getId()
            );
        }

        boolean isAdmin = membership.getRole() == GroupMembership.Role.ADMIN;
        return groupPostMapper.toDto(post, isAdmin);
    }

    public Page<GroupPostResponse> getGroupPosts(String username, String groupId, Pageable pageable) {
        User user = findUserByUsername(username);
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        // Ensure user is a member to view posts
        GroupMembership member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group."));
        
        boolean isGroupAdmin = member.getRole() == GroupMembership.Role.ADMIN;

        Page<PostEntity> posts = groupPostRepository.findByGroup(group, pageable);
        
        return posts.map(post -> groupPostMapper.toDto(post, isGroupAdmin));
    }

    @Transactional
    public void deleteGroupPost(String username, String postId) {
        User user = findUserByUsername(username);
        PostEntity post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (post.getGroup() == null) {
            throw new IllegalArgumentException("This is not a group post.");
        }

        GroupEntity group = post.getGroup();
        boolean isAuthor = post.getAuthor() != null && post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = groupMemberRepository.findByGroupAndUser(group, user)
                .map(member -> member.getRole() == GroupMembership.Role.ADMIN)
                .orElse(false);

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this post.");
        }

        groupPostRepository.delete(post);
    }

    @Transactional
    public GroupPostResponse toggleLike(String username, String postId) {
        User user = findUserByUsername(username);
        PostEntity post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        GroupEntity group = post.getGroup();
        if (group == null) {
            throw new IllegalArgumentException("This is not a group post.");
        }

        GroupMembership member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group."));

        Optional<GroupPostLike> existingLike = groupPostLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            groupPostLikeRepository.delete(existingLike.get());
        } else {
            GroupPostLike newLike = GroupPostLike.builder()
                    .id(UUID.randomUUID().toString())
                    .post(post)
                    .user(user)
                    .createdAt(Instant.now())
                    .build();
            groupPostLikeRepository.save(newLike);
        }

        boolean isGroupAdmin = member.getRole() == GroupMembership.Role.ADMIN;
        return groupPostMapper.toDto(post, isGroupAdmin);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));
    }
}
