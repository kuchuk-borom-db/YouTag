package dev.kuku.youtagserver.user.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidUser extends ResponseException {
    public InvalidUser() {
        super(HttpStatus.BAD_REQUEST, "Invalid user");
    }
}
