package com.example.krieger.service;

import com.example.Krieger.dto.DocumentDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.repository.DocumentRepository;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock AuthorRepository authorRepository;

    @InjectMocks DocumentService service;

    @BeforeEach
    void init() { MockitoAnnotations.openMocks(this); }

    @Test
    void create_requires_authorId() {
        DocumentDTO dto = new DocumentDTO();
        dto.setTitle("A");
        dto.setBody("B");
        dto.setReference("C");
        dto.setAuthorID(null);

        assertThrows(IllegalArgumentException.class, () -> service.createDocument(dto));
        verify(documentRepository, never()).save(any());
    }

    @Test
    void create_author_must_exist() {
        DocumentDTO dto = new DocumentDTO();
        dto.setTitle("A");
        dto.setBody("B");
        dto.setReference("C");
        dto.setAuthorID(7L);

        when(authorRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createDocument(dto));
    }

    @Test
    void update_ignores_nulls_and_blanks_and_trims() {
        // existing doc
        Document existing = new Document();
        existing.setId(1L);
        existing.setTitle("Old Title");
        existing.setBody("Old Body");
        existing.setReferences("RFC-0");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        // dto with null/blank updates; title has spaces and should trim
        DocumentDTO dto = new DocumentDTO();
        dto.setTitle("  New Title  ");          // -> "New Title"
        dto.setBody(null);                      // ignored
        dto.setReference("    ");               // empty after trim => ignored
        dto.setAuthorID(null);                  // ignored

        Document updated = service.updateDocument(1L, dto);

        assertEquals("New Title", updated.getTitle()); // trimmed
        assertEquals("Old Body", updated.getBody());   // unchanged
        assertEquals("RFC-0", updated.getReferences()); // unchanged
        verify(authorRepository, never()).findById(anyLong());
        verify(documentRepository, times(1)).save(existing);
    }

    @Test
    void update_changes_author_when_id_present() {
        Document existing = new Document();
        existing.setId(2L);

        Author newAuthor = new Author();
        newAuthor.setId(99L);

        when(documentRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(authorRepository.findById(99L)).thenReturn(Optional.of(newAuthor));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentDTO dto = new DocumentDTO();
        dto.setAuthorID(99L);

        Document updated = service.updateDocument(2L, dto);
        assertEquals(newAuthor, updated.getAuthor());
        verify(authorRepository).findById(99L);
        verify(documentRepository).save(existing);
    }

    @Test
    void update_404_when_new_author_missing() {
        Document existing = new Document();
        existing.setId(3L);

        when(documentRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(authorRepository.findById(5L)).thenReturn(Optional.empty());

        DocumentDTO dto = new DocumentDTO();
        dto.setAuthorID(5L);

        assertThrows(ResourceNotFoundException.class, () -> service.updateDocument(3L, dto));
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void delete_404_when_document_missing() {
        when(documentRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteDocument(42L));
        verify(documentRepository, never()).deleteById(anyLong());
    }

    @Test
    void update_calls_save_once_and_does_not_lookup_author_when_absent() {
        Document existing = new Document();
        existing.setId(10L);
        existing.setTitle("T1");
        when(documentRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentDTO dto = new DocumentDTO();
        dto.setTitle("T2"); // only title change

        Document result = service.updateDocument(10L, dto);
        assertEquals("T2", result.getTitle());

        verify(authorRepository, never()).findById(anyLong());
        verify(documentRepository, times(1)).save(existing);
    }

    @Test
    void update_changes_multiple_fields_atomically() {
        Document existing = new Document();
        existing.setId(20L);
        existing.setTitle("Old");
        existing.setBody("Body");
        existing.setReferences("Ref");

        Author author = new Author();
        author.setId(11L);

        when(documentRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(authorRepository.findById(11L)).thenReturn(Optional.of(author));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentDTO dto = new DocumentDTO();
        dto.setTitle("New");
        dto.setBody("New Body");
        dto.setReference("New Ref");
        dto.setAuthorID(11L);

        Document result = service.updateDocument(20L, dto);

        assertEquals("New", result.getTitle());
        assertEquals("New Body", result.getBody());
        assertEquals("New Ref", result.getReferences());
        assertEquals(author, result.getAuthor());
    }

    // Append to: src/test/java/com/example/krieger/service/DocumentServiceTest.java

    @Test
    void create_normalizes_blanks() {
        DocumentDTO dto = new DocumentDTO();
        dto.setTitle("  Hello  ");
        dto.setBody("  ");                 // becomes null
        dto.setReference("  RFC-42  ");    // trims
        dto.setAuthorID(1L);

        Author a = new Author(); a.setId(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(a));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        Document out = service.createDocument(dto);
        assertEquals("Hello", out.getTitle());
        assertNull(out.getBody());
        assertEquals("RFC-42", out.getReferences());
    }

    @Test
    void delete_success_when_present() {
        Document d = new Document(); d.setId(9L);
        when(documentRepository.findById(9L)).thenReturn(Optional.of(d));
        doNothing().when(documentRepository).deleteById(9L);

        assertDoesNotThrow(() -> service.deleteDocument(9L));
        verify(documentRepository).deleteById(9L);
    }

}
