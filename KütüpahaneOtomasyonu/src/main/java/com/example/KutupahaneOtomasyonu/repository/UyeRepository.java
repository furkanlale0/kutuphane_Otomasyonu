package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Uye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/*
 * BU SINIF NE İŞE YARAR?
 * Üye tablosu ("Uyeler") için veri erişim katmanıdır.
 * Özellikle Giriş (Login) ve Kayıt (Register) işlemleri sırasında
 * kullanıcıyı E-posta adresine göre bulmak için özelleştirilmiştir.
 */
@Repository
public interface UyeRepository extends JpaRepository<Uye, Integer> {

    /*
     * E-POSTA İLE ÜYE BULMA
     * Giriş yaparken kullanıcı adı yerine E-posta kullanıldığı için,
     * sistemin kullanıcıyı veritabanında bulmasını sağlayan temel sorgudur.
     * SQL Karşılığı: SELECT * FROM Uyeler WHERE email = ?
     *
     * @return Optional<Uye>: Kullanıcı bulunamazsa null yerine boş bir kutu döner (NullPointerException önlenir).
     */
    Optional<Uye> findByEmail(String email);

    /*
     * MÜKERRER KAYIT KONTROLÜ
     * Yeni üye kaydı sırasında, girilen E-posta adresinin
     * sistemde daha önce kullanılıp kullanılmadığını kontrol eder.
     * Kayıt formunda "Bu E-posta zaten kullanımda" hatası vermek için kullanılır.
     */
    boolean existsByEmail(String email);
}