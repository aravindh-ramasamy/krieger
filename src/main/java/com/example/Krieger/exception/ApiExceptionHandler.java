package com.example.Krieger.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Centralized API error mapping (add to existing handler if you already have one). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPagination(InvalidPaginationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "invalid_pagination");
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }
}
