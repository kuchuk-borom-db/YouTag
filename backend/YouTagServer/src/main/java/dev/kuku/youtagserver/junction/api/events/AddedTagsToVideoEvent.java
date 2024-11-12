package dev.kuku.youtagserver.junction.api.events;

import dev.kuku.youtagserver.junction.domain.Tag;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public record AddedTagsToVideoEvent(List<Tag> tagsEntity) {
}
