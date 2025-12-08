package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Author;
import com.example.KutupahaneOtomasyonu.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuthorService {
    
    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<Author> findAll() {
        return authorRepository.findAll();
    }
    
    public Author save(Author author) {
        return authorRepository.save(author);
    }
}