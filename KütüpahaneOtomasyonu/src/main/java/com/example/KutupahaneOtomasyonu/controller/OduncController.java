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
    public OduncController(OduncServisi oduncServisi) { this.oduncServisi = oduncServisi; }

    @PostMapping("/al")
    public ResponseEntity<?> al(@RequestBody OduncIstegi i) {
        String s = oduncServisi.kitapOduncVer(i.getUyeId(), i.getKitapId());
        return s.equals("Islem Basarili") ? ResponseEntity.ok("Tamam") : ResponseEntity.badRequest().body(s);
    }

    @PostMapping("/iade")
    public ResponseEntity<?> iade(@RequestBody OduncIstegi i) {
        String s = oduncServisi.kitapIadeAl(i.getUyeId(), i.getKitapId());
        return s.equals("Islem Basarili") ? ResponseEntity.ok("Tamam") : ResponseEntity.badRequest().body(s);
    }

    @GetMapping("/uye/{id}/aktif")
    public List<Map<String, Object>> aktif(@PathVariable Integer id) { return oduncServisi.aktifOduncleriGetir(id); }

    @GetMapping("/uye/{id}/gecmis")
    public List<Map<String, Object>> gecmis(@PathVariable Integer id) { return oduncServisi.uyeGecmisiniGetir(id); }

    // --- YENİ EKLENENLER ---

    // 1. Admin Cezalar Sayfası
    @GetMapping("/admin/cezalar")
    public List<Map<String, Object>> adminCezalar() { return oduncServisi.tumCezalariGetir(); }

    // 2. Üye Ceza Detayları (Profil)
    @GetMapping("/uye/{id}/ceza-detay")
    public List<Map<String, Object>> uyeCezalar(@PathVariable Integer id) { return oduncServisi.uyeCezaDetaylari(id); }

    // 3. Üye "Ödedim" Bildirimi
    @PostMapping("/ceza-bildir/{uyeId}")
    public ResponseEntity<?> cezaBildir(@PathVariable Integer uyeId) {
        boolean sonuc = oduncServisi.odemeBildirimiYap(uyeId);
        return sonuc ? ResponseEntity.ok("Bildirim gönderildi") : ResponseEntity.badRequest().body("Ödenecek ceza yok.");
    }

    // 4. Admin "Onayla" İşlemi
    @PostMapping("/ceza-onayla/{oduncId}")
    public ResponseEntity<?> cezaOnayla(@PathVariable Integer oduncId) {
        boolean sonuc = oduncServisi.odemeyiOnayla(oduncId);
        return sonuc ? ResponseEntity.ok("Onaylandı") : ResponseEntity.badRequest().body("Hata");
    }
}
class OduncIstegi {
    private Integer uyeId; private Integer kitapId;
    public Integer getUyeId() { return uyeId; } public void setUyeId(Integer uyeId) { this.uyeId = uyeId; }
    public Integer getKitapId() { return kitapId; } public void setKitapId(Integer kitapId) { this.kitapId = kitapId; }
}