package dev.kuku.youtagserver.shared.exceptions;

import org.springframework.http.HttpStatusCode;

public class ResponseException extends Throwable {
    final HttpStatusCode code;

    public ResponseException(HttpStatusCode code, String message) {
        super(message);
        this.code = code;
    }
}
