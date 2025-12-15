package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Yazar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// @Repository: Spring'e diyoruz ki: "Bu dosya Yazar tablosuyla konusan depo sorumlusudur."
@Repository
// JpaRepository<Yazar, Integer>:
// "Ben Yazar tablosunu yonetiyorum ve bu tablonun ID'si Integer (Sayi) turunde."
// Bunu dedigimiz an; kaydetme, silme, hepsini getirme gibi ozellikleri otomatik kazaniriz.
public interface YazarRepository extends JpaRepository<Yazar, Integer> { // AuthorRepository -> YazarRepository

    // --- OZEL ARAMA METODU (SIHIRLI KISIM) ---
    // Spring Data JPA'nin en buyuk sihri buradadir: "Method Name Strategy" (Isimden Sorgu Turetme).

    // DIKKAT: Yazar entity'sinde degisken isimlerini "ad" ve "soyad" yapmistik.
    // O yuzden metod adini da ona uydurmak ZORUNDAYIZ: "findByAdAndSoyad".

    // Spring arka planda su SQL sorgusunu yazar ve calistirir:
    // "SELECT * FROM Yazarlar WHERE ad = ? AND soyad = ?"

    // Optional<Yazar>: Sonuc bos donebilir (Belki boyle bir yazar yoktur).
    // O yuzden sonucu Optional kutusu icinde doneriz ki "Null Hatasi" almayalim.
    Optional<Yazar> findByAdAndSoyad(String ad, String soyad);
}