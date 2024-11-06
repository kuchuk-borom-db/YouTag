package dev.kuku.youtagserver.user.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExists extends ResponseException {
    public UserAlreadyExists(String userId) {
        super(HttpStatus.ALREADY_REPORTED, String.format("User %s already exists", userId));
    }
}
