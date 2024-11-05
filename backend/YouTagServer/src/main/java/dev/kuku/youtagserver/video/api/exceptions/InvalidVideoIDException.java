package dev.kuku.youtagserver.video.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidVideoIDException extends ResponseException {
    public InvalidVideoIDException(String videoID) {
        super(HttpStatus.BAD_REQUEST, String.format("Invalid video ID %s", videoID));
    }
}
