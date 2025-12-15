package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Ceza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// @Repository: Spring'e diyoruz ki: "Bu dosya Cezalar tablosunu yoneten depodur."
@Repository
// JpaRepository<Ceza, Integer>:
// "Ben Ceza tablosunu yonetiyorum ve bu tablonun ID'si Integer turunde."
// Bunu dedigimiz an; save(), delete(), findAll() gibi tum standart metodlari bedavadan kazaniriz.
public interface CezaRepository extends JpaRepository<Ceza, Integer> { // FineRepository -> CezaRepository

    // ICI BOS - Su an icin hata yok.
    // Neden bos? Cunku standart islemler (Kaydet, Sil, Getir) bize yetiyor.

    // EKSTRA (Istersen bunu ekleyebilirsin, sunumda havali durur):
    // "Sadece Odenmemis (Aktif) Cezalari Getir"
    // Entity icindeki degisken adi 'odendiMi' (boolean) oldugu icin:
    List<Ceza> findByOdendiMiFalse();
    // SQL Karsiligi: SELECT * FROM Cezalar WHERE odendi_mi = 0
}