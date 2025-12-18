package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Kitap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/*
 * BU SINIF NE İŞE YARAR?
 * Kitap varlığı (Entity) için veri erişim katmanıdır.
 * Spring Data JPA teknolojisini kullanarak, "Kitaplar" tablosu üzerinde
 * CRUD (Ekleme, Okuma, Güncelleme, Silme) işlemlerini otomatikleştirir.
 */
@Repository
public interface KitapRepository extends JpaRepository<Kitap, Integer> {

    /*
     * DİNAMİK ARAMA METODU (Derived Query Method)
     * Kullanıcının girdiği kelimeye göre kitap adında arama yapar.
     * SQL Karşılığı: SELECT * FROM Kitaplar WHERE LOWER(kitap_adi) LIKE LOWER('%parametre%')
     * Özellik: Büyük/Küçük harf duyarlılığı yoktur (Case Insensitive) ve kısmi eşleşmeyi kabul eder.
     */
    List<Kitap> findByKitapAdiContainingIgnoreCase(String kitapAdi);

    /*
     * STOK KONTROL SORGUSU
     * Stok sayısı belirtilen değerden büyük olan kitapları listeler.
     * Genellikle parametre olarak '0' verilerek, sadece stoğu tükenmemiş (mevcut)
     * kitapları listelemek için kullanılır.
     */
    List<Kitap> findByStokSayisiGreaterThan(int stokSayisi);
}