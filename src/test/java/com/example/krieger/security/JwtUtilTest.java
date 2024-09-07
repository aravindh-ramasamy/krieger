package com.example.krieger.security;

import com.example.Krieger.config.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateToken_ShouldGenerateValidToken() {
        String token = jwtUtil.generateToken("random");
        assertNotNull(token);
        assertTrue(jwtUtil.extractUsername(token).equals("random"));
    }

    @Test
    void isTokenValid_Validation() {
        String token = jwtUtil.generateToken("random");
        assertTrue(jwtUtil.isTokenValid(token, "random"));
    }

    @Test
    void isTokenExpired_ExpiredToken() {
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZWZhdWx0VXNlciIsImlhdCI6MTcyNTYzNjcyOSwiZXhwIjoxNzI1NjQwMzI5fQ.3fyjXJH1U50Ci3BjIFntpWpqDXDAnfRAxowQMMaFBgA";
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.extractUsername(expiredToken));
    }
}
