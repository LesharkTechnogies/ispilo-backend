package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.model.entity.User;
import com.ispilo.model.entity.Video;
import com.ispilo.model.entity.VideoLike;
import com.ispilo.repository.UserRepository;
import com.ispilo.repository.VideoLikeRepository;
import com.ispilo.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoLikeService {

    private final VideoLikeRepository videoLikeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    @Transactional
    public void toggleLike(String videoId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NotFoundException("Video not found"));

        Optional<VideoLike> existingLike = videoLikeRepository.findByUserIdAndVideoId(user.getId(), video.getId());

        if (existingLike.isPresent()) {
            videoLikeRepository.delete(existingLike.get());
            video.setLikesCount(Math.max(0, video.getLikesCount() - 1));
        } else {
            VideoLike like = VideoLike.builder()
                    .user(user)
                    .video(video)
                    .build();
            videoLikeRepository.save(like);
            video.setLikesCount(video.getLikesCount() + 1);
        }

        videoRepository.save(video);
    }
}
