package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.model.dto.response.VideoResponse;
import com.ispilo.model.entity.User;
import com.ispilo.model.entity.Video;
import com.ispilo.model.enums.VideoStatus;
import com.ispilo.repository.UserFollowRepository;
import com.ispilo.repository.UserRepository;
import com.ispilo.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.ispilo.repository.HashtagRepository;
import com.ispilo.model.dto.response.HashtagResponse;
// ...
@Service
@RequiredArgsConstructor
public class VideoFeedService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final HashtagRepository hashtagRepository;
    private final VideoService videoService;

    public PageResponse<VideoResponse> getDiscoverFeed(String userEmail, Pageable pageable) {
        String userId = null;
        if (userEmail != null && !userEmail.isEmpty()) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            userId = user.getId();
        }

        Page<Video> videoPage = videoRepository.findByStatus(VideoStatus.ACTIVE, pageable);

        String finalUserId = userId;
        List<VideoResponse> responses = videoPage.getContent().stream()
                .map(video -> videoService.mapToResponse(video, finalUserId))
                .collect(Collectors.toList());

        return new PageResponse<>(
                responses,
                videoPage.getNumber(),
                videoPage.getSize(),
                videoPage.getTotalElements(),
                videoPage.getTotalPages(),
                videoPage.isLast()
        );
    }
    
    public PageResponse<VideoResponse> getFollowingFeed(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
                
        List<User> following = userFollowRepository.findAllByFollower(user).stream()
                .map(follow -> follow.getFollowing())
                .collect(Collectors.toList());

        Page<Video> videoPage;
        if (following.isEmpty()) {
            videoPage = Page.empty(pageable);
        } else {
            videoPage = videoRepository.findByCreatorInAndStatus(following, VideoStatus.ACTIVE, pageable);
        }

        String finalUserId = user.getId();
        List<VideoResponse> responses = videoPage.getContent().stream()
                .map(video -> videoService.mapToResponse(video, finalUserId))
                .collect(Collectors.toList());

        return new PageResponse<>(
                responses,
                videoPage.getNumber(),
                videoPage.getSize(),
                videoPage.getTotalElements(),
                videoPage.getTotalPages(),
                videoPage.isLast()
        );
    }

    public PageResponse<VideoResponse> getTrendingFeed(String userEmail, Pageable pageable) {
        String userId = null;
        if (userEmail != null && !userEmail.isEmpty()) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            userId = user.getId();
        }

        Page<Video> videoPage = videoRepository.findByStatusOrderByViewCountDesc(VideoStatus.ACTIVE, pageable);

        String finalUserId = userId;
        List<VideoResponse> responses = videoPage.getContent().stream()
                .map(video -> videoService.mapToResponse(video, finalUserId))
                .collect(Collectors.toList());

        return new PageResponse<>(
                responses,
                videoPage.getNumber(),
                videoPage.getSize(),
                videoPage.getTotalElements(),
                videoPage.getTotalPages(),
                videoPage.isLast()
        );
    }
    
    public PageResponse<VideoResponse> getUserVideos(String targetUserId, String userEmail, Pageable pageable) {
        String currentUserId = null;
        if (userEmail != null && !userEmail.isEmpty()) {
             User u = userRepository.findByEmail(userEmail).orElse(null);
             if (u != null) {
                 currentUserId = u.getId();
             }
        }
        
        Page<Video> videoPage = videoRepository.findByCreatorIdAndStatus(targetUserId, VideoStatus.ACTIVE, pageable);
        
        String finalUserId = currentUserId;
        List<VideoResponse> responses = videoPage.getContent().stream()
                .map(video -> videoService.mapToResponse(video, finalUserId))
                .collect(Collectors.toList());

        return new PageResponse<>(
                responses,
                videoPage.getNumber(),
                videoPage.getSize(),
                videoPage.getTotalElements(),
                videoPage.getTotalPages(),
                videoPage.isLast()
        );
    }

    public PageResponse<VideoResponse> getVideosByHashtag(String hashtag, String userEmail, Pageable pageable) {
        String currentUserId = null;
        if (userEmail != null && !userEmail.isEmpty()) {
            User u = userRepository.findByEmail(userEmail).orElse(null);
            if (u != null) {
                currentUserId = u.getId();
            }
        }

        String searchTag = "#" + hashtag;
        Page<Video> videoPage = videoRepository.findByCaptionContainingIgnoreCaseAndStatus(searchTag, VideoStatus.ACTIVE, pageable);

        String finalUserId = currentUserId;
        List<VideoResponse> responses = videoPage.getContent().stream()
                .map(video -> videoService.mapToResponse(video, finalUserId))
                .collect(Collectors.toList());

        return new PageResponse<>(
                responses,
                videoPage.getNumber(),
                videoPage.getSize(),
                videoPage.getTotalElements(),
                videoPage.getTotalPages(),
                videoPage.isLast()
        );
    }

    public PageResponse<HashtagResponse> getTrendingHashtags(Pageable pageable) {
        Page<com.ispilo.model.entity.Hashtag> hashtagPage = hashtagRepository.findTrendingHashtags(pageable);
        
        List<HashtagResponse> responses = hashtagPage.getContent().stream()
                .map(hashtag -> HashtagResponse.builder()
                        .id(hashtag.getId())
                        .name(hashtag.getName())
                        .usageCount(hashtag.getUsageCount())
                        .build())
                .collect(Collectors.toList());
                
        return new PageResponse<>(
                responses,
                hashtagPage.getNumber(),
                hashtagPage.getSize(),
                hashtagPage.getTotalElements(),
                hashtagPage.getTotalPages(),
                hashtagPage.isLast()
        );
    }
}
