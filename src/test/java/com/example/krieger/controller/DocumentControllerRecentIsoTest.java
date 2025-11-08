package com.example.krieger.controller;

import com.example.Krieger.controller.DocumentController;
import com.example.Krieger.entity.Document;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerRecentIsoTest {

    @Mock private DocumentService documentService;
    @InjectMocks private DocumentController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void recent_returnsIso8601UpdatedAtString_defaultLimit() throws Exception {
        Document d1 = org.mockito.Mockito.mock(Document.class);
        when(d1.getId()).thenReturn(20L);
        when(d1.getTitle()).thenReturn("Design Notes");
        when(d1.getUpdatedAt()).thenReturn(Instant.parse("2025-10-05T12:00:00Z"));

        Page<Document> page = new PageImpl<>(List.of(d1), PageRequest.of(0, 10), 1);
        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/documents/recent").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Recent documents"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                // F2P assertion: used to be epoch numeric; now ISO-8601 string
                .andExpect(jsonPath("$.data[0].updatedAt").value("2025-10-05T12:00:00Z"));
    }

    @Test
    void recent_returnsIso8601UpdatedAtString_withFilters() throws Exception {
        Document d = org.mockito.Mockito.mock(Document.class);
        when(d.getId()).thenReturn(7L);
        when(d.getTitle()).thenReturn("Filtered Note");
        when(d.getUpdatedAt()).thenReturn(Instant.parse("2025-11-07T09:30:00Z"));

        Page<Document> page = new PageImpl<>(List.of(d), PageRequest.of(0, 3), 1);
        when(documentService.searchDocuments(any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/documents/recent")
                        .param("limit", "3")
                        .param("authorId", "42")
                        .param("q", "note")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Recent documents"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                // F2P assertion: strict ISO-8601 with 'Z'
                .andExpect(jsonPath("$.data[0].updatedAt").value("2025-11-07T09:30:00Z"));
    }
}
