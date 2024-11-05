package dev.kuku.youtagserver.user_video_tags.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class UserAndVideoAlreadyLinked extends ResponseException {
    public UserAndVideoAlreadyLinked(String videoID, String userID) {
        super(HttpStatus.BAD_REQUEST, String.format("user %s already has %s linked", userID, videoID));
    }
}
