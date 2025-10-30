package com.example.Krieger.consumer;

import com.example.Krieger.config.RabbitMQConfig;
import com.example.Krieger.entity.Document;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Krieger.messaging.EventCodec;
import com.example.Krieger.messaging.EventType;

import java.util.List;

@Service
public class Consumer {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private DocumentRepository documentRepository;

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    // Listens for messages from the Queue
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeMessage(String message) {
        log.info("Received message: {}", message);

        java.util.Optional<EventCodec.DecodedEvent> decoded = EventCodec.decode(message);
        if (decoded.isEmpty()) {
            log.warn("Ignoring malformed message: {}", message);
            return;
        }

        EventCodec.DecodedEvent evt = decoded.get();
        EventType eventType = evt.getType();
        Long authorId = evt.getId();

        // If DELETE event type, remove the author and their associated documents.
        if (eventType == EventType.DELETE) {
            java.util.List<Document> documents = documentRepository.findByAuthorId(authorId);
            documentRepository.deleteAll(documents);
            authorRepository.deleteById(authorId);
            log.info("Author {} and documents deleted", authorId);
        }
    }
}
