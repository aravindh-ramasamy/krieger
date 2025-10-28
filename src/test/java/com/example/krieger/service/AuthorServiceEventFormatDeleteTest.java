package com.example.krieger.service;

import com.example.Krieger.config.RabbitMQConfig;
import com.example.Krieger.entity.Author;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthorServiceEventFormatDeleteTest {

    @InjectMocks
    private AuthorService authorService;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishAuthorEvent_DELETE_usesStrictFormat() {
        Author a = new Author();
        a.setId(42L);

        authorService.publishAuthorEvent("DELETE", a);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                "DELETE: 42"
        );
    }
}
