package dev.kuku.youtagserver.user.api.services;

import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.domain.exception.InvalidEmailException;

public interface UserService {
    UserDTO getUser(String email) throws InvalidEmailException;
}
