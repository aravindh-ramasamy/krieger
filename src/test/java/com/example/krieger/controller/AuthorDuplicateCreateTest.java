package com.example.krieger.controller;

import com.example.Krieger.KriegerApplication;
import com.example.Krieger.entity.Author;
import com.example.Krieger.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = KriegerApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthorDuplicateCreateTest {

    @Autowired private MockMvc mvc;
    @Autowired private AuthorRepository authorRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setup() {
        authorRepository.deleteAll();
    }

    @Test
    void duplicateCreate_returns409() throws Exception {
        // First create -> 201
        mvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                .andExpect(status().isCreated());

        // Second create (duplicate) -> 409
        mvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                .andExpect(status().isConflict());
    }
}
