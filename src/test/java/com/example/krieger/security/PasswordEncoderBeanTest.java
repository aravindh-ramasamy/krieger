package com.example.krieger.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PasswordEncoderBeanTest {

    @Autowired private PasswordEncoder encoder;

    @Test
    void passwordEncoder_isPresent_andEncodes() {
        assertNotNull(encoder);
        String raw = "secret";
        String hash = encoder.encode(raw);
        assertNotNull(hash);
        assertNotEquals(raw, hash);
        assertTrue(encoder.matches(raw, hash));
    }
}
