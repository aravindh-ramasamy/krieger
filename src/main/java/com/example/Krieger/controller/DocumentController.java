package com.example.Krieger.controller;

import com.example.Krieger.DTO.ApiResponse;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Document>> createDocument(@RequestBody Document document) {
        if (document.getTitle() == null || document.getBody() == null) {
            throw new CustomException("Title or body cannot be empty", HttpStatus.BAD_REQUEST);
        }

        Document createdDocument = documentService.createDocument(document);
        throw new SuccessException("Document created successfully", HttpStatus.CREATED, createdDocument);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Document>> getDocumentById(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        if (document == null) {
            throw new CustomException("Document not found with ID: " + id, HttpStatus.NOT_FOUND);
        }

        throw new SuccessException("Document retrieved successfully", HttpStatus.OK, document);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Document>> updateDocument(@PathVariable Long id, @RequestBody Document document) {
        Document updatedDocument = documentService.updateDocument(id, document);
        throw new SuccessException("Document updated successfully", HttpStatus.OK, updatedDocument);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        throw new SuccessException("Document deleted successfully", HttpStatus.NO_CONTENT, null);
    }
}