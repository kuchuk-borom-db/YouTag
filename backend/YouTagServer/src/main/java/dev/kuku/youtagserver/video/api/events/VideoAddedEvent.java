package dev.kuku.youtagserver.video.api.events;

import lombok.Getter;

@Getter
public class VideoAddedEvent {
    String videoId;

    public VideoAddedEvent(String videoId) {
        this.videoId = videoId;
    }
}
