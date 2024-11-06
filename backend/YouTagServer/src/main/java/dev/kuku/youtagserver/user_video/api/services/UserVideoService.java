package dev.kuku.youtagserver.user_video.api.services;

import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;

import java.util.List;

public interface UserVideoService {
    //CREATE
    void create(String userId, String videoId) throws VideoAlreadyLinkedToUser;

    //READ
    UserVideoDTO get(String userId, String videoId);

    List<UserVideoDTO> getWithUserId(String userId);

    List<UserVideoDTO> getWithVideoId(String videoId);

    //DELETE
    void delete(String userId, String videoId) throws UserVideoLinkNotFound;

    void deleteWithUserId(String userId);

    void deleteWithVideoId(String videoId);

}
