package dev.kuku.youtagserver.video.api.events;

public class VideoAddedEvent {
    String videoId;

    public VideoAddedEvent(String videoId) {
        this.videoId = videoId;
    }
}
