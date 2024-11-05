package dev.kuku.youtagserver.user.api.services;

import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;

public interface UserService {
    UserDTO getUser(String email) throws EmailNotFound;
}
