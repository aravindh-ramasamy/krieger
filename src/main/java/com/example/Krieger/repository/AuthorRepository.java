package com.example.Krieger.repository;

import com.example.Krieger.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// JPA repository for Author
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}

