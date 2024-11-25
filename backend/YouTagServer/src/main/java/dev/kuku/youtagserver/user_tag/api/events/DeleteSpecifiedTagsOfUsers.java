package dev.kuku.youtagserver.user_tag.api.events;

import java.util.List;

public record DeleteSpecifiedTagsOfUsers(List<String> userIds, List<String> tags) {
}
