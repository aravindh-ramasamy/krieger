package com.example.krieger.controller;

import com.example.Krieger.controller.AuthorController;
import com.example.Krieger.dto.AuthorDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController authorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAuthor() {
        AuthorDTO mockAuthorDTO = new AuthorDTO();
        mockAuthorDTO.setFirstName("John");
        mockAuthorDTO.setLastName("Doe");
        Author mockAuthor = new Author();
        mockAuthor.setFirstName("John");
        mockAuthor.setLastName("Doe");
        when(authorService.createAuthor(any(AuthorDTO.class))).thenReturn(mockAuthor);
        SuccessException thrown = assertThrows(SuccessException.class, () -> {
            authorController.createAuthor(mockAuthorDTO);
        });


        assertEquals("Author created successfully", thrown.getMessage());
        assertEquals(mockAuthor, thrown.getData());
        assertEquals(HttpStatus.CREATED, thrown.getHttpStatus());
        verify(authorService, times(1)).createAuthor(any(AuthorDTO.class));
    }

    @Test
    void createAuthorEmptyFields() {
        AuthorDTO mockAuthor = new AuthorDTO();

        CustomException thrown = assertThrows(CustomException.class, () -> {
            authorController.createAuthor(mockAuthor);
        });

        assertEquals("First name or last name cannot be empty", thrown.getMessage());
        verify(authorService, never()).createAuthor(any(AuthorDTO.class));
    }

    @Test
    void getAuthorByIdNotFound() {
        when(authorService.getAuthorById(1L)).thenReturn(null);

        CustomException thrown = assertThrows(CustomException.class, () -> {
            authorController.getAuthorById(1L);
        });

        assertEquals("Author not found with ID: 1", thrown.getMessage());
    }

}