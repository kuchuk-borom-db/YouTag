package dev.kuku.youtagserver.video.application.services;

import dev.kuku.youtagserver.shared.helper.CacheSystem;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.events.VideoAddedEvent;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoIDException;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.video.domain.entity.Video;
import dev.kuku.youtagserver.video.infrastructure.repo.VideoRepo;
import dev.kuku.youtagserver.webscraper.api.services.YoutubeScrapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    final YoutubeScrapperService youtubeScrapperService;
    final CacheSystem cacheSystem;

    @Override
    public VideoDTO getVideo(String id) throws VideoNotFound {
        log.info("Getting video {}", id);
        Video video = (Video) cacheSystem.getObject("vid", id);
        if (video == null) {
            video = videoRepo.findById(id).orElse(null);
            cacheSystem.cache("vid", id, video);
        }
        if (video == null) {
            throw new VideoNotFound(id);
        }
        return toDTO(video);
    }

    @Override
    public VideoDTO addVideo(String id) throws VideoAlreadyExists, InvalidVideoIDException {
        log.info("Adding video: {}", id);
        var vid = videoRepo.findById(id);
        if (vid.isPresent()) {
            throw new VideoAlreadyExists(id);
        }
        if (!youtubeScrapperService.validateVideo(id)) {
            throw new InvalidVideoIDException(id);
        }
        var saved = videoRepo.save(new Video(id, "NA", "NA", "NA", LocalDateTime.now()));
        eventPublisher.publishEvent(new VideoAddedEvent(id));
        return toDTO(saved);
    }

    private VideoDTO toDTO(Video video) {
        if (video == null) {
            return null;
        }
        return new VideoDTO(video.getId(), video.getTitle(), video.getDescription(), video.getThumbnail(), video.getUpdated());
    }


}
