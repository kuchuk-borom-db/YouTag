package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.api.events.VideoDeletedEvent;
import dev.kuku.youtagserver.video.api.events.VideoUpdatedEvent;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoId;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.video.domain.Video;
import dev.kuku.youtagserver.video.infrastructure.VideoRepo;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VideoServiceImpl implements VideoService {
    final VideoRepo videoRepo;
    final ApplicationEventPublisher eventPublisher;
    final CacheManager cacheManager;
    Cache cacheStore;
    final YoutubeScrapperService scrapperService;

    @PostConstruct
    void setup() {
        // Initialize the cache for video data
        cacheStore = cacheManager.getCache(this.getClass().getName());
        log.debug("Initialized cache for {}", this.getClass().getName());
    }

    @Override
    public VideoDTO getVideo(String id) throws VideoNotFound, VideoDTOHasNullValues {
        log.debug("Fetching video with id {}", id);

        // Attempt to fetch video from cache
        Cache.ValueWrapper cacheVal = cacheStore.get(id);
        Video video = cacheVal != null ? (Video) cacheVal.get() : null;

        // If video is not in cache or casting fails, retrieve from repository
        if (video == null) {
            log.debug("Video with id {} not found in cache, retrieving from repository", id);
            video = videoRepo.findById(id).orElseThrow(() -> new VideoNotFound(id));
            log.debug("Video with id {} retrieved from repository, storing in cache", id);
            cacheStore.put(id, video);
        } else {
            log.debug("Video with id {} retrieved from cache", id);
        }

        // Convert to DTO and return
        log.debug("Converting video with id {} to DTO", id);
        return toDto(video);
    }

    @Override
    public void addVideo(String id) throws VideoAlreadyExists, InvalidVideoId, VideoDTOHasNullValues {
        log.info("Adding video with id {}", id);

        // Check if video already exists
        try {
            getVideo(id);
            throw new VideoAlreadyExists(id);
        } catch (VideoNotFound ex) {
            log.debug("No existing video with id {}, proceeding with addition", id);
        }

        // Validate video ID with the scrapper service
        log.debug("Validating video ID with YouTubeScrapperService");
        if (!scrapperService.validateVideo(id)) {
            throw new InvalidVideoId(id);
        }

        // Create and save new video entry
        log.debug("Creating new video entry in repository for id {}", id);
        Video newVideo = new Video(id, "NA", "NA", "NA", LocalDateTime.now());
        videoRepo.save(newVideo);

        // Publish event for added video
        VideoAddedEvent videoAddedEvent = new VideoAddedEvent(id);
        log.debug("Publishing VideoAddedEvent for id {}", id);
        eventPublisher.publishEvent(videoAddedEvent);
    }

    @Override
    public void updateVideo(VideoDTO video) throws VideoNotFound, VideoDTOHasNullValues {
        log.debug("Updating video with id {}", video.getId());

        // Ensure the video exists before updating
        getVideo(video.getId());

        // Update video details and save
        Video updatedVideo = new Video(video.getId(), video.getTitle(), video.getDescription(), video.getThumbnail(), LocalDateTime.now());
        log.debug("Saving updated video with id {}", updatedVideo.getId());
        videoRepo.save(updatedVideo);

        // Publish event for video update
        VideoUpdatedEvent videoUpdatedEvent = new VideoUpdatedEvent(video);
        log.debug("Publishing VideoUpdatedEvent for id {}", video.getId());
        eventPublisher.publishEvent(videoUpdatedEvent);
    }

    @Override
    public void deleteVideo(String id) throws VideoNotFound, VideoDTOHasNullValues {
        log.debug("Deleting video with id {}", id);

        // Ensure video exists before deletion
        getVideo(id);

        // Delete video entry
        log.debug("Removing video with id {} from repository", id);
        videoRepo.deleteById(id);

        // Publish deletion event
        VideoDeletedEvent videoDeletedEvent = new VideoDeletedEvent(id);
        log.debug("Publishing VideoDeletedEvent for id {}", id);
        eventPublisher.publishEvent(videoDeletedEvent);
    }

    @Override
    public VideoDTO toDto(Video e) throws VideoDTOHasNullValues {
        log.debug("Converting Video entity to DTO for id {}", e.getId());
        return new VideoDTO(e.getId(), e.getTitle(), e.getDescription(), e.getThumbnail(), e.getUpdated());
    }
}
