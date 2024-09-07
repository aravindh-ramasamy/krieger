package com.example.krieger.controller;

import com.example.Krieger.controller.AuthorController;
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
import static org.mockito.Mockito.when;

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
        Author mockAuthor = new Author();
        mockAuthor.setFirstName("John");
        mockAuthor.setLastName("Doe");

        when(authorService.createAuthor(any(Author.class))).thenReturn(mockAuthor);

        SuccessException thrown = assertThrows(SuccessException.class, () -> {
            authorController.createAuthor(mockAuthor);
        });

        assertEquals("Author created successfully", thrown.getMessage());
        assertEquals(mockAuthor, thrown.getData());
        assertEquals(HttpStatus.CREATED, thrown.getHttpStatus());
    }

    @Test
    void createAuthorEmptyFields() {
        Author mockAuthor = new Author();

        CustomException thrown = assertThrows(CustomException.class, () -> {
            authorController.createAuthor(mockAuthor);
        });

        assertEquals("First name or last name cannot be empty", thrown.getMessage());
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