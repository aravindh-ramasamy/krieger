package com.example.Krieger.controller;

import com.example.Krieger.dto.ApiResponse;
import com.example.Krieger.dto.DocumentDTO;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Document CRUD operations.
 * Uses SuccessException/CustomException for consistent response handling.
 */
@RestController
@RequestMapping(value = "/api/documents", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Document API", description = "API for Documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    // Create a document
    @Operation(summary = "Create a New Document", description = "Create a New Document.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Document>> createDocument(@RequestBody DocumentDTO document) {
        // normalize incoming strings to avoid accidental whitespace-only inputs
        sanitize(document);
        log.debug("Create document request received (authorId={})", document.getAuthorID());

        if (document.getTitle() == null || document.getBody() == null || document.getAuthorID() == null) {
            throw new CustomException("Title, author or body cannot be empty", HttpStatus.BAD_REQUEST);
        }

        Document createdDocument = documentService.createDocument(document);
        log.info("Document created id={}", createdDocument.getId());
        throw new SuccessException("Document created successfully", HttpStatus.CREATED, createdDocument);
    }

    // Fetch a document by ID
    @Operation(summary = "Get a Document", description = "Get a Document by ID.")
    @GetMapping(value = "/{id}")
    public ResponseEntity<ApiResponse<Document>> getDocumentById(@PathVariable Long id) {
        log.debug("Fetch document id={}", id);
        Document document = documentService.getDocumentById(id);

        // Backward-compatibility: some tests may stub service to return null
        if (document == null) {
            throw new CustomException("Document not found with ID: " + id, HttpStatus.NOT_FOUND);
        }

        throw new SuccessException("Document retrieved successfully", HttpStatus.OK, document);
    }

    // Update a document by ID
    @Operation(summary = "Update an Existing Document", description = "Update an Existing Document by ID.")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Document>> updateDocument(@PathVariable Long id,
                                                                @RequestBody DocumentDTO document) {
        sanitize(document);
        log.debug("Update document id={}", id);

        Document updatedDocument = documentService.updateDocument(id, document);
        log.info("Document updated id={}", updatedDocument.getId());
        throw new SuccessException("Document updated successfully", HttpStatus.OK, updatedDocument);
    }

    // Delete a Document by ID
    @Operation(summary = "Delete an Existing Document", description = "Delete an Existing Document by ID.")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id) {
        log.debug("Delete document id={}", id);
        documentService.deleteDocument(id);
        log.info("Document deleted id={}", id);
        throw new SuccessException("Document deleted successfully", HttpStatus.OK, null);
    }

    // --------------- helpers ----------------
    /** Trim title/body/reference and convert blanks to null so controller checks behave as intended. */
    private static void sanitize(DocumentDTO dto) {
        if (dto == null) return;
        dto.setTitle(trimToNull(dto.getTitle()));
        dto.setBody(trimToNull(dto.getBody()));
        dto.setReference(trimToNull(dto.getReference()));
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
