package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository: Spring'e diyoruz ki: "Bu dosya veritabanı ile konuşan bir Depo (Repository) görevlisidir."
// Veritabanı işlemlerini ve olası hataları yönetir.
@Repository
// interface: Burası bir sınıf değil, bir arayüzdür. Yani metodların içini biz doldurmayız.
// extends JpaRepository<Admin, Integer>:
// Burası sihirli kısımdır. JpaRepository'yi miras aldığımız için;
// "Kaydet (save)", "Sil (delete)", "Hepsini Getir (findAll)", "ID ile Bul (findById)" gibi
// yüzlerce satırlık temel kodları yazmamıza gerek kalmaz. Spring bunları bize hazır verir.
// <Admin, Integer> -> Bu depo "Admin" tablosunu yönetir ve o tablonun ID'si "Integer" türündedir.
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    // Spring Data JPA'nın "Sihirli Metod İsimlendirme" yeteneği:
    // Biz sadece "findByUsername" (KullanıcıAdınaGöreBul) yazarız.
    // Spring bunu okur ve arka planda şu SQL'i yazar: "SELECT * FROM admins WHERE username = ?"

    // Optional<Admin>: Bu metodun sonucu boş dönebilir (Böyle bir kullanıcı olmayabilir).
    // Java'da "NullPointerException" hatası almamak için, sonucu "Optional" (İsteğe Bağlı) kutusunda döneriz.
    // "Kutuyu aç, içinde admin varsa ver, yoksa hata fırlat" diyebilmek için.
    Optional<Admin> findByUsername(String username);
}