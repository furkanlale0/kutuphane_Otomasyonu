package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
    // İsim ve Soyisim ile yazar bulan özel sorgu
    Optional<Author> findByNameAndSurname(String name, String surname);
}