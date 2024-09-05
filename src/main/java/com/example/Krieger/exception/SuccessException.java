package com.example.Krieger.exception;

import org.springframework.http.HttpStatus;

public class SuccessException extends RuntimeException {

    private HttpStatus httpStatus;
    private Object data;

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
