package dev.kuku.youtagserver.user_video_tag.api.services;

import dev.kuku.youtagserver.user_video_tag.api.dto.UserVideoTagDTO;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagNotFound;

import java.util.List;

public interface UserVideoTagService {
    //Create
    void addTagsToVid(String userId, String videoId, List<String> tags);

    //Read
    UserVideoTagDTO get(String userId, String videoId, String tag) throws UserVideoTagNotFound;

    List<UserVideoTagDTO> getWithUserId(String userId);

    List<UserVideoTagDTO> getWithUserIdAndVideoId(String userId, String videoId);

    List<UserVideoTagDTO> getWithUserIdAndTags(String userId, String[] tags);

    //Delete
    void deleteWithUserIdAndTag(String userId, String[] tag);

    void deleteWithUserIdAndVideoIdAndTagIn(String userId, String videoId, List<String> tags);

}
