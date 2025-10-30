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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = KriegerApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthorDuplicateUpdateTest {

    @Autowired private MockMvc mvc;
    @Autowired private AuthorRepository authorRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setup() {
        authorRepository.deleteAll();
        // A = John Doe (idA), B = Jane Roe (idB)
        Author a = new Author(); a.setFirstName("John"); a.setLastName("Doe"); authorRepository.save(a);
        Author b = new Author(); b.setFirstName("Jane"); b.setLastName("Roe"); authorRepository.save(b);
    }

    @Test
    void updateToExistingName_returns409() throws Exception {
        Long idB = authorRepository.findAll().stream()
                .filter(x -> "Jane".equals(x.getFirstName()))
                .findFirst().get().getId();

        // Try to change B -> "John Doe" which already exists -> 409
        mvc.perform(put("/api/authors/" + idB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                .andExpect(status().isConflict());
    }
}
