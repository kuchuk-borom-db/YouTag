package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import dev.kuku.youtagserver.video.api.exceptions.VideoAlreadyExists;
import dev.kuku.youtagserver.video.api.exceptions.VideoNotFound;
import dev.kuku.youtagserver.video.api.services.VideoService;
import dev.kuku.youtagserver.video.domain.Video;
import dev.kuku.youtagserver.video.infrastructure.VideoRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VideoServiceImpl implements VideoService {

    private final VideoRepo videoRepo;
    private final Map<String, VideoDTO> cache = new ConcurrentHashMap<>();

    /**
     * Generates a cache key for a video based on its ID.
     */
    private String generateCacheKey(String videoId) {
        return "video:" + videoId;
    }

    /**
     * Evicts a specific video from the cache.
     */
    private void evictCache(String videoId) {
        String cacheKey = generateCacheKey(videoId);
        cache.remove(cacheKey);
        log.debug("Evicted cache entry for video {}", videoId);
    }

    @PostConstruct
    void setup() {
        log.debug("Initialized cache for video service");
    }

    @Override
    public VideoDTO getVideoInfo(String id) throws VideoNotFound {
        log.debug("Fetching video with id {}", id);
        String cacheKey = generateCacheKey(id);

        var vidInfo = cache.get(cacheKey);

        if (vidInfo == null) {
            var repoVid = videoRepo.findById(id);
            if (repoVid.isEmpty()) {
                throw new VideoNotFound(id);
            } else {
                vidInfo = toDto(repoVid.get());
                cache.put(cacheKey, vidInfo);
            }
        }
        return vidInfo;
    }

    @Override
    public List<VideoDTO> getVideoInfos(List<String> ids) {
        log.debug("Fetching videos with ids {}", ids);
        return videoRepo.findAllByIdIn(ids).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void addVideo(VideoDTO video) throws VideoAlreadyExists {
        try {
            getVideoInfo(video.getId());
            throw new VideoAlreadyExists(video.getId());
        } catch (VideoNotFound e) {
            log.debug("Adding new video by dto {}", video);
            Video newVideo = new Video(
                    video.getId(),
                    video.getTitle(),
                    video.getDescription(),
                    video.getThumbnail(),
                    LocalDateTime.now()
            );
            videoRepo.save(newVideo);
            evictCache(video.getId());
        }
    }

    @Override
    public void addVideos(List<String> ids) {
        log.debug("Adding videos {}", ids);
        videoRepo.saveAll(ids.stream().map(s -> new Video(s, "NA", "NA", "NA", LocalDateTime.now())).collect(Collectors.toList()));
    }

    @Override
    public void updateVideo(VideoDTO video) throws VideoNotFound {
        log.debug("Updating video with id {}", video.getId());

        // Ensure the video exists before updating
        getVideoInfo(video.getId());

        // Update video details and save
        Video updatedVideo = new Video(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getThumbnail(),
                LocalDateTime.now()
        );
        videoRepo.save(updatedVideo);

        // Evict the cached entry
        evictCache(video.getId());
        log.debug("Updated video with id {} saved and cache evicted", video.getId());
    }

    @Override
    public void updateVideos(List<VideoDTO> videos) {
        log.debug("Updating videos {}", videos);
        videoRepo.saveAll(videos.stream().map(videoDTO -> new Video(videoDTO.getId(), videoDTO.getTitle(), videoDTO.getDescription(), videoDTO.getThumbnail(), LocalDateTime.now())).collect(Collectors.toList()));
    }

    @Override
    public void deleteSpecifiedVideos(Set<String> videoIds) {
        log.debug("Deleting videos {}", videoIds);
        videoRepo.deleteAllById(videoIds);
    }


    @Override
    public VideoDTO toDto(Video e) {
        log.debug("Converting Video entity to DTO for id {}", e.getId());
        return new VideoDTO(e.getId(), e.getTitle(), e.getDescription(), e.getThumbnail());
    }
}