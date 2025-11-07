// src/test/java/com/example/krieger/controller/DocumentControllerPatchTitleTest.java
package com.example.krieger.controller;

import com.example.Krieger.controller.DocumentController;
import com.example.Krieger.dto.UpdateTitleRequest;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.GlobalExceptionHandler; // <-- use your app's handler
import com.example.Krieger.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerPatchTitleTest {

    @Mock private DocumentService documentService;
    @InjectMocks private DocumentController controller;

    private MockMvc mockMvc;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // Register the global exception handler so CustomException -> 400 in tests
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())  // <-- important
                .build();
    }

    @Test
    void patchTitle_happyPath_trimsAndUpdates() throws Exception {
        // Use a REAL entity, not a Mockito mock, so Jackson can serialize it
        Document updated = new Document();
        updated.setId(10L);
        updated.setTitle("My Title");

        when(documentService.updateTitle(eq(10L), eq("My Title"))).thenReturn(updated);

        UpdateTitleRequest req = new UpdateTitleRequest("  My Title  ");

        mockMvc.perform(patch("/api/documents/{id}/title", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Title updated"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.title").value("My Title"));
    }

    @Test
    void patchTitle_blankTitle_returns400() throws Exception {
        UpdateTitleRequest req = new UpdateTitleRequest("   ");

        mockMvc.perform(patch("/api/documents/{id}/title", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("title")))
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("blank")));
    }
}
