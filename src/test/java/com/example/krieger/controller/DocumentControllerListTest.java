package com.example.krieger.controller;

import com.example.Krieger.controller.DocumentController;
import com.example.Krieger.controller.DocumentController.PageResult;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentControllerListTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController controller;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listDocuments_defaultParams_success() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        List<Document> docs = List.of(new Document());
        Page<Document> page = new PageImpl<>(docs, pageable, 1);

        when(documentService.searchDocuments(null, null, pageable)).thenReturn(page);

        SuccessException ex = assertThrows(SuccessException.class, () ->
                controller.listDocuments(null, null, 0, 20, "id,desc")
        );

        assertEquals("Documents retrieved successfully", ex.getMessage());
        assertEquals(org.springframework.http.HttpStatus.OK, ex.getHttpStatus());

        @SuppressWarnings("unchecked")
        PageResult<Document> body = (PageResult<Document>) ex.getData();

        assertEquals(docs, body.items());
        assertEquals(0, body.page());
        assertEquals(20, body.size());
        assertEquals(1, body.totalElements());
        assertEquals(1, body.totalPages());
        assertEquals("id,desc", body.sort());
        assertTrue(body.filters().isEmpty());

        verify(documentService).searchDocuments(null, null, pageable);
    }

    @Test
    void listDocuments_withFilters_clampsAndParsesSort_correctlyCallsService() {
        // page < 0 -> 0; size > 100 -> 100; q trimmed to "rfc"; sort -> title,asc
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        List<Document> docs = List.of(new Document(), new Document());
        // The controller will clamp to page=0,size=100 and use title,asc
        Page<Document> page = new PageImpl<>(docs, PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "title")), 2);

        when(documentService.searchDocuments(eq(10L), eq("rfc"), any(Pageable.class))).thenReturn(page);

        SuccessException ex = assertThrows(SuccessException.class, () ->
                controller.listDocuments(10L, "  rfc  ", -5, 1000, "title,asc")
        );

        @SuppressWarnings("unchecked")
        PageResult<Document> body = (PageResult<Document>) ex.getData();

        assertEquals(0, body.page());
        assertEquals(100, body.size());
        assertEquals(2, body.items().size());
        assertEquals("title,asc", body.sort());
        Map<String, Object> filters = body.filters();
        assertEquals(10L, filters.get("authorId"));
        assertEquals("rfc", filters.get("q"));

        verify(documentService).searchDocuments(eq(10L), eq("rfc"), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertEquals(0, used.getPageNumber());
        assertEquals(100, used.getPageSize());
        Sort.Order order = used.getSort().iterator().next();
        assertEquals("title", order.getProperty());
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void listDocuments_blankSort_defaultsToIdDesc() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Page<Document> page = new PageImpl<>(List.of(), PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")), 0);

        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        SuccessException ex = assertThrows(SuccessException.class, () ->
                controller.listDocuments(null, null, 0, 1, "   ")
        );

        @SuppressWarnings("unchecked")
        PageResult<Document> body = (PageResult<Document>) ex.getData();
        assertEquals("id,desc", body.sort());

        verify(documentService).searchDocuments(isNull(), isNull(), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertEquals(0, used.getPageNumber());
        assertEquals(1, used.getPageSize());
        Sort.Order order = used.getSort().iterator().next();
        assertEquals("id", order.getProperty());
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }
}
