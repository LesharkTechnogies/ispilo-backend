package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.CreatePostRequest;
import com.ispilo.model.dto.response.PostResponse;
import com.ispilo.model.entity.Post;
import com.ispilo.model.entity.User;
import com.ispilo.repository.PostRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

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
}
