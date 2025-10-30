package com.example.Krieger.consumer;

import com.example.Krieger.config.RabbitMQConfig;
import com.example.Krieger.entity.Document;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.repository.DocumentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Consumer {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private DocumentRepository documentRepository;

    // Listens for messages from the Queue
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeMessage(String message) {
        log.info("Received message: {}", message);

        var decodedOpt = EventCodec.decode(message);
        if (decodedOpt.isEmpty()) {
            log.warn("Ignoring malformed message: {}", message);
            return;
        }
        String eventType = m.group(1);
        Long authorId = Long.parseLong(m.group(2));

        var decoded = decodedOpt.get();
        if (decoded.getType() != EventType.DELETE) {
            log.debug("Ignoring non-DELETE event: {}", decoded.getType());
            return;
        }

        long authorId = decoded.getId();
        java.util.List<Document> documents = documentRepository.findByAuthorId(authorId);
        documentRepository.deleteAll(documents);
        authorRepository.deleteById(authorId);
        log.info("Author {} and documents deleted", authorId);
    }
}
