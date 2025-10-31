package com.example.Krieger.service;

import com.example.Krieger.config.RabbitMQConfig;
import com.example.Krieger.dto.AuthorDTO;
import com.example.Krieger.dto.AuthorSummaryDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.CustomException;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.AuthorRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // creates a new author
    public Author createAuthor(AuthorDTO authorDTO) {
        String fn = normalize(authorDTO.getFirstName());
        String ln = normalize(authorDTO.getLastName());

        // 409 if another author with same (firstName, lastName) exists
        if (authorRepository.existsByFirstNameAndLastName(fn, ln)) {
            throw new CustomException("Author already exists: " + fn + " " + ln, HttpStatus.CONFLICT);
        }

        Author author = new Author();
        author.setFirstName(fn);
        author.setLastName(ln);
        Author savedAuthor = authorRepository.save(author);
        publishAuthorEvent("CREATE", savedAuthor);
        return savedAuthor;
    }

    // Update author by ID
    public Author updateAuthor(Long id, AuthorDTO authorDetails) {
        Author author = getAuthorById(id);

        String newFn = normalize(authorDetails.getFirstName());
        String newLn = normalize(authorDetails.getLastName());

        boolean nameChanged = !safeEq(author.getFirstName(), newFn) || !safeEq(author.getLastName(), newLn);
        if (nameChanged && authorRepository.existsByFirstNameAndLastNameAndIdNot(newFn, newLn, id)) {
            throw new CustomException("Another author already uses: " + newFn + " " + newLn, HttpStatus.CONFLICT);
        }

        author.setFirstName(newFn);
        author.setLastName(newLn);
        Author updatedAuthor = authorRepository.save(author);
        publishAuthorEvent("UPDATE", updatedAuthor);
        return updatedAuthor;
    }

    // Delete author by ID
    public void deleteAuthor(Long id) {
        Author author = getAuthorById(id);
        authorRepository.deleteById(id);
        publishAuthorEvent("DELETE", author);
    }

    // Publishes event to Queue with event type
    public void publishAuthorEvent(String eventType, Author author) {
        String message = eventType + ": " + author.getId();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
    }

    public List<AuthorSummaryDTO> searchAuthorsAsDtos(String query, int page, int size, Sort sort) {
        String q = query == null ? "" : query.trim();
        PageRequest pr = PageRequest.of(page, size, sort);
        Page<Author> result = authorRepository.searchByName(q, pr);
        return result.getContent().stream()
                .map(a -> new AuthorSummaryDTO(a.getId(), a.getFirstName(), a.getLastName()))
                .toList();
    }

    public Author getAuthorById(Long id) {
        return authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Author not found with ID: " + id));
    }

    // helpers
    private static String normalize(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean safeEq(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
}
