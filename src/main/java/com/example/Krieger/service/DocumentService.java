package com.example.Krieger.service;

import com.example.Krieger.dto.DocumentDTO;
import com.example.Krieger.entity.Author;
import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.AuthorRepository;
import com.example.Krieger.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AuthorRepository authorRepository;

    // Creates new document
    public Document createDocument(DocumentDTO documentDTO) {
        Document document = new Document();
        document.setBody(documentDTO.getBody());
        document.setTitle(documentDTO.getTitle());
        document.setReferences(documentDTO.getReference());
        Author author = authorRepository.findById(documentDTO.getAuthorID())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with ID: " + documentDTO.getAuthorID()));
        document.setAuthor(author);
        return documentRepository.save(document);
    }

    // Fetches Document by ID
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    // Updates document by ID
    public Document updateDocument(Long id, DocumentDTO documentDetails) {
        Document document = getDocumentById(id);
        document.setTitle(documentDetails.getTitle());
        document.setBody(documentDetails.getBody());
        Author author = authorRepository.findById(documentDetails.getAuthorID())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with ID: " + documentDetails.getAuthorID()));
        document.setAuthor(author);
        document.setReferences(documentDetails.getReference());
        return documentRepository.save(document);
    }

    // Deletes Document by ID
    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}