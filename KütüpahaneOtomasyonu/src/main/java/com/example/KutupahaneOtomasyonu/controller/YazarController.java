package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Yazar;
import com.example.KutupahaneOtomasyonu.service.YazarServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/*
 * BU SINIF NE İŞE YARAR?
 * Yazar işlemlerini yöneten REST API kontrolcüsüdür.
 * Frontend'den gelen yazar listeleme ve yeni yazar ekleme isteklerini karşılar.
 */
@RestController
@RequestMapping("/api/yazarlar")
public class YazarController {

    private final YazarServisi yazarServisi;

    @Autowired
    public YazarController(YazarServisi yazarServisi) {
        this.yazarServisi = yazarServisi;
    }

    /*
     * TÜM YAZARLARI GETİRME METODU
     * Veritabanında kayıtlı olan bütün yazarların listesini döner.
     * Kitap ekleme sayfasında yazar seçimi için kullanılır.
     */
    @GetMapping
    public List<Yazar> tumYazarlariGetir() {
        return yazarServisi.tumunuGetir();
    }

    /*
     * YENİ YAZAR EKLEME METODU
     * Frontend'den gelen yazar bilgilerini (Ad, Soyad vb.) alır
     * ve veritabanına kaydetmesi için servise iletir.
     */
    @PostMapping
    public Yazar yazarEkle(@RequestBody Yazar yazar) {
        return yazarServisi.kaydet(yazar);
    }
}