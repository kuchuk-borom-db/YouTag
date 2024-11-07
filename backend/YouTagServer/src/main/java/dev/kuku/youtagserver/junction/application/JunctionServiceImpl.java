package dev.kuku.youtagserver.junction.application;

import dev.kuku.youtagserver.junction.api.dtos.JunctionDTO;
import dev.kuku.youtagserver.junction.api.events.JunctionAddedEvent;
import dev.kuku.youtagserver.junction.api.events.JunctionDeletedEvent;
import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import dev.kuku.youtagserver.junction.api.services.JunctionService;
import dev.kuku.youtagserver.junction.domain.Junction;
import dev.kuku.youtagserver.junction.infrastructure.JunctionRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JunctionServiceImpl implements JunctionService {

    private final JunctionRepo repo;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheManager cacheManager;
    private Cache cacheStore; // TODO: Use cache in methods as needed

    @PostConstruct
    void setup() {
        cacheStore = cacheManager.getCache(this.getClass().getName());
    }

    /**
     * Adds multiple videos with associated tags for a user, optimized to avoid nested loops.
     */
    @Override
    public void addVideosWithTags(String userId, List<String> videos, List<String> tags) {
        log.debug("Attempting to add videos with tags for user {}", userId);

        // Creates junctions for each combination of video and tag without nested loops
        List<Junction> junctions = tags.stream()
                .flatMap(tag -> videos.stream().map(videoId -> new Junction(userId, videoId, tag)))
                .collect(Collectors.toList());

        repo.saveAll(junctions);
        log.info("Saved {} junctions for user {}", junctions.size(), userId);

        publishAddedEvents(junctions);
    }

    /**
     * Deletes all videos and tags for a given user and publishes the deletion event.
     */
    @Override
    public void deleteAllVideosAndTags(String userId) {
        log.debug("Deleting all videos and tags of user {}", userId);

        List<Junction> deleted = repo.deleteAllByUserId(userId);
        log.info("Deleted {} junctions for user {}", deleted.size(), userId);

        publishDeletedEvents(deleted);
    }

    /**
     * Deletes specified tags from all videos of a user and publishes the deletion event.
     */
    @Override
    public void deleteTagsFromAllVideos(String userId, List<String> tags) {
        log.debug("Deleting tags {} from all videos for user {}", tags, userId);

        List<Junction> deleted = repo.deleteAllByUserIdAndTagIn(userId, tags);
        log.info("Deleted {} junctions with specified tags for user {}", deleted.size(), userId);

        publishDeletedEvents(deleted);
    }

    /**
     * Deletes specified tags from specific videos of a user and publishes the deletion event.
     */
    @Override
    public void deleteTagsFromVideos(String userId, List<String> videos, List<String> tags) {
        log.debug("Deleting tags {} from videos {} for user {}", tags, videos, userId);

        List<Junction> deleted = repo.deleteAllByUserIdAndVideoIdInAndTagIn(userId, videos, tags);
        log.info("Deleted {} junctions with specified tags from specific videos for user {}", deleted.size(), userId);

        publishDeletedEvents(deleted);
    }

    /**
     * Deletes specific videos for a user and publishes the deletion event.
     */
    @Override
    public void deleteVideosFromUser(String userId, List<String> videoIds) {
        log.debug("Deleting videos {} for user {}", videoIds, userId);

        List<Junction> deleted = repo.deleteAllByUserIdAndVideoIdIn(userId, videoIds);
        log.info("Deleted {} videos for user {}", deleted.size(), userId);

        publishDeletedEvents(deleted);
    }

    /**
     * Retrieves all junction entries for a user, with pagination.
     */
    @Override
    public List<JunctionDTO> getAllJunctionOfUser(String userId, int skip, int limit) {
        log.debug("Retrieving all junction entries for user {} with skip {} and limit {}", userId, skip, limit);

        List<Junction> junctions = repo.findAllByUserId(userId, PageRequest.of(skip, limit));
        log.info("Retrieved {} junction entries for user {}", junctions.size(), userId);

        return toDtoList(junctions);
    }

    /**
     * Retrieves videos with specified tags for a user, with pagination.
     */
    @Override
    public List<JunctionDTO> getAllVideosWithTags(String userId, List<String> tags, int skip, int limit) {
        log.debug("Retrieving videos with tags {} for user {} with skip {} and limit {}", tags, userId, skip, limit);

        List<Junction> junctions = repo.findAllByUserIdAndTagIn(userId, tags, PageRequest.of(skip, limit));
        log.info("Retrieved {} videos with tags for user {}", junctions.size(), userId);

        return toDtoList(junctions);
    }

    /**
     * Retrieves specific videos for a user, with pagination.
     */
    @Override
    public List<JunctionDTO> getVideosOfUser(String userId, List<String> videos, int skip, int limit) {
        log.debug("Retrieving specified videos {} for user {} with skip {} and limit {}", videos, userId, skip, limit);

        List<Junction> junctions = repo.findAllByUserIdAndVideoIdIn(userId, videos, PageRequest.of(skip, limit));
        log.info("Retrieved {} specified videos for user {}", junctions.size(), userId);

        return toDtoList(junctions);
    }

    /**
     * Converts a Junction to a JunctionDTO.
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
        log.debug("Published JunctionAddedEvent for {} junctions", addedDTOs.size());
    }

    /**
     * Publishes a JunctionDeletedEvent with the provided junctions converted to DTOs.
     */
    private void publishDeletedEvents(List<Junction> junctions) {
        List<JunctionDTO> deletedDTOs = toDtoList(junctions);
        eventPublisher.publishEvent(new JunctionDeletedEvent(deletedDTOs));
        log.debug("Published JunctionDeletedEvent for {} junctions", deletedDTOs.size());
    }
}
