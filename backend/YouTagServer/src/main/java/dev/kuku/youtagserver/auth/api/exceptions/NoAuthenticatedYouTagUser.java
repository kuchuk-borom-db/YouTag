package dev.kuku.youtagserver.auth.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class NoAuthenticatedYouTagUser extends ResponseException {
    public NoAuthenticatedYouTagUser() {
        super(HttpStatus.FORBIDDEN, "No you tag user found. Please check Authorization token and pass a valid JWT token");
    }
}
