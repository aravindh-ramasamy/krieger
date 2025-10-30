package com.example.Krieger.service;

import com.example.Krieger.config.RabbitMQConfig;
import com.example.Krieger.dto.AuthorDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.AuthorRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Krieger.messaging.EventCodec;
import com.example.Krieger.messaging.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(AuthorService.class);

    // creates a new author
    public Author createAuthor(AuthorDTO authorDTO) {
        Author author = new Author();
        author.setFirstName(authorDTO.getFirstName());
        author.setLastName(authorDTO.getLastName());
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
    public Author updateAuthor(Long id, AuthorDTO authorDetails) {
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
        if (author == null || author.getId() == null) {
            log.warn("publishAuthorEvent called with null author or id; skipping");
            return;
        }
        EventType type = EventType.fromString(eventType);
        if (type == null) {
            log.warn("Unknown eventType '{}'; skipping publish", eventType);
            return;
        }
        String payload = EventCodec.encode(type, author.getId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                payload
        );
        log.debug("Published author event: {}", payload);
    }
}

