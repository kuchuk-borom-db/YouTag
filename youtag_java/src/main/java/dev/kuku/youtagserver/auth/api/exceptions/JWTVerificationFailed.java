package dev.kuku.youtagserver.auth.api.exceptions;

public class JWTVerificationFailed extends RuntimeException {
    public JWTVerificationFailed(String text) {
        super(text);
    }
}
