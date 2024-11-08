package dev.kuku.youtagserver.junction.application;

import dev.kuku.youtagserver.junction.api.dtos.JunctionDTO;
import dev.kuku.youtagserver.junction.api.events.JunctionAddedEvent;
import dev.kuku.youtagserver.junction.api.events.JunctionDeletedEvent;
import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import dev.kuku.youtagserver.junction.api.services.JunctionService;
import dev.kuku.youtagserver.junction.domain.Junction;
import dev.kuku.youtagserver.junction.infrastructure.JunctionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JunctionServiceImpl implements JunctionService {

    private final JunctionRepo repo;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, List<JunctionDTO>> cache = new ConcurrentHashMap<>();

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
     * Adds multiple videos with associated tags for a user. Each video is associated with each tag
     * in a flat structure. This method also evicts the user's cache to ensure consistency.
     */
    @Override
    public void addVideosWithTags(String userId, List<String> videos, List<String> tags) {
        tags = validateAndProcessTags(tags);
        log.debug("Attempting to add videos {} with tags {} for user {}", videos, tags, userId);

        // Evict cache for user before modification
        evictCache(userId);

        // Creates a list of junctions by combining each video with each tag
        List<Junction> junctions = tags.stream()
                .flatMap(tag -> videos.stream()
                        .map(videoId -> new Junction(UUID.randomUUID().toString(), userId, videoId, tag)))
                .toList();

        for (Junction junction : junctions) {
            try {
                repo.save(junction);
                log.debug("Saved junction for user {}, video {}, and tag {}", junction.getUserId(), junction.getVideoId(), junction.getTag());
            } catch (DataIntegrityViolationException e) {
                log.warn("Duplicate record detected, skipping: {}", e.getMessage());
                // Handle the exception, e.g., log a warning and continue
            }
        }
        publishAddedEvents(junctions);
    }

    /**
     * Deletes all videos and tags for a given user, then evicts the cache and publishes deletion events.
     */
    @Override
    public void deleteAllVideosAndTags(String userId) {
        log.debug("Deleting all videos and tags of user {}", userId);

        // Evict cache for user before modification
        evictCache(userId);

        // Delete all junctions for the user
        List<Junction> deleted = repo.deleteAllByUserId(userId);
        log.info("Deleted {} junctions for user {}", deleted.size(), userId);

        // Publish deletion events
        publishDeletedEvents(deleted);
    }

    /**
     * Deletes specified tags from all videos of a user, evicts the cache, and publishes deletion events.
     */
    @Override
    public void deleteTagsFromAllVideos(String userId, List<String> tags) {
        tags = validateAndProcessTags(tags);
        log.debug("Deleting tags {} from all videos for user {}", tags, userId);

        // Evict cache for user before modification
        evictCache(userId);

        // Delete all junctions with specified tags for the user
        List<Junction> deleted = repo.deleteAllByUserIdAndTagIn(userId, tags);

        log.info("Deleted {} junctions with specified tags for user {}", deleted.size(), userId);

        // Publish deletion events
        publishDeletedEvents(deleted);
    }

    /**
     * Deletes specified tags from specific videos of a user, evicts the cache, and publishes deletion events.
     */
    @Override
    public void deleteTagsFromVideos(String userId, List<String> videos, List<String> tags) {
        tags = validateAndProcessTags(tags);
        log.debug("Deleting tags {} from videos {} for user {}", tags, videos, userId);

        // Evict cache for user before modification
        evictCache(userId);

        // Delete specified junctions based on userId, videos, and tags
        List<Junction> deleted = repo.deleteAllByUserIdAndVideoIdInAndTagIn(userId, videos, tags);
        log.info("Deleted {} junctions with specified tags from specific videos for user {}", deleted.size(), userId);

        // Publish deletion events
        publishDeletedEvents(deleted);
    }

    /**
     * Deletes specific videos for a user, evicts the cache, and publishes deletion events.
     */
    @Override
    public void deleteVideosFromUser(String userId, List<String> videoIds) {
        log.debug("Deleting videos {} for user {}", videoIds, userId);

        // Evict cache for user before modification
        evictCache(userId);

        // Delete junctions with specified videos for the user
        List<Junction> deleted = repo.deleteAllByUserIdAndVideoIdIn(userId, videoIds);
        log.info("Deleted {} videos for user {}", deleted.size(), userId);

        // Publish deletion events
        publishDeletedEvents(deleted);
    }

    @Override
    public void deleteVideos(List<String> ids) {
        log.debug("Deleting all videos for all user with ids {}", ids);
        var deleted = repo.deleteAllByVideoIdIn(ids);
        publishDeletedEvents(deleted);
    }

    /**
     * Retrieves all junction entries for a user, with pagination, and caches the result.
     */
    @Override
    public List<JunctionDTO> getAllJunctionOfUser(String userId, int skip, int limit) {
        String cacheKey = generateCacheKey(userId, null, null, skip, limit);
        return cache.computeIfAbsent(cacheKey, _ -> {
            log.debug("Cache miss for key: {}", cacheKey);

            // Fetch junctions from the database
            List<Junction> junctions = repo.findAllByUserId(userId, PageRequest.of(skip, limit));
            log.info("Retrieved {} junction entries for user {}", junctions.size(), userId);

            // Convert to DTOs
            return toDtoList(junctions);
        });
    }

    /**
     * Retrieves videos with specified tags for a user, with pagination, and caches the result.
     */
    @Override
    public List<JunctionDTO> getAllVideosWithTags(String userId, List<String> tags, int skip, int limit) {
        tags = validateAndProcessTags(tags);
        String cacheKey = generateCacheKey(userId, null, tags, skip, limit);
        List<String> finalTags = tags;
        return cache.computeIfAbsent(cacheKey, _ -> {
            log.debug("Cache miss for key: {}", cacheKey);

            // Fetch junctions from the database
            List<Junction> junctions = repo.findAllByUserIdAndTagIn(userId, finalTags, PageRequest.of(skip, limit));
            log.info("Retrieved {} videos with tags for user {}", junctions.size(), userId);

            // Convert to DTOs
            return toDtoList(junctions);
        });
    }

    /**
     * Retrieves specific videos for a user, with pagination, and caches the result.
     */
    @Override
    public List<JunctionDTO> getVideosOfUser(String userId, List<String> videos, int skip, int limit) {
        String cacheKey = generateCacheKey(userId, videos, null, skip, limit);
        return cache.computeIfAbsent(cacheKey, _ -> {
            log.debug("Cache miss for key: {}", cacheKey);

            // Fetch junctions from the database
            List<Junction> junctions = repo.findAllByUserIdAndVideoIdIn(userId, videos, PageRequest.of(skip, limit));
            log.info("Retrieved {} specified videos for user {}", junctions.size(), userId);

            // Convert to DTOs
            return toDtoList(junctions);
        });
    }


    /**
     * Converts a Junction entity to a JunctionDTO, throwing an exception if any required field is null.
     */
    @Override
    public JunctionDTO toDto(Junction e) throws JunctionDTOHasNullValues {
        return new JunctionDTO(e.getUserId(), e.getVideoId(), e.getTag());
    }

    /**
     * Converts a list of Junction entities to a list of JunctionDTOs.
     */
    private List<JunctionDTO> toDtoList(List<Junction> junctions) {
        return junctions.stream()
                .map(junction -> {
                    try {
                        return toDto(junction); // Convert to DTO
                    } catch (JunctionDTOHasNullValues e) {
                        log.error("Error converting Junction to JunctionDTO: {}", e.getMessage());
                        return null; // Return null if conversion fails
                    }
                })
                .filter(Objects::nonNull) // Filter out null results
                .collect(Collectors.toList());
    }

    /**
     * Publishes a JunctionAddedEvent with the provided junctions converted to DTOs.
     */
    private void publishAddedEvents(List<Junction> junctions) {
        List<JunctionDTO> addedDTOs = toDtoList(junctions);
        eventPublisher.publishEvent(new JunctionAddedEvent(addedDTOs));
    }

    /**
     * Publishes a JunctionDeletedEvent with the provided junctions converted to DTOs.
     */
    private void publishDeletedEvents(List<Junction> deleted) {
        List<JunctionDTO> deletedDTOs = toDtoList(deleted);
        eventPublisher.publishEvent(new JunctionDeletedEvent(deletedDTOs));
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
                .filter(tag -> !tag.equals("*")) // Exclude prohibited tag
                .collect(Collectors.toList());
    }
}
