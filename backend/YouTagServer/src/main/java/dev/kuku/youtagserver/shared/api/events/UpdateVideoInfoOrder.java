package dev.kuku.youtagserver.shared.api.events;

import dev.kuku.youtagserver.video.api.dto.VideoDTO;
import lombok.Getter;

@Getter
public class UpdateVideoInfoOrder {
    private final VideoDTO video;

    public UpdateVideoInfoOrder(VideoDTO videoDTO) {
        this.video = videoDTO;
    }
}
