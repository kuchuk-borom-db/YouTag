package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.user_tag.api.services.UserTagService;
import dev.kuku.youtagserver.user_video.api.services.UserVideoService;
import dev.kuku.youtagserver.user_video_tag.api.UserVideoTagService;
import dev.kuku.youtagserver.video.api.services.VideoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class OrchestratorService {
    final UserVideoService userVideoService;
    final UserVideoTagService userVideoTagService;
    final UserTagService userTagService;
    final VideoService videoService;

    /**
     * Delete the entry from user_video table
     * Delete tags from user_video_tag tables
     * <p>
     * Remove redundant tags and Remove redundant video if they are not used anymore
     *
     * @param userId   userId
     * @param videoIds videos to delete
     */
    public void deleteSpecificSavedVideosOfUser(String userId, Set<String> videoIds) {
        log.debug("Deleting Saved videos {} of user {}", videoIds, userId);
        //Remove the saved entry
        userVideoService.deleteSpecificSavedVideosFromUser(userId, videoIds);
        //Remove tag entries of the video and user
        Set<String> deletedTags = userVideoTagService.deleteAllTagsFromSpecificSavedVideosOfUser(userId, videoIds);

        //Remove redundant tags from user_tag table
        Set<String> unusedTags = userVideoTagService.getUnusedTagsOfUserFromList(userId, deletedTags);
        userTagService.deleteSpecifiedTagsOfUser(userId, unusedTags.stream().toList());

        //Remove videos that are not being used by any user
        Set<String> unusedVideos = userVideoService.getUnusedVideosFromSet(videoIds);
        videoService.deleteSpecifiedVideos(unusedVideos);

    }

    /**
     * Delete specified tags from specified videos
     * Delete redundant tags
     *
     * @param userId   userId
     * @param videoIds videoId to delete the tags from
     * @param tags     tags to delete
     */
    public void deleteSpecificTagsFromSpecificSavedVideosOfUser(String userId, Set<String> videoIds, Set<String> tags) {
        log.debug("Deleting tags {} from Saved videos {} of user {}", tags, videoIds, userId);
        userVideoTagService.deleteSpecificTagsFromSavedVideosOfUser(userId, videoIds, tags);

        //Remove redundant tags that are not used by the user anymore
        Set<String> unusedTags = userVideoTagService.getUnusedTagsOfUserFromList(userId, new HashSet<>(tags));
        userTagService.deleteSpecifiedTagsOfUser(userId, unusedTags.stream().toList());

    }

    /**
     * Delete specified tags from all videos of user
     * Delete redundant tags
     *
     * @param userId userId
     * @param tags   tags to delete from all videos
     */
    public void deleteSpecificTagsFromAllSavedVideosOfUser(String userId, Set<String> tags) {
        log.debug("Deleting specific tags {} from all videos of user {}", tags, userId);
        userVideoTagService.deleteAllTagsFromSpecificSavedVideosOfUser(userId, tags);

        //Remove redundant tags that are not used by the user anymore
        Set<String> unusedTags = userVideoTagService.getUnusedTagsOfUserFromList(userId, new HashSet<>(tags));
        userTagService.deleteSpecifiedTagsOfUser(userId, unusedTags.stream().toList());
    }

    /**
     * Delete all tags of specific videos
     * Delete redundant tags
     *
     * @param userId   userId
     * @param videoIds video Ids to delete the tags from
     */
    public void deleteAllTagsFromSpecificSavedVideosOfUser(String userId, Set<String> videoIds) {
        log.debug("Deleting all tags from saved videos {} of user {}", videoIds, userId);
        Set<String> deletedTags = userVideoTagService.deleteAllTagsFromSpecificSavedVideosOfUser(userId, videoIds);

        //Remove redundant tags
        Set<String> unusedTags = userVideoTagService.getUnusedTagsOfUserFromList(userId, deletedTags);
        userTagService.deleteSpecifiedTagsOfUser(userId, unusedTags.stream().toList());
    }

    /**
     * Delete the videos from video table
     * Remove all saved videos using this video for all users
     * Remove all tags for users using this videos
     * Remove redundant tags
     *
     * @param videoIds video Ids to delete
     */
    public void deleteSpecificVideos(Set<String> videoIds) {
        log.debug("Deleting videos {}", videoIds);
        videoService.deleteSpecifiedVideos(videoIds);
        Set<String> deletedTags = userVideoTagService.deleteAllTagsFromSpecificSavedVideosForAllUser(videoIds);
        userVideoService.deleteSpecificSavedVideosForAllUsers(videoIds.stream().toList());

        //Remove redundant tags
        Set<String> unusedTags = userVideoTagService.getUnusedTagsForAllUserFromList(deletedTags);
        userTagService.deleteSpecifiedTagsFromAllUsers(unusedTags);
    }
}
