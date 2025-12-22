package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Kitap;
import com.example.KutupahaneOtomasyonu.repository.KitapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kitaplar")
public class KitapController {

    @Autowired
    private KitapRepository kitapRepository;

    // 1. KITAPLARI LISTELE
    @GetMapping
    public List<Kitap> tumKitaplar() {
        return kitapRepository.findAll();
    }

    // 2. KITAP EKLE
    @PostMapping
    public Kitap kitapEkle(@RequestBody Kitap kitap) {
        return kitapRepository.save(kitap);
    }

    // 3. KITAP SIL
    @DeleteMapping("/{id}")
    public ResponseEntity<?> kitapSil(@PathVariable Integer id) {
        try {
            if (!kitapRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("Kitap bulunamadı.");
            }
            kitapRepository.deleteById(id);
            return ResponseEntity.ok("Kitap silindi.");
        } catch (Exception e) {
            // Eger kitap odunc verilmisse veritabani silmeye izin vermeyebilir (Foreign Key hatasi)
            return ResponseEntity.badRequest().body("Bu kitap şu an kullanımda veya geçmiş kaydı olduğu için silinemiyor.");
        }
    }
}