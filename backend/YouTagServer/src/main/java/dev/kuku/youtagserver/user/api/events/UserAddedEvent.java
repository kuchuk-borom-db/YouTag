package dev.kuku.youtagserver.user.api.events;

import dev.kuku.youtagserver.user.domain.entity.User;

public record UserAddedEvent(User user) {
}
