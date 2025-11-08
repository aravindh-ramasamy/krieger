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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerRecentTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController controller;

    private MockMvc mockMvc;

    @Captor
    private ArgumentCaptor<Long> authorCaptor;

    @Captor
    private ArgumentCaptor<String> qCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void recent_default_returnsUpTo10_sortedAndLightweight() throws Exception {
        // Arrange: two documents in correct order (updatedAt DESC, id DESC)
        Document d1 = org.mockito.Mockito.mock(Document.class);
        when(d1.getId()).thenReturn(20L);
        when(d1.getTitle()).thenReturn("Design Notes");
        when(d1.getUpdatedAt()).thenReturn(Instant.parse("2025-10-05T12:00:00Z"));

        Document d2 = org.mockito.Mockito.mock(Document.class);
        when(d2.getId()).thenReturn(10L);
        when(d2.getTitle()).thenReturn("Spec Draft");
        when(d2.getUpdatedAt()).thenReturn(Instant.parse("2025-10-01T08:15:00Z"));

        List<Document> docs = List.of(d1, d2);
        Page<Document> page = new PageImpl<>(docs, PageRequest.of(0, 10), docs.size());

        // IMPORTANT: use Mockito's isNull/any (from ArgumentMatchers)
        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(get("/api/documents/recent").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // ApiResponse fields (ApiResponse has "msg", "code", "data")
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Recent documents"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                // Lightweight list shape
                .andExpect(jsonPath("$.data[0].id").value(20))
                .andExpect(jsonPath("$.data[0].title").value("Design Notes"))
                .andExpect(jsonPath("$.data[0].updatedAt").value("2025-10-05T12:00:00Z"))
                .andExpect(jsonPath("$.data[1].id").value(10))
                .andExpect(jsonPath("$.data[1].title").value("Spec Draft"))
                .andExpect(jsonPath("$.data[1].updatedAt").value("2025-10-01T08:15:00Z"));

        // Verify the pageable defaults: size 10, sort contains updatedAt DESC then id DESC
        verify(documentService).searchDocuments(isNull(), isNull(), pageableCaptor.capture());
        Pageable p = pageableCaptor.getValue();
        // size
        org.junit.jupiter.api.Assertions.assertEquals(10, p.getPageSize());
        // sort: updatedAt DESC then id DESC
        Sort.Order first = p.getSort().getOrderFor("updatedAt");
        Sort.Order second = p.getSort().getOrderFor("id");
        org.junit.jupiter.api.Assertions.assertNotNull(first);
        org.junit.jupiter.api.Assertions.assertNotNull(second);
        org.junit.jupiter.api.Assertions.assertEquals(Sort.Direction.DESC, first.getDirection());
        org.junit.jupiter.api.Assertions.assertEquals(Sort.Direction.DESC, second.getDirection());
    }

    @Test
    void recent_withFilters_appliesAuthor_q_andLimitClamped() throws Exception {
        // Arrange: three results
        Document d1 = org.mockito.Mockito.mock(Document.class);
        when(d1.getId()).thenReturn(3L);
        when(d1.getTitle()).thenReturn("Note 3");
        when(d1.getUpdatedAt()).thenReturn(Instant.parse("2025-11-07T10:00:00Z"));

        Document d2 = org.mockito.Mockito.mock(Document.class);
        when(d2.getId()).thenReturn(2L);
        when(d2.getTitle()).thenReturn("Note 2");
        when(d2.getUpdatedAt()).thenReturn(Instant.parse("2025-11-07T09:00:00Z"));

        Document d3 = org.mockito.Mockito.mock(Document.class);
        when(d3.getId()).thenReturn(1L);
        when(d3.getTitle()).thenReturn("Note 1");
        when(d3.getUpdatedAt()).thenReturn(Instant.parse("2025-11-07T08:00:00Z"));

        List<Document> docs = List.of(d1, d2, d3);
        Page<Document> page = new PageImpl<>(docs, PageRequest.of(0, 3), 3);

        when(documentService.searchDocuments(Mockito.any(), Mockito.any(), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(get("/api/documents/recent")
                        .param("limit", "3")
                        .param("authorId", "42")
                        .param("q", "note")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Recent documents"))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].id").value(3))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[2].id").value(1));

        // Verify service called with filters + limit (3) and proper sort
        verify(documentService).searchDocuments(authorCaptor.capture(), qCaptor.capture(), pageableCaptor.capture());
        Long capturedAuthor = authorCaptor.getValue();
        String capturedQ = qCaptor.getValue();
        Pageable p = pageableCaptor.getValue();

        org.junit.jupiter.api.Assertions.assertEquals(42L, capturedAuthor);
        org.junit.jupiter.api.Assertions.assertEquals("note", capturedQ);
        org.junit.jupiter.api.Assertions.assertEquals(3, p.getPageSize());
        Sort.Order first = p.getSort().getOrderFor("updatedAt");
        Sort.Order second = p.getSort().getOrderFor("id");
        org.junit.jupiter.api.Assertions.assertNotNull(first);
        org.junit.jupiter.api.Assertions.assertNotNull(second);
        org.junit.jupiter.api.Assertions.assertEquals(Sort.Direction.DESC, first.getDirection());
        org.junit.jupiter.api.Assertions.assertEquals(Sort.Direction.DESC, second.getDirection());
    }
}
