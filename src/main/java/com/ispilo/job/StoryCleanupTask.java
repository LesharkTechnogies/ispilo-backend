package com.ispilo.job;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ispilo.model.entity.Story;
import com.ispilo.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoryCleanupTask {

    private final StoryRepository storyRepository;
    private final Cloudinary cloudinary;

    // Runs every hour
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredStories() {
        log.info("Starting scheduled cleanup of expired stories...");
        LocalDateTime now = LocalDateTime.now();
        List<Story> expiredStories = storyRepository.findExpiredStories(now);

        if (expiredStories.isEmpty()) {
            log.info("No expired stories found to clean up.");
            return;
        }

        log.info("Found {} expired stories. Proceeding with cleanup.", expiredStories.size());

        for (Story story : expiredStories) {
            try {
                // Delete from Cloudinary
                // We use resource_type mapping if it's a video. Images are default.
                String resourceType = "IMAGE".equalsIgnoreCase(story.getMediaType()) ? "image" : "video";
                cloudinary.uploader().destroy(story.getPublicId(), ObjectUtils.asMap("resource_type", resourceType));
                
                // Mark as deleted
                story.setDeleted(true);
                storyRepository.save(story);
                
                log.info("Successfully deleted story {} with publicId {}", story.getId(), story.getPublicId());
            } catch (Exception e) {
                log.error("Failed to delete story {} from Cloudinary. Error: {}", story.getId(), e.getMessage());
            }
        }
        
        log.info("Completed cleanup of expired stories.");
    }
}
