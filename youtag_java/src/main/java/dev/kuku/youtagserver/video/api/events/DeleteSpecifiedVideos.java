package dev.kuku.youtagserver.video.api.events;

import java.util.List;

public record DeleteSpecifiedVideos(List<String> videoIds) {
}
