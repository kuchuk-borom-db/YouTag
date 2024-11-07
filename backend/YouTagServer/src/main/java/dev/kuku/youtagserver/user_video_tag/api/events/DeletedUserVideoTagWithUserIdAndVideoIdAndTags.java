package dev.kuku.youtagserver.user_video_tag.api.events;

import java.util.List;

public record DeletedUserVideoTagWithUserIdAndVideoIdAndTags(String userId, String videoId, List<String> tags) {
}
