package dev.kuku.youtagserver.user_video_tag.application;

import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTag;
import dev.kuku.youtagserver.user_video_tag.infrastructure.UserVideoTagRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserVideoTagServiceImpl implements UserVideoTagService {
    final UserVideoTagRepo repo;

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
    }

    @Override
    public void deleteSpecificTagsFromSavedVideosOfUser(String userId, Set<String> videoIds, Set<String> tags) {
        log.debug("Deleting tags {} of user {}", tags, userId);
        repo.deleteAllByUserIdAndVideoIdInAndTagIn(userId, videoIds, tags);
    }


    @Override
    public Set<String> deleteAllTagsFromSpecificSavedVideosOfUser(String userId, Set<String> videoIds) {
        log.debug("Deleting all tags from saved videos {} of user {}", videoIds, userId);
        var deleted = repo.deleteAllByUserIdAndVideoIdIn(userId, videoIds);
        return deleted.stream().map(UserVideoTag::getTag).collect(Collectors.toSet());
    }

    @Override
    public void deleteAllTagsFromAllVideosOfUser(String userId) {
        log.debug("Deleting all tags from all videos saved for user {}", userId);
        repo.deleteAllByUserId(userId);
    }

    //How to get entries with DISTINCT videoID and needs to contain all tags specified in tags list
    /*
    the table looks like this
    userId tag videoId

    so we need to get distinct entry based on videoId which contains all the tags mentioned
     */
    @Override
    public Set<String> getAllSavedVideosOfUserWithTags(String userId, List<String> tags, int skip, int limit) {
        log.debug("Getting all saved videos of user {} with tags {}", userId, tags);
        if (tags.isEmpty()) {
            return Collections.emptySet();
        }
        List<String> videoIds = repo.findVideoIdsWithAllTags(userId, tags, tags.size(), PageRequest.of(skip / limit, limit));
        log.debug("Got videos: {}", videoIds);
        return new HashSet<>(videoIds);
    }


    @Override
    public long getCountOfSavedVideosOfUserWithTags(String userId, List<String> tags) {
        log.debug("Getting countOfSavedVideos of user {} with tags {}", userId, tags);
        long count = repo.countDistinctVideosByUserIdAndTags(userId, tags, tags.size());
        log.debug("Got countOfSavedVideos of user {} with tags {} = {}", userId, tags, count);
        return count;
    }

    @Override
    public Set<String> getAllTagsOfSavedVideosOfUser(String userId, List<String> videoIds, int skip, int limit) {
        log.debug("Getting all tags of videos {} for user {}", videoIds, userId);
        Set<String> tags = repo.findAllByUserIdAndVideoIdIn(userId, videoIds, PageRequest.of(skip / limit, limit)).stream().map(UserVideoTag::getTag).collect(Collectors.toSet());
        log.debug("Got tags {} for saved videos {}", tags, videoIds);
        return tags;
    }

    @Override
    public Set<String> deleteAllTagsFromSpecificSavedVideosForAllUser(Set<String> videoIds) {
        log.debug("Deleting all tags from videos {} for all users", videoIds);
        List<UserVideoTag> deletedEntries = repo.deleteAllByVideoIdIn(videoIds.stream().toList());
        return deletedEntries.stream().map(UserVideoTag::getTag).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getUnusedTagsOfUserFromList(String userId, Set<String> tagsToCheck) {
        log.debug("Getting tags from {} that are being used by user {}", tagsToCheck, userId);
        Set<String> entries = repo.findAllByUserIdAndTagIn(userId, tagsToCheck.stream().toList(), Pageable.unpaged())
                .stream().map(UserVideoTag::getTag).collect(Collectors.toSet());
        //Return back tags from tagsToCheck that are not present in entries
        return tagsToCheck.stream().filter(tag -> !entries.contains(tag)).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getUnusedTagsForAllUserFromList(Set<String> tags) {
        log.debug("Getting unused tags from {} that are not used by any user", tags);
        List<String> usedTags = repo.findAllByTagIn(tags.stream().toList()).stream().map(UserVideoTag::getTag).toList();
        return tags.stream().filter(tag -> !usedTags.contains(tag)).collect(Collectors.toSet());
    }
}
