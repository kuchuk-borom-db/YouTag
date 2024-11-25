package dev.kuku.youtagserver.user_tag.api.events;

import java.util.List;

public record DeleteSpecifiedTagsOfUser(String userId, List<String> tagsToDelete) {
}
