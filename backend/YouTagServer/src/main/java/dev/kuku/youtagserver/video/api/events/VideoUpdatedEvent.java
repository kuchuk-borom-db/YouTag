package dev.kuku.youtagserver.video.api.events;

import dev.kuku.youtagserver.video.api.dto.VideoDTO;

public record VideoUpdatedEvent(VideoDTO video) {
}
