package dev.kuku.youtagserver.user_video.api.exception;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class UserVideoLinkNotFound extends ResponseException {
    public UserVideoLinkNotFound(String userId, String videoId) {
        super(HttpStatus.NOT_FOUND, String.format("User %s not linked to video %s", userId, videoId));
    }
}
