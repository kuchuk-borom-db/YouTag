package dev.kuku.youtagserver.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ResponseException extends Throwable {
    final HttpStatusCode code;

    public ResponseException(HttpStatusCode code, String message) {
        super(message);
        this.code = code;
    }
}
