package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Yonetici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * BU SINIF NE İŞE YARAR?
 * Yönetici (Admin) varlıkları için veri erişim katmanıdır.
 * "Yoneticiler" tablosu üzerinde çalışır.
 * Özellikle yönetici girişi (Login) işlemlerinde kullanıcıyı veritabanında bulmak için kullanılır.
 */
@Repository
public interface YoneticiRepository extends JpaRepository<Yonetici, Integer> {

    /*
     * KULLANICI ADI İLE YÖNETİCİ BULMA
     * Sisteme giriş yapmaya çalışan yöneticinin kaydını getirir.
     * Spring Data JPA tarafından otomatik olarak SQL sorgusuna dönüştürülür.
     * SQL Karşılığı: SELECT * FROM Yoneticiler WHERE kullanici_adi = ?
     *
     * @return Optional<Yonetici>: Kullanıcı bulunamazsa null hatası vermemek için Optional döner.
     */
    Optional<Yonetici> findByKullaniciAdi(String kullaniciAdi);
}