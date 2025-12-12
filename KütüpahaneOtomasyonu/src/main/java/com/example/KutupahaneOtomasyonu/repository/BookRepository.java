package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// @Repository: Spring'e "Bu dosya Kitap tablosuyla konuşan yetkili servistir" diyoruz.
@Repository
// JpaRepository<Book, Integer>:
// "Ben Book (Kitap) tablosunu yönetiyorum ve ID'si Integer türünde."
// Bunu dediğimiz an, Java bize 'save', 'delete', 'findAll' gibi temel metodları hediye eder.
public interface BookRepository extends JpaRepository<Book, Integer> {

    // --- SİHİRLİ ARAMA METODU 1 ---
    // findByTitleContainingIgnoreCase:
    // Bu metod ismi Spring tarafından parçalanır ve şu anlama gelir:
    // 1. findByTitle: Başlığa göre ara.
    // 2. Containing: Tam eşleşme şart değil, içinde geçiyorsa kabul et (SQL'deki LIKE %kelime% komutu).
    // 3. IgnoreCase: Büyük/Küçük harf fark etmez (JAVA = java = Java).

    // Yani kullanıcı "potter" yazsa bile "Harry Potter" kitabını bulur.
    List<Book> findByTitleContainingIgnoreCase(String title);

    // --- SİHİRLİ ARAMA METODU 2 ---
    // findByCopiesGreaterThan:
    // "Kopya sayısı (Stok), verilen sayıdan BÜYÜK olanları getir."
    // SQL Karşılığı: SELECT * FROM books WHERE copies > ?

    // Biz bunu genelde "findByCopiesGreaterThan(0)" diyerek,
    // sadece stokta KALMIŞ (bitmemiş) kitapları listelemek için kullanırız.
    List<Book> findByCopiesGreaterThan(int copies);
}