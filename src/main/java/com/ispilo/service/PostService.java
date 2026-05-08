package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.CreatePostRequest;
import com.ispilo.model.dto.response.PostResponse;
import com.ispilo.model.entity.Post;
import com.ispilo.model.entity.User;
import com.ispilo.model.entity.Comment;
import com.ispilo.model.entity.PostLike;
import com.ispilo.model.dto.request.CreateCommentRequest;
import com.ispilo.model.dto.response.CommentResponse;
import com.ispilo.repository.PostRepository;
import com.ispilo.repository.UserRepository;
import com.ispilo.repository.CommentRepository;
import com.ispilo.repository.PostLikeRepository;
import com.ispilo.repository.UserFollowRepository;
import com.ispilo.model.entity.UserFollow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ispilo.repository.GroupPostRepository;
import com.ispilo.model.entity.GroupPost;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationService notificationService;
    private final UserFollowRepository userFollowRepository;
    private final SmartFeedService smartFeedService;
    private final PostInteractionService postInteractionService;
    private final PostPublishingService postPublishingService;
    private final GroupPostRepository groupPostRepository;

    private User resolveUser(String usernameOrPhoneOrEmail) {
        return userRepository.findByEmail(usernameOrPhoneOrEmail)
                .orElseGet(() -> userRepository.findByPhone(usernameOrPhoneOrEmail)
                        .orElseThrow(() -> new NotFoundException("User not found")));
    }

    private PostResponse toPostResponse(Post post, User viewer) {
        boolean likedByCurrentUser = viewer != null && postLikeRepository.existsByUserAndPost(viewer, post);
        return PostResponse.fromEntity(post, likedByCurrentUser);
    }

    public Page<PostResponse> getFeed(String username, Pageable pageable) {
        User user = resolveUser(username);

        // 1. Generate Highly Optimized Smart Feed (Facebook-Style Ranking)
        List<Post> rankedPosts = smartFeedService.getRankedFeedForUser(user.getId(), pageable.getPageNumber(), pageable.getPageSize());
        
        List<PostResponse> responses = rankedPosts.stream()
                .map(post -> toPostResponse(post, user))
                .collect(Collectors.toList());
                
        return new org.springframework.data.domain.PageImpl<>(responses, pageable, 1000); // 1000 is dummy total for infinite scroll
    }

    public Page<PostResponse> getUserPosts(String userId, Pageable pageable) {
        return getUserPosts(userId, null, pageable);
    }

    public Page<PostResponse> getUserPosts(String userId, String viewerUsername, Pageable pageable) {
    final User viewerFinal = (viewerUsername != null && !viewerUsername.isBlank())
        ? resolveUser(viewerUsername)
        : null;

    return postRepository.findByUserId(userId, pageable)
        .map(post -> toPostResponse(post, viewerFinal));
    }

    public Page<PostResponse> getMyPosts(String username, Pageable pageable) {
        User currentUser = resolveUser(username);
        return postRepository.findByUserId(currentUser.getId(), pageable)
                .map(post -> toPostResponse(post, currentUser));
    }

    @Transactional
    public PostResponse createPost(String username, CreatePostRequest request) {
    User user = resolveUser(username);

        String postContent = request.getActualContent();

        Post post = Post.builder()
                .user(user)
                .content(postContent != null ? postContent : "")
                .description(postContent != null ? postContent : "")
                .imageUrl(request.getImageUrl())
                .mediaUrls(request.getMediaUrls() != null ? request.getMediaUrls() : new java.util.ArrayList<>())
                .build();

        post = postRepository.save(post);

        // Notify followers
        List<UserFollow> followers = userFollowRepository.findByFollowing(user);
        List<User> followersToNotify = followers.stream().map(UserFollow::getFollower).collect(Collectors.toList());
        notificationService.sendPushNotifications(followersToNotify, "New Post", user.getName() + " just shared a new post.", "NEW_POST", post.getId());

        // Fan-out delivery: Sync new post directly to active followers' WebSockets immediately
        List<String> followerIds = followersToNotify.stream().map(User::getId).collect(Collectors.toList());
        postPublishingService.syncNewPostToFeeds(post, followerIds);

    return toPostResponse(post, user);
    }

    public PostResponse getPost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        return PostResponse.fromEntity(post);
    }

    @Transactional
    public PostResponse updatePost(String username, String postId, CreatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

    User authUser = resolveUser(username);

        if (!post.getUser().getId().equals(authUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }

        String updatedContent = request.getActualContent();
        if (updatedContent != null && !updatedContent.isEmpty()) {
            post.setContent(updatedContent);
            post.setDescription(updatedContent);
        }
        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }
        if (request.getMediaUrls() != null) {
            post.setMediaUrls(request.getMediaUrls());
        }

    return toPostResponse(postRepository.save(post), authUser);
    }

    @Transactional
    public void deletePost(String username, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

    User authUser = resolveUser(username);

        if (!post.getUser().getId().equals(authUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    @Transactional
    public PostResponse toggleLike(String username, String postId) {
    User authUser = resolveUser(username);
        
        // Delegate to high-concurrency event-driven Atomic Interaction Service
        postInteractionService.toggleLike(postId, authUser.getId());
        
        // Fetch the atomically updated state without persisting hibernate memory state over it
        Post post = postRepository.findById(postId).orElseThrow();

    return toPostResponse(post, authUser);
    }

    @Transactional
    public PostResponse sharePost(String username, String postId, CreatePostRequest request) {
        User authUser = resolveUser(username);
        postInteractionService.sharePost(postId, authUser.getId());
        Post originalPost = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));

        String content = request != null ? request.getActualContent() : "";
        Post sharedPost = Post.builder()
                .user(authUser)
                .content(content)
                .description(content)
                .sharedFromPost(originalPost)
                .mediaUrls(new java.util.ArrayList<>())
                .ctaButtons(new java.util.ArrayList<>())
                .comments(new java.util.ArrayList<>())
                .build();

        sharedPost = postRepository.save(sharedPost);
        return toPostResponse(sharedPost, authUser);
    }

    @Transactional
    public PostResponse shareGroupPost(String username, String groupId, String postId, CreatePostRequest request) {
        User authUser = resolveUser(username);
        GroupPost originalGroupPost = groupPostRepository.findById(postId)
                .filter(gp -> gp.getGroup() != null && gp.getGroup().getId().equals(groupId))
                .orElseThrow(() -> new NotFoundException("Group post not found"));

        String content = request != null ? request.getActualContent() : "";
        Post sharedPost = Post.builder()
                .user(authUser)
                .content(content)
                .description(content)
                .sharedFromGroupPost(originalGroupPost)
                .mediaUrls(new java.util.ArrayList<>())
                .ctaButtons(new java.util.ArrayList<>())
                .comments(new java.util.ArrayList<>())
                .build();

        sharedPost = postRepository.save(sharedPost);
        return toPostResponse(sharedPost, authUser);
    }

    @Transactional
    public CommentResponse addComment(String username, String postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

    User authUser = resolveUser(username);

        Comment parentComment = null;
        if (request.getParentCommentId() != null && !request.getParentCommentId().isEmpty()) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(authUser)
                .content(request.getContent())
                .parentComment(parentComment)
                .build();

        comment = commentRepository.save(comment);

        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        // Send notification to post owner if it's not their own comment
        if (!post.getUser().getId().equals(authUser.getId())) {
            String title = "New Comment";
            String body = authUser.getName() + " commented on your post";
            notificationService.sendPushNotification(post.getUser(), title, body, "POST_COMMENT", post.getId());
        }

        return CommentResponse.fromEntity(comment);
    }

    public Page<CommentResponse> getPostComments(String postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        // Only fetch top-level comments; replies will be loaded eagerly/lazily via DTO mapper.
        Page<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId, pageable);
        return comments.map(CommentResponse::fromEntity);
    }
}
