package dev.kuku.youtagserver.user.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.user.api.dto.UserDTO;
import org.springframework.http.HttpStatus;

public class UserDTOHasNullValues extends ResponseException {
    public UserDTOHasNullValues(UserDTO userDTO) {
        super(HttpStatus.BAD_REQUEST, String.format("UserDTO has null values %s", userDTO.toString()));
    }
}
