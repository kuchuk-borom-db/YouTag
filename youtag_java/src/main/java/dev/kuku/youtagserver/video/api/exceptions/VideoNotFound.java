package dev.kuku.youtagserver.video.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class VideoNotFound extends ResponseException {
    public VideoNotFound(String id) {
        super(HttpStatus.NOT_FOUND, "Video with ID " + id + " not found");
    }
}
