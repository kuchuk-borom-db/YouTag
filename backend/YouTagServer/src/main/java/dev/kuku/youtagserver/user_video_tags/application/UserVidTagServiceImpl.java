package dev.kuku.youtagserver.user_video_tags.application;

import dev.kuku.youtagserver.shared.helper.CacheSystem;
import dev.kuku.youtagserver.user_video_tags.api.dto.UserVidTagDto;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoLinkNotFound;
import dev.kuku.youtagserver.user_video_tags.api.services.UserVidTagService;
import dev.kuku.youtagserver.user_video_tags.domain.entity.UserVidTag;
import dev.kuku.youtagserver.user_video_tags.infrastructure.repo.UserVidTagRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserVidTagServiceImpl implements UserVidTagService {
    private final UserVidTagRepo repo;
    private final CacheSystem cacheSystem;

    @Override
    public void linkUserAndVideo(String userEmail, String videoId) throws UserAndVideoAlreadyLinked {
        log.info("Linking user {} with videoId {}", userEmail, videoId);
        log.info("Checking if link already exists");
        try {
            getUserAndVideoTag(userEmail, videoId);
            throw new UserAndVideoAlreadyLinked(videoId, userEmail);
        } catch (UserAndVideoLinkNotFound e) {
            log.info("Previous Link not found. Creating a new one");
            repo.save(new UserVidTag(userEmail, videoId, new ArrayList<>()));
        }
    }

    @Override
    public void addTags(String userEmail, String videoId, String[] tags) throws UserAndVideoLinkNotFound {
        log.info("Attempting to add tags {} for user {} and video {}", tags, userEmail, videoId);
        UserVidTag existing = getUserAndVideoTagFromCache(userEmail, videoId);
        if (existing == null) {
            throw new UserAndVideoLinkNotFound(videoId, userEmail);
        }
        List<String> updatedTags = new ArrayList<>(existing.getTags());
        for (String tag : tags) {
            if (!updatedTags.contains(tag)) {
                updatedTags.add(tag);
            }
        }
        existing.setTags(updatedTags);
        cacheSystem.evict(this.getClass().toString(), getCacheKey(userEmail, videoId));
        repo.save(existing);
    }

    @Override
    public void removeTags(String userEmail, String videoId, String[] tags) throws UserAndVideoLinkNotFound {
        log.info("Attempting to remove tags {} for user {} and video {}", tags, userEmail, videoId);
        UserVidTag existing = getUserAndVideoTagFromCache(userEmail, videoId);
        if (existing == null) {
            throw new UserAndVideoLinkNotFound(videoId, userEmail);
        }
        List<String> updatedTags = existing.getTags().stream()
                .filter(t -> !contains(tags, t))
                .collect(Collectors.toList());
        existing.setTags(updatedTags);
        cacheSystem.evict(this.getClass().toString(), getCacheKey(userEmail, videoId));
        repo.save(existing);
    }

    @Override
    public UserVidTagDto getUserAndVideoTag(String userEmail, String videoId) throws UserAndVideoLinkNotFound {
        log.info("Fetching link for user {} and video {}", userEmail, videoId);
        UserVidTag userVidTag = getUserAndVideoTagFromCache(userEmail, videoId);
        if (userVidTag == null) {
            userVidTag = repo.findUserVidTagByUserEmailAndVideoId(userEmail, videoId);
            if (userVidTag == null) {
                throw new UserAndVideoLinkNotFound(videoId, userEmail);
            }
            cacheSystem.cache(this.getClass().toString(), getCacheKey(userEmail, videoId), userVidTag);
        }
        return toDto(userVidTag);
    }

    @Override
    public List<UserVidTagDto> getUserAndVideoTagsByUser(String userEmail) {
        log.info("Fetching all links for user {}", userEmail);
        List<UserVidTag> userVidTags = repo.findAllByUserEmail(userEmail);
        return userVidTags.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserVidTagDto> getUserAndVideoTagsByVideo(String videoId) {
        log.info("Fetching all links for video {}", videoId);
        List<UserVidTag> userVidTags = repo.findAllByVideoId(videoId);
        return userVidTags.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserVidTagDto> getUserAndVideoTagsByTag(String tag) {
        log.info("Fetching all links for tag {}", tag);
        List<UserVidTag> userVidTags = repo.findAllByTagsContaining(List.of(tag).toArray(new String[0]));
        return userVidTags.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<UserVidTagDto> getUserAndVideoTagsByUserAndTag(String userEmail, String tag) {
        log.info("Fetching all links for user {} and tag {}", userEmail, tag);
        List<UserVidTag> userVidTags = repo.findAllByUserEmailAndTagsContaining(userEmail, List.of(tag).toArray(new String[0]));
        return userVidTags.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private UserVidTag getUserAndVideoTagFromCache(String userEmail, String videoId) {
        return (UserVidTag) cacheSystem.getObject(this.getClass().toString(), getCacheKey(userEmail, videoId));
    }

    private String getCacheKey(String userEmail, String videoId) {
        return String.format("%s-%s", userEmail, videoId);
    }

    private UserVidTagDto toDto(UserVidTag userVidTag) {
        return new UserVidTagDto(userVidTag.getUserEmail(), userVidTag.getVideoId(), userVidTag.getTags());
    }

    private UserVidTag toEntity(UserVidTagDto userVidTagDto) {
        return new UserVidTag(userVidTagDto.user_email(), userVidTagDto.video_id(), userVidTagDto.tags());
    }

    private boolean contains(String[] array, String value) {
        for (String item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }
}