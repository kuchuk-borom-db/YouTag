package dev.kuku.youtagserver.user_video_tags.api.services;

import dev.kuku.youtagserver.user_video_tags.api.dto.UserVidTagDto;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoLinkNotFound;
import org.springframework.stereotype.Service;

@Service
public interface UserVidTagService {

    void linkUserAndVideo(String user, String video) throws UserAndVideoAlreadyLinked;

    void addTag(String user, String video, String tag) throws UserAndVideoLinkNotFound;

    UserVidTagDto getUserAndVideoTag(String user, String video) throws UserAndVideoLinkNotFound;
}
