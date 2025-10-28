package com.example.krieger.config;

import com.example.Krieger.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMQQueueDurabilityTest {

    @Test
    void queue_isDurable_andNotAutoDelete() {
        RabbitMQConfig cfg = new RabbitMQConfig();
        Queue q = cfg.queue();
        assertTrue(q.isDurable());
        assertFalse(q.isAutoDelete());
        // exclusive should be false in this setup
        assertFalse(q.isExclusive());
    }
}
