package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.service.OduncServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/odunc")
public class OduncController {

    private final OduncServisi oduncServisi;

    @Autowired
    public OduncController(OduncServisi oduncServisi) {
        this.oduncServisi = oduncServisi;
    }

    // --- 1. KITAP ODUNC AL ---
    @PostMapping("/al")
    public ResponseEntity<?> kitapAl(@RequestBody OduncIstegi istek) {
        String sonuc = oduncServisi.kitapOduncVer(istek.getUyeId(), istek.getKitapId());
        if ("Islem Basarili".equals(sonuc)) {
            return ResponseEntity.ok("Kitap başarıyla ödünç alındı.");
        }
        return ResponseEntity.badRequest().body(sonuc);
    }

    // --- 2. KITAP IADE ET ---
    @PostMapping("/iade")
    public ResponseEntity<?> kitapIade(@RequestBody OduncIstegi istek) {
        String sonuc = oduncServisi.kitapIadeAl(istek.getUyeId(), istek.getKitapId());
        if ("Islem Basarili".equals(sonuc)) {
            return ResponseEntity.ok("Kitap başarıyla iade edildi.");
        }
        return ResponseEntity.badRequest().body(sonuc);
    }

    // --- 3. UYENIN AKTIF ODUNCLERI (Yan Menü İçin) ---
    @GetMapping("/uye/{uyeId}/aktif")
    public List<Map<String, Object>> aktifOduncleriGetir(@PathVariable Integer uyeId) {
        return oduncServisi.aktifOduncleriGetir(uyeId);
    }

    // --- 4. UYENIN GECMISI (Modal İçin) ---
    @GetMapping("/uye/{uyeId}/gecmis")
    public List<Map<String, Object>> gecmisiGetir(@PathVariable Integer uyeId) {
        return oduncServisi.uyeGecmisiniGetir(uyeId);
    }

    // --- 5. TUM CEZALAR (Admin İçin) ---
    @GetMapping("/admin/cezalar")
    public List<Map<String, Object>> tumCezalar() {
        return oduncServisi.tumCezalariHesapla();
    }

    // --- 6. CEZA ODEME (Tahsilat) ---
    @PostMapping("/ceza-ode/{oduncId}")
    public ResponseEntity<?> cezaOde(@PathVariable Integer oduncId) {
        boolean sonuc = oduncServisi.cezaOde(oduncId);
        if (sonuc) return ResponseEntity.ok("Ceza ödendi olarak işaretlendi.");
        return ResponseEntity.badRequest().body("İşlem başarısız.");
    }
}

// Frontend'den gelen { "uyeId": 1, "kitapId": 5 } verisini tutan kutu
class OduncIstegi {
    private Integer uyeId;
    private Integer kitapId;

    public Integer getUyeId() { return uyeId; }
    public void setUyeId(Integer uyeId) { this.uyeId = uyeId; }
    public Integer getKitapId() { return kitapId; }
    public void setKitapId(Integer kitapId) { this.kitapId = kitapId; }
}