package dev.kuku.youtagserver.user_tag.application;

import dev.kuku.youtagserver.user_tag.api.UserTagDTO;
import dev.kuku.youtagserver.user_tag.api.UserTagService;
import dev.kuku.youtagserver.user_tag.domain.UserTag;
import dev.kuku.youtagserver.user_tag.domain.UserTagId;
import dev.kuku.youtagserver.user_tag.infrastructure.UserTagRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserTagServiceImpl implements UserTagService {
    final UserTagRepo repo;

    @Override
    public void addTagsToUser(String userId, List<String> tags) {
        log.debug("Adding tags {} to user {}", tags, userId);
        List<UserTag> userTags = tags.stream().map(tag -> new UserTag(userId, tag)).toList();
        repo.saveAll(userTags);
        //TODO Evict everything with the matching user
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
    }

    @Override
    public void deleteSpecifiedTagsOfUser(String userId, List<String> tagsToDelete) {
        log.debug("Deleting specified tags {} from user {}", tagsToDelete, userId);
        repo.deleteAllById(tagsToDelete.stream().map(tag -> new UserTagId(userId, tag)).collect(Collectors.toList()));
    }


    @Override
    public void deleteSpecifiedTagsFromAllUsers(Set<String> tags) {
        log.debug("Deleting specific tags {} from all users.", tags);
        repo.deleteAllByTagIn(tags);
    }

    @Override
    public long getTagCountOfUser(String userId) {
        log.debug("Getting tag count of user {}", userId);
        return repo.countAllByUserId(userId);
    }

    @Override
    public long getTagCountOfUserContaining(String userId, String keyword) {
        log.debug("Getting tag count of user {} containing keyword {}", userId, keyword);
        return repo.countAllByUserIdAndTagContaining(userId, keyword);
    }

    @Override
    public List<String> getTagsOfUserContaining(String userId, String keyword, int skip, int limit) {
        log.debug("Getting tags containing ${} of user {}", keyword, userId);
        return repo.findAllByUserIdAndTagContaining(userId, keyword, PageRequest.of(skip/limit, limit)).stream().map(UserTag::getTag).toList();
    }

    @Override
    public UserTagDTO toDto(UserTag e) {
        return new UserTagDTO(e.getUserId(), e.getTag());
    }
}
