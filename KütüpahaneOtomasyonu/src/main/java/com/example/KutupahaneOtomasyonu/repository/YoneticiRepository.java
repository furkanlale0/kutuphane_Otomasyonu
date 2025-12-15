package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Yonetici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository: Spring'e diyoruz ki: "Bu dosya veritabani ile konusan bir Depo (Repository) gorevlisidir."
// Veritabani islemlerini ve olasi hatalari yonetir.
@Repository
// interface: Burasi bir sinif degil, bir arayuzdur. Yani metodlarin icini biz doldurmayiz.
// extends JpaRepository<Yonetici, Integer>:
// Burasi sihirli kisimdir. JpaRepository'yi miras aldigimiz icin;
// "Kaydet (save)", "Sil (delete)", "Hepsini Getir (findAll)", "ID ile Bul (findById)" gibi
// yuzlerce satirlik temel kodlari yazmamiza gerek kalmaz. Spring bunlari bize hazir verir.
// <Yonetici, Integer> -> Bu depo "Yonetici" tablosunu yonetir ve o tablonun ID'si "Integer" turundedir.
public interface YoneticiRepository extends JpaRepository<Yonetici, Integer> { // AdminRepository -> YoneticiRepository

    // Spring Data JPA'nin "Sihirli Metod Isimlendirme" yetenegi:
    // Biz sadece "findByKullaniciAdi" yazariz.
    // Spring bunu okur, Yonetici sinifindaki "kullaniciAdi" degiskenine bakar ve
    // arka planda su SQL'i yazar: "SELECT * FROM Yoneticiler WHERE kullanici_adi = ?"

    // NOT: Metod ismi Entity icindeki degisken adiyla (kullaniciAdi) AYNI OLMAK ZORUNDADIR.
    // Eger "findByUsername" yazarsan, Spring "kullaniciAdi" degiskenini bulamayip hata verir.

    // Optional<Yonetici>: Bu metodun sonucu bos donebilir (Boyle bir kullanici olmayabilir).
    // Java'da "NullPointerException" hatasi almamak icin, sonucu "Optional" (Istege Bagli) kutusunda doneriz.
    Optional<Yonetici> findByKullaniciAdi(String kullaniciAdi);
}