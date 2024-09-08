package com.example.Krieger.controller;

import com.example.Krieger.dto.ApiResponse;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//Rest api's for Documents CRUD operations and sends responses
@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document API", description = "API for Documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Operation(summary = "Create a New Document", description = "Create a New Document.")
    @PostMapping
    public ResponseEntity<ApiResponse<Document>> createDocument(@RequestBody Document document) {
        if (document.getTitle() == null || document.getBody() == null) {
            throw new CustomException("Title or body cannot be empty", HttpStatus.BAD_REQUEST);
        }

        Document createdDocument = documentService.createDocument(document);
        throw new SuccessException("Document created successfully", HttpStatus.CREATED, createdDocument);
    }

    @Operation(summary = "Get a Document", description = "Get a Document by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Document>> getDocumentById(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        if (document == null) {
            throw new CustomException("Document not found with ID: " + id, HttpStatus.NOT_FOUND);
        }

        throw new SuccessException("Document retrieved successfully", HttpStatus.OK, document);
    }

    @Operation(summary = "Update a Existing Document", description = "Update a Existing Document by ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Document>> updateDocument(@PathVariable Long id, @RequestBody Document document) {
        Document updatedDocument = documentService.updateDocument(id, document);
        throw new SuccessException("Document updated successfully", HttpStatus.OK, updatedDocument);
    }

    @Operation(summary = "Delete a Existing Document", description = "Delete a Existing Document by ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        throw new SuccessException("Document deleted successfully", HttpStatus.NO_CONTENT, null);
    }
}