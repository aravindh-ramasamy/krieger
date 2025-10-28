package com.example.krieger.service;

import com.example.Krieger.config.RabbitMQConfig;
import com.example.Krieger.dto.AuthorDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAuthor_Success() {
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setFirstName("John");
        authorDTO.setLastName("Doe");
        Author savedAuthor = new Author();
        savedAuthor.setId(1L);
        savedAuthor.setFirstName("John");
        savedAuthor.setLastName("Doe");
        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);
        Author createdAuthor = authorService.createAuthor(authorDTO);

        assertNotNull(createdAuthor);
        assertEquals("John", createdAuthor.getFirstName());
        assertEquals("Doe", createdAuthor.getLastName());

        // Verify repository and RabbitMQ
        verify(authorRepository, times(1)).save(any(Author.class));  // Verify save operation
        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                "CREATE: " + createdAuthor.getId()  // verify RabbitMQ message is published
        );
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
        Author mockAuthor = new Author();
        mockAuthor.setId(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(mockAuthor));
        doNothing().when(authorRepository).deleteById(1L);

        assertDoesNotThrow(() -> authorService.deleteAuthor(1L));

        // Verify repository and RabbitMQ event publishing
        verify(authorRepository, times(1)).findById(1L);
        verify(authorRepository, times(1)).deleteById(1L);
        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                "DELETE: " + mockAuthor.getId()
        );
    }
}