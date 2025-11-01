package com.example.Krieger.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Author(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName  = lastName;
    }

    // Convenience ctor for tests/DTO mapping where an id is known
    public Author(Long id, String firstName, String lastName) {
        this.id        = id;
        this.firstName = firstName;
        this.lastName  = lastName;
    }

    @Column(nullable = false)
    @NotBlank(message = "firstname is mandatory")
    private String firstName;

    @Column(nullable = false)
    @NotBlank(message = "lastname is mandatory")
    private String lastName;

    // One to Many relation with document, author can have many documents
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Document> documents;
}
