package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Yazar;
import com.example.KutupahaneOtomasyonu.service.YazarServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// @RestController: Bu sinifin web isteklerini (HTTP) karsilayan bir kontrolcu oldugunu,
// ve geriye HTML sayfasi degil, JSON verisi (data) dondurecegini belirtir.
@RestController
// @RequestMapping: Bu siniftaki tum islemlerin "/api/yazarlar" adresi altinda oldugunu soyler.
// Yani tarayiciya "localhost:8080/api/yazarlar" yazildiginda burasi dinler.
@RequestMapping("/api/yazarlar")
public class YazarController { // AuthorController -> YazarController

    // Isleri yaptiracagimiz "Usta"yi (Servisi) tanimliyoruz.
    private final YazarServisi yazarServisi;

    // Constructor Injection: Spring'e "Bana calisan bir YazarServisi ver" diyoruz.
    @Autowired
    public YazarController(YazarServisi yazarServisi) {
        this.yazarServisi = yazarServisi;
    }

    // --- YAZARLARI LISTELE ---
    // @GetMapping: Eger kullanici bu adrese "GET" (Veri Okuma) istegi atarsa burasi calisir.
    // Orn: Tarayicidan siteye girmek bir GET istegidir.
    @GetMapping
    public List<Yazar> tumYazarlariGetir() {
        // Servise emrediyoruz: "Git veritabanindaki butun yazarlari bul getir."
        return yazarServisi.tumunuGetir();
    }

    // --- YAZAR EKLE ---
    // @PostMapping: Eger kullanici bu adrese "POST" (Veri Gonderme/Kaydetme) istegi atarsa burasi calisir.
    // @RequestBody: Gelen istegin icindeki veriyi (JSON formatindaki Yazar adi, soyadi vs.)
    // alir ve Java'daki 'Yazar' nesnesine donusturur.
    @PostMapping
    public Yazar yazarEkle(@RequestBody Yazar yazar) {
        // Servise emrediyoruz: "Elimdeki bu yazar bilgisini veritabanina kaydet."
        return yazarServisi.kaydet(yazar);
    }
}