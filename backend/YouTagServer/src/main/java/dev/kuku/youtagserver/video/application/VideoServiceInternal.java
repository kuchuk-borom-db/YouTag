package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.user_video_tags.api.exceptions.UserAndVideoAlreadyLinked;

public interface VideoServiceInternal {
    boolean addVideoForUser(String video, String userEmail) throws UserAndVideoAlreadyLinked;
}
