package com.example.Krieger.service;

import com.example.Krieger.entity.Document;
import com.example.Krieger.exception.ResourceNotFoundException;
import com.example.Krieger.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    public Document createDocument(Document document) {
        return documentRepository.save(document);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    public Document updateDocument(Long id, Document documentDetails) {
        Document document = getDocumentById(id);
        document.setTitle(documentDetails.getTitle());
        document.setBody(documentDetails.getBody());
        document.setReferences(documentDetails.getReferences());
        return documentRepository.save(document);
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}