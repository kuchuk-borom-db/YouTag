package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.api.events.VideoDeletedEvent;
import dev.kuku.youtagserver.video.api.events.VideoUpdatedEvent;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.video.domain.Video;
import dev.kuku.youtagserver.video.infrastructure.VideoRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VideoServiceImpl implements VideoService {

    private final VideoRepo videoRepo;
    private final ApplicationEventPublisher eventPublisher;

    // Cache to store video data for faster access
    private final Map<String, Video> cache = new ConcurrentHashMap<>();

    /**
     * Initializes the video service by loading cache with existing data if necessary.
     */
    @PostConstruct
    void setup() {
        log.debug("Initialized cache for video service");
    }

    /**
     * Retrieves a video by ID, either from the cache or database if not cached.
     */
    @Override
    public VideoDTO getVideoInfo(String id) throws VideoNotFound {
        log.debug("Fetching video with id {}", id);

        // Attempt to fetch video from cache
        Video video = cache.get(id);

        // If video is not in cache, retrieve from repository
        if (video == null) {
            log.debug("Video with id {} not found in cache, retrieving from repository", id);
            video = videoRepo.findById(id).orElseThrow(() -> new VideoNotFound(id));
            log.debug("Video with id {} retrieved from repository, storing in cache", id);
            cache.put(id, video); // Store video in cache for future requests
        } else {
            log.debug("Video with id {} retrieved from cache", id);
        }

        // Convert to DTO and return
        return toDto(video);
    }

    @Override
    public List<VideoDTO> getVideoInfos(List<String> ids) {
        log.debug("Getting info of videos {}", ids);
        return videoRepo.findAllByIdIn(ids).stream().map(video -> {
            try {
                return toDto(video);
            } catch (VideoDTOHasNullValues _) {

            }
            return null;
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public void addVideo(VideoDTO video) throws VideoAlreadyExists {
        try {
            getVideoInfo(video.getId());
            throw new VideoAlreadyExists(video.getId());
        } catch (VideoNotFound | VideoDTOHasNullValues _) {
        }
        log.debug("Adding new video by dto {}", video);
        videoRepo.save(new Video(video.getId(), video.getTitle(), video.getDescription(), video.getThumbnail(), LocalDateTime.now()));
        eventPublisher.publishEvent(new VideoAddedEvent(video));
    }

    /**
     * Updates an existing video. Publishes an event for the update and updates cache.
     */
    @Override
    public void updateVideo(VideoDTO video) throws VideoNotFound, VideoDTOHasNullValues {
        log.debug("Updating video with id {}", video.getId());

        // Ensure the video exists before updating
        getVideoInfo(video.getId());

        // Update video details and save
        Video updatedVideo = new Video(video.getId(), video.getTitle(), video.getDescription(), video.getThumbnail(), LocalDateTime.now());
        videoRepo.save(updatedVideo);

        // Update the cache with the new video details
        cache.put(video.getId(), updatedVideo);
        log.debug("Updated video with id {} saved and cache updated", video.getId());

        // Publish event for video update
        VideoUpdatedEvent videoUpdatedEvent = new VideoUpdatedEvent(video);
        eventPublisher.publishEvent(videoUpdatedEvent);
    }

    /**
     * Deletes a video by ID, removes it from the cache, and publishes a deletion event.
     */
    @Override
    public void deleteVideo(String id) throws VideoNotFound, VideoDTOHasNullValues {
        log.debug("Deleting video with id {}", id);

        // Ensure video exists before deletion
        getVideoInfo(id);

        // Delete video entry
        videoRepo.deleteById(id);

        // Remove video from cache
        cache.remove(id);
        log.debug("Video with id {} deleted and removed from cache", id);

        // Publish deletion event
        VideoDeletedEvent videoDeletedEvent = new VideoDeletedEvent(id);
        eventPublisher.publishEvent(videoDeletedEvent);
    }

    /**
     * Converts a Video entity to a VideoDTO.
     */
    @Override
    public VideoDTO toDto(Video e) {
        log.debug("Converting Video entity to DTO for id {}", e.getId());
        return new VideoDTO(e.getId(), e.getTitle(), e.getDescription(), e.getThumbnail());
    }
}
