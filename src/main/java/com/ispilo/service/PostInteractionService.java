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

    // Autonomous recovery: retries automatically if database transaction fails/locks
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    @Transactional
    public void toggleLike(String postId, String userId) {
        
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        java.util.Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);
        boolean alreadyLiked = existingLike.isPresent();

        if (alreadyLiked) {
            postLikeRepository.delete(existingLike.get());
            
            // 2. Safely decrement count in DB atomically
            postMetricsRepository.decrementLikes(postId);
        } else {
            postLikeRepository.save(PostLike.builder().post(post).user(user).build());
            
            // 2. Safely increment count in DB atomically
            postMetricsRepository.incrementLikes(postId);
        }

        // 3. EVENT-DRIVEN UI UPDATE: Broadcast live reaction change via STOMP WebSockets
        // Frontend listens to: /topic/posts/{postId}/interactions
        messagingTemplate.convertAndSend(
            "/topic/posts/" + postId + "/interactions",
            Map.of("postId", postId, "action", alreadyLiked ? "UNLIKE" : "LIKE", "actorId", userId)
        );
        log.info("Broadcasted live interaction update for post {}", postId);
    }

    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    @Transactional
    public void sharePost(String postId, String userId) {
        // 1. Safely increment share count atomically
        postMetricsRepository.incrementShares(postId);

        // 2. Broadcast live share update to all users viewing the post
        messagingTemplate.convertAndSend(
            "/topic/posts/" + postId + "/interactions",
            Map.of("postId", postId, "action", "SHARE", "actorId", userId)
        );
        log.info("Broadcasted live share update for post {}", postId);
    }

    // Fallback method if the system crashes 3 times in a row (Prevents cascading failures)
    @Recover
    public void recoverInteraction(Exception e, String postId, String userId) {
        log.error("Failed to process interaction for post {} by user {} after 3 retries. Autonomous repair skipped. Error: {}", 
                  postId, userId, e.getMessage());
        // At scale (Facebook), you would push this failed action to a Kafka/RabbitMQ Dead Letter Queue 
        // so it eventually processes when the database recovers.
    }
}