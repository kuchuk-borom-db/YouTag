package dev.kuku.youtagserver.user_video.api.events;

import java.util.List;

public record UnlinkedVideoFromUser(String userId, List<String> videoId) {
}
