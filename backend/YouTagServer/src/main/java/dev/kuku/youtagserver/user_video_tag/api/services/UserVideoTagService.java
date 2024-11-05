package dev.kuku.youtagserver.user_video_tag.api.services;

import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagAlreadyExists;
import dev.kuku.youtagserver.user_video_tag.api.exceptions.UserVideoTagNotFound;

public interface UserVideoTagService {
    void addTagToVid(String id, String userId, String tag) throws UserVideoTagAlreadyExists;

    void get(String id, String userId, String tag) throws UserVideoTagNotFound;
}
