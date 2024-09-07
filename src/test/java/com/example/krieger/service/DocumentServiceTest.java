package com.example.krieger.service;

import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.ResourceNotFoundException;
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

    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createDocument() {
        Document mockDocument = new Document();
        mockDocument.setId(1L);
        mockDocument.setTitle("Document");
        mockDocument.setBody("This is document body.");
        when(documentRepository.save(any(Document.class))).thenReturn(mockDocument);
        Document createdDocument = documentService.createDocument(mockDocument);
        assertNotNull(createdDocument);
        assertEquals("Document", createdDocument.getTitle());
        assertEquals("This is document body.", createdDocument.getBody());
        assertEquals(1L, createdDocument.getId());
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
        Document documentUpdate = new Document();
        documentUpdate.setId(1L);
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            documentService.updateDocument(1L, documentUpdate);
        });
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    void delete_Success() {
        doNothing().when(documentRepository).deleteById(1L);
        assertDoesNotThrow(() -> documentService.deleteDocument(1L));
        verify(documentRepository, times(1)).deleteById(1L);
    }
}