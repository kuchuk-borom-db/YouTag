package dev.kuku.youtagserver.user_tag.application;

import dev.kuku.youtagserver.user_tag.api.dtos.UserTagDTO;
import dev.kuku.youtagserver.user_tag.api.exceptions.TagDTOHasNullValues;
import dev.kuku.youtagserver.user_tag.api.services.UserTagService;
import dev.kuku.youtagserver.user_tag.domain.UserTag;
import dev.kuku.youtagserver.user_tag.infrastructure.TagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private List<String> validateAndProcessTags(List<String> tags) {
        return tags.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllTagsOfUser(String userId, int skip, int limit) {
        //TODO complete
    }
}