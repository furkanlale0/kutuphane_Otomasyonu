package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Author;
import com.example.KutupahaneOtomasyonu.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// @RestController: Bu sınıfın web isteklerini (HTTP) karşılayan bir kontrolcü olduğunu,
// ve geriye HTML sayfası değil, JSON verisi (data) döndüreceğini belirtir.
@RestController
// @RequestMapping: Bu sınıftaki tüm işlemlerin "/api/authors" adresi altında olduğunu söyler.
// Yani tarayıcıya "localhost:8080/api/authors" yazıldığında burası dinler.
@RequestMapping("/api/authors")
public class AuthorController {

    // İşleri yaptıracağımız "Usta"yı (Servisi) tanımlıyoruz.
    private final AuthorService authorService;

    // Constructor Injection: Spring'e "Bana çalışan bir AuthorService ver" diyoruz.
    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // --- YAZARLARI LİSTELE ---
    // @GetMapping: Eğer kullanıcı bu adrese "GET" (Veri Okuma) isteği atarsa burası çalışır.
    // Örn: Tarayıcıdan siteye girmek bir GET isteğidir.
    @GetMapping
    public List<Author> getAllAuthors() {
        // Servise emrediyoruz: "Git veritabanındaki bütün yazarları bul getir."
        return authorService.findAll();
    }

    // --- YAZAR EKLE ---
    // @PostMapping: Eğer kullanıcı bu adrese "POST" (Veri Gönderme/Kaydetme) isteği atarsa burası çalışır.
    // @RequestBody: Gelen isteğin içindeki veriyi (JSON formatındaki Yazar adı, soyadı vs.)
    // alır ve Java'daki 'Author' nesnesine dönüştürür.
    @PostMapping
    public Author addAuthor(@RequestBody Author author) {
        // Servise emrediyoruz: "Elimdeki bu yazar bilgisini veritabanına kaydet."
        return authorService.save(author);
    }
}