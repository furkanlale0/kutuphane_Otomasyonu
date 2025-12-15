package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.service.UyeServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// @RestController: Bu sinifin bir Web API oldugunu ve geriye veri (JSON) dondurecegini belirtir.
// @RequestMapping: "localhost:8080/api/uyeler" adresine gelen istekleri bu sinif dinler.
@RestController
@RequestMapping("/api/uyeler")
public class UyeController { // MemberController -> UyeController

    // --- MIMARI DUZELTME ---
    // Artik veritabaniyla dogrudan konusmuyoruz (Repository yok).
    // Sadece is kurallarini bilen Servis (Service) ile iletisim kuruyoruz.
    private final UyeServisi uyeServisi;

    // Constructor Injection: Spring'e "Calisan bir UyeServisi örnegini buraya bagla" diyoruz.
    @Autowired
    public UyeController(UyeServisi uyeServisi) {
        this.uyeServisi = uyeServisi;
    }

    // --- PROFIL VE CEZA DETAYLARINI GETIRME ---
    // URL Ornegi: /api/uyeler/10/profil (ID'si 10 olan uye)
    // @GetMapping: Veri okuma istegi.
    // @PathVariable: URL'deki {id} kismini (orn: 10) alip 'Integer id' degiskenine atar.
    @GetMapping("/{id}/profil")
    public Map<String, Object> profilGetir(@PathVariable Integer id) {
        // Controller olarak biz hesap kitap (ceza hesaplama, borc toplama) yapmayiz.
        // Sadece Servis'e "Bu ID'li uyenin profilini hazirla" deriz.
        // Servis bize icinde Isim, E-posta, Toplam Borc ve Ceza Detaylari olan hazir bir paket (Map) doner.
        // Map yapisi su sekilde doner: { "adSoyad": "Ali Veli", "borc": 50.0, "kitaplar": [...] }
        return uyeServisi.uyeProfiliniGetir(id);
    }
}