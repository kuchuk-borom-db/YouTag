package dev.kuku.youtagserver.user_video.api;


import dev.kuku.youtagserver.shared.application.Service;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoNotFound;
import dev.kuku.youtagserver.user_video.domain.UserVideo;

import java.util.List;

public interface UserVideoService extends Service<UserVideo, UserVideoDTO> {

    void linkVideoToUser(String userId, String videoId) throws UserVideoAlreadyLinked;

    void unlinkVideoFromUser(String userId, String videoId) throws UserVideoNotFound;

    void unlinkAllVideosFromUser(String userId);

    void unlinkVideoFromAllUsers(String videoId);

    List<UserVideoDTO> getVideosOfUser(String userId, int skip, int limit);

    boolean isVideoLinkedWithUser(String userId, String videoId);

}
