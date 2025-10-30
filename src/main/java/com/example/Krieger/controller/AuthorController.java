package com.example.Krieger.controller;

import com.example.Krieger.dto.ApiResponse;
import com.example.Krieger.dto.AuthorDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// Rest API for author CRUD operations and sends responses
@RestController
@RequestMapping("/api/authors")
@Tag(name = "Author API", description = "API for Authors")
@Validated
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    // Create new author
    @Operation(summary = "Create a New Author", description = "Create a New Author.")
    @PostMapping
    public ResponseEntity<ApiResponse<Author>> createAuthor(@Valid @RequestBody AuthorDTO author) {
        // Bean Validation handles field checks (e.g., @NotBlank in AuthorDTO)
        Author createdAuthor = authorService.createAuthor(author);
        throw new SuccessException("Author created successfully", HttpStatus.CREATED, createdAuthor);
    }

    // Retrieve author by ID
    @Operation(summary = "Retrieve an author by ID", description = "Get an author by specific ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Author>> getAuthorById(@PathVariable @Positive Long id) {
        Author author = authorService.getAuthorById(id);
        if (author == null) {
            throw new CustomException("Author not found with ID: " + id, HttpStatus.NOT_FOUND);
        }
        throw new SuccessException("Author retrieved successfully", HttpStatus.OK, author);
    }

    // Update author by ID
    @Operation(summary = "Update an existing author", description = "Update an existing author.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Author>> updateAuthor(@PathVariable @Positive Long id,
                                                            @Valid @RequestBody AuthorDTO author) {
        Author updatedAuthor = authorService.updateAuthor(id, author);
        throw new SuccessException("Author updated successfully", HttpStatus.OK, updatedAuthor);
    }

    // Delete an author by ID
    @Operation(summary = "Delete an existing author", description = "Delete an existing author.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAuthor(@PathVariable @Positive Long id) {
        authorService.deleteAuthor(id);
        throw new SuccessException("Author deleted successfully", HttpStatus.OK, null);
    }
}
