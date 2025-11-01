package com.example.krieger.controller;

import com.example.Krieger.controller.AuthorController;
import com.example.Krieger.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.example.Krieger.controller.AuthorController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthorBulkDeleteRouteTest {

    @Autowired MockMvc mvc;

    @MockBean com.example.Krieger.config.services.AuthenticationService authenticationService;
    @MockBean com.example.Krieger.config.jwt.JwtUtil jwtUtil;

    @MockBean com.example.Krieger.service.AuthorService authorService;
    @MockBean com.example.Krieger.messaging.OutboxPublisher outboxPublisher;

    @Test
    void route_get_bulkDelete_returns405() throws Exception {
        mvc.perform(get("/api/authors/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }
}
