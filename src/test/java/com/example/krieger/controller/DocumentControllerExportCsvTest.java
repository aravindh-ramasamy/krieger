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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerExportCsvTest {

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
    void exportCsv_happyPath_respectsFiltersAndPagingAndSort() throws Exception {
        // Mock Documents using Mockito so we don't depend on setters
        Document d1 = mock(Document.class);
        when(d1.getId()).thenReturn(2L);
        when(d1.getTitle()).thenReturn("Report, \"Q1\"");
        when(d1.getContent()).thenReturn("Alpha\nBeta");
        when(d1.getCreatedAt()).thenReturn(java.time.Instant.parse("2024-01-02T03:04:05Z"));
        when(d1.getUpdatedAt()).thenReturn(java.time.Instant.parse("2024-02-03T04:05:06Z"));

        Document d2 = mock(Document.class);
        when(d2.getId()).thenReturn(1L);
        when(d2.getTitle()).thenReturn("Notes");
        when(d2.getContent()).thenReturn("Hello,world");
        when(d2.getCreatedAt()).thenReturn(java.time.Instant.parse("2024-01-01T00:00:00Z"));
        when(d2.getUpdatedAt()).thenReturn(java.time.Instant.parse("2024-01-01T00:00:01Z"));

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = new PageImpl<>(List.of(d1, d2), pageable, 2);

        when(documentService.searchDocuments(eq(42L), eq("doc"), any(Pageable.class)))
                .thenReturn(page);

        MvcResult res = mockMvc.perform(get("/api/documents/export.csv")
                        .param("authorId", "42")
                        .param("q", "doc")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,desc")
                        .accept(MediaType.valueOf("text/csv")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"documents_")))
                .andExpect(content().string(containsString("id,title,authorId,createdAt,updatedAt,contentPreview")))
                // quoted fields and escaping checks
                .andExpect(content().string(containsString("\"2\",\"Report, \"\"Q1\"\"\",\"\",\"2024-01-02T03:04:05Z\",\"2024-02-03T04:05:06Z\",\"Alpha Beta\"")))
                .andExpect(content().string(containsString("\"1\",\"Notes\",\"\",\"2024-01-01T00:00:00Z\",\"2024-01-01T00:00:01Z\",\"Hello,world\"")))
                .andReturn();

        // Ensure header + 2 rows (3 lines total)
        String body = res.getResponse().getContentAsString();
        long lines = body.lines().count();
        assertEquals(3, lines, "CSV should have 1 header + 2 data rows");
    }

    @Test
    void exportCsv_handlesEmptyPage_stillHasHeaderOnly() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = new PageImpl<>(List.of(), pageable, 0);
        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        MvcResult res = mockMvc.perform(get("/api/documents/export.csv")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id,title,authorId,createdAt,updatedAt,contentPreview")))
                .andReturn();

        String body = res.getResponse().getContentAsString();
        long lines = body.lines().count();
        assertEquals(1, lines, "CSV should contain only the header when there are no rows");
    }

    @Test
    void exportCsv_withBomTrue_prefixesBOM() throws Exception {
        // mock two docs (same as earlier test)
        Document d1 = mock(Document.class);
        when(d1.getId()).thenReturn(2L);
        when(d1.getTitle()).thenReturn("Report");
        when(d1.getContent()).thenReturn("Alpha");
        when(d1.getCreatedAt()).thenReturn(java.time.Instant.parse("2024-01-02T03:04:05Z"));
        when(d1.getUpdatedAt()).thenReturn(java.time.Instant.parse("2024-02-03T04:05:06Z"));

        Document d2 = mock(Document.class);
        when(d2.getId()).thenReturn(1L);
        when(d2.getTitle()).thenReturn("Notes");
        when(d2.getContent()).thenReturn("Beta");
        when(d2.getCreatedAt()).thenReturn(java.time.Instant.parse("2024-01-01T00:00:00Z"));
        when(d2.getUpdatedAt()).thenReturn(java.time.Instant.parse("2024-01-01T00:00:01Z"));

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = new PageImpl<>(java.util.List.of(d1, d2), pageable, 2);

        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        MvcResult res = mockMvc.perform(get("/api/documents/export.csv")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,desc")
                        .param("bom", "true")) // enable BOM
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
                .andReturn();

        String body = res.getResponse().getContentAsString();
        assertTrue(body.startsWith("\uFEFF"), "CSV should start with UTF-8 BOM when bom=true");
        // sanity: header row present after BOM
        assertTrue(body.contains("id,title,authorId,createdAt,updatedAt,contentPreview"));
    }

    @Test
    void exportCsv_withoutBom_doesNotPrefixBOM() throws Exception {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = new PageImpl<>(java.util.List.of(), pageable, 0);
        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        MvcResult res = mockMvc.perform(get("/api/documents/export.csv")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andReturn();

        String body = res.getResponse().getContentAsString();
        // must NOT start with BOM by default
        assertTrue(!body.startsWith("\uFEFF"), "CSV should not start with BOM unless bom=true is provided");
    }

}
