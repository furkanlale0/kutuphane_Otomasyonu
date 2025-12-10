package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @Column(name = "publication_year")
    private Integer year;

    private Integer copies;

    // YENİ EKLENEN ALAN: ÖZET
    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    // Admin tarafından mı eklendi?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by")
    @JsonIgnoreProperties({"books", "password", "hibernateLazyInitializer"})
    private Admin addedByAdmin;
}