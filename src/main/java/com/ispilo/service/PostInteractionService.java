package com.ispilo.service;

import com.ispilo.repository.PostMetricsRepository;
import com.ispilo.repository.PostLikeRepository;
import com.ispilo.repository.PostRepository;
import com.ispilo.repository.UserRepository;
import com.ispilo.model.entity.Post;
import com.ispilo.model.entity.User;
import com.ispilo.model.entity.PostLike;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostInteractionService {

    private final PostMetricsRepository postMetricsRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String POST_LIKES_KEY_PREFIX = "post:likes:";

    // Autonomous recovery: retries automatically if database transaction fails/locks
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    @Transactional
    public void toggleLike(String postId, String userId) {
        
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        String redisKey = POST_LIKES_KEY_PREFIX + postId;
        
        java.util.Optional<PostLike> existingLike = postLikeRepository.findByUserIdAndPostId(userId, postId);
        boolean alreadyLiked = existingLike.isPresent();

        if (alreadyLiked) {
            postLikeRepository.delete(existingLike.get());
            postMetricsRepository.decrementLikes(postId);
            redisTemplate.opsForSet().remove(redisKey, userId); // Fast memory like removal
        } else {
            postLikeRepository.save(PostLike.builder().post(post).user(user).build());
            postMetricsRepository.incrementLikes(postId);
            redisTemplate.opsForSet().add(redisKey, userId); // Fast memory like addition
        }

        // 3. EVENT-DRIVEN UI UPDATE: Broadcast live reaction change via STOMP WebSockets
        messagingTemplate.convertAndSend(
            "/topic/posts/" + postId + "/interactions",
            Map.of("postId", postId, "action", alreadyLiked ? "UNLIKE" : "LIKE", "actorId", userId)
        );
        log.info("Broadcasted live interaction update for post {}", postId);
    }

    public boolean isPostLikedByUser(String postId, String userId) {
        String redisKey = POST_LIKES_KEY_PREFIX + postId;
        Boolean isMember = redisTemplate.opsForSet().isMember(redisKey, userId);
        if (Boolean.TRUE.equals(isMember)) {
            return true;
        }
        // Fallback to DB if Redis key expired or missing
        boolean dbLike = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if (dbLike) {
            redisTemplate.opsForSet().add(redisKey, userId);
        }
        return dbLike;
    }

    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    @Transactional
    public void sharePost(String postId, String userId) {
        postMetricsRepository.incrementShares(postId);
        messagingTemplate.convertAndSend(
            "/topic/posts/" + postId + "/interactions",
            Map.of("postId", postId, "action", "SHARE", "actorId", userId)
        );
        log.info("Broadcasted live share update for post {}", postId);
    }

    @Recover
    public void recoverInteraction(Exception e, String postId, String userId) {
        log.error("Failed to process interaction for post {} by user {} after 3 retries. Autonomous repair skipped. Error: {}", 
                  postId, userId, e.getMessage());
    }
}