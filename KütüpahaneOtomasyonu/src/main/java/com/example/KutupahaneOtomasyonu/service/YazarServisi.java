package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Yazar;
import com.example.KutupahaneOtomasyonu.repository.YazarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/*
 * BU SINIF NE İŞE YARAR?
 * Yazar işlemleri için İş Mantığı (Business Logic) katmanıdır.
 * Controller ile Repository arasında köprü görevi görür.
 * Özellikle Kitap Ekleme işlemi sırasında, yazar kontrolü ve yönetimi için
 * KitapServisi tarafından sıkça kullanılır.
 */
@Service
public class YazarServisi {

    private final YazarRepository yazarRepository;

    @Autowired
    public YazarServisi(YazarRepository yazarRepository) {
        this.yazarRepository = yazarRepository;
    }

    /*
     * TÜM YAZARLARI LİSTELEME
     * Kitap ekleme formunda veya yazar listesi sayfasında
     * mevcut yazarları göstermek için kullanılır.
     */
    public List<Yazar> tumunuGetir() {
        return yazarRepository.findAll();
    }

    /*
     * YENİ YAZAR KAYDETME
     * Veritabanına yeni bir yazar ekler.
     * Genellikle KitapServisi içinden, eğer kitap yeni bir yazara aitse çağrılır.
     */
    public Yazar kaydet(Yazar yazar) {
        return yazarRepository.save(yazar);
    }

    /*
     * İSİM VE SOYİSİM İLE ARAMA
     * Mükerrer kayıt kontrolü için kritik öneme sahiptir.
     * Kitap eklenirken, girilen yazar isminin sistemde olup olmadığını
     * bu metod sayesinde kontrol ederiz.
     */
    public Optional<Yazar> adSoyadIleBul(String ad, String soyad) {
        return yazarRepository.findByAdAndSoyad(ad, soyad);
    }
}