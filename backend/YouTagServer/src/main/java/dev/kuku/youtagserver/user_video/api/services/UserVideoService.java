package dev.kuku.youtagserver.user_video.api.services;

import dev.kuku.youtagserver.shared.api.services.Service;
import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.domain.UserVideo;

import java.util.List;

public interface UserVideoService extends Service<UserVideo, UserVideoDTO> {

    /**
     * Save a video to a user.
     *
     * @param userId   userId
     * @param videoIds ID of the video to save
     */
    void saveVideoToUser(String userId, List<String> videoIds);

    /**
     * Remove saved video from user
     *
     * @param userId  userId
     * @param videoId videoId to remove
     */
    void removeSavedVideosFromUser(String userId, List<String> videoId);

    /**
     * get all saved videos of a user
     *
     * @param userId userId
     * @param skip   how many to skip
     * @param limit  how many to limit
     * @return all videos of the user
     */
    List<String> getAllSavedVideosOfUser(String userId, int skip, int limit);

    /**
     * Get specified videos of the user if they are saved for user
     *
     * @param userId   userId
     * @param videoIds ID of videos to get
     * @return valid saved specified videos of the user
     */
    List<String> getSpecificSavedVideosOfUser(String userId, List<String> videoIds);
}
