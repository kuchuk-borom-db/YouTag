package dev.kuku.youtagserver.user_video_tag.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class UserVideoTagAlreadyExists extends ResponseException {
    public UserVideoTagAlreadyExists(String userId, String id, String tag) {
        super(HttpStatus.ALREADY_REPORTED, String.format("User Video Tag already exists %s %s %s", userId, id, tag));
    }
}
