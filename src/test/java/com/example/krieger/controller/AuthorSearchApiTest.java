package com.example.krieger.controller;

import com.example.Krieger.KriegerApplication;
import com.example.Krieger.entity.Author;
import com.example.Krieger.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = KriegerApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthorSearchApiTest {

    @Autowired private MockMvc mvc;
    @Autowired private AuthorRepository authorRepository;

    // Avoid real AMQP calls during tests
    @MockBean private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setup() {
        authorRepository.deleteAll();

        Author a = new Author(); a.setFirstName("John"); a.setLastName("Doe"); authorRepository.save(a);
        Author b = new Author(); b.setFirstName("Jane"); b.setLastName("Roe"); authorRepository.save(b);
        Author c = new Author(); c.setFirstName("Alice"); c.setLastName("Johnson"); authorRepository.save(c);
    }

    @Test
    void search_withMatches_returns200() throws Exception {
        // "jo" should match "John Doe" and "Alice Johnson"
        mvc.perform(get("/api/authors/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("query", "jo"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_noMatches_returns200_emptyList() throws Exception {
        mvc.perform(get("/api/authors/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("query", "zzzzzz"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
