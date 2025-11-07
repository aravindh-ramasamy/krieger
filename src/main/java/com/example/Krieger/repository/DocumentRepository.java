package com.example.Krieger.repository;

import com.example.Krieger.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // ✅ FIX: traverse the @ManyToOne relation (Document.author.id)
    List<Document> findByAuthor_Id(Long authorId);

    // 👉 Used by /api/documents/suggest (no author filter)
    List<Document> findByTitleContainingIgnoreCase(String q, Pageable pageable);

    // 👉 Used by /api/documents/suggest (with author filter)
    List<Document> findByTitleContainingIgnoreCaseAndAuthor_Id(String q, Long authorId, Pageable pageable);

    // 👉 Reusable search for list/count endpoints
    @Query("""
           SELECT d FROM Document d
           WHERE (:authorId IS NULL OR d.author.id = :authorId)
             AND (
                 :q IS NULL
                 OR LOWER(d.title)      LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(d.body)       LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(d.references) LIKE LOWER(CONCAT('%', :q, '%'))
             )
           """)
    Page<Document> search(@Param("authorId") Long authorId,
                          @Param("q") String q,
                          Pageable pageable);
}
