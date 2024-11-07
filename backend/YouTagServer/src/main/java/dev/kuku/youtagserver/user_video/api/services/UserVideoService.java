package dev.kuku.youtagserver.user_video.api.services;

import dev.kuku.youtagserver.user_video.api.dto.UserVideoDTO;
import dev.kuku.youtagserver.user_video.api.exception.UserVideoLinkNotFound;
import dev.kuku.youtagserver.user_video.api.exception.VideoAlreadyLinkedToUser;

import java.util.List;

public interface UserVideoService {
    //CREATE
    void create(String userId, String videoId) throws VideoAlreadyLinkedToUser;

    //READ
    UserVideoDTO get(String userId, String videoId) throws UserVideoLinkNotFound;

    List<UserVideoDTO> getWithUserId(String userId);

    //DELETE
    void delete(String userId, String videoId) throws UserVideoLinkNotFound;

    void deleteAll(String userId);

}
