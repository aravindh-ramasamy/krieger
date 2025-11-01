// src/test/java/com/example/krieger/controller/AuthorExistsApiTest.java
package com.example.krieger.controller;

import com.example.Krieger.config.jwt.JwtRequestFilter;
import com.example.Krieger.config.services.AuthenticationService;
import com.example.Krieger.controller.AuthorController;
import com.example.Krieger.service.AuthorService;
import com.example.Krieger.messaging.OutboxPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthorController.class)
@AutoConfigureMockMvc(addFilters = false) // disable Spring Security filters
class AuthorExistsApiTest {

    @Autowired MockMvc mockMvc;

    @MockBean AuthorService authorService;
    @MockBean JwtRequestFilter jwtRequestFilter;
    @MockBean AuthenticationService authenticationService;
    @MockBean OutboxPublisher outboxPublisher;

    @Test
    void exists_present_returns200_true() throws Exception {
        when(authorService.authorExistsByName("john", "DOE")).thenReturn(true);

        mockMvc.perform(get("/api/authors/name/exists")
                        .param("firstName", "john")
                        .param("lastName", "DOE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists", is(true)));
    }

    @Test
    void exists_absent_returns200_false() throws Exception {
        when(authorService.authorExistsByName("Jane", "Smith")).thenReturn(false);

        mockMvc.perform(get("/api/authors/name/exists")
                        .param("firstName", "Jane")
                        .param("lastName", "Smith")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists", is(false)));
    }
}
