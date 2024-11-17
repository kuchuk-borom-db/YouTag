package dev.kuku.youtagserver.tag.api.events;

import java.util.List;

public record DeleteTagsFromAllVideosOfUser(String userId, List<String> tags) {
}
