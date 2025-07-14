package com.store.mgmt.auth.controller;

import com.store.mgmt.auth.exception.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(jakarta.validation.ConstraintViolationException ex) {
        Map<String, String> response = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                response.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}