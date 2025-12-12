package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// @Repository: Spring'e diyoruz ki: "Bu dosya Üyeler (members) tablosunu yöneten depodur."
@Repository
// JpaRepository<Member, Integer>:
// "Ben Member (Üye) tablosunu yönetiyorum ve bu tablonun ID'si Integer türünde."
// Bunu dediğimiz an; save (kaydet), delete (sil), findAll (hepsini getir) gibi yetenekleri kazanırız.
public interface MemberRepository extends JpaRepository<Member, Integer> {

    // --- ÖZEL ARAMA 1: E-POSTA İLE BUL ---
    // Spring Data JPA Sihri: Metodun ismine bakarak SQL yazar.
    // SQL Karşılığı: "SELECT * FROM members WHERE email = ?"
    // Optional: Sonuç boş dönebilir (Böyle bir mail olmayabilir), o yüzden güvenli kutu (Optional) içinde döneriz.
    Optional<Member> findByEmail(String email);

    // --- ÖZEL ARAMA 2: KULLANICI ADI İLE BUL ---
    // SQL Karşılığı: "SELECT * FROM members WHERE username = ?"
    // Bu metod özellikle "Giriş Yapma (Login)" sırasında çok kritiktir.
    // Çünkü kullanıcı adını yazıp şifresini kontrol etmemiz gerekir.
    Optional<Member> findByUsername(String username); // YENİ
}