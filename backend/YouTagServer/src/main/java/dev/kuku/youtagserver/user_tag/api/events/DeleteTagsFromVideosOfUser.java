package dev.kuku.youtagserver.user_tag.api.events;

import java.util.List;

public record DeleteTagsFromVideosOfUser(String userId, List<String> tags,
                                         List<String> videoIds) {
}
