package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoIDException;
import dev.kuku.youtagserver.video.api.services.VideoService;

public interface VideoServiceInternal extends VideoService {
    /**
     * Validates videoId and userEmail. <br>
     * Saves videoId to database if missing and fire "VideoAdded" event. <br>
     * Links the user and the videoId. <br>
     * @param videoId id of the video NOT complete. url
     * @param userEmail user email
     * @throws UserAndVideoAlreadyLinked if already linked
     * @throws InvalidVideoIDException if videoID is invalid
     */
    void addVideoForUser(String videoId, String userEmail) throws UserAndVideoAlreadyLinked, InvalidVideoIDException;
}
