package dev.kuku.youtagserver.user_video_tag.api.events;

import java.util.List;

public record DeleteSpecificTagsFromAllSavedVideos(String userId, List<String> tags) {
}
