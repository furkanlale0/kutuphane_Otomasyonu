package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.service.OduncServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/*
 * BU SINIF NE İŞE YARAR?
 * Kütüphane işlemlerini (Ödünç, İade, Ceza) yöneten kontrolcüdür.
 * Frontend'den gelen "Kitap Al", "İade Et", "Borç Öde" gibi istekleri karşılar
 * ve gerekli mantığı çalıştırması için Servis katmanına iletir.
 */
@RestController
@RequestMapping("/api/odunc")
public class OduncController {

    private final OduncServisi oduncServisi;

    @Autowired
    public OduncController(OduncServisi oduncServisi) {
        this.oduncServisi = oduncServisi;
    }

    /*
     * KİTAP ÖDÜNÇ ALMA İŞLEMİ
     * Kullanıcının seçtiği kitabı alıp alamayacağını (Stok, Borç durumu vb.) kontrol eder.
     * Uygunsa kitabı kullanıcıya zimmetler.
     */
    @PostMapping("/al")
    public ResponseEntity<?> al(@RequestBody OduncIstegi i) {
        String s = oduncServisi.kitapOduncVer(i.getUyeId(), i.getKitapId());
        return s.equals("Islem Basarili") ? ResponseEntity.ok("Tamam") : ResponseEntity.badRequest().body(s);
    }

    /*
     * KİTAP İADE ETME İŞLEMİ
     * Kitabı kullanıcıdan geri alır.
     * Eğer teslim tarihi geçmişse otomatik olarak ceza hesaplar ve sisteme işler.
     */
    @PostMapping("/iade")
    public ResponseEntity<?> iade(@RequestBody OduncIstegi i) {
        String s = oduncServisi.kitapIadeAl(i.getUyeId(), i.getKitapId());
        return s.equals("Islem Basarili") ? ResponseEntity.ok("Tamam") : ResponseEntity.badRequest().body(s);
    }

    /*
     * KULLANICININ ELİNDEKİ KİTAPLAR
     * Bir üyenin şu an okumakta olduğu (henüz iade etmediği) kitapları listeler.
     */
    @GetMapping("/uye/{id}/aktif")
    public List<Map<String, Object>> aktif(@PathVariable Integer id) {
        return oduncServisi.aktifOduncleriGetir(id);
    }

    /*
     * KULLANICININ İŞLEM GEÇMİŞİ
     * Üyenin daha önce alıp iade ettiği tüm kitapların listesini getirir.
     */
    @GetMapping("/uye/{id}/gecmis")
    public List<Map<String, Object>> gecmis(@PathVariable Integer id) {
        return oduncServisi.uyeGecmisiniGetir(id);
    }

    // --- CEZA YÖNETİMİ ---

    /*
     * ADMIN: TÜM CEZALAR
     * Yönetici panelinde görünen, tüm üyelerin ödenmemiş veya onay bekleyen
     * cezalarını listeleyen metoddur.
     */
    @GetMapping("/admin/cezalar")
    public List<Map<String, Object>> adminCezalar() {
        return oduncServisi.tumCezalariGetir();
    }

    /*
     * ÜYE: CEZA DETAYLARI
     * Üyenin profil sayfasında, hangi kitaptan ne kadar ceza yediğini
     * detaylı tablo olarak gösterir.
     */
    @GetMapping("/uye/{id}/ceza-detay")
    public List<Map<String, Object>> uyeCezalar(@PathVariable Integer id) {
        return oduncServisi.uyeCezaDetaylari(id);
    }

    /*
     * ÜYE: ÖDEME BİLDİRİMİ
     * Üye borcunu IBAN'a attıktan sonra "Ödedim" butonuna basınca çalışır.
     * Borcu silmez, sadece durumunu "Onay Bekliyor" yapar.
     */
    @PostMapping("/ceza-bildir/{uyeId}")
    public ResponseEntity<?> cezaBildir(@PathVariable Integer uyeId) {
        boolean sonuc = oduncServisi.odemeBildirimiYap(uyeId);
        return sonuc ? ResponseEntity.ok("Bildirim gönderildi") : ResponseEntity.badRequest().body("Ödenecek ceza yok.");
    }

    /*
     * ADMIN: ÖDEME ONAYI
     * Yönetici, paranın hesaba geldiğini görünce "Onayla" butonuna basar.
     * Bu metod çalışır ve borcu sistemden kalıcı olarak siler (Durumu 'ODENDI' yapar).
     */
    @PostMapping("/ceza-onayla/{oduncId}")
    public ResponseEntity<?> cezaOnayla(@PathVariable Integer oduncId) {
        boolean sonuc = oduncServisi.odemeyiOnayla(oduncId);
        return sonuc ? ResponseEntity.ok("Onaylandı") : ResponseEntity.badRequest().body("Hata");
    }
}

/*
 * DTO (Data Transfer Object)
 * Frontend'den gelen "Hangi Üye? Hangi Kitap?" bilgisini taşıyan basit kutudur.
 */
class OduncIstegi {
    private Integer uyeId;
    private Integer kitapId;

    public Integer getUyeId() { return uyeId; }
    public void setUyeId(Integer uyeId) { this.uyeId = uyeId; }
    public Integer getKitapId() { return kitapId; }
    public void setKitapId(Integer kitapId) { this.kitapId = kitapId; }
}