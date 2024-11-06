package dev.kuku.youtagserver.user.api.services;

import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.InvalidUser;
import dev.kuku.youtagserver.user.api.exceptions.UserAlreadyExists;

public interface UserService {
    UserDTO getUser(String email) throws EmailNotFound;
    void addUser(UserDTO userDTO) throws UserAlreadyExists, InvalidUser;
}
