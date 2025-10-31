package com.example.krieger.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthorRouteMappingTest {

    @Autowired MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Test
    void search_path_hits_search_method() throws Exception {
        mvc.perform(get("/api/authors/search").param("query", "jo"))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(com.example.Krieger.controller.AuthorController.class))
                .andExpect(handler().methodName("searchAuthors"));
    }
}
