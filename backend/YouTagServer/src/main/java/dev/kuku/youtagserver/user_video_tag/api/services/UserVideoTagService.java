package dev.kuku.youtagserver.user_video_tag.api.services;

import dev.kuku.youtagserver.user_video_tag.api.dto.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagAlreadyExists;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagNotFound;

import java.util.List;

public interface UserVideoTagService {
    void addTagToVid(String id, String userId, String tag) throws UserVideoTagAlreadyExists;

    List<UserVideoTagDTO> getVideosOfUserWithTag(String userId, String tag);

    UserVideoTagDTO get(String id, String userId, String tag) throws UserVideoTagNotFound;
}
