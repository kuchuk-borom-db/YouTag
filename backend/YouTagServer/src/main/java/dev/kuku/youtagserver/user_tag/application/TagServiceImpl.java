package dev.kuku.youtagserver.user_tag.application;

import dev.kuku.youtagserver.user_tag.api.dtos.TagDTO;
import dev.kuku.youtagserver.user_tag.api.events.*;
import dev.kuku.youtagserver.user_tag.api.exceptions.TagDTOHasNullValues;
import dev.kuku.youtagserver.user_tag.api.services.TagService;
import dev.kuku.youtagserver.user_tag.domain.Tag;
import dev.kuku.youtagserver.user_tag.infrastructure.TagRepo;
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

    private String generateCacheKey(String userId, List<String> videos, List<String> tags, int skip, int limit, String containing) {
        return userId + ":" +
                (videos != null ? videos.toString() : "") + ":" +
                (tags != null ? tags.toString() : "") + ":" +
                skip + ":" +
                limit + ":" +
                (containing != null ? containing : "");
    }

    private void evictCache(String userId) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
        log.debug("Evicted cache entries for user {}", userId);
    }

    @Override
    public TagDTO toDto(Tag e) throws TagDTOHasNullValues {
        return new TagDTO(e.getUserId(), e.getVideoId(), e.getTag());
    }

    private List<TagDTO> toDtoList(List<Tag> tags) {
        return tags.stream()
                .map(tag -> {
                    try {
                        return toDto(tag);
                    } catch (TagDTOHasNullValues e) {
                        log.error("Error converting Tag to TagDTO: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

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
        evictCache(userId);
        eventPublisher.publishEvent(new AddedTagsToVideoEvent(tagsEntity));
    }

    @Override
    public List<TagDTO> getAllTagsOfUser(String userId, int skip, int limit) {
        log.debug("Getting tags of user {} skip {} and limit {}", userId, skip, limit);
        String cacheKey = generateCacheKey(userId, null, null, skip, limit, null);

        return cache.computeIfAbsent(cacheKey, _ -> {
            int pageNum = skip / limit;
            List<Tag> tags = repo.findAllByUserId(userId, PageRequest.of(pageNum, limit));
            return toDtoList(tags);
        });
    }

    @Override
    public List<TagDTO> getAllTagsOfUserContaining(String userId, String containing, int skip, int limit) {
        log.debug("Getting tags containing {} for user {}. Skipping {} and Limit {}", containing, userId, skip, limit);
        String cacheKey = generateCacheKey(userId, null, null, skip, limit, containing);
        return cache.computeIfAbsent(cacheKey, _ -> {
            int pageNum = skip / limit;
            List<Tag> tags = repo.findAllByUserIdAndTagContaining(userId, containing, PageRequest.of(pageNum, limit));
            return toDtoList(tags);
        });
    }

    @Override
    public List<TagDTO> getTagsOfVideo(String userId, String videoId) {
        log.debug("Getting tags of video {}", videoId);
        String cacheKey = generateCacheKey(userId, Collections.singletonList(videoId), null, 0, 0, null);

        return cache.computeIfAbsent(cacheKey, _ -> {
            List<Tag> tags = repo.findAllByUserIdAndVideoId(userId, videoId);
            return toDtoList(tags);
        });
    }

    @Override
    public List<TagDTO> getVideosWithTag(String userId, List<String> tags, int skip, int limit) {
        log.debug("Getting videos of user {} with tag {}, skipping {} and limit to {}", userId, tags, skip, limit);
        String cacheKey = generateCacheKey(userId, null, tags, skip, limit, null);
        List<String> finalTags = validateAndProcessTags(tags);
        return cache.computeIfAbsent(cacheKey, _ -> {
            int pageNumber = skip / limit;
            List<Tag> videos = repo.findAllByUserIdAndTagIn(userId, finalTags, PageRequest.of(pageNumber, limit));
            return toDtoList(videos);
        });
    }

    @Override
    public void deleteAllTagsOfUser(String userId) {
        log.debug("Deleting all tags of user {}", userId);
        repo.deleteAllByUserId(userId);
        eventPublisher.publishEvent(new DeleteAllTagsOfUser(userId));
        evictCache(userId);
    }

    @Override
    public void DeleteTagsFromAllVideosOfUser(String userId, List<String> tags) {
        log.debug("Deleting tags {} from all videos for user {}", tags, userId);
        tags = validateAndProcessTags(tags);
        repo.deleteAllByUserIdAndTagIn(userId, tags);
        eventPublisher.publishEvent(new DeleteTagsFromAllVideosOfUser(userId, tags));
        evictCache(userId);
    }

    @Override
    public void deleteTagsFromVideosOfUser(String userId, List<String> tags, List<String> videoIds) {
        log.debug("Deleting tags {} from videos {} for user {}", tags, videoIds, userId);
        tags = validateAndProcessTags(tags);
        repo.deleteAllByUserIdAndVideoIdInAndTagIn(userId, videoIds, tags);
        eventPublisher.publishEvent(new DeleteTagsFromVideosOfUser(userId, tags, videoIds));
        evictCache(userId);
    }

    @Override
    public void deleteAllTagsOfVideoOfUser(String userId, String videoId) {
        log.debug("Deleting all tags of video {} of user {}", videoId, userId);
        repo.deleteAllByUserIdAndVideoIdIn(userId, List.of(videoId));
        evictCache(userId);
        eventPublisher.publishEvent(new DeleteAllTagsFromVideoOfUser(userId, videoId));
    }

    @Override
    public void deleteAllTagsOfAllUsersOfVideo(String videoId) {
        // First get all affected users
        List<String> affectedUsers = repo.findAllByVideoId(videoId)
                .stream()
                .map(Tag::getUserId)
                .distinct()
                .toList();

        // Delete all tags
        repo.deleteAllByVideoId(videoId);

        // Evict cache for all affected users
        affectedUsers.forEach(this::evictCache);

        eventPublisher.publishEvent(new DeleteAllTagsFromAllUsersOfVideo(videoId));
    }
}