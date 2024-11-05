package dev.kuku.youtagserver.shared.exceptions;

import org.springframework.http.HttpStatus;

public class AuthenticatedUserNotFound extends ResponseException {
    public AuthenticatedUserNotFound() {
        super(HttpStatus.NOT_ACCEPTABLE, "Authenticated user not found");
    }
}
