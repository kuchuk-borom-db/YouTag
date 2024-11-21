package dev.kuku.youtagserver.user_tag.application;

import dev.kuku.youtagserver.user_tag.api.dtos.UserTagDTO;
import dev.kuku.youtagserver.user_tag.api.events.TagsAddedForUser;
import dev.kuku.youtagserver.user_tag.api.exceptions.TagDTOHasNullValues;
import dev.kuku.youtagserver.user_tag.api.services.UserTagService;
import dev.kuku.youtagserver.user_tag.domain.UserTag;
import dev.kuku.youtagserver.user_tag.infrastructure.TagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
public class UserTagServiceImpl implements UserTagService {

    private final TagRepo repo;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, List<UserTagDTO>> cache = new ConcurrentHashMap<>();

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
    public UserTagDTO toDto(UserTag e) throws TagDTOHasNullValues {
        return new UserTagDTO(e.getUserId(), e.getTag());
    }

    private List<UserTagDTO> toDtoList(List<UserTag> userTags) {
        return userTags.stream()
                .map(userTag -> {
                    try {
                        return toDto(userTag);
                    } catch (TagDTOHasNullValues e) {
                        log.error("Error converting Tag to TagDTO: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void addTagsToUser(String userId, List<String> tags) {
        /*
        Only add tags that are missing.
         */
        log.debug("Adding tags to user {}", userId);
        List<String> existingTags = repo.findAllByUserIdAndTagIn(userId, tags)
                .stream()
                .map(UserTag::getTag)
                .toList();

        List<UserTag> missingTags = tags.stream()
                .filter(t -> !existingTags.contains(t))
                .map(t -> new UserTag(UUID.randomUUID().toString(), t, userId)).toList();
        log.debug("Tags missing from user {} -> {}", userId, missingTags);
        repo.saveAll(missingTags);
        eventPublisher.publishEvent(new TagsAddedForUser(userId, missingTags));
    }

    @Override
    public List<String> getAllTagsOfUser(String userId, int skip, int limit) {
        log.debug("Getting all tags of user {}", userId);
        int pageNumber = skip / limit;
        List<String> userTags = repo.findAllByUserId(userId, PageRequest.of(pageNumber, limit))
                .stream()
                .map(UserTag::getTag).toList();
        log.debug("Tags retrieved for user {} is {}", userId, userTags);
        return userTags;
    }
}