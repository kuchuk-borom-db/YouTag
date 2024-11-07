package dev.kuku.youtagserver.shared.exceptions;

import dev.kuku.youtagserver.shared.models.VideoInfoTagDTO;
import org.springframework.http.HttpStatus;

public class VideoInfoTagDTOHasNullValues extends ResponseException {
    public VideoInfoTagDTOHasNullValues(VideoInfoTagDTO videoInfoTagDTO) {
        super(HttpStatus.BAD_REQUEST, String.format("Video-info and tag dto has null values : %s", videoInfoTagDTO));
    }
}
