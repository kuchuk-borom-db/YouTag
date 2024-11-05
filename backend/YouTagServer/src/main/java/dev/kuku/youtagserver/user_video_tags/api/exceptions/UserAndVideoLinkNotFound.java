package dev.kuku.youtagserver.user_video_tags.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class UserAndVideoLinkNotFound extends ResponseException {
    public UserAndVideoLinkNotFound(String videoID, String userID) {
        super(HttpStatus.NOT_FOUND, String.format("user %s doesn't have video %s linked", userID, videoID));
    }
}
