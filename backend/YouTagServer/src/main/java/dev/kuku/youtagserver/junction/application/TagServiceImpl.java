package dev.kuku.youtagserver.junction.application;

import dev.kuku.youtagserver.junction.api.dtos.TagDTO;
import dev.kuku.youtagserver.junction.api.events.AddedTagsToVideoEvent;
import dev.kuku.youtagserver.junction.api.exceptions.TagDTOHasNullValues;
import dev.kuku.youtagserver.junction.api.services.TagService;
import dev.kuku.youtagserver.junction.domain.Tag;
import dev.kuku.youtagserver.junction.infrastructure.TagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepo repo;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, List<TagDTO>> cache = new ConcurrentHashMap<>();

    /**
     * Generates a unique key for caching based on userId, videos, tags, skip, and limit parameters.
     * This ensures each unique request has a unique cache entry.
     */
    private String generateCacheKey(String userId, List<String> videos, List<String> tags, int skip, int limit) {
        return userId + ":" + (videos != null ? videos.toString() : "") + ":" +
                (tags != null ? tags.toString() : "") + ":" + skip + ":" + limit;
    }

    /**
     * Evicts cache entries related to a specific user. This method is called before modifications
     * (additions or deletions) to ensure stale data is removed from the cache.
     */
    private void evictCache(String userId) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
        log.debug("Evicted cache entries for user {}", userId);
    }


    /**
     * Converts a Junction entity to a JunctionDTO, throwing an exception if any required field is null.
     */
    @Override
    public TagDTO toDto(Tag e) throws TagDTOHasNullValues {
        return new TagDTO(e.getUserId(), e.getVideoId(), e.getTag());
    }

    /**
     * Converts a list of Junction entities to a list of JunctionDTOs.
     */
    private List<TagDTO> toDtoList(List<Tag> tags) {
        return tags.stream()
                .map(tag -> {
                    try {
                        return toDto(tag); // Convert to DTO
                    } catch (TagDTOHasNullValues e) {
                        log.error("Error converting Junction to JunctionDTO: {}", e.getMessage());
                        return null; // Return null if conversion fails
                    }
                })
                .filter(Objects::nonNull) // Filter out null results
                .collect(Collectors.toList());
    }

    /**
     * Processes tags by trimming whitespace, converting to lowercase, and filtering out invalid tags (e.g., "*").
     *
     * @param tags the list of tags to process
     * @return a list of validated and processed tags
     */
    private List<String> validateAndProcessTags(List<String> tags) {
        return tags.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    @Override
    public void addTagsToVideo(String userId, String videoId, List<String> tags) {
        log.debug("Adding tags {} to video {} for user {}", tags, videoId, userId);
        tags = validateAndProcessTags(tags);
        List<Tag> tagsEntity = new ArrayList<>();
        for (var t : tags) {
            tagsEntity.add(new Tag(UUID.randomUUID().toString(), userId, videoId, t));
        }
        repo.saveAll(tagsEntity);
        eventPublisher.publishEvent(new AddedTagsToVideoEvent(tagsEntity));
    }

    @Override
    public List<TagDTO> getAllTagsOfUser(String userId, int skip, int limit) {
        log.debug("Getting tags of user {} skip {} and limit {}", userId, skip, limit);
        int pageNum = skip / limit;
        return toDtoList(repo.findAllByUserId(userId, PageRequest.of(pageNum, limit)));
    }

    @Override
    public List<TagDTO> getTagsOfVideo(String userId, String videoId) {
        log.debug("Getting tags of video {}", videoId);
        List<Tag> tags = repo.findAllByUserIdAndVideoId(userId, videoId);
        return toDtoList(tags);
    }

    @Override
    public List<TagDTO> getVideosWithTag(String userId, List<String> tags, int skip, int limit) {
        log.debug("Getting videos of user {} with tag {}, skipping {} and limit to {}", userId, tags, skip, limit);
        tags = validateAndProcessTags(tags);
        int pageNumber = skip / limit;
        var videos = repo.findAllByUserIdAndTagIn(userId, tags, PageRequest.of(pageNumber, limit));
        return toDtoList(videos);
    }

    @Override
    public void deleteTagsFromAllVideos(String userId, List<String> tags) {
        log.debug("Deleting tags {} from all videos for user {}", tags, userId);
        tags = validateAndProcessTags(tags);
        repo.deleteAllByUserIdAndTagIn(userId, tags);
    }

    @Override
    public void deleteTagsFromVideos(String userId, List<String> tags, List<String> videoIds) {
        log.debug("Deleting tags {} from videos {} for user {}", tags, videoIds, userId);
        tags = validateAndProcessTags(tags);
        repo.deleteAllByUserIdAndVideoIdInAndTagIn(userId, videoIds, tags);
    }
}
