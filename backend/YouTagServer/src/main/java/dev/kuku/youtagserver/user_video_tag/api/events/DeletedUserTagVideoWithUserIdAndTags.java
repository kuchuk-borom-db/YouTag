package dev.kuku.youtagserver.user_video_tag.api.events;

public record DeletedUserTagVideoWithUserIdAndTags(String userId, String[] tags) {
}
