package dev.kuku.youtagserver.user_video.api.services;

import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;

public interface UserVideoService {
    void linkVideoToUser(String videoId, String currentUserId) throws VideoAlreadyLinkedToUser;

    UserVideoDTO getUserVideoByUserIdAndVideo(String userId, String videoId) throws UserVideoLinkNotFound;

    boolean isVideoLinkedToUser(String email, String id);
}
