package dev.kuku.youtagserver.user_video.api;


import dev.kuku.youtagserver.shared.application.Service;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video.api.exceptions.UserVideoNotFound;
import dev.kuku.youtagserver.user_video.domain.UserVideo;

import java.util.List;

public interface UserVideoService extends Service<UserVideo, UserVideoDTO> {

    void saveVideoToUser(String userId, String videoId) throws UserVideoAlreadyLinked;

    void disconnectVidFromUser(String userId, String videoId) throws UserVideoNotFound;

    void removeAllConnectionOfUser(String userId);

    void removeConnectionFromAllUsers(String videoId);

    List<UserVideoDTO> getAllSavedVideosOfUser(String userId, int skip, int limit);

    List<UserVideoDTO> getSavedVideosOfUser(String currentUser, List<String> videoIds);

    boolean isVidSavedToUser(String userId, String videoId);

    void removeSavedVideosFromUser(String currentUser, List<String> videoIds);

}
