package com.example.krieger.service;

import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentServiceTransactionalAnnotationTest {

    @Test
    void methods_have_transactional_annotation() throws Exception {
        Class<?> dto = Class.forName("com.example.Krieger.dto.DocumentDTO");

        // Service methods use wrapper Long, not primitive long
        assertAnnotated(DocumentService.class.getMethod("createDocument", dto));
        assertAnnotated(DocumentService.class.getMethod("updateDocument", Long.class, dto));
        assertAnnotated(DocumentService.class.getMethod("deleteDocument", Long.class));
    }

    private static void assertAnnotated(Method m) {
        assertTrue(m.isAnnotationPresent(Transactional.class),
                "Expected @Transactional on " + m.getName());
    }
}
