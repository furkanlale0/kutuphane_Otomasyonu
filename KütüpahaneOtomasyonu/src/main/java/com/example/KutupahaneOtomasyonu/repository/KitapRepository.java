package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Kitap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// @Repository: Spring'e "Bu dosya Kitap tablosuyla konusan yetkili servistir" diyoruz.
@Repository
// JpaRepository<Kitap, Integer>:
// "Ben Kitap tablosunu yonetiyorum ve ID'si Integer turunde."
// Bunu dedigimiz an, Java bize 'save', 'delete', 'findAll' gibi temel metodlari hediye eder.
public interface KitapRepository extends JpaRepository<Kitap, Integer> { // BookRepository -> KitapRepository

    // --- SIHIRLI ARAMA METODU 1 ---
    // findByKitapAdiContainingIgnoreCase:
    // DIKKAT: Entity icindeki degisken adi "kitapAdi" oldugu icin metod ismi de boyle baslamak zorundadir.

    // Bu metod ismi Spring tarafindan parcalanir ve su anlama gelir:
    // 1. findByKitapAdi: Kitap Adina gore ara.
    // 2. Containing: Tam eslesme sart degil, icinde geciyorsa kabul et (SQL'deki LIKE %kelime% komutu).
    // 3. IgnoreCase: Buyuk/Kucuk harf fark etmez (JAVA = java = Java).

    // Yani kullanici "potter" yazsa bile "Harry Potter" kitabini bulur.
    List<Kitap> findByKitapAdiContainingIgnoreCase(String kitapAdi); // findByTitle... -> findByKitapAdi...

    // --- SIHIRLI ARAMA METODU 2 ---
    // findByStokSayisiGreaterThan:
    // DIKKAT: Entity icindeki degisken adi "stokSayisi" oldugu icin metod ismi de boyle olmak zorundadir.

    // "Stok sayisi, verilen sayidan BUYUK olanlari getir."
    // SQL Karsiligi: SELECT * FROM Kitaplar WHERE stok_sayisi > ?

    // Biz bunu genelde "findByStokSayisiGreaterThan(0)" diyerek,
    // sadece stokta KALMIS (bitmemis) kitaplari listelemek icin kullaniriz.
    List<Kitap> findByStokSayisiGreaterThan(int stokSayisi); // findByCopies... -> findByStokSayisi...
}