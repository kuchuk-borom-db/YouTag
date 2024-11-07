package dev.kuku.youtagserver.video.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import org.springframework.http.HttpStatus;

public class VideoDTOHasNullValues extends ResponseException {
    public VideoDTOHasNullValues(VideoDTO videoDTO) {
        super(HttpStatus.BAD_REQUEST, "VideoDTO has null values. Please check again " + videoDTO.toString());
    }
}
