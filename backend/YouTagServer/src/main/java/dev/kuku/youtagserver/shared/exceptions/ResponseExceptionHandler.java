package dev.kuku.youtagserver.shared.exceptions;

import dev.kuku.youtagserver.shared.models.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
class ResponseExceptionHandler {
    @ExceptionHandler(ResponseException.class)
    ResponseEntity<ResponseModel<Object>> handler(ResponseException e) {
        log.error(e.getMessage());
        log.error(Arrays.toString(e.getStackTrace()));
        return ResponseEntity.status(e.getCode()).body(new ResponseModel<>(null, e.getMessage()));
    }
}
