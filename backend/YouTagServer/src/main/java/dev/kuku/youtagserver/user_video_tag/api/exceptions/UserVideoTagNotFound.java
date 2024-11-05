package dev.kuku.youtagserver.user_video_tag.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class UserVideoTagNotFound extends ResponseException {
    public UserVideoTagNotFound(String userId, String id, String tag) {
        super(HttpStatus.NOT_FOUND, String.format("User Video Tag not found %s %s %s", userId, id, tag));
    }
}
