package dev.kuku.youtagserver.tag.api.events;

import dev.kuku.youtagserver.tag.domain.Tag;

import java.util.List;

public record AddedTagsToVideoEvent(List<Tag> tagsEntity) {
}
