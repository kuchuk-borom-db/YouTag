package dev.kuku.youtagserver.user.event.dispatcher.api;

import dev.kuku.youtagserver.user.entity.internal.User;

public record UserAddedEvent(User user) {
}
