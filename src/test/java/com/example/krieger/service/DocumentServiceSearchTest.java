package com.example.krieger.service;

import com.example.Krieger.entity.Document;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.repository.DocumentRepository;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceSearchTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private DocumentService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchDocuments_trimsQuery_and_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        List<Document> docs = List.of(new Document(), new Document());
        Page<Document> page = new PageImpl<>(docs, pageable, 2);

        when(documentRepository.search(null, "RFC", pageable)).thenReturn(page);

        Page<Document> out = service.searchDocuments(null, "  RFC  ", pageable);

        assertSame(page, out);
        verify(documentRepository, times(1)).search(null, "RFC", pageable);
    }

    @Test
    void searchDocuments_nullQuery_passesNullToRepository() {
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "title"));
        Page<Document> page = new PageImpl<>(List.of(), pageable, 0);

        when(documentRepository.search(10L, null, pageable)).thenReturn(page);

        Page<Document> out = service.searchDocuments(10L, null, pageable);

        assertSame(page, out);
        verify(documentRepository).search(10L, null, pageable);
    }
}
