package com.example.Krieger.messaging;

import com.example.Krieger.config.RabbitMQConfig;
import com.example.Krieger.events.EventCodec;
import com.example.Krieger.events.EventType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OutboxPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Publish author-delete event directly to the queue consumer listens on. */
    public void publishAuthorDelete(long authorId) {
        String msg = EventCodec.encode(EventType.DELETE, authorId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, msg);
    }
}
