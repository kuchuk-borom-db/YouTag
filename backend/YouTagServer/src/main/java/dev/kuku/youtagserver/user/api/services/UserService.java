package dev.kuku.youtagserver.user.api.services;

import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.InvalidUser;
import dev.kuku.youtagserver.user.api.exceptions.UserAlreadyExists;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;

public interface UserService {
    UserDTO getUser(String email) throws EmailNotFound, UserDTOHasNullValues;

    void addUser(UserDTO userDTO) throws UserAlreadyExists, InvalidUser;

    void updateUser(UserDTO userDTO) throws InvalidUser;

    void deleteUser(String email) throws EmailNotFound, UserDTOHasNullValues;

    boolean isUserOutdated(UserDTO user);

}
