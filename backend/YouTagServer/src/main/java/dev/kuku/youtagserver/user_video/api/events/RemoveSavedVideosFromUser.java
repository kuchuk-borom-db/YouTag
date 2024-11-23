package dev.kuku.youtagserver.user_video.api.events;

import java.util.List;

public record RemoveSavedVideosFromUser(String userId, List<String> videoIds) {
}
