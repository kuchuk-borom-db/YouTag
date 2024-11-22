package dev.kuku.youtagserver.user.api.services;

import dev.kuku.youtagserver.shared.api.services.Service;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import dev.kuku.youtagserver.user.api.exceptions.EmailNotFound;
import dev.kuku.youtagserver.user.api.exceptions.InvalidUser;
import dev.kuku.youtagserver.user.api.exceptions.UserAlreadyExists;
import dev.kuku.youtagserver.user.api.exceptions.UserDTOHasNullValues;
import dev.kuku.youtagserver.user.domain.User;

public interface UserService extends Service<User, UserDTO> {
    UserDTO getUser(String email) throws EmailNotFound, UserDTOHasNullValues;

    void addUser(String email, String name, String thumbnail) throws UserAlreadyExists, InvalidUser;

    void updateUser(UserDTO userDTO) throws InvalidUser;

    void deleteUser(String email) throws EmailNotFound, UserDTOHasNullValues;

    boolean isUserOutdated(UserDTO user);

}
