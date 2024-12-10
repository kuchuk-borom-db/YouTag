package dev.kuku.youtagserver.webscraper.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidVideoId extends ResponseException {
    public InvalidVideoId(String videoID) {
        super(HttpStatus.BAD_REQUEST, String.format("Invalid video ID %s", videoID));
    }
}
