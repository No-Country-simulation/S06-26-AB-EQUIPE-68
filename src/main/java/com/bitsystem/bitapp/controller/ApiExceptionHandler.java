package com.bitsystem.bitapp.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ApiController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
        IllegalArgumentException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            Map.of("error", ex.getMessage())
        );
    }
}
