package dev.kuku.youtagserver.user_video_tag.api.events;

import java.util.List;

public record DeleteTagsFromSpecificSavedVideos(String userId, List<String> videoIds,
                                                List<String> tags) {
}
