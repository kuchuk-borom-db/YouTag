package dev.kuku.youtagserver.shared.models;

import dev.kuku.youtagserver.video.api.dto.VideoDTO;

import java.util.Collections;
import java.util.List;

public record VideoInfoTagDTO(VideoDTO videoDTO, List<String> tags) {

    // Constructor
    public VideoInfoTagDTO(VideoDTO videoDTO, List<String> tags) {
        this.videoDTO = videoDTO;
        this.tags = (tags == null) ? Collections.emptyList() : tags;
    }

}
