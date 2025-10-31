package com.example.Krieger.repository;

import com.example.Krieger.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    boolean existsByFirstNameAndLastName(String firstName, String lastName);
    boolean existsByFirstNameAndLastNameAndIdNot(String firstName, String lastName, Long id);

    @Query(
            value = """
      SELECT a FROM Author a
      WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
         OR LOWER(a.lastName)  LIKE LOWER(CONCAT('%', :q, '%'))
    """,
            countQuery = """
      SELECT COUNT(a) FROM Author a
      WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
         OR LOWER(a.lastName)  LIKE LOWER(CONCAT('%', :q, '%'))
    """
    )
    Page<Author> searchByName(@Param("q") String q, Pageable pageable);
}
