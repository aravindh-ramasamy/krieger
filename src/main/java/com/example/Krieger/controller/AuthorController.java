package com.example.Krieger.controller;

import com.example.Krieger.dto.ApiResponse;
import com.example.Krieger.dto.AuthorDTO;
import com.example.Krieger.dto.AuthorSummaryDTO;
import com.example.Krieger.dto.BulkDeleteRequest;
import com.example.Krieger.dto.BulkDeleteResult;
import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.messaging.OutboxPublisher;
import com.example.Krieger.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Rest API for author CRUD operations and sends responses
@RestController
@RequestMapping("/api/authors")
@Tag(name = "Author API", description = "API for Authors")
@Validated
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private OutboxPublisher outboxPublisher;

    // Create new author
    @Operation(summary = "Create a New Author", description = "Create a New Author.")
    @PostMapping
    public ResponseEntity<ApiResponse<Author>> createAuthor(@Valid @RequestBody AuthorDTO author) {
        // Bean Validation handles field checks (e.g., @NotBlank in AuthorDTO)
        Author createdAuthor = authorService.createAuthor(author);
        throw new SuccessException("Author created successfully", HttpStatus.CREATED, createdAuthor);
    }

    // Update author by ID
    @Operation(summary = "Update an existing author", description = "Update an existing author.")
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<Author>> updateAuthor(@PathVariable @Positive Long id,
                                                            @Valid @RequestBody AuthorDTO author) {
        Author updatedAuthor = authorService.updateAuthor(id, author);
        throw new SuccessException("Author updated successfully", HttpStatus.OK, updatedAuthor);
    }

    // Delete an author by ID
    @Operation(summary = "Delete an existing author", description = "Delete an existing author.")
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        var author = authorService.getAuthorById(id);
        if (author == null) {
            throw new CustomException("Author not found with ID: " + id, HttpStatus.NOT_FOUND);
        }
        outboxPublisher.publishAuthorDelete(id);
        return ResponseEntity.accepted().build(); // 202
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAuthors(
            @RequestParam("query") @NotBlank(message = "query must not be blank") String query,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(name = "sort", required = false) String sortParam // e.g. lastName,asc;firstName,asc
    )   {
        Sort sort = parseSort(sortParam).orElseGet(() ->
                Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())
        );
        List<AuthorSummaryDTO> dtoList = authorService.searchAuthorsAsDtos(query, page, size, sort);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable long id) {
        Author author = authorService.getAuthorById(id);
        if (author == null) {
            throw new CustomException("Author not found with ID: " + id, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(author);
    }


    private Optional<Sort> parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Optional.empty();
        // format: field,dir;field2,dir2 (dir in [asc,desc], case-insensitive)
        String[] parts = sortParam.split(";");
        List<Sort.Order> orders = new ArrayList<>();
        for (String p : parts) {
            String[] seg = p.trim().split(",", 2);
            if (seg.length == 0 || seg[0].isBlank()) continue;
            String prop = seg[0].trim();
            String dir = seg.length > 1 ? seg[1].trim().toLowerCase(Locale.ROOT) : "asc";
            Sort.Order o = "desc".equals(dir) ? Sort.Order.desc(prop) : Sort.Order.asc(prop);
            orders.add(o);
        }
        if (orders.isEmpty()) return Optional.empty();
        return Optional.of(Sort.by(orders));
    }

    @GetMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDeleteGetNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<BulkDeleteResult>> bulkDelete(
            @org.springframework.web.bind.annotation.RequestBody BulkDeleteRequest req) {

        if (req == null || req.getIds() == null || req.getIds().isEmpty()) {
            throw new com.example.Krieger.exception.CustomException("ids must not be empty",
                    HttpStatus.BAD_REQUEST);
        }
        BulkDeleteResult result = authorService.enqueueDeleteByIds(req.getIds());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Delete events enqueued",
                        HttpStatus.ACCEPTED.value(), result));
    }
}
