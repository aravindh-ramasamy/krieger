package com.example.krieger.controller;

import com.example.Krieger.controller.DocumentController;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createDocument_Success() {
        Document mockDocument = new Document();
        mockDocument.setId(1L);
        mockDocument.setTitle("Test Document");
        mockDocument.setBody("This is a test document body.");

        when(documentService.createDocument(any(Document.class))).thenReturn(mockDocument);

        SuccessException thrown = assertThrows(SuccessException.class, () -> {
            documentController.createDocument(mockDocument);
        });

        assertEquals("Document created successfully", thrown.getMessage());
        assertEquals(HttpStatus.CREATED, thrown.getHttpStatus());
        assertEquals(mockDocument, thrown.getData());
    }

    @Test
    void createDocument_MissingTitle() {
        Document mockDocument = new Document();
        mockDocument.setBody("This is a test document body.");

        CustomException thrown = assertThrows(CustomException.class, () -> {
            documentController.createDocument(mockDocument);
        });

        assertEquals("Title or body cannot be empty", thrown.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getHttpStatus());
    }

    @Test
    void getDocumentById_NotFound() {
        when(documentService.getDocumentById(1L)).thenReturn(null);

        CustomException thrown = assertThrows(CustomException.class, () -> {
            documentController.getDocumentById(1L);
        });

        assertEquals("Document not found with ID: 1", thrown.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, thrown.getHttpStatus());
    }

    @Test
    void deleteDocument_Success() {
        doNothing().when(documentService).deleteDocument(1L);

        SuccessException thrown = assertThrows(SuccessException.class, () -> {
            documentController.deleteDocument(1L);
        });

        assertEquals("Document deleted successfully", thrown.getMessage());
        assertEquals(HttpStatus.NO_CONTENT, thrown.getHttpStatus());
        assertNull(thrown.getData());
    }
}