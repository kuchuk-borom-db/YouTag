package dev.kuku.youtagserver.shared.api.events;

import java.util.Set;

public record RemoveVideosOrder(Set<String> invalidVideos) {
}
