package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Kitap;
import com.example.KutupahaneOtomasyonu.service.KitapServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kitaplar")
public class KitapController {

    private final KitapServisi kitapServisi;

    @Autowired
    public KitapController(KitapServisi kitapServisi) {
        this.kitapServisi = kitapServisi;
    }

    // --- 1. TUM KITAPLARI LISTELE ---
    @GetMapping
    public List<Kitap> tumunuGetir() {
        // DUZELTME: Metot ismi 'tumunuGetir' olarak guncellendi
        return kitapServisi.tumunuGetir();
    }

    // --- 2. YENI KITAP EKLE ---
    @PostMapping
    public ResponseEntity<?> kaydet(@RequestBody Kitap kitap) {
        try {
            // DUZELTME: Metot ismi 'kaydet' olarak guncellendi
            Kitap kaydedilenKitap = kitapServisi.kaydet(kitap);
            return ResponseEntity.ok(kaydedilenKitap);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }

    // --- 3. ID ILE KITAP GETIR ---
    @GetMapping("/{id}")
    public ResponseEntity<Kitap> idIleGetir(@PathVariable Integer id) {
        return kitapServisi.idIleGetir(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- 4. KITAP SIL ---
    @DeleteMapping("/{id}")
    public ResponseEntity<String> sil(@PathVariable Integer id) {
        try {
            kitapServisi.sil(id);
            return ResponseEntity.ok("Kitap basariyla silindi.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 5. ISME GORE ARA ---
    @GetMapping("/ara")
    public List<Kitap> ara(@RequestParam String isim) {
        return kitapServisi.ismeGoreAra(isim);
    }
}