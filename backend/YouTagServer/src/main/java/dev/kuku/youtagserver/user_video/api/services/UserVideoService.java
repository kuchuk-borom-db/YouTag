package dev.kuku.youtagserver.user_video.api.services;


import dev.kuku.youtagserver.shared.application.Service;
import dev.kuku.youtagserver.user_video.api.dtos.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video.domain.UserVideo;

import java.util.List;

public interface UserVideoService extends Service<UserVideo, UserVideoDTO> {
    /**
     * Save a video to the user
     *
     * @param userId  userId
     * @param videoId video to save for the user
     * @throws UserVideoAlreadyLinked if video is already linked
     */
    void saveVideoToUser(String userId, String videoId) throws UserVideoAlreadyLinked;

    /**
     * Save videos to user. Ignores if video is already saved for user
     *
     * @param userId   userId
     * @param videoIds videos to save to the user
     */
    void saveVideosToUser(String userId, List<String> videoIds);


    /**
     * Remove saved videos from user
     *
     * @param userId   userId
     * @param videoIds video Ids to remove
     */
    void removeSavedVideosFromUser(String userId, List<String> videoIds);

    /**
     * Get all videos saved for the user
     *
     * @param userId userId
     * @param skip   how many to skip
     * @param limit  how many to limit
     * @return video Ids saved for the user
     */
    List<String> getAllSavedVideosOfUser(String userId, int skip, int limit);

    /**
     * get list of videos saved for the user from the given videoIds. If the video is not saved for the user it will be ignored.
     *
     * @param userId   userId
     * @param videoIds video Ids to check
     * @return list of videos saved for the user from the given list
     */
    List<String> getSavedVideosOfUser(String userId, List<String> videoIds);

    /**
     * Check if a video is saved for user
     * @param userId userId
     * @param videoId videoId to check
     * @return true if video is saved for user
     */
    boolean isVidSavedToUser(String userId, String videoId);



}
