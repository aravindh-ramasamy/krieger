package com.example.krieger.config;

import com.example.Krieger.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RabbitMQConfigWiringTest {

    @Test
    void queueExchangeBindingUseExpectedNames() {
        RabbitMQConfig cfg = new RabbitMQConfig();

        Queue q = cfg.queue();
        DirectExchange ex = cfg.exchange();
        Binding b = cfg.binding(q, ex);

        assertEquals(RabbitMQConfig.QUEUE_NAME, q.getName());
        assertEquals(RabbitMQConfig.EXCHANGE_NAME, ex.getName());
        assertEquals(RabbitMQConfig.ROUTING_KEY, b.getRoutingKey());
    }
}
