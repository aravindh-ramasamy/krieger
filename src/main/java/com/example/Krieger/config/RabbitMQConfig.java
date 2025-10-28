package com.example.Krieger.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// RabbitMQ configuration
@Configuration
public class RabbitMQConfig {

    // Setting name for queue, exchange and routing key
    public static final String QUEUE_NAME = "demo_queue";
    public static final String EXCHANGE_NAME = "demo_exchange";
    public static final String ROUTING_KEY = "demo_routing_key";

    @Bean
    public Queue queue() {
        return org.springframework.amqp.core.QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
}