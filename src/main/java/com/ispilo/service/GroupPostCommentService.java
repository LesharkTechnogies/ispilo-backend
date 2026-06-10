package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.CreateGroupPostCommentRequest;
import com.ispilo.model.dto.response.GroupPostCommentResponse;
import com.ispilo.model.entity.GroupEntity;
import com.ispilo.model.entity.GroupMembershipEntity;
import com.ispilo.model.entity.GroupPost;
import com.ispilo.model.entity.GroupPostComment;
import com.ispilo.model.entity.User;
import com.ispilo.repository.GroupMemberRepository;
import com.ispilo.repository.GroupPostCommentRepository;
import com.ispilo.repository.GroupPostRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPostCommentService {

    private final GroupPostCommentRepository groupPostCommentRepository;
    private final GroupPostRepository groupPostRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupPostCommentResponse createComment(String username, String groupId, String postId, CreateGroupPostCommentRequest request) {
        User user = findUserByUsername(username);
        GroupPost post = groupPostRepository.findByIdAndGroupIdWithLock(postId, groupId)
                .orElseThrow(() -> new NotFoundException("Post not found in this group"));

        GroupEntity group = post.getGroup();
        groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group."));

        GroupPostComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = groupPostCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
        }

        GroupPostComment comment = GroupPostComment.builder()
                .id(UUID.randomUUID().toString())
                .post(post)
                .user(user)
                .content(request.getContent())
                .parentComment(parentComment)
                .createdAt(LocalDateTime.now())
                .build();

        comment = groupPostCommentRepository.save(comment);

        return GroupPostCommentResponse.fromEntity(comment);
    }

    public Page<GroupPostCommentResponse> getComments(String username, String groupId, String postId, Pageable pageable) {
        User user = findUserByUsername(username);
        GroupPost post = groupPostRepository.findByIdAndGroupIdWithLock(postId, groupId)
                .orElseThrow(() -> new NotFoundException("Post not found in this group"));

        GroupEntity group = post.getGroup();
        groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group."));

        Page<GroupPostComment> comments = groupPostCommentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post, pageable);
        return comments.map(GroupPostCommentResponse::fromEntity);
    }

    @Transactional
    public void deleteComment(String username, String groupId, String postId, String commentId) {
        User user = findUserByUsername(username);
        GroupPost post = groupPostRepository.findByIdAndGroupIdWithLock(postId, groupId)
                .orElseThrow(() -> new NotFoundException("Post not found in this group"));

        GroupEntity group = post.getGroup();
        GroupMembershipEntity membership = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group."));

        GroupPostComment comment = groupPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getPost().getId().equals(post.getId())) {
            throw new NotFoundException("Comment does not belong to this post");
        }

        boolean isAuthor = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = membership.getRole() == com.ispilo.model.enums.GroupRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }

        groupPostCommentRepository.delete(comment);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));
    }
}
