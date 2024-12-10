package dev.kuku.youtagserver.auth.api.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class JwtTokenExpired extends ResponseException {
    public JwtTokenExpired() {
        super(HttpStatus.UNAUTHORIZED, "JWT Token Expired");
    }
}
