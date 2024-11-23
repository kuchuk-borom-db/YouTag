package dev.kuku.youtagserver.user_video_tag.api.events;

import java.util.List;

public record DeleteAllTagsFromSpecificSavedVideosOfUser(String userId,
                                                         List<String> videoIds) {
}
