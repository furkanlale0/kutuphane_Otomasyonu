package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository: Spring'e diyoruz ki: "Bu dosya Cezalar (fines) tablosunu yöneten depodur."
@Repository
// JpaRepository<Fine, Integer>:
// "Ben Fine (Ceza) tablosunu yönetiyorum ve bu tablonun ID'si Integer türünde."
// Bunu dediğimiz an; save(), delete(), findAll() gibi tüm standart metodları bedavadan kazanırız.
public interface FineRepository extends JpaRepository<Fine, Integer> {

    // İÇİ BOŞ - Hata yok.
    // Neden boş? Çünkü şu an için "Cezaları adına göre bul" gibi özel bir fantezimiz yok.
    // Standart işlemler (Kaydet, Sil, Getir) bize yetiyor.
    // İleride "Ödenmemiş cezaları getir" dersek buraya bir satır kod ekleriz.
}