package com.example.krieger.consumer;

import com.example.Krieger.consumer.Consumer;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ConsumerNonNumericIdTest {

    @Mock AuthorRepository authorRepository;
    @Mock DocumentRepository documentRepository;

    @InjectMocks Consumer consumer;

    @Test
    void nonNumericId_isIgnored() {
        consumer.consumeMessage("DELETE: abc"); // before: NumberFormatException; now: ignored
        verifyNoInteractions(authorRepository, documentRepository);
    }
}
