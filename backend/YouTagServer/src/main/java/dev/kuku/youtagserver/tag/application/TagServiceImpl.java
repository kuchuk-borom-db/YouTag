package dev.kuku.youtagserver.tag.application;

import dev.kuku.youtagserver.tag.api.dtos.TagDTO;
import dev.kuku.youtagserver.tag.api.events.AddedTagsToVideoEvent;
import dev.kuku.youtagserver.tag.api.exceptions.TagDTOHasNullValues;
import dev.kuku.youtagserver.tag.api.services.TagService;
import dev.kuku.youtagserver.tag.domain.Tag;
import dev.kuku.youtagserver.tag.infrastructure.TagRepo;
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

    private String generateCacheKey(String userId, List<String> videos, List<String> tags, int skip, int limit) {
        return userId + ":" + (videos != null ? videos.toString() : "") + ":" +
                (tags != null ? tags.toString() : "") + ":" + skip + ":" + limit;
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
        String cacheKey = generateCacheKey(userId, null, null, skip, limit);

        return cache.computeIfAbsent(cacheKey, _ -> {
            int pageNum = skip / limit;
            List<Tag> tags = repo.findAllByUserId(userId, PageRequest.of(pageNum, limit));
            return toDtoList(tags);
        });
    }

    @Override
    public List<TagDTO> getTagsOfVideo(String userId, String videoId) {
        log.debug("Getting tags of video {}", videoId);
        String cacheKey = generateCacheKey(userId, Collections.singletonList(videoId), null, 0, 0);

        return cache.computeIfAbsent(cacheKey, _ -> {
            List<Tag> tags = repo.findAllByUserIdAndVideoId(userId, videoId);
            return toDtoList(tags);
        });
    }

    @Override
    public List<TagDTO> getVideosWithTag(String userId, List<String> tags, int skip, int limit) {
        log.debug("Getting videos of user {} with tag {}, skipping {} and limit to {}", userId, tags, skip, limit);
        String cacheKey = generateCacheKey(userId, null, tags, skip, limit);
        List<String> finalTags = validateAndProcessTags(tags);
        return cache.computeIfAbsent(cacheKey, _ -> {
            int pageNumber = skip / limit;
            List<Tag> videos = repo.findAllByUserIdAndTagIn(userId, finalTags, PageRequest.of(pageNumber, limit));
            return toDtoList(videos);
        });
    }

    @Override
    public void deleteTagsFromAllVideos(String userId, List<String> tags) {
        log.debug("Deleting tags {} from all videos for user {}", tags, userId);
        tags = validateAndProcessTags(tags);
        repo.deleteAllByUserIdAndTagIn(userId, tags);
        evictCache(userId);
    }

    @Override
    public void deleteTagsFromVideos(String userId, List<String> tags, List<String> videoIds) {
        log.debug("Deleting tags {} from videos {} for user {}", tags, videoIds, userId);
        tags = validateAndProcessTags(tags);
        repo.deleteAllByUserIdAndVideoIdInAndTagIn(userId, videoIds, tags);
        evictCache(userId);
    }
}