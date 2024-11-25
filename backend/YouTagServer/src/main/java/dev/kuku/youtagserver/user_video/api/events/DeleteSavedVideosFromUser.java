package dev.kuku.youtagserver.user_video.api.events;

import java.util.List;

public record DeleteSavedVideosFromUser(String userId, List<String> videoIds) {
}
