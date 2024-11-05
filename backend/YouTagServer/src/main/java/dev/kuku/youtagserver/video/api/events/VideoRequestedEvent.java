package dev.kuku.youtagserver.video.api.events;

import lombok.Getter;

public class VideoRequestedEvent {
    @Getter
    private final String id;

    public VideoRequestedEvent(String id) {
        this.id = id;
    }
}
