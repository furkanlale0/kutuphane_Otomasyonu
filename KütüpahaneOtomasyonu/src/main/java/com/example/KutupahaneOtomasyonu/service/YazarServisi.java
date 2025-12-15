package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Yazar;
import com.example.KutupahaneOtomasyonu.repository.YazarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

// @Service: Spring'e "Burası iş mantığı katmanıdır" diyoruz.
// Controller direkt Repository ile konuşmaz, önce buraya (Müdür'e) gelir.
@Service
public class YazarServisi { // AuthorService -> YazarServisi

    private final YazarRepository yazarRepository;

    @Autowired
    public YazarServisi(YazarRepository yazarRepository) {
        this.yazarRepository = yazarRepository;
    }

    // Tüm yazarları listele
    public List<Yazar> tumunuGetir() {
        return yazarRepository.findAll();
    }

    // Yeni yazar kaydet
    public Yazar kaydet(Yazar yazar) {
        // İLERİ SEVİYE NOT:
        // Buraya "Eğer aynı isimde yazar varsa kaydetme, hata fırlat" gibi if-else kodları yazılabilir.
        // Şimdilik direkt kaydediyoruz.
        return yazarRepository.save(yazar);
    }

    // Repository'e eklediğimiz özel arama metodunu burada dış dünyaya açıyoruz.
    // Kitap eklerken "Bu yazar sistemde var mı?" diye sormak için kullanacağız.
    public Optional<Yazar> adSoyadIleBul(String ad, String soyad) {
        return yazarRepository.findByAdAndSoyad(ad, soyad);
    }
}