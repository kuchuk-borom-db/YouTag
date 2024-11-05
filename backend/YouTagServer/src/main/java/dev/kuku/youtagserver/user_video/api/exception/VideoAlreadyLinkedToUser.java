package dev.kuku.youtagserver.user_video.api.exception;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class VideoAlreadyLinkedToUser extends ResponseException {
    public VideoAlreadyLinkedToUser(String userId, String videoId) {
        super(HttpStatus.ALREADY_REPORTED, String.format("Video %s is already linked to user %s", videoId, userId));
    }
}
