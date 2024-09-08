package com.example.Krieger.service;

import com.example.Krieger.config.RabbitMQConfig;
import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.AuthorRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // creates a new author
    public Author createAuthor(Author author) {
        Author savedAuthor = authorRepository.save(author);
        publishAuthorEvent("CREATE", savedAuthor);
        return savedAuthor;
    }

    // Fetches an author byt ID
    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
    }

    // Update author by DI
    public Author updateAuthor(Long id, Author authorDetails) {
        Author author = getAuthorById(id);
        author.setFirstName(authorDetails.getFirstName());
        author.setLastName(authorDetails.getLastName());
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
        String message = String.format("%s event: %s", eventType, author.getId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
    }

}

