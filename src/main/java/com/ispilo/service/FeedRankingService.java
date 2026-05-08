package com.ispilo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class FeedRankingService {

    // Facebook-style weighting configuration
    private static final double W_ENGAGEMENT = 0.4;
    private static final double W_RECENCY = 0.3;
    private static final double W_RELATIONSHIP = 0.2;
    private static final double W_VIRALITY = 0.1;
    
    // Time decay factor (Gravity)
    private static final double GRAVITY = 0.05; 

    /**
     * Calculates a relevance score for a post tailored to a specific user.
     */
    public double calculatePostScore(int likesCount, int commentsCount, int sharesCount, 
                                     LocalDateTime createdAt, boolean isFollowing, int pastInteractions) {
        
        // 1. Engagement: Total interactions (weighted by effort)
        double engagementScore = likesCount + (commentsCount * 2.0) + (sharesCount * 3.0);

        // 2. Recency: Exponential decay based on hours passed
        long hoursSincePost = Math.max(1, Duration.between(createdAt, LocalDateTime.now()).toHours());
        double recencyScore = Math.exp(-GRAVITY * hoursSincePost) * 100.0;

        // 3. Relationship: Affinity with the author
        double relationshipScore = isFollowing ? 50.0 : 0.0;
        relationshipScore += Math.min(pastInteractions * 10.0, 50.0); // Cap interaction bonus at 50

        // 4. Virality (Velocity): How fast is it accumulating engagement?
        double viralityScore = (engagementScore / hoursSincePost) * 10.0;

        // Final Score Formula
        return (W_ENGAGEMENT * engagementScore)
             + (W_RECENCY * recencyScore)
             + (W_RELATIONSHIP * relationshipScore)
             + (W_VIRALITY * viralityScore);
    }
}