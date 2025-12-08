package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "authors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Integer authorId;

    private String name;
    private String surname;
    private Integer birthYear;

    // --- KRİTİK NOKTA BURASI ---
    // Bir yazarı çekerken, yazdığı kitapları da çekmeye çalışırsa sonsuz döngü olur.
    // @JsonIgnore diyerek "Yazarı JSON'a çevirirken kitap listesini görmezden gel" diyoruz.
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Book> books;
}