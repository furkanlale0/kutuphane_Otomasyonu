package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Yazar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/*
 * BU SINIF NE İŞE YARAR?
 * "Yazarlar" tablosu için veri erişim katmanıdır.
 * Yazar ekleme, silme ve isim-soyisim kombinasyonuna göre arama yapma
 * işlemlerini yönetir.
 */
@Repository
public interface YazarRepository extends JpaRepository<Yazar, Integer> {

    /*
     * İSİM VE SOYİSİM İLE YAZAR BULMA
     * Veritabanında aynı isim ve soyisimde bir yazarın kayıtlı olup olmadığını
     * kontrol etmek için kullanılır.
     * Genellikle kitap ekleme işleminden önce, yazarın sistemde var olup olmadığı
     * bu metod ile teyit edilir (Mükerrer kayıt önleme).
     *
     * SQL Karşılığı: SELECT * FROM Yazarlar WHERE ad = ? AND soyad = ?
     */
    Optional<Yazar> findByAdAndSoyad(String ad, String soyad);
}