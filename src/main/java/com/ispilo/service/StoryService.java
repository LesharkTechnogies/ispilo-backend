package com.ispilo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ispilo.exception.BadRequestException;
import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.CreateStoryRequest;
import com.ispilo.model.dto.response.StoryResponse;
import com.ispilo.model.dto.response.UserStoryGroupResponse;
import com.ispilo.model.entity.Story;
import com.ispilo.model.entity.User;
import com.ispilo.repository.StoryRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    @Transactional
    public StoryResponse createStory(String username, CreateStoryRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        // Check the 30-story limit
        LocalDateTime now = LocalDateTime.now();
        List<Story> activeStories = storyRepository.findActiveStoriesByUser(user.getId(), now);
        if (activeStories.size() >= 30) {
            throw new BadRequestException("You have reached the maximum limit of 30 active stories.");
        }

        Story story = Story.builder()
                .user(user)
                .mediaUrl(request.getMediaUrl())
                .publicId(request.getPublicId())
                .mediaType(request.getMediaType())
                .expiresAt(now.plusHours(24))
                .build();

        story = storyRepository.save(story);
        return StoryResponse.fromEntity(story);
    }

    public List<UserStoryGroupResponse> getActiveStoriesGroupedByUser() {
        LocalDateTime now = LocalDateTime.now();
        List<Story> activeStories = storyRepository.findActiveStories(now);

        Map<User, List<Story>> grouped = activeStories.stream()
                .collect(Collectors.groupingBy(Story::getUser));

        List<UserStoryGroupResponse> result = new ArrayList<>();
        for (Map.Entry<User, List<Story>> entry : grouped.entrySet()) {
            User user = entry.getKey();
            List<Story> userStories = entry.getValue();
            
            userStories.sort(Comparator.comparing(Story::getCreatedAt));
            LocalDateTime latestStoryAt = userStories.get(userStories.size() - 1).getCreatedAt();

            List<StoryResponse> storyResponses = userStories.stream()
                    .map(StoryResponse::fromEntity)
                    .collect(Collectors.toList());

            result.add(UserStoryGroupResponse.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .userAvatar(user.getAvatar())
                    .stories(storyResponses)
                    .latestStoryAt(latestStoryAt)
                    .build());
        }

        result.sort((a, b) -> b.getLatestStoryAt().compareTo(a.getLatestStoryAt()));
        return result;
    }

    @Transactional
    public void deleteStory(String username, String storyId) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));

        if (!story.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this story");
        }

        try {
            // Actual deleting in Cloudinary
            String resourceType = "IMAGE".equalsIgnoreCase(story.getMediaType()) ? "image" : "video";
            cloudinary.uploader().destroy(story.getPublicId(), ObjectUtils.asMap("resource_type", resourceType));
            log.info("Successfully deleted story {} from Cloudinary", story.getPublicId());
        } catch (Exception e) {
            log.error("Failed to delete story {} from Cloudinary. Error: {}", story.getPublicId(), e.getMessage());
            // Proceed to delete from DB even if Cloudinary fails, or throw an error based on preference.
            // For now we log and remove the record so it disappears from the app.
        }

        story.setDeleted(true);
        storyRepository.save(story);
    }

    @Transactional
    public void deleteAllUserStories(String userId) {
        List<Story> userStories = storyRepository.findByUserId(userId);
        for (Story story : userStories) {
            try {
                String resourceType = "IMAGE".equalsIgnoreCase(story.getMediaType()) ? "image" : "video";
                cloudinary.uploader().destroy(story.getPublicId(), ObjectUtils.asMap("resource_type", resourceType));
                log.info("Successfully deleted story {} from Cloudinary during user deletion", story.getPublicId());
            } catch (Exception e) {
                log.error("Failed to delete story {} from Cloudinary during user deletion. Error: {}", story.getPublicId(), e.getMessage());
            }
        }
        storyRepository.deleteAll(userStories);
    }
}
