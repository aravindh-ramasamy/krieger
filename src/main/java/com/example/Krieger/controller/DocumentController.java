package com.example.Krieger.controller;

import com.example.Krieger.dto.*;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.service.DocumentService;
import com.example.Krieger.util.CsvEscaper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Krieger.util.Pagination;
import com.example.Krieger.util.PaginationHeaders;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // List / search documents with pagination & sorting
    @Operation(summary = "List Documents", description = "Paginated list with optional filters: authorId, q (case-insensitive search).")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Document>>> listDocuments(
            @RequestParam(value = "authorId", required = false) Long authorId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false) String pageParam,
            @RequestParam(value = "size", required = false) String sizeParam,
            @RequestParam(value = "sort", defaultValue = "id,desc") String sortExpr) {

        // parse & validate (throws 400 via GlobalExceptionHandler on bad input)
        final int page = Pagination.safePage(pageParam);
        final int size = Pagination.safeSize(sizeParam);

        // sort + pageable
        final Sort sort = parseSort(sortExpr);
        final Pageable pageable = PageRequest.of(page, size, sort);

        // sanitize filters
        q = trimToNull(q);

        // normalize sort string once
        final String normalizedSortExpr =
                (sortExpr == null || sortExpr.isBlank()) ? "id,desc" : sortExpr.trim();

        // query + payload
        final Page<Document> pageData = documentService.searchDocuments(authorId, q, pageable);
        final PageResult<Document> body = PageResult.of(pageData, normalizedSortExpr, authorId, q);
        final HttpHeaders headers = PaginationHeaders.build(pageData);
        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("Documents retrieved successfully", HttpStatus.OK.value(), body));
    }

    // ---- helpers for pagination response & sort parsing ----
    private static Sort parseSort(String sortExpr) {
        if (sortExpr == null || sortExpr.isBlank()) return Sort.by(Sort.Direction.DESC, "id");
        String[] parts = sortExpr.split(",", 2);
        String field = parts[0].trim().isEmpty() ? "id" : parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    public record PageResult<T>(
            List<T> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String sort,
            Map<String, Object> filters
    ) {
        static <T> PageResult<T> of(Page<T> p, String sortExpr, Long authorId, String q) {
            Map<String, Object> f = new LinkedHashMap<>();
            if (authorId != null) f.put("authorId", authorId);
            if (q != null) f.put("q", q);
            return new PageResult<>(
                    p.getContent(), p.getNumber(), p.getSize(),
                    p.getTotalElements(), p.getTotalPages(),
                    sortExpr, f
            );
        }
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

    @Operation(summary = "Count Documents", description = "Returns the total number of documents matching optional filters.")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<CountResult>> countDocuments(
            @RequestParam(value = "authorId", required = false) Long authorId,
            @RequestParam(value = "q", required = false) String q) {

        q = trimToNull(q);

        // Query a 1-sized page to reuse existing service and read totalElements
        Pageable oneItem = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = documentService.searchDocuments(authorId, q, oneItem);

        CountResult result = new CountResult(page.getTotalElements());
        return ResponseEntity.ok(
                ApiResponse.success("Count retrieved successfully", HttpStatus.OK.value(), result)
        );
    }

    @Operation(
            summary = "Export current page as CSV",
            description = "Exports the current page of documents using the same filters and sorting as the list endpoint."
    )
    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(value = "authorId", required = false) Long authorId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false) String pageParam,
            @RequestParam(value = "size", required = false) String sizeParam,
            @RequestParam(value = "sort", defaultValue = "id,desc") String sortExpr,
            @RequestParam(value = "bom", required = false) Boolean bom,
            @RequestParam(value = "delimiter", required = false) String delimiter,
            @RequestParam(value = "filename", required = false) String customFilename,
            @RequestParam(value = "includeHeader", required = false) Boolean includeHeader,
            @RequestParam(value = "previewLen", required = false) Integer previewLen
    ) {

        final int page = Pagination.safePage(pageParam);
        final int size = Pagination.safeSize(sizeParam);
        final Sort sort = parseSort(sortExpr);
        final Pageable pageable = PageRequest.of(page, size, sort);
        q = trimToNull(q);

        final Page<Document> pageData = documentService.searchDocuments(authorId, q, pageable);

        final char delim = com.example.Krieger.util.CsvEscaper.resolveDelimiter(delimiter); // ',', ';', '\t'
        final boolean header = (includeHeader == null) ? true : includeHeader;
        final int preview = com.example.Krieger.util.CsvEscaper.clamp(previewLen, 20, 500, 120);

        StringBuilder sb = new StringBuilder(256 + pageData.getNumberOfElements() * 128);

        // ---- Header: must match test expectation exactly with default comma delimiter ----
        if (header) {
            if (delim == ',') {
                // unquoted, comma-separated header (tests assert this exact string)
                sb.append("id,title,authorId,createdAt,updatedAt,contentPreview").append('\n');
            } else {
                // other delimiters: still unquoted header, just use the chosen delimiter
                sb.append("id").append(delim)
                        .append("title").append(delim)
                        .append("authorId").append(delim)
                        .append("createdAt").append(delim)
                        .append("updatedAt").append(delim)
                        .append("contentPreview").append('\n');
            }
        }

        // ---- Data rows (quoted & escaped) ----
        for (Document d : pageData.getContent()) {
            String id = d.getId() == null ? "" : String.valueOf(d.getId());
            String title = safeStr(getTitle(d));
            String author = getAuthorIdString(d); // normalizes null/0 → ""
            String created = toStringSafe(getCreatedAt(d));
            String updated = toStringSafe(getUpdatedAt(d));
            String previewStr = com.example.Krieger.util.CsvEscaper.preview(getContent(d), preview);

            com.example.Krieger.util.CsvEscaper.appendRow(
                    sb, delim, id, title, author, created, updated, previewStr
            );
        }

        String body = sb.toString();
        if (Boolean.TRUE.equals(bom)) {
            body = "\uFEFF" + body; // UTF-8 BOM prefix
        }

        // Filename handling
        String ts = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .format(java.time.LocalDateTime.now());
        String fallbackBase = "documents_" + ts;
        String base = com.example.Krieger.util.CsvEscaper.sanitizeFilename(customFilename, fallbackBase);
        String filename = base.toLowerCase().endsWith(".csv") ? base : (base + ".csv");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        // IMPORTANT: declare UTF-8 so MockMvc decodes BOM correctly
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }




    // ---- small helpers to safely access entity fields ----
    private static String safeStr(String v) { return v == null ? "" : v; }

    private static String getTitle(Document d) { return d.getTitle(); }
    private static String getContent(Document d) { return d.getContent(); } // alias to body, provided on entity

    private static Object getCreatedAt(Document d) { return d.getCreatedAt(); }
    private static Object getUpdatedAt(Document d) { return d.getUpdatedAt(); }

    /**
     * Resolve authorId as string:
     * - use Document#getAuthorId() if present;
     * - treat null or 0 as empty string;
     * - fallback to author.getId() if available;
     * - otherwise empty.
     */
    private static String getAuthorIdString(Document d) {
        try {
            var m = d.getClass().getMethod("getAuthorId");
            Object val = m.invoke(d);
            if (val == null) return "";
            if (val instanceof Number) {
                long v = ((Number) val).longValue();
                return v == 0L ? "" : String.valueOf(v);
            }
            String s = String.valueOf(val);
            return ("0".equals(s) || "null".equalsIgnoreCase(s)) ? "" : s;
        } catch (Exception ignore) {
            // fallback: author.getId()
            try {
                var getAuthor = d.getClass().getMethod("getAuthor");
                Object author = getAuthor.invoke(d);
                if (author != null) {
                    var getId = author.getClass().getMethod("getId");
                    Object id = getId.invoke(author);
                    if (id == null) return "";
                    if (id instanceof Number && ((Number) id).longValue() == 0L) return "";
                    String s = String.valueOf(id);
                    return ("0".equals(s) || "null".equalsIgnoreCase(s)) ? "" : s;
                }
            } catch (Exception ignored) { /* swallow */ }
            return "";
        }
    }

    private static String toStringSafe(Object o) {
        return o == null ? "" : o.toString();
    }

    @Operation(summary = "Get a Document Summary",
            description = "Returns metadata + a short preview without sending the full body/content.")
    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getDocumentSummary(
            @PathVariable Long id,
            @RequestParam(value = "previewLen", required = false) Integer previewLen,
            @RequestParam(value = "stripHtml", required = false) Boolean stripHtml
    ) {
        // defaults: previewLen default=200, clamped to [50..500]; stripHtml default=true
        final int len = clamp(previewLen, 200, 50, 500);
        final boolean doStripHtml = (stripHtml == null) ? true : stripHtml.booleanValue();

        // load
        Document d = documentService.getDocumentById(id);
        if (d == null) {
            throw new com.example.Krieger.exception.CustomException("Document not found with ID: " + id,
                    org.springframework.http.HttpStatus.NOT_FOUND);
        }

        // compute preview (always a non-null String)
        String body = getContent(d);
        if (doStripHtml) {
            body = com.example.Krieger.util.TextSanitizer.stripHtml(body);
        }
        String preview = com.example.Krieger.util.CsvEscaper.preview(body == null ? "" : body, len);

        // author id (prefer convenience getter if present)
        Long authorId = extractAuthorId(d);

        // build a plain JSON object with { message, code, data:{...} }
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("id", d.getId());
        data.put("title", safeStr(getTitle(d)));
        data.put("authorId", authorId);
        data.put("createdAt", (d.getCreatedAt() instanceof java.time.Instant) ? d.getCreatedAt() : null);
        data.put("updatedAt", (d.getUpdatedAt() instanceof java.time.Instant) ? d.getUpdatedAt() : null);
        data.put("preview", preview);                 // string
        data.put("previewLength", preview.length());  // numeric length for assertions

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("message", "Summary retrieved");
        response.put("code", 200);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }


// ---------- keep these small helpers with your other helpers ----------

    private static int clamp(Integer val, int defaultVal, int min, int max) {
        int v = (val == null) ? defaultVal : val.intValue();
        if (v < min) v = min;
        if (v > max) v = max;
        return v;
    }

    /** Prefer Document#getAuthorId(); fallback to author.getId() via reflection; else null. */
    private static Long extractAuthorId(Document d) {
        try {
            var m = d.getClass().getMethod("getAuthorId");
            Object val = m.invoke(d);
            if (val instanceof Number) return ((Number) val).longValue();
            if (val != null) return Long.valueOf(String.valueOf(val));
        } catch (Exception ignore) {
            // ignore
        }
        try {
            var m = d.getClass().getMethod("getAuthor");
            Object author = m.invoke(d);
            if (author != null) {
                var mid = author.getClass().getMethod("getId");
                Object id = mid.invoke(author);
                if (id instanceof Number) return ((Number) id).longValue();
                if (id != null) return Long.valueOf(String.valueOf(id));
            }
        } catch (Exception ignore) { /* no-op */ }
        return null;
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update only the title of a document",
            description = "Trims and validates the provided title; max length 200."
    )
    @org.springframework.web.bind.annotation.PatchMapping(
            value = "/{id}/title",
            consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
            produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
    )
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> updateTitle(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody com.example.Krieger.dto.UpdateTitleRequest body
    ) {
        if (body == null) {
            java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
            resp.put("message", "Request body is required");
            resp.put("code", 400);
            resp.put("data", null);
            return org.springframework.http.ResponseEntity.badRequest().body(resp);
        }

        String raw = body.getTitle();
        String trimmed = (raw == null) ? null : raw.trim();

        if (trimmed == null || trimmed.isEmpty()) {
            java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
            resp.put("message", "Title must not be blank");
            resp.put("code", 400);
            resp.put("data", null);
            return org.springframework.http.ResponseEntity.badRequest().body(resp);
        }
        if (trimmed.length() > 200) {
            java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
            resp.put("message", "Title must be at most 200 characters");
            resp.put("code", 400);
            resp.put("data", null);
            return org.springframework.http.ResponseEntity.badRequest().body(resp);
        }

        com.example.Krieger.entity.Document updated = documentService.updateTitle(id, trimmed);

        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("message", "Title updated");
        resp.put("code", 200);
        resp.put("data", updated);
        return org.springframework.http.ResponseEntity.ok(resp);
    }

}
