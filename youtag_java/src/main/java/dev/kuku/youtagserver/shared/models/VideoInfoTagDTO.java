package dev.kuku.youtagserver.shared.models;

import dev.kuku.youtagserver.video.api.dto.VideoDTO;

import java.util.Collections;
import java.util.Set;

public record VideoInfoTagDTO(VideoDTO videoDTO, Set<String> tags) {

    // Constructor
    public VideoInfoTagDTO(VideoDTO videoDTO, Set<String> tags) {
        this.videoDTO = videoDTO;
        this.tags = (tags == null) ? Collections.emptySet() : tags;
    }

}
