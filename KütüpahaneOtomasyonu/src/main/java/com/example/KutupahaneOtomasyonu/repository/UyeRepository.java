package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Uye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// @Repository: Spring'e diyoruz ki: "Bu dosya Uyeler tablosunu yoneten depodur."
@Repository
// JpaRepository<Uye, Integer>:
// "Ben Uye tablosunu yonetiyorum ve bu tablonun ID'si Integer turunde."
public interface UyeRepository extends JpaRepository<Uye, Integer> { // MemberRepository -> UyeRepository

    // --- OZEL ARAMA 1: E-POSTA ILE BUL ---
    // Spring Data JPA Sihri: Metodun ismine bakarak SQL yazar.
    // SQL Karsiligi: "SELECT * FROM Uyeler WHERE email = ?"

    // Giris yaparken (Login) kullanici adini degil, bu e-posta adresini kontrol edecegiz.
    // Cunku senin veritabaninda ayri bir "kullanici_adi" sutunu yok.
    Optional<Uye> findByEmail(String email);

    // --- OZEL KONTROL: BU MAIL VAR MI? ---
    // Kayit olurken (Register) kullanilir.
    // "Bu email adresiyle kayitli biri var mi?" sorusunun cevabini (True/False) doner.
    // Eger True donerse, kullaniciya "Bu mail zaten kullanimda" hatasi veririz.
    boolean existsByEmail(String email);
}