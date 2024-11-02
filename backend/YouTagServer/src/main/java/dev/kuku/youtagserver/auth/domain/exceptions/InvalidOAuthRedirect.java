package dev.kuku.youtagserver.auth.domain.exceptions;

import dev.kuku.youtagserver.common.domain.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidOAuthRedirect extends ResponseException {
    public InvalidOAuthRedirect(String s) {
        super(HttpStatus.BAD_REQUEST, s);
    }
}
