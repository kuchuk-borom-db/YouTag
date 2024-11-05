package dev.kuku.youtagserver.user_video.api.services;

import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;

import java.util.List;

public interface UserVideoService {
    void linkVideoToUser(String videoId, String currentUserId) throws VideoAlreadyLinkedToUser;

    UserVideoDTO getUserVideoByUserIdAndVideo(String userId, String videoId) throws UserVideoLinkNotFound;

    List<UserVideoDTO> getVideosByUserId(String userId);

    boolean isVideoLinkedToUser(String email, String id);
}
