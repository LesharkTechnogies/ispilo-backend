package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.VideoCommentRequest;
import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.model.dto.response.UserProfileResponse;
import com.ispilo.model.dto.response.VideoCommentResponse;
import com.ispilo.model.entity.User;
import com.ispilo.model.entity.Video;
import com.ispilo.model.entity.VideoComment;
import com.ispilo.model.entity.VideoCommentLike;
import com.ispilo.repository.UserRepository;
import com.ispilo.repository.VideoCommentLikeRepository;
import com.ispilo.repository.VideoCommentRepository;
import com.ispilo.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoCommentService {

    private final VideoCommentRepository videoCommentRepository;
    private final VideoCommentLikeRepository videoCommentLikeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    @Transactional
    public VideoCommentResponse addComment(String videoId, String userEmail, VideoCommentRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException("Video not found"));

        VideoComment parentComment = null;
        if (request.getParentCommentId() != null && !request.getParentCommentId().isEmpty()) {
            parentComment = videoCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
        }

        VideoComment comment = VideoComment.builder()
                .user(user)
                .video(video)
                .parentComment(parentComment)
                .content(request.getContent())
                .build();

        comment = videoCommentRepository.save(comment);

        video.setCommentsCount(video.getCommentsCount() + 1);
        videoRepository.save(video);

        return mapToResponse(comment, user.getId());
    }

    public PageResponse<VideoCommentResponse> getComments(String videoId, String userEmail, Pageable pageable) {
        String currentUserId = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                currentUserId = user.getId();
            }
        }
        
        Page<VideoComment> commentPage = videoCommentRepository.findByVideoIdAndParentCommentIsNull(videoId, pageable);
        String finalCurrentUserId = currentUserId;
        List<VideoCommentResponse> responses = commentPage.getContent().stream()
                .map(comment -> mapToResponse(comment, finalCurrentUserId))
                .collect(Collectors.toList());

        return new PageResponse<>(
                responses,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isLast()
        );
    }
    
    public PageResponse<VideoCommentResponse> getReplies(String commentId, String userEmail, Pageable pageable) {
        String currentUserId = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                currentUserId = user.getId();
            }
        }

        Page<VideoComment> commentPage = videoCommentRepository.findByParentCommentId(commentId, pageable);
        String finalCurrentUserId = currentUserId;
        List<VideoCommentResponse> responses = commentPage.getContent().stream()
                .map(comment -> mapToResponse(comment, finalCurrentUserId))
                .collect(Collectors.toList());

        return new PageResponse<>(
                responses,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isLast()
        );
    }

    @Transactional
    public void toggleCommentLike(String commentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        VideoComment comment = videoCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        Optional<VideoCommentLike> existingLike = videoCommentLikeRepository.findByUserIdAndCommentId(user.getId(), comment.getId());

        if (existingLike.isPresent()) {
            videoCommentLikeRepository.delete(existingLike.get());
            comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
        } else {
            VideoCommentLike like = VideoCommentLike.builder()
                    .user(user)
                    .comment(comment)
                    .build();
            videoCommentLikeRepository.save(like);
            comment.setLikesCount((comment.getLikesCount() != null ? comment.getLikesCount() : 0) + 1);
        }

        videoCommentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(String commentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        VideoComment comment = videoCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId()) && !comment.getVideo().getCreator().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to delete this comment");
        }

        Video video = comment.getVideo();
        video.setCommentsCount(Math.max(0, video.getCommentsCount() - 1));
        videoRepository.save(video);

        videoCommentRepository.delete(comment);
    }

    private VideoCommentResponse mapToResponse(VideoComment comment, String currentUserId) {
        UserProfileResponse userResponse = UserProfileResponse.builder()
                .id(comment.getUser().getId())
                .name(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .avatar(comment.getUser().getAvatar())
                .build();

        Boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = videoCommentLikeRepository.existsByUserIdAndCommentId(currentUserId, comment.getId());
        }

        return VideoCommentResponse.builder()
                .id(comment.getId())
                .videoId(comment.getVideo().getId())
                .user(userResponse)
                .content(comment.getContent())
                .likesCount(comment.getLikesCount() != null ? comment.getLikesCount() : 0)
                .isLiked(isLiked)
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
