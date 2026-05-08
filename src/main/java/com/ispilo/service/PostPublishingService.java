package com.ispilo.service;

import com.ispilo.model.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostPublishingService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Fan-out delivery: Syncs newly created posts to users in real-time.
     * 
     * @param newPost The newly created post saved to the DB
     * @param followerIds List of user IDs who follow the author
     */
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void syncNewPostToFeeds(Post newPost, List<String> followerIds) {
        
        // 1. Push to global trending live feed
        messagingTemplate.convertAndSend("/topic/feed/global", newPost);

        // 2. Fan-out to specific online followers (Private Queue)
        for (String followerId : followerIds) {
            messagingTemplate.convertAndSendToUser(
                followerId, 
                "/queue/feed/new", 
                newPost
            );
        }
        log.info("Autonomously synced new post {} to {} active user feeds", newPost.getId(), followerIds.size());
    }
}