package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    // Proje gereksinimi: Kitap arama işlevi için özel sorgu
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Kitabın stok durumuna göre arama yapmak için:
    List<Book> findByCopiesGreaterThan(int copies);
}