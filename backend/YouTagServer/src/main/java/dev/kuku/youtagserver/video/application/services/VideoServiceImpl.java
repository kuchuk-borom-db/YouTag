package dev.kuku.youtagserver.video.application.services;

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
    final CacheManager _cacheManager;
    Cache cacheStore;
    final YoutubeScrapperService scrapperService;

    @PostConstruct
    void setup() {
        cacheStore = _cacheManager.getCache(this.getClass().getName());
        log.debug("Saving cache store for class {}", this.getClass().getName());
    }

    @Override
    public VideoDTO getVideo(String id) throws VideoNotFound, VideoDTOHasNullValues {
        log.debug("Attempting to get video with id {}", id);
        Video video;

        //Get video from cache
        var cacheVal = cacheStore.get(id);

        //If not found in cache, get it from repo. Throw exception if not found.
        if (cacheVal == null || cacheVal.get() == null) {
            log.debug("Failed to get video with id {} from cache. Getting it from repository", id);
            video = videoRepo.findById(id).orElseThrow(() -> new VideoNotFound(id));
            log.debug("Found video {} in repository. Adding it in cache", video);

            //Cache the video we got from repo
            cacheStore.put(id, video);
        } else {
            log.debug("Found video {} in cache.", id);
            video = (Video) cacheVal.get();
            //If for some reason casting then attempt to access from repo.
            if (video == null) {
                log.debug("Failed to cast video with id {} from cache. Getting it from repository", id);
                video = videoRepo.findById(id).orElseThrow(() -> new VideoNotFound(id));

                //Cache the video we got from repo
                log.debug("Found video {} in repository. Adding it in cache", video);
                cacheStore.put(id, video);
            }
        }
        log.debug("Found video with id {} : {}", id, video);
        return toDto(video);
    }

    @Override
    public void addVideo(String id) throws VideoAlreadyExists, InvalidVideoId, VideoDTOHasNullValues {
        log.info("Attempting to add video with id {}", id);
        //Check if video exists
        try {
            getVideo(id);
            throw new VideoAlreadyExists(id);
        } catch (VideoNotFound _) {
            log.debug("No existing video with id {}. Continuing adding the video", id);
        }

        //Validate videoID using scrapper
        log.debug("Validating video ID");
        if (!scrapperService.validateVideo(id)) throw new InvalidVideoId(id);

        //Save the video
        log.debug("Adding video to repo {}", id);
        videoRepo.save(new Video(id, "NA", "NA", "NA", LocalDateTime.now()));
        var videoToAdd = new VideoAddedEvent(id);

        //Publish video added event
        log.debug("Publishing event of added video {}", videoToAdd);
        eventPublisher.publishEvent(videoToAdd);
    }

    @Override
    public void updateVideo(VideoDTO video) throws VideoNotFound, VideoDTOHasNullValues {
        log.debug("Attempting to update video {}", video);
        getVideo(video.getId());
        var updated = new Video(video.getId(), video.getTitle(), video.getDescription(), video.getThumbnail(), LocalDateTime.now());
        log.debug("Updating video {}", updated);
        videoRepo.save(updated);
        eventPublisher.publishEvent(new VideoUpdatedEvent(video));
    }

    @Override
    public void deleteVideo(String id) throws VideoNotFound, VideoDTOHasNullValues {
        log.debug("Attempting to delete video {}", id);
        //Validate that the video exists
        getVideo(id);

        //Delete video
        log.debug("Deleting video {}", id);
        videoRepo.deleteById(id);

        //Publish deleted event
        eventPublisher.publishEvent(new VideoDeletedEvent(id));
    }

    @Override
    public VideoDTO toDto(Video e) throws VideoDTOHasNullValues {
        return new VideoDTO(e.getId(), e.getTitle(), e.getDescription(), e.getThumbnail(), e.getUpdated());
    }
}
