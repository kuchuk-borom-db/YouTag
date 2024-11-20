package dev.kuku.youtagserver.user_video_tag.api.services;


import java.util.List;
import java.util.Set;

public interface UserVideoTagService {
    /**
     * Get all tags of a video saved by a user
     *
     * @param userId  userID of the user
     * @param videoId video to get the tags of
     * @return list of tags of the video
     */
    List<String> getTagsOfSavedVideoOfUser(String userId, String videoId);

    /**
     * Add tags to videos saved by the user.
     *
     * @param userId   userId
     * @param tags     tags to save
     * @param videoIds videos to save the tags to
     */
    void addTagsForSavedVideosOfUser(String userId, List<String> tags, List<String> videoIds);

    /**
     * Delete tags from videos
     *
     * @param userId   user Id
     * @param tags     tags to delete
     * @param videoIds videos from where the tags will be deleted from
     */
    void deleteTagsForSavedVideosOfUser(String userId, List<String> tags, List<String> videoIds);

    /**
     * Delete tags from ALL saved video of the user
     *
     * @param userId userId
     * @param tags   tags to delete from all videos
     */
    void deleteTagsFromAllSavedVideosOfUser(String userId, List<String> tags);

    /**
     * Deletes all tags from videos
     *
     * @param userId   userId
     * @param videoIds videos Ids all from where all tags will be deleted from.
     */
    void deleteAllTagsFromSavedVideosOfUser(String userId, List<String> videoIds);

    /**
     * Get all the videos with the tags
     *
     * @param userId userId
     * @param tags   tags the videos need to have
     * @param skip   how many to skip
     * @param limit  how many to limit to
     * @return list of videoIds with the given tag
     */
    List<String> getAllVideosWithTags(String userId, List<String> tags, int skip, int limit);

    /**
     * Get all the tags of the given videos. No duplicates. If tag A exists in multiple videos it's only going to be listed once.
     *
     * @param userId   userId
     * @param videoIds videoIds to get the tags of
     * @param skip     how many to skip
     * @param limit    how many to limit to
     * @return tags present in all the videos combined
     */
    Set<String> getAllTagsOfVideos(String userId, List<String> videoIds, int skip, int limit);
}
