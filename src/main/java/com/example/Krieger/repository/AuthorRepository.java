package com.example.Krieger.repository;

import com.example.Krieger.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    boolean existsByFirstNameAndLastName(String firstName, String lastName);
    boolean existsByFirstNameAndLastNameAndIdNot(String firstName, String lastName, Long id);
}
