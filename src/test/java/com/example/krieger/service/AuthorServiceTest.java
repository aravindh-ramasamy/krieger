package com.example.krieger.service;

import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAuthor_Success() {
        Author author = new Author();
        author.setFirstName("John");
        author.setLastName("Doe");
        when(authorRepository.save(any(Author.class))).thenReturn(author);
        Author createdAuthor = authorService.createAuthor(author);
        assertNotNull(createdAuthor);
        assertEquals("John", createdAuthor.getFirstName());
    }

    @Test
    void getAuthorById_NotFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            authorService.getAuthorById(1L);
        });
        verify(authorRepository, times(1)).findById(1L);
    }

    @Test
    void deleteAuthor_Success() {
        doNothing().when(authorRepository).deleteById(1L);

        assertDoesNotThrow(() -> authorService.deleteAuthor(1L));
    }
}