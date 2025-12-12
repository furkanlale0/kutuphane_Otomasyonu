package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// @Repository: Spring'e diyoruz ki: "Bu dosya Yazar tablosuyla konuşan depo sorumlusudur."
@Repository
// JpaRepository<Author, Integer>:
// "Ben Author (Yazar) tablosunu yönetiyorum ve bu tablonun ID'si Integer (Sayı) türünde."
// Bunu dediğimiz an; kaydetme, silme, hepsini getirme gibi özellikleri otomatik kazanırız.
public interface AuthorRepository extends JpaRepository<Author, Integer> {

    // --- ÖZEL ARAMA METODU ---
    // Spring Data JPA'nın en büyük sihri buradadır: "Method Name Strategy" (İsimden Sorgu Türetme).

    // Biz metoda "findByNameAndSurname" (İsimVeSoyisimeGöreBul) adını verdiğimiz an;
    // Spring arka planda şu SQL sorgusunu yazar ve çalıştırır:
    // "SELECT * FROM authors WHERE name = ? AND surname = ?"

    // Optional<Author>: Sonuç boş dönebilir (Belki böyle bir yazar yoktur).
    // O yüzden sonucu Optional kutusu içinde döneriz ki "Null Hatası" almayalım.
    Optional<Author> findByNameAndSurname(String name, String surname);
}