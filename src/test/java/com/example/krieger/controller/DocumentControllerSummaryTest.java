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

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerSummaryTest {

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
    void summary_default_returnsMetadata_andHtmlStrippedPreview200() throws Exception {
        // long HTML-y content to force clipping
        String raw = "<h1>Title</h1><p>" + "X".repeat(260) + "</p>";
        Document d = mock(Document.class);
        when(d.getId()).thenReturn(123L);
        when(d.getTitle()).thenReturn("Design Notes");
        // Use convenience getter if present on your entity
        try { when(d.getAuthorId()).thenReturn(42L); } catch (Throwable ignore) {}

        when(d.getCreatedAt()).thenReturn(Instant.parse("2024-10-01T08:15:00Z"));
        when(d.getUpdatedAt()).thenReturn(Instant.parse("2024-10-05T12:00:00Z"));
        when(d.getContent()).thenReturn(raw);

        when(documentService.getDocumentById(anyLong())).thenReturn(d);

        mockMvc.perform(get("/api/documents/{id}/summary", 123L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Summary retrieved"))
                .andExpect(jsonPath("$.data.id").value(123))
                .andExpect(jsonPath("$.data.title").value("Design Notes"))
                .andExpect(jsonPath("$.data.preview", not(containsString("<h1>"))))
                .andExpect(jsonPath("$.data.previewLength").value(lessThanOrEqualTo(201))); // 200 + ellipsis

        verify(documentService).getDocumentById(123L);
    }

    @Test
    void summary_withParams_respectsPreviewLen_andStripHtmlFalse() throws Exception {
        String raw = "<b>Hello</b> " + "A".repeat(200);
        Document d = mock(Document.class);
        when(d.getId()).thenReturn(9L);
        when(d.getTitle()).thenReturn("Raw Preview");
        when(d.getContent()).thenReturn(raw);
        when(d.getCreatedAt()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
        when(d.getUpdatedAt()).thenReturn(Instant.parse("2024-01-02T00:00:00Z"));

        when(documentService.getDocumentById(9L)).thenReturn(d);

        mockMvc.perform(get("/api/documents/{id}/summary", 9L)
                        .param("previewLen", "60")
                        .param("stripHtml", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Raw Preview"))
                .andExpect(jsonPath("$.data.preview", containsString("<b>Hello</b>")))
                .andExpect(jsonPath("$.data.previewLength").value(lessThanOrEqualTo(61))); // 60 + ellipsis

        verify(documentService).getDocumentById(9L);
    }
}
