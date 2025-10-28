package com.example.krieger.consumer;

import com.example.Krieger.consumer.Consumer;
import com.example.Krieger.entity.Document;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerDeleteBehaviorTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private AuthorRepository authorRepository;

    @InjectMocks private Consumer consumer;

    @Test
    void deleteMessage_deletesAuthorAndDocs() {
        // Given
        when(documentRepository.findByAuthorId(42L))
                .thenReturn(List.of(new Document()));

        // When
        consumer.consumeMessage("DELETE: 42");

        // Then
        verify(documentRepository).findByAuthorId(42L);
        verify(documentRepository).deleteAll(anyList());
        verify(authorRepository).deleteById(42L);
        verifyNoMoreInteractions(documentRepository, authorRepository);
    }

    @Test
    void legacyFormat_doesNotTriggerDeletion() {
        // When
        consumer.consumeMessage("DELETE event: 42");

        // Then: no repository interactions (eventType != "DELETE")
        verifyNoInteractions(documentRepository, authorRepository);
    }
}
