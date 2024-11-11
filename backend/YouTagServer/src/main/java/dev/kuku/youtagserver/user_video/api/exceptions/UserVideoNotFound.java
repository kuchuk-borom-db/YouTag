package dev.kuku.youtagserver.user_video.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class UserVideoNotFound extends ResponseException {
    public UserVideoNotFound(String userId, String videoId) {
        super(HttpStatus.NOT_FOUND, String.format("UserVideo not found with userId %s and videoId %s", userId, videoId));
    }
}
