package dev.kuku.youtagserver.user_video.api.events;

import java.util.List;

public record DeleteAllSavedVideosFromUser(String userId, List<String> deletedVideos) {
}
