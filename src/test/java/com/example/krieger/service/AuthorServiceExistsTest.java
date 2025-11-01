package com.example.krieger.service;

import com.example.Krieger.exception.CustomException;
import com.example.Krieger.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceExistsTest {

    @Mock AuthorRepository authorRepository;
    @InjectMocks com.example.Krieger.service.AuthorService authorService;

    @Test
    void authorExists_trimsAndIgnoresCase() {
        when(authorRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase("john", "doe"))
                .thenReturn(true);

        assertTrue(authorService.authorExistsByName("  John  ", "  DOE "));
    }

    @Test
    void authorExists_blankParams_throws400() {
        CustomException ex = assertThrows(CustomException.class,
                () -> authorService.authorExistsByName(" ", "   "));
        assertEquals(400, ex.getHttpStatus().value());
    }
}
