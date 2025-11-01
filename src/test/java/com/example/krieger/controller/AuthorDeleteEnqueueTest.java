package com.example.krieger.controller;

import com.example.Krieger.controller.AuthorController;
import com.example.Krieger.entity.Author;
import com.example.Krieger.messaging.OutboxPublisher;
import com.example.Krieger.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthorController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthorDeleteEnqueueTest {

    @Autowired MockMvc mvc;

    @MockBean AuthorService authorService;
    @MockBean OutboxPublisher outbox;
    @MockBean
    private com.example.Krieger.config.jwt.JwtRequestFilter jwtRequestFilter;

    @Test
    void delete_existingAuthor_enqueuesAndReturns202() throws Exception {
        Mockito.when(authorService.getAuthorById(42L))
                .thenReturn(new Author(42L, "John", "Doe"));

        mvc.perform(delete("/api/authors/{id}", 42L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        verify(outbox).publishAuthorDelete(42L);
    }

    @Test
    void delete_missingAuthor_returns404_noEnqueue() throws Exception {
        Mockito.when(authorService.getAuthorById(99L)).thenReturn(null);

        mvc.perform(delete("/api/authors/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(outbox, never()).publishAuthorDelete(anyLong());
    }
}
