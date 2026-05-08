  package com.ispilo.service;

import com.ispilo.model.entity.Post;
import com.ispilo.model.entity.User;
import com.ispilo.model.entity.UserFollow;
import com.ispilo.repository.PostRepository;
import com.ispilo.repository.UserFollowRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmartFeedService {

    private final FeedRankingService feedRankingService;
    private final PostRepository postRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    /**
     * Generates a ranked, personalized feed.
     * Hybrid Approach: Fetch candidates -> Rank -> Sort -> Paginate
     */
    @Transactional(readOnly = true)
    public List<Post> getRankedFeedForUser(String userId, int page, int size) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new com.ispilo.exception.NotFoundException("User not found"));

    // 1. CANDIDATE GENERATION
    LocalDateTime followCutoff = LocalDateTime.now().minusDays(7);
    LocalDateTime trendingCutoff = LocalDateTime.now().minusDays(3);

    List<User> followingUsers = userFollowRepository.findAllByFollower(user).stream()
        .map(UserFollow::getFollowing)
        .collect(Collectors.toList());

    int candidateSize = Math.max(size * 3, 30);
    List<Post> followingPosts = followingUsers.isEmpty()
        ? List.of()
        : postRepository.findByUserInAndCreatedAtAfter(
            followingUsers,
            followCutoff,
            PageRequest.of(0, candidateSize, Sort.by("createdAt").descending()))
        .getContent();

    List<Post> trendingPosts = postRepository.findTrendingPosts(
            trendingCutoff,
            PageRequest.of(0, candidateSize, Sort.by("createdAt").descending()))
        .getContent();

    List<Post> candidates = new java.util.ArrayList<>();
    java.util.Set<String> seen = new java.util.HashSet<>();
    for (Post post : followingPosts) {
        if (seen.add(post.getId())) {
        candidates.add(post);
        }
    }
    for (Post post : trendingPosts) {
        if (seen.add(post.getId())) {
        candidates.add(post);
        }
    }

        // 2. FEATURE EXTRACTION & SCORING
        java.util.Set<String> followingIds = followingUsers.stream()
                .map(User::getId)
                .collect(java.util.stream.Collectors.toSet());

        List<RankedPost> rankedPosts = candidates.stream().map(post -> {
            
            // Example: check if user follows author
            boolean isFollowing = followingIds.contains(post.getUser().getId()); 
            int pastInteractions = 0; // TODO: wire with interaction stats

            double score = feedRankingService.calculatePostScore(
                post.getLikesCount() == null ? 0 : post.getLikesCount(),
                post.getCommentsCount() == null ? 0 : post.getCommentsCount(),
                post.getSharesCount() == null ? 0 : post.getSharesCount(),
                post.getCreatedAt(),
                isFollowing,
                pastInteractions
            );
            
            return new RankedPost(post, score);
        }).collect(Collectors.toList());

        // 3. RANKING & PAGINATION (Infinite Scroll Support)
        return rankedPosts.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score)) // Descending score
                .skip((long) page * size)
                .limit(size)
                .map(rp -> rp.post)
                .collect(Collectors.toList());
    }

    // Internal wrapper for sorting
    private record RankedPost(Post post, double score) {}
}