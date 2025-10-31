package com.example.Krieger.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.*;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)  // ensure this runs before generic handlers
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                 HttpServletRequest request) {
        ApiError err = new ApiError();
        err.setStatus(400);
        err.setError("Bad Request");
        err.setMessage("Validation failed");
        err.setPath(request.getRequestURI());
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> err.addFieldError(fe.getField(), fe.getDefaultMessage()));
        return err;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException ex,
                                              HttpServletRequest request) {
        ApiError err = new ApiError();
        err.setStatus(400);
        err.setError("Bad Request");
        err.setMessage("Validation failed");
        err.setPath(request.getRequestURI());
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String field = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "unknown";
            err.addFieldError(field, v.getMessage());
        }
        return err;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("msg", "Invalid path parameter: " + ex.getName());
        body.put("status", "ERROR");
        body.put("code", 400);
        body.put("data", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

}
