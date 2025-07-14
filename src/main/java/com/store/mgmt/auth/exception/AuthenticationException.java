package com.store.mgmt.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus makes Spring automatically return the specified HTTP status
// when this exception is thrown from a controller.
@ResponseStatus(HttpStatus.UNAUTHORIZED) // Default to 401 Unauthorized
public class AuthenticationException extends RuntimeException {

    private HttpStatus status;

    public AuthenticationException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED; // Default if not specified
    }

    public AuthenticationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}