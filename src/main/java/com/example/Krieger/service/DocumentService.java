package com.example.Krieger.service;

import com.example.Krieger.dto.DocumentDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Objects;

/**
 * Service for CRUD on Document with:
 *  - null/blank-aware updates
 *  - string normalization (trim; blank -> null)
 *  - length validation for title/reference
 *  - transactional read/write boundaries
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    // Soft limits to keep DB rows tidy; adjust if your schema differs
    private static final int TITLE_MAX = 255;
    private static final int REFERENCE_MAX = 255;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Transactional
    public Document createDocument(DocumentDTO documentDTO) {
        Objects.requireNonNull(documentDTO, "documentDTO");

        String title = normalize(documentDTO.getTitle());
        String body = normalize(documentDTO.getBody());
        String reference = normalize(documentDTO.getReference());

        // basic validations (title required by controller already; enforce max lengths here)
        checkLength(title, TITLE_MAX, "title");
        checkLength(reference, REFERENCE_MAX, "reference");

        Long authorId = documentDTO.getAuthorID();
        if (authorId == null) {
            throw new IllegalArgumentException("authorID is required to create a document");
        }

        Author author = getAuthorOr404(authorId);

        Document document = new Document();
        document.setTitle(title);
        document.setBody(body);
        document.setReferences(reference);
        document.setAuthor(author);

        Document saved = documentRepository.save(document);
        log.debug("Created document id={} (authorId={})", saved.getId(), authorId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    @Transactional
    public Document updateDocument(Long id, DocumentDTO documentDetails) {
        Objects.requireNonNull(documentDetails, "documentDetails");

        Document document = getDocumentById(id);

        // Normalize incoming values
        String title = normalize(documentDetails.getTitle());
        String body = normalize(documentDetails.getBody());
        String reference = normalize(documentDetails.getReference());
        Long newAuthorId = documentDetails.getAuthorID();

        // Validate lengths (if provided)
        checkLength(title, TITLE_MAX, "title");
        checkLength(reference, REFERENCE_MAX, "reference");

        // Apply only the provided fields
        if (title != null) {
            document.setTitle(title);
        }
        if (body != null) {
            document.setBody(body);
        }
        if (reference != null) {
            document.setReferences(reference);
        }
        if (newAuthorId != null) {
            Author newAuthor = getAuthorOr404(newAuthorId);
            document.setAuthor(newAuthor);
        }

        Document saved = documentRepository.save(document);
        log.debug("Updated document id={}", saved.getId());
        return saved;
    }

    @Transactional
    public void deleteDocument(Long id) {
        // Aligns with get-by-id semantics (404 if missing)
        Document existing = getDocumentById(id);
        documentRepository.deleteById(existing.getId());
        log.debug("Deleted document id={}", id);
    }

    @Transactional(readOnly = true)
    public Page<Document> searchDocuments(Long authorId, String q, Pageable pageable) {
        return documentRepository.search(authorId, normalize(q), pageable);
    }

    private Author getAuthorOr404(Long authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with ID: " + authorId));
    }

    /** Trim, convert blanks to null. */
    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Enforce max length if value present. */
    private static void checkLength(String value, int max, String field) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(field + " must be <= " + max + " characters");
        }
    }
}
