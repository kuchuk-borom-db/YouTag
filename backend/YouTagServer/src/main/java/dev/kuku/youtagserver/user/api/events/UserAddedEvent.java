package dev.kuku.youtagserver.user.api.events;

import dev.kuku.youtagserver.user.domain.User;

public record UserAddedEvent(User user) {
}
