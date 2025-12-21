package com.ispilo.service;

import com.ispilo.model.dto.response.PostResponse;
import com.ispilo.model.entity.Post;
import com.ispilo.repository.PostRepository;
import com.ispilo.repository.PostViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final PostViewRepository postViewRepository;

    private static final double VIEW_THRESHOLD = 0.7; // 70% of post must be viewed

    public Page<PostResponse> getPersonalizedFeed(String userId, Pageable pageable) {
        // Get posts viewed in last 7 days to exclude them
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        Set<String> viewedPostIds = postViewRepository.findRecentViewedPostIds(userId, cutoff);

        Page<Post> posts;
        if (viewedPostIds.isEmpty()) {
            posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            posts = postRepository.findPersonalizedFeed(viewedPostIds, pageable);
        }

        return posts.map(PostResponse::fromEntity);
    }

    @Transactional
    public void trackPostView(String userId, String postId, Double viewPercentage, Integer viewDurationMs) {
        postViewRepository.upsertView(userId, postId, viewPercentage, viewDurationMs, VIEW_THRESHOLD);
        updatePostViewCount(postId);
    }

    @Async
    @Transactional
    public void updatePostViewCount(String postId) {
        postRepository.incrementViewCount(postId);
    }
}
