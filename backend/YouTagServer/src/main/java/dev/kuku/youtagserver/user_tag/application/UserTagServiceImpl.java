package dev.kuku.youtagserver.user_tag.application;

import dev.kuku.youtagserver.shared.helper.CacheSystem;
import dev.kuku.youtagserver.user_tag.api.api.events.RemoveAllTagsOfUser;
import dev.kuku.youtagserver.user_tag.api.dtos.UserTagDTO;
import dev.kuku.youtagserver.user_tag.api.services.UserTagService;
import dev.kuku.youtagserver.user_tag.domain.UserTag;
import dev.kuku.youtagserver.user_tag.infrastructure.UserTagRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserTagServiceImpl implements UserTagService {
    final UserTagRepo repo;
    final CacheSystem cacheSystem;
    final String cacheStorageName = "user_tag";
    final ApplicationEventPublisher eventPublisher;

    @Override
    public void addTagsToUser(String userId, List<String> tags) {
        log.debug("Adding tags {} to user {}", tags, userId);
        List<UserTag> userTags = tags.stream().map(tag -> new UserTag(userId, tag)).toList();
        repo.saveAll(userTags);
        //TODO Evict everything with the matching user
    }

    @Override
    public List<String> getSpecificTagsOfUser(String userId, List<String> tags) {
        log.debug("Getting valid existing tags {} from user {}", tags, userId);
        List<UserTag> existingEntries = repo.findAllByUserIdAndTagIn(userId, tags);
        List<String> existingTags = existingEntries.stream().map(UserTag::getTag).collect(Collectors.toList());
        log.debug("Got tags {} for user {}", existingTags, userId);
        //TODO Cache the result
        return existingTags;
    }

    @Override
    public List<String> getAllTagsOfUser(String userId, int skip, int limit) {
        log.debug("Getting all tags from user {}", userId);
        List<UserTag> entries = repo.findAllByUserId(userId, PageRequest.of(skip / limit, limit));
        List<String> tags = entries.stream().map(UserTag::getTag).toList();
        log.debug("Got all tags {} for user {}", tags, userId);
        //TODO cache the result based on limit
        return tags;
    }

    @Override
    public void deleteAllTagsOfUser(String userId) {
        log.debug("Removing all tags from user {}", userId);
        repo.deleteAllByUserId(userId);
        eventPublisher.publishEvent(new RemoveAllTagsOfUser(userId));
    }

    @Override
    public UserTagDTO toDto(UserTag e) {
        return new UserTagDTO(e.getUserId(), e.getTag());
    }
}
