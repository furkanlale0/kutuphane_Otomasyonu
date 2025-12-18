package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Yonetici;
import com.example.KutupahaneOtomasyonu.repository.YoneticiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/*
 * BU SINIF NE İŞE YARAR?
 * Yönetici (Admin) işlemleri için İş Mantığı (Business Logic) katmanıdır.
 * Veritabanı ile güvenlik katmanı arasında köprü görevi görür.
 * Özellikle giriş (Login) işlemleri sırasında, yöneticinin varlığını doğrulamak için kullanılır.
 */
@Service
public class YoneticiServisi {

    private final YoneticiRepository yoneticiRepository;

    @Autowired
    public YoneticiServisi(YoneticiRepository yoneticiRepository) {
        this.yoneticiRepository = yoneticiRepository;
    }

    /*
     * KULLANICI ADI İLE YÖNETİCİ BULMA
     * Veritabanında belirtilen kullanıcı adına sahip bir yöneticiyi arar.
     * Genellikle 'KullaniciDetayServisi' tarafından, sisteme girmeye çalışan kişinin
     * bilgilerini doğrulamak amacıyla çağrılır.
     */
    public Optional<Yonetici> kullaniciAdiIleGetir(String kullaniciAdi) {
        return yoneticiRepository.findByKullaniciAdi(kullaniciAdi);
    }
}