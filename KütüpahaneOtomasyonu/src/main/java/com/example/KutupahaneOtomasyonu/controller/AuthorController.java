package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Author;
import com.example.KutupahaneOtomasyonu.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // Yazarları Listele
    @GetMapping
    public List<Author> getAllAuthors() {
        return authorService.findAll();
    }
    
    // Yazar Ekle
    @PostMapping
    public Author addAuthor(@RequestBody Author author) {
        return authorService.save(author);
    }
}