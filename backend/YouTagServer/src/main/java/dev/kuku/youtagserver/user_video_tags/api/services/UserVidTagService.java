package dev.kuku.youtagserver.user_video_tags.api.services;

import dev.kuku.youtagserver.user_video_tags.api.dto.UserVidTagDto;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoLinkNotFound;

public interface UserVidTagService {

    void linkUserAndVideo(String user, String video) throws UserAndVideoAlreadyLinked;

    void addTags(String user, String video, String[] tags) throws UserAndVideoLinkNotFound;

    UserVidTagDto getUserAndVideoTag(String user, String video) throws UserAndVideoLinkNotFound;
}
