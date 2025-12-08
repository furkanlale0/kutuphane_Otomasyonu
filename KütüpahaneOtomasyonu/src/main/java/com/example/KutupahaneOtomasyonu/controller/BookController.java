package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Book;
import com.example.KutupahaneOtomasyonu.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.findAll();
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookService.save(book);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String title) {
        return bookService.searchByTitle(title);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Integer id) {
        try {
            bookService.deleteById(id);
            return ResponseEntity.ok("Kitap silindi.");
        } catch (IllegalStateException e) {
            // Servisten gelen "Silinemez" hatasını kullanıcıya gönder
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}