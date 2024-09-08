package com.example.krieger.service;

import com.example.Krieger.dto.DocumentDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.repository.DocumentRepository;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createDocument() {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setTitle("Test Title");
        documentDTO.setBody("Test Body");
        documentDTO.setAuthorID(1L);
        documentDTO.setReference("Test Reference");

        Document document = new Document();
        document.setId(1L);
        document.setTitle(documentDTO.getTitle());
        document.setBody(documentDTO.getBody());
        document.setReferences(documentDTO.getReference());

        when(authorRepository.findById(1L)).thenReturn(Optional.of(new Author()));
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        Document createdDocument = documentService.createDocument(documentDTO);

        assertNotNull(createdDocument);
        assertEquals("Test Title", createdDocument.getTitle());
        assertEquals("Test Body", createdDocument.getBody());
    }

    @Test
    void getDocumentById_NotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            documentService.getDocumentById(1L);
        });
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    void update_NotFound() {
        DocumentDTO documentUpdateDTO = new DocumentDTO();
        documentUpdateDTO.setId(1L);
        documentUpdateDTO.setTitle("Updated Title");
        documentUpdateDTO.setBody("Updated Body");
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            documentService.updateDocument(1L, documentUpdateDTO);
        });
        verify(documentRepository, times(1)).findById(1L);
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void delete_Success() {
        doNothing().when(documentRepository).deleteById(1L);
        assertDoesNotThrow(() -> documentService.deleteDocument(1L));
        verify(documentRepository, times(1)).deleteById(1L);
    }
}