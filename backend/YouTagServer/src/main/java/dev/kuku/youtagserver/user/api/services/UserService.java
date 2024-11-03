package dev.kuku.youtagserver.user.api.services;

import dev.kuku.youtagserver.user.domain.entity.User;

public interface UserService {
    User getUser(String email);

    boolean addUser(User user);

    boolean updateUser(User user);

    boolean isUserOutdated(User user);
}
