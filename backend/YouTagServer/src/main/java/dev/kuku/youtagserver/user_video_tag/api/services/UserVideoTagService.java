package dev.kuku.youtagserver.user_video_tag.api.services;

import dev.kuku.youtagserver.user_video_tag.api.dto.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagAlreadyExists;

import java.util.List;

public interface UserVideoTagService {
    //Create
    void addTagToVid(String userId, String videoId, String tag) throws UserVideoTagAlreadyExists;

    //Read
    UserVideoTagDTO get(String userId, String videoId, String tag);

    List<UserVideoTagDTO> getWithUserId(String userId);

    List<UserVideoTagDTO> getWithVideoId(String videoId);

    List<UserVideoTagDTO> getWithTag(String tag);

    List<UserVideoTagDTO> getWithUserIdAndVideoId(String userId, String videoId);

    List<UserVideoTagDTO> getWithUserIdAndTag(String userId, String tag);

    List<UserVideoTagDTO> getWithUserIdAndTag(String userId, String[] tags);

    List<UserVideoTagDTO> getWithVideoIdAndTag(String videoId, String tag);

    //Delete
    void delete(String userId, String videoId, String tag);

    void deleteWithUserId(String userId);

    void deleteWithVideoId(String videoId);

    void deleteWithTag(String tag);

    void deleteWithUserIdAndVideoId(String userId, String videoId);

    void deleteWithUserIdAndTag(String userId, String tag);

    void deleteWithVideoIdAndTag(String videoId, String tag);
}
