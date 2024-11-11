package dev.kuku.youtagserver.user_video.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class UserVideoAlreadyLinked extends ResponseException {
    public UserVideoAlreadyLinked(String userId, String videoId) {
        super(HttpStatus.ALREADY_REPORTED, String.format("User %s Video %s already exists", userId, videoId));
    }
}
