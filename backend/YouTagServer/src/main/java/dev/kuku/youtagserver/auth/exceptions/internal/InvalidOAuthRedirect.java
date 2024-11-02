package dev.kuku.youtagserver.auth.exceptions.internal;

import dev.kuku.youtagserver.common.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class InvalidOAuthRedirect extends ResponseException {
    public InvalidOAuthRedirect(String s) {
        super(HttpStatus.BAD_REQUEST, s);
    }
}
