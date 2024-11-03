package dev.kuku.youtagserver.user.domain.exception;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidEmailException extends ResponseException {
    public InvalidEmailException(String email) {
        super(HttpStatus.BAD_REQUEST, "Invalid email : " + email);
    }
}
