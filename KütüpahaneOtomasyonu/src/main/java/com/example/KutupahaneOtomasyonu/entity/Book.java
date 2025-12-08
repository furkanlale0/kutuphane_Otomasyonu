package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;

    @Column(nullable = false)
    private String title;

    private String isbn;
    private Integer year;
    private String category;

    @Column(columnDefinition = "INT DEFAULT 1")
    private int copies;

    // Yazar bilgisi önemli, onu getiriyoruz (EAGER)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    // --- SORUN BURADAYDI ---
    // Kitabı kimin eklediği bilgisini JSON'a çevirirken GÖRMEZDEN GEL (@JsonIgnore)
    // Yoksa "LazyInitializationException" veya Sonsuz Döngü hatası verir.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_admin")
    @JsonIgnore
    private Admin addedByAdmin;
}