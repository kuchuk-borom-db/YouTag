package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;
import dev.kuku.youtagserver.video.api.exceptions.InvalidVideoIDException;
import dev.kuku.youtagserver.video.api.services.VideoService;

public interface VideoServiceInternal extends VideoService {
    void addVideoForUser(String video, String userEmail) throws UserAndVideoAlreadyLinked, InvalidVideoIDException;
}
