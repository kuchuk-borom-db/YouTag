package dev.kuku.youtagserver.user_video.api.events;

import java.util.List;

public record VideosRemovedFromUser(String userId, List<String> videoIds) {
}
