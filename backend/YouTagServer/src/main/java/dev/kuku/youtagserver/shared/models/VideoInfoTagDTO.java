package dev.kuku.youtagserver.shared.models;

import dev.kuku.youtagserver.shared.exceptions.VideoInfoTagDTOHasNullValues;
import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class VideoInfoTagDTO {

    // Getters
    private final VideoDTO videoDTO;
    private final List<String> tags;

    // Constructor
    public VideoInfoTagDTO(VideoDTO videoDTO, List<String> tags) throws VideoInfoTagDTOHasNullValues {
        if (videoDTO == null) {
            throw new VideoInfoTagDTOHasNullValues(this);
        }
        this.videoDTO = videoDTO;
        this.tags = (tags == null) ? Collections.emptyList() : tags;
    }

}
