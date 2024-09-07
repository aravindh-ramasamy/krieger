package com.example.krieger.exception;

import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void successException_ShouldReturnCorrectValues() {
        SuccessException successException = new SuccessException("Success", HttpStatus.OK, null);
        assertEquals("Success", successException.getMessage());
        assertEquals(HttpStatus.OK, successException.getHttpStatus());
    }

    @Test
    void customException_ShouldReturnCorrectValues() {
        CustomException customException = new CustomException("Error occurred", HttpStatus.BAD_REQUEST);
        assertEquals("Error occurred", customException.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
    }
}