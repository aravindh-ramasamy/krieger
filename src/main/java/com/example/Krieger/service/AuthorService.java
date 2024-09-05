package com.example.Krieger.service;

import com.example.Krieger.entity.Author;
import com.example.Krieger.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    public Author createAuthor(Author author) {
        return authorRepository.save(author);
    }

}

