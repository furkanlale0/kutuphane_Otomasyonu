package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.service.UyeServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
 * BU SINIF NE İŞE YARAR?
 * Üye işlemlerini yöneten API kontrolcüsüdür.
 * Özellikle üyenin profil sayfasında göreceği kişisel bilgileri,
 * toplam borcunu ve ceza detaylarını getirmek için kullanılır.
 */
@RestController
@RequestMapping("/api/uyeler")
public class UyeController {

    private final UyeServisi uyeServisi;

    @Autowired
    public UyeController(UyeServisi uyeServisi) {
        this.uyeServisi = uyeServisi;
    }

    /*
     * ÜYE PROFİLİ GETİRME METODU
     * Verilen üye ID'sine göre;
     * 1. Kullanıcının Adı, Soyadı, E-postası
     * 2. Varsa toplam gecikme borcu
     * 3. Ceza detaylarını
     * Servis katmanından hazır bir paket (Map) olarak ister ve Frontend'e döner.
     */
    @GetMapping("/{id}/profil")
    public Map<String, Object> profilGetir(@PathVariable Integer id) {
        return uyeServisi.uyeProfiliniGetir(id);
    }
}