package dev.kuku.youtagserver.user_video.api.services;

import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;

import java.util.List;

public interface UserVideoService {
    void linkVideoToUser(String videoId, String currentUserId) throws VideoAlreadyLinkedToUser;

    void unlinkVideoFromUser(String videoId, String currentUserId) throws UserVideoLinkNotFound;

    UserVideoDTO getUserVideo(String userId, String videoId) throws UserVideoLinkNotFound;

    List<UserVideoDTO> getUserVideosOfUser(String userId);

    boolean isVideoLinkedToUser(String email, String id);

    List<UserVideoDTO> getUserVideosContainingVideoId(String videoId);

}
