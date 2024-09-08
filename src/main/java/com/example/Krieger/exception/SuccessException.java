package com.example.Krieger.exception;

import org.springframework.http.HttpStatus;

public class SuccessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final Object data;

    public SuccessException(String message, HttpStatus httpStatus, Object data) {
        super(message);
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Object getData() {
        return data;
    }
}
