package dev.kuku.youtagserver.video.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class VideoAlreadyExists extends ResponseException {
    public VideoAlreadyExists(String id) {
        super(HttpStatus.ALREADY_REPORTED, String.format("Video %s already exists", id));
    }
}
