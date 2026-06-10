package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.CreateVideoRequest;
import com.ispilo.model.dto.request.VideoUploadInitiateRequest;
import com.ispilo.model.dto.response.UploadUrlResponse;
import com.ispilo.model.dto.response.UserProfileResponse;
import com.ispilo.model.dto.response.VideoResponse;
import com.ispilo.model.entity.Hashtag;
import com.ispilo.model.entity.User;
import com.ispilo.model.entity.Video;
import com.ispilo.model.enums.VideoStatus;
import com.ispilo.repository.HashtagRepository;
import com.ispilo.repository.UserRepository;
import com.ispilo.repository.VideoLikeRepository;
import com.ispilo.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    private final VideoLikeRepository videoLikeRepository;
    private final VideoProcessingService videoProcessingService;
    private final R2Service r2Service;

    @Transactional
    public UploadUrlResponse initiateUpload(String userEmail, VideoUploadInitiateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Video video = Video.builder()
                .creator(user)
                .caption(request.getCaption())
                .durationSeconds(request.getDurationSeconds() != null ? request.getDurationSeconds().intValue() : 0)
                .status(VideoStatus.UPLOADING)
                .build();
                
        video = videoRepository.saveAndFlush(video);
        
        String videoId = video.getId();
        String objectKey = "videos/" + user.getId() + "/" + videoId + ".mp4"; // Assuming mp4 for now
        
        String uploadUrl = r2Service.generatePresignedUploadUrl(objectKey, request.getContentType());
        
        video.setVideoUrl(r2Service.getPublicUrl(objectKey));
        video = videoRepository.save(video);
        
        // Process hashtags early or wait until complete
        if (request.getHashtags() != null) {
            for (String tag : request.getHashtags()) {
                String cleanTag = tag.replace("#", "").trim().toLowerCase();
                if (!cleanTag.isEmpty()) {
                    Hashtag hashtag = hashtagRepository.findByNameIgnoreCase(cleanTag)
                            .orElse(Hashtag.builder().name(cleanTag).usageCount(0L).build());
                    hashtag.setUsageCount(hashtag.getUsageCount() + 1);
                    hashtagRepository.save(hashtag);
                }
            }
        }
        
        return UploadUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .videoId(video.getId())
                .build();
    }

    @Transactional
    public void completeUpload(String userEmail, String videoId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
                
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException("Video not found"));
                
        if (!video.getCreator().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to complete this upload");
        }
        
        video.setStatus(VideoStatus.PROCESSING);
        videoRepository.save(video);
        
        videoProcessingService.processVideo(videoId);
    }

    @Transactional
    public VideoResponse createVideo(String userEmail, CreateVideoRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Video video = Video.builder()
                .creator(user)
                .caption(request.getCaption())
                .videoUrl(request.getVideoUrl())
                .thumbnailUrl(request.getThumbnailUrl())
                .previewImageUrl(request.getPreviewImageUrl())
                .durationSeconds(request.getDurationSeconds())
                .status(VideoStatus.UPLOADING)
                .build();

        video = videoRepository.save(video);

        // Process hashtags
        if (request.getHashtags() != null) {
            for (String tag : request.getHashtags()) {
                String cleanTag = tag.replace("#", "").trim().toLowerCase();
                if (!cleanTag.isEmpty()) {
                    Hashtag hashtag = hashtagRepository.findByNameIgnoreCase(cleanTag)
                            .orElse(Hashtag.builder().name(cleanTag).usageCount(0L).build());
                    hashtag.setUsageCount(hashtag.getUsageCount() + 1);
                    hashtagRepository.save(hashtag);
                }
            }
        }

        // Kick off async processing
        videoProcessingService.processVideo(video.getId());

        return mapToResponse(video, user.getId());
    }

    public VideoResponse getVideo(String videoId, String userEmail) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException("Video not found"));

        String userId = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            userId = user.getId();
        }

        return mapToResponse(video, userId);
    }

    @Transactional
    public void deleteVideo(String videoId, String userEmail) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException("Video not found"));
                
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!video.getCreator().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to delete this video");
        }

        videoRepository.delete(video);
    }
    
    @Transactional
    public void incrementViewCount(String videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException("Video not found"));
        video.setViewCount(video.getViewCount() + 1);
        videoRepository.save(video);
    }

    @Transactional
    public void incrementShareCount(String videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException("Video not found"));
        video.setSharesCount(video.getSharesCount() + 1);
        videoRepository.save(video);
    }

    public VideoResponse mapToResponse(Video video, String currentUserId) {
        Boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = videoLikeRepository.existsByUserIdAndVideoId(currentUserId, video.getId());
        }

        UserProfileResponse creatorResponse = UserProfileResponse.builder()
                .id(video.getCreator().getId())
                .name(video.getCreator().getFirstName() + " " + video.getCreator().getLastName())
                .avatar(video.getCreator().getAvatar())
                // .isVerified(...) could be added based on User model
                .build();

        return VideoResponse.builder()
                .id(video.getId())
                .creator(creatorResponse)
                .caption(video.getCaption())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .previewImageUrl(video.getPreviewImageUrl())
                .durationSeconds(video.getDurationSeconds())
                .status(video.getStatus())
                .viewCount(video.getViewCount())
                .likesCount(video.getLikesCount())
                .commentsCount(video.getCommentsCount())
                .sharesCount(video.getSharesCount())
                .isLiked(isLiked)
                .createdAt(video.getCreatedAt())
                .build();
    }
}
