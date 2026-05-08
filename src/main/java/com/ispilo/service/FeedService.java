package com.ispilo.service;

import com.ispilo.model.dto.response.PostResponse;
import com.ispilo.model.entity.Post;
import com.ispilo.repository.PostRepository;
import com.ispilo.repository.PostLikeRepository;
import com.ispilo.repository.PostViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostViewRepository postViewRepository;
    private final com.ispilo.repository.UserRepository userRepository;
    private final SmartFeedService smartFeedService;

    private static final double VIEW_THRESHOLD = 0.7; // 70% of post must be viewed

    public Page<PostResponse> getPersonalizedFeed(String username, Pageable pageable) {
        // Get user ID
        com.ispilo.model.entity.User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new com.ispilo.exception.NotFoundException("User not found")));
        String userId = user.getId();

        // Get posts viewed in last 7 days to exclude them
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        Set<String> viewedPostIds = postViewRepository.findRecentViewedPostIds(userId, cutoff);

        List<Post> rankedPosts = smartFeedService.getRankedFeedForUser(userId, pageable.getPageNumber(), pageable.getPageSize());
        List<PostResponse> responses = rankedPosts.stream()
                .map(post -> PostResponse.fromEntity(post, postLikeRepository.existsByUserAndPost(user, post)))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(responses, pageable, responses.size());
    }

    @Transactional
    public void trackPostView(String username, String postId, Double viewPercentage, Integer viewDurationMs) {
        com.ispilo.model.entity.User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new com.ispilo.exception.NotFoundException("User not found")));
        
        postViewRepository.upsertView(user.getId(), postId, viewPercentage, viewDurationMs, VIEW_THRESHOLD);
        updatePostViewCount(postId);
    }

    @Async
    @Transactional
    public void updatePostViewCount(String postId) {
        postRepository.incrementViewCount(postId);
    }
}
