package com.example.krieger.controller;

import com.example.Krieger.controller.DocumentController;
import com.example.Krieger.dto.ApiResponse;
import com.example.Krieger.dto.CountResult;
import com.example.Krieger.entity.Document;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentControllerCountTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void countDocuments_withFilters_returnsTotal() {
        // totalElements = 137 regardless of content page size
        Pageable oneItem = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = new PageImpl<>(List.of(new Document()), oneItem, 137);

        when(documentService.searchDocuments(eq(42L), eq("foo"), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<?> resp = controller.countDocuments(42L, "  foo  ");

        assertEquals(200, resp.getStatusCodeValue());

        @SuppressWarnings("unchecked")
        ApiResponse<CountResult> api = (ApiResponse<CountResult>) resp.getBody();
        assertNotNull(api);
        assertEquals(200, api.getCode());
        assertNotNull(api.getData());
        assertEquals(137L, api.getData().getCount());

        verify(documentService).searchDocuments(eq(42L), eq("foo"), any(Pageable.class));
    }

    @Test
    void countDocuments_withoutFilters_returnsTotal() {
        Pageable oneItem = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = new PageImpl<>(List.of(), oneItem, 0);

        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<?> resp = controller.countDocuments(null, null);

        assertEquals(200, resp.getStatusCodeValue());

        @SuppressWarnings("unchecked")
        ApiResponse<CountResult> api = (ApiResponse<CountResult>) resp.getBody();
        assertNotNull(api);
        assertEquals(0L, api.getData().getCount());

        verify(documentService).searchDocuments(isNull(), isNull(), any(Pageable.class));
    }
}
