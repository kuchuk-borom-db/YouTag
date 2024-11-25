package dev.kuku.youtagserver.user_video_tag.api.events;

import java.util.List;

public record DeleteAllTagsFromSpecifiedSavedVideosOfUsers(List<String> userIds,
                                                           List<String> videoIds) {
}
