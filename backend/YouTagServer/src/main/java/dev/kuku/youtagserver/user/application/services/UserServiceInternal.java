package dev.kuku.youtagserver.user.application.services;

import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.services.UserService;
import dev.kuku.youtagserver.user.domain.exception.InvalidEmailException;

public interface UserServiceInternal extends UserService {
    boolean addUser(UserDTO user) throws InvalidEmailException;

    boolean updateUser(UserDTO user);

    boolean isUserOutdated(UserDTO user);
}
