package com.example.Krieger.exception;

import com.example.Krieger.DTO.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        ApiResponse<Void> response = new ApiResponse<>(
                ex.getMessage(),    // Custom message from the controller
                "ERROR",            // Status for errors
                ex.getHttpStatus().value(),
                null                // No data in case of an error
        );
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    @ExceptionHandler(SuccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleSuccessException(SuccessException ex) {
        ApiResponse<Object> response = new ApiResponse<>(
                ex.getMessage(),    // Custom success message from the controller
                "SUCCESS",          // Status for success
                ex.getHttpStatus().value(),
                ex.getData()        // Data included in case of success
        );
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>(
                "An unexpected error occurred", // Default error message
                "ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
