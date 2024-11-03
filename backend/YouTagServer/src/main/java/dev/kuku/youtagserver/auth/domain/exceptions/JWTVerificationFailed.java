package dev.kuku.youtagserver.auth.domain.exceptions;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import org.springframework.http.HttpStatus;

public class JWTVerificationFailed extends ResponseException {
    public JWTVerificationFailed(String invalidJwt) {
        super(HttpStatus.UNAUTHORIZED, invalidJwt);
    }
}
