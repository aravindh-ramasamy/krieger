package com.example.krieger.controller;

import com.example.Krieger.controller.DocumentController;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.InvalidPaginationException;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

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
        Page<Document> page = new PageImpl<>(List.of(new Document()), pageable, 1);

        when(documentService.searchDocuments(null, null, pageable)).thenReturn(page);

        ResponseEntity<?> resp = controller.listDocuments(null, null, null, null, "id,desc");

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("1", resp.getHeaders().getFirst("X-Total-Count"));
        verify(documentService).searchDocuments(null, null, pageable);
    }

    @Test
    void listDocuments_withFilters_validPagination_parsesSortAndCallsService() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        Pageable expected = PageRequest.of(2, 50, Sort.by(Sort.Direction.ASC, "title"));
        Page<Document> page = new PageImpl<>(List.of(new Document()), expected, 1);

        when(documentService.searchDocuments(eq(42L), eq("foo"), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<?> resp = controller.listDocuments(42L, "  foo  ", "2", "50", "title,asc");

        assertEquals(200, resp.getStatusCodeValue());
        verify(documentService).searchDocuments(eq(42L), eq("foo"), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertEquals(2, used.getPageNumber());
        assertEquals(50, used.getPageSize());
        Sort.Order order = used.getSort().iterator().next();
        assertEquals("title", order.getProperty());
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void listDocuments_withFilters_invalidPagination_returns400_andDoesNotCallService() {
        assertThrows(InvalidPaginationException.class, () ->
                controller.listDocuments(42L, "foo", "-1", "0", "title,asc")
        );
        verifyNoInteractions(documentService);
    }

    @Test
    void listDocuments_blankSort_defaultsToIdDesc() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Page<Document> page = new PageImpl<>(List.of(), PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")), 0);

        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<?> resp = controller.listDocuments(null, null, "0", "1", "   ");
        assertEquals(200, resp.getStatusCodeValue());

        verify(documentService).searchDocuments(isNull(), isNull(), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertEquals(0, used.getPageNumber());
        assertEquals(1, used.getPageSize());
        Sort.Order order = used.getSort().iterator().next();
        assertEquals("id", order.getProperty());
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void listDocuments_nonNumericPage_returns400() {
        assertThrows(InvalidPaginationException.class, () ->
                controller.listDocuments(null, null, "abc", "20", "id,desc")
        );
        verifyNoInteractions(documentService);
    }

    @Test
    void listDocuments_negativePage_returns400() {
        assertThrows(InvalidPaginationException.class, () ->
                controller.listDocuments(null, null, "-1", "20", "id,desc")
        );
        verifyNoInteractions(documentService);
    }

    @Test
    void listDocuments_sizeZero_returns400() {
        assertThrows(InvalidPaginationException.class, () ->
                controller.listDocuments(null, null, "0", "0", "id,desc")
        );
        verifyNoInteractions(documentService);
    }

    @Test
    void listDocuments_sizeTooLarge_returns400() {
        assertThrows(InvalidPaginationException.class, () ->
                controller.listDocuments(null, null, "0", "101", "id,desc")
        );
        verifyNoInteractions(documentService);
    }

    @Test
    void listDocuments_missingPageAndSize_defaultsTo0And20() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        Pageable expected = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = new PageImpl<>(List.of(new Document()), expected, 1);

        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<?> resp = controller.listDocuments(null, null, null, null, "id,desc");

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("1", resp.getHeaders().getFirst("X-Total-Count"));

        verify(documentService).searchDocuments(isNull(), isNull(), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertEquals(0, used.getPageNumber());
        assertEquals(20, used.getPageSize());
        Sort.Order order = used.getSort().iterator().next();
        assertEquals("id", order.getProperty());
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }
}
