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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public PostResponse createPost(String username, CreatePostRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        String postContent = request.getActualContent();

        Post post = Post.builder()
                .user(user)
                .content(postContent != null ? postContent : "")
                .description(postContent != null ? postContent : "")
                .imageUrl(request.getImageUrl())
                .mediaUrls(request.getMediaUrls() != null ? request.getMediaUrls() : new java.util.ArrayList<>())
                .build();

        return PostResponse.fromEntity(postRepository.save(post));
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

        User authUser = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

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

        return PostResponse.fromEntity(postRepository.save(post));
    }

    @Transactional
    public void deletePost(String username, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        User authUser = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        if (!post.getUser().getId().equals(authUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    @Transactional
    public PostResponse toggleLike(String username, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        User authUser = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(authUser, post);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        } else {
            PostLike newLike = PostLike.builder()
                    .user(authUser)
                    .post(post)
                    .build();
            postLikeRepository.save(newLike);
            post.setLikesCount((post.getLikesCount() == null ? 0 : post.getLikesCount()) + 1);
        }

        return PostResponse.fromEntity(postRepository.save(post));
    }

    @Transactional
    public CommentResponse addComment(String username, String postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        User authUser = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        Comment comment = Comment.builder()
                .post(post)
                .user(authUser)
                .content(request.getContent())
                .build();

        comment = commentRepository.save(comment);

        post.setCommentsCount((post.getCommentsCount() == null ? 0 : post.getCommentsCount()) + 1);
        postRepository.save(post);

        return CommentResponse.fromEntity(comment);
    }

    public Page<CommentResponse> getPostComments(String postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        Page<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
        return comments.map(CommentResponse::fromEntity);
    }
}
