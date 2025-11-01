package com.example.krieger.controller;

import com.example.Krieger.controller.AuthorController;
import com.example.Krieger.dto.AuthorDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.GlobalExceptionHandler;
import com.example.Krieger.exception.ValidationExceptionHandler;
import com.example.Krieger.messaging.OutboxPublisher;
import com.example.Krieger.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Adjust endpoint paths if your controller base mapping differs.
 * Assumes @RequestMapping("/api/authors") on AuthorController.
 */
@WebMvcTest(controllers = AuthorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ValidationExceptionHandler.class, GlobalExceptionHandler.class})
class AuthorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private com.example.Krieger.config.jwt.JwtRequestFilter jwtRequestFilter;

    @MockBean
    private com.example.Krieger.config.services.AuthenticationService authenticationService;

    @MockBean private OutboxPublisher outboxPublisher;

    @Test
    void createAuthor_valid_returns201() throws Exception {
        // Arrange: mock service return
        Author saved = new Author();
        saved.setId(1L);
        saved.setFirstName("John");
        saved.setLastName("Doe");
        when(authorService.createAuthor(any(AuthorDTO.class))).thenReturn(saved);

        // Act + Assert: valid payload -> 201 Created
        String body = "{ \"firstName\": \"John\", \"lastName\": \"Doe\" }";

        mvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(authorService, times(1)).createAuthor(any(AuthorDTO.class));
    }

    @Test
    void getAuthorById_notFound_returns404() throws Exception {
        when(authorService.getAuthorById(1L)).thenReturn(null);

        mvc.perform(get("/api/authors/1"))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).getAuthorById(1L);
    }

    @Test
    void createAuthor_missingFirstName_returns400_withFieldError() throws Exception {
        // Missing firstName -> should be 400 with field error; service must not be called
        String body = "{ \"lastName\": \"Doe\" }";

        mvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='firstName')]").exists());

        verifyNoInteractions(authorService);
    }
}
