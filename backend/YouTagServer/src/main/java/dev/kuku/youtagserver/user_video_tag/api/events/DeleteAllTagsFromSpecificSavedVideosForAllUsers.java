package dev.kuku.youtagserver.user_video_tag.api.events;

import java.util.List;

public record DeleteAllTagsFromSpecificSavedVideosForAllUsers(List<String> affectedUsers,
                                                              List<String> videoIds) {
}
