// src/test/java/com/example/krieger/controller/DocumentControllerMetricsTest.java
package com.example.krieger.controller;

import com.example.Krieger.controller.DocumentController;
import com.example.Krieger.entity.Document;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerMetricsTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void metrics_default_stripHtmlTrue_wpmDefault200_countsAndReadingTime() throws Exception {
        // HTML content that collapses to "Hi A B C"
        String html = "<h1>Hi</h1>\r\n<p>A B C</p>\n";

        Document d = org.mockito.Mockito.mock(Document.class);
        when(d.getId()).thenReturn(123L);
        when(d.getContent()).thenReturn(html);

        when(documentService.getDocumentById(anyLong())).thenReturn(d);

        mockMvc.perform(get("/api/documents/{id}/metrics", 123L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Metrics computed"))
                .andExpect(jsonPath("$.data.id").value(123))
                // After stripHtml + collapse: "Hi A B C" → words=4, chars=8, lines=1
                .andExpect(jsonPath("$.data.words").value(4))
                .andExpect(jsonPath("$.data.characters").value(8))
                .andExpect(jsonPath("$.data.lines").value(1))
                // reading time = ceil(4/200*60) = 2
                .andExpect(jsonPath("$.data.readingTimeSeconds").value(2));
    }

    @Test
    void metrics_withParams_stripHtmlFalse_wpm300_countsRespectParams() throws Exception {
        // raw text with explicit newlines → lines=4 (includes blank line)
        String raw = "L1\nL2\n\nL4";

        Document d = org.mockito.Mockito.mock(Document.class);
        when(d.getId()).thenReturn(9L);
        when(d.getContent()).thenReturn(raw);

        when(documentService.getDocumentById(9L)).thenReturn(d);

        mockMvc.perform(get("/api/documents/{id}/metrics", 9L)
                        .param("stripHtml", "false")
                        .param("wpm", "300")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Metrics computed"))
                .andExpect(jsonPath("$.data.id").value(9))
                // "L1\nL2\n\nL4": words=3 ("L1","L2","L4"), chars=9 (includes newlines), lines=4
                .andExpect(jsonPath("$.data.words").value(3))
                .andExpect(jsonPath("$.data.characters").value(9))
                .andExpect(jsonPath("$.data.lines").value(4))
                // reading time = ceil(3/300*60) = 1
                .andExpect(jsonPath("$.data.readingTimeSeconds").value(1));
    }
}
