package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
import dev.kuku.youtagserver.user_video_tag.api.events.DeleteAllTagsFromSpecificSavedVideosOfUser;
import dev.kuku.youtagserver.user_video_tag.api.events.DeleteSpecificTagsFromAllSavedVideos;
import dev.kuku.youtagserver.user_video_tag.api.events.DeleteTagsFromSpecificSavedVideos;
import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.infrastructure.UserVideoTagRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserVideoTagServiceImpl implements UserVideoTagService {
    final UserVideoTagRepo repo;
    final ApplicationEventPublisher eventPublisher;

    @Override
    public UserVideoTagDTO toDto(UserVideoTag e) {
        return new UserVideoTagDTO(e.getUserId(), e.getVideoId(), e.getTag());
    }

    @Override
    public Set<String> getTagsOfSavedVideoOfUser(String userId, String videoId) {
        log.debug("Get tags of saved video {} of user {}", videoId, userId);
        Set<String> tags = repo.findAllByUserIdAndVideoId(userId, videoId).stream().map(UserVideoTag::getTag).collect(Collectors.toSet());
        log.debug("Got tags {} for saved video {} of user {}", tags, videoId, userId);
        return tags;
    }

    @Override
    public void addTagsToSpecificSavedVideosOfUser(String userId, List<String> videoIds, List<String> tags) {
        log.debug("Adding tags {} to videos {} of user {}", tags, videoIds, userId);
        List<UserVideoTag> entriesToSave = new ArrayList<>();
        videoIds.forEach(videoId -> tags.forEach(tag -> entriesToSave.add(new UserVideoTag(userId, videoId, tag))));
        repo.saveAll(entriesToSave);
        //TODO Publish event
    }

    @Override
    public void deleteSpecificTagsFromSavedVideosOfUser(String userId, List<String> videoIds, List<String> tags) {
        log.debug("Deleting tags {} of user {}", tags, userId);
        repo.deleteAllByUserIdAndVideoIdInAndTagIn(userId, videoIds, tags);
        //Event needs to remove the tags from user if they are not used in any saved videos of user
        eventPublisher.publishEvent(new DeleteTagsFromSpecificSavedVideos(userId, videoIds, tags));
    }

    @Override
    public void deleteSpecificTagsFromAllSavedVideosOfUser(String userId, List<String> tags) {
        log.debug("Deleting tags {} of user {} from all videos.", tags, userId);
        repo.deleteAllByUserIdAndTagIn(userId, tags);
        //Event needs to remove tags from user as they are not used in any saved videos anymore.
        eventPublisher.publishEvent(new DeleteSpecificTagsFromAllSavedVideos(userId, tags));
    }

    @Override
    public void deleteAllTagsFromSpecificSavedVideosOfUser(String userId, List<String> videoIds) {
        log.debug("Deleting all tags from saved videos {} of user {}", videoIds, userId);
        repo.deleteAllByUserIdAndVideoIdIn(userId, videoIds);
        //Event needs to remove tags from user if they are not used in any other saved video
        eventPublisher.publishEvent(new DeleteAllTagsFromSpecificSavedVideosOfUser(userId, videoIds));
    }

    @Override
    public Set<String> getAllSavedVideosOfUserWithTags(String userId, List<String> tags, int skip, int limit) {
        log.debug("Getting all saved videos of user {} with tags {}", userId, tags);
        List<UserVideoTag> entries = repo.findAllByUserIdAndTagIn(userId, tags, PageRequest.of(skip / limit, limit));
        log.debug("Got tags {} for saved videos {}", tags, entries);
        return entries.stream().map(UserVideoTag::getTag).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getAllTagsOfSavedVideosOfUser(String userId, List<String> videoIds, int skip, int limit) {
        log.debug("Getting all tags of videos {} for user {}", videoIds, userId);
        Set<String> tags = repo.findAllByUserIdAndVideoIdIn(userId, videoIds, PageRequest.of(skip / limit, limit)).stream().map(UserVideoTag::getTag).collect(Collectors.toSet());
        log.debug("Got tags {} for saved videos {}", tags, videoIds);
        return tags;
    }
}
