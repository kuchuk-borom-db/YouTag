package dev.kuku.youtagserver.user.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class EmailNotFound extends ResponseException {
    public EmailNotFound(String email) {
        super(HttpStatus.BAD_REQUEST, "Invalid email : " + email);
    }
}
