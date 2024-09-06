package com.example.Krieger.controller;

import com.example.Krieger.DTO.ApiResponse;
import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.SuccessException;
import com.example.Krieger.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/authors")
public class AuthorController {
    @Autowired
    private AuthorService authorService;

    @PostMapping
    public ResponseEntity<ApiResponse<Author>> createAuthor(@RequestBody Author author) {
        if (author.getFirstName() == null || author.getLastName() == null) {
            throw new CustomException("First name or last name cannot be empty", HttpStatus.BAD_REQUEST);
        }

        Author createdAuthor = authorService.createAuthor(author);
        throw new SuccessException("Author created successfully", HttpStatus.CREATED, createdAuthor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Author>> getAuthorById(@PathVariable Long id) {
        Author author = authorService.getAuthorById(id);
        if (author == null) {
            throw new CustomException("Author not found with ID: " + id, HttpStatus.NOT_FOUND);
        }

        throw new SuccessException("Author retrieved successfully", HttpStatus.OK, author);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Author>> updateAuthor(@PathVariable Long id, @RequestBody Author author) {
        Author updatedAuthor = authorService.updateAuthor(id, author);
        throw new SuccessException("Author updated successfully", HttpStatus.OK, updatedAuthor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        throw new SuccessException("Author deleted successfully", HttpStatus.NO_CONTENT, null);
    }

}
