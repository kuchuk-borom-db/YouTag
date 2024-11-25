package dev.kuku.youtagserver.user_video_tag.api;

import dev.kuku.youtagserver.shared.api.services.Service;
import dev.kuku.youtagserver.user_video_tag.domain.UserVideoTag;

import java.util.List;
import java.util.Set;

public interface UserVideoTagService extends Service<UserVideoTag, UserVideoTagDTO> {
    /**
     * Get tags of saved video of user
     *
     * @param userId  userId
     * @param videoId videoId to get the tags of
     * @return list of tags of saved video of user
     */
    Set<String> getTagsOfSavedVideoOfUser(String userId, String videoId);

    /**
     * Add tags to saved videos of user
     *
     * @param userId   userId
     * @param videoIds videos to add the tags to
     * @param tags     tags to add
     */
    void addTagsToSpecificSavedVideosOfUser(String userId, List<String> videoIds, List<String> tags);

    /**
     * Delete specified tags from saved videos of user
     *
     * @param userId   userId
     * @param videoIds videos to delete the tags from
     * @param tags     tags to delete
     */
    void deleteSpecificTagsFromSavedVideosOfUser(String userId, List<String> videoIds, List<String> tags);

    /**
     * Delete the specified tags from all videos saved by user
     *
     * @param userId userId
     * @param tags   tags to delete from ALL videos
     */
    void deleteSpecificTagsFromAllSavedVideosOfUser(String userId, List<String> tags);

    /**
     * Delete all tags from the specified video Ids saved to user
     *
     * @param userId   userId
     * @param videoIds video ids to delete all tags from
     * @return deleted tags
     */
    List<String> deleteAllTagsFromSpecificSavedVideosOfUser(String userId, List<String> videoIds);

    /**
     * Delete all entries of with matching userId. Essentially removing tags from all videos of user
     *
     * @param userId userId
     */
    void deleteAllTagsFromAllVideosOfUser(String userId);

    /**
     * Get all the video Ids which have the following tag
     *
     * @param userId userId
     * @param tags   tags that the videos needs to have
     * @param skip   how many to skip
     * @param limit  how many to limit
     * @return set of video ids that contains the tags
     */
    Set<String> getAllSavedVideosOfUserWithTags(String userId, List<String> tags, int skip, int limit);

    /**
     * Get all tags of the saved videos
     *
     * @param userId   userId
     * @param videoIds video Ids to get the tags of
     * @param skip     how many to skip
     * @param limit    how many to limit
     * @return set of tags present in all the videos
     */
    Set<String> getAllTagsOfSavedVideosOfUser(String userId, List<String> videoIds, int skip, int limit);

    /**
     * Delete all tags from specified videos for all users
     *
     * @param videoIds video ids to delete tags from for all users
     */
    void deleteAllTagsFromSpecificSavedVideosForAllUser(List<String> videoIds);

    /**
     * Remove all tags from specified videos from specified users
     *
     * @param userIds  userIds to delete  from
     * @param videoIds video ids to delete the tags from
     * @return deleted tags
     */
    List<String> deleteAllTagsFromSpecificSavedVideosOfUsers(List<String> userIds, List<String> videoIds);
}
