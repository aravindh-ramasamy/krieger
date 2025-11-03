package com.example.Krieger.exception;

/** Thrown when pagination query params are invalid. */
public class InvalidPaginationException extends RuntimeException {
    public InvalidPaginationException(String message) {
        super(message);
    }
}
