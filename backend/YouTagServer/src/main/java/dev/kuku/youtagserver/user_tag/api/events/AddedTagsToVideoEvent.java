package dev.kuku.youtagserver.user_tag.api.events;

import dev.kuku.youtagserver.user_tag.domain.UserTag;

import java.util.List;

public record AddedTagsToVideoEvent(List<UserTag> tagsEntity) {
}
