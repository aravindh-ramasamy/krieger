package com.example.Krieger.repository;

import com.example.Krieger.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// JPA repository for Document
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByAuthorId(Long authorId); // finds document by author ID
}
