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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthorController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthorRouteMappingDeleteTest {

    @Autowired MockMvc mvc;
    @MockBean AuthorService authorService;
    @MockBean OutboxPublisher outbox;
    @MockBean
    private com.example.Krieger.config.jwt.JwtRequestFilter jwtRequestFilter;

    @Test
    void route_maps_to_deleteAuthor() throws Exception {
        Mockito.when(authorService.getAuthorById(7L)).thenReturn(new Author(7L, "A", "B"));
        mvc.perform(delete("/api/authors/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(handler().handlerType(AuthorController.class))
                .andExpect(handler().methodName("deleteAuthor"));
    }
}
