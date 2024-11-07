package dev.kuku.youtagserver.user_video_tag.api.events;

import java.util.List;

public record AddedTagsToVideo(String userId, String videoId, List<String> tags) {
}
