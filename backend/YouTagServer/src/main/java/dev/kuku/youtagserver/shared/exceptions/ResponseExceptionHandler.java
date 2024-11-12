package dev.kuku.youtagserver.shared.exceptions;

import dev.kuku.youtagserver.shared.models.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
class ResponseExceptionHandler {

    @ExceptionHandler(ResponseException.class)
    ResponseEntity<ResponseModel<Object>> handleResponseException(ResponseException e) {
        log.error(e.getMessage());
        log.error(Arrays.toString(e.getStackTrace()));
        return ResponseEntity.status(e.getCode())
                .body(new ResponseModel<>(null, e.getMessage()));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            UsernameNotFoundException.class
    })
    ResponseEntity<ResponseModel<Object>> handleAuthenticationExceptions(AuthenticationException e) {
        log.error("Authentication failed: {}", e.getMessage());
        log.error(Arrays.toString(e.getStackTrace()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseModel<>(null, e.getMessage()));
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    ResponseEntity<ResponseModel<Object>> handleAuthServiceException(AuthenticationServiceException e) {
        log.error("Authentication service error: {}", e.getMessage());
        log.error(Arrays.toString(e.getStackTrace()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseModel<>(null, e.getMessage()));
    }
}