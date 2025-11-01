package com.example.krieger.controller;

import com.example.Krieger.KriegerApplication;
import com.example.Krieger.controller.AuthorController;
import com.example.Krieger.dto.BulkDeleteRequest;
import com.example.Krieger.dto.BulkDeleteResult;
import com.example.Krieger.dto.ApiResponse;
import com.example.Krieger.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.example.Krieger.controller.AuthorController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthorBulkDeleteApiTest {

    @Autowired MockMvc mvc;

    @MockBean com.example.Krieger.config.services.AuthenticationService authenticationService;
    @MockBean com.example.Krieger.config.jwt.JwtUtil jwtUtil;

    @MockBean com.example.Krieger.service.AuthorService authorService;
    @MockBean com.example.Krieger.messaging.OutboxPublisher outboxPublisher;

    @Test
    void bulkDelete_emptyIds_returns400_withMessage() throws Exception {
        String body = "{\"ids\":[]}";

        mvc.perform(post("/api/authors/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.msg", containsString("ids must not be empty")));
    }

    @Test
    void bulkDelete_mixedIds_returns202_andBodyHasCounts() throws Exception {
        BulkDeleteResult result = new BulkDeleteResult(3, 2, java.util.List.of(999L));
        Mockito.when(authorService.enqueueDeleteByIds(java.util.List.of(1L, 2L, 999L)))
                .thenReturn(result);

        String body = "{\"ids\":[1,2,999]}";

        mvc.perform(post("/api/authors/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.code", is(202)))
                .andExpect(jsonPath("$.data.requested", is(3)))
                .andExpect(jsonPath("$.data.enqueued", is(2)))
                .andExpect(jsonPath("$.data.missing[0]", is(999)));
    }
}
