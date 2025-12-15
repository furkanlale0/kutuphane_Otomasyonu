package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Yonetici;
import com.example.KutupahaneOtomasyonu.entity.Uye;
import com.example.KutupahaneOtomasyonu.service.YoneticiServisi;
import com.example.KutupahaneOtomasyonu.service.JwtServisi;
import com.example.KutupahaneOtomasyonu.service.UyeServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class GirisIslemleriController {

    private final YoneticiServisi yoneticiServisi;
    private final UyeServisi uyeServisi;
    private final PasswordEncoder sifreleyici;
    private final JwtServisi jwtServisi;

    @Autowired
    public GirisIslemleriController(YoneticiServisi yoneticiServisi, UyeServisi uyeServisi, PasswordEncoder sifreleyici, JwtServisi jwtServisi) {
        this.yoneticiServisi = yoneticiServisi;
        this.uyeServisi = uyeServisi;
        this.sifreleyici = sifreleyici;
        this.jwtServisi = jwtServisi;
    }

    // --- KAYIT OLMA (Frontend: /api/auth/kayit) ---
    // DUZELTME 1: Endpoint adini "register" yerine "kayit" yaptik ki JS ile uyussun.
    @PostMapping("/kayit")
    public ResponseEntity<?> kayitOl(@RequestBody Uye uye) {
        String sonuc = uyeServisi.uyeKaydet(uye);

        if ("Islem Basarili".equals(sonuc)) {
            return ResponseEntity.ok("Kayit basarili. Giris yapabilirsiniz.");
        }
        return ResponseEntity.badRequest().body(sonuc);
    }

    // --- GIRIS YAPMA (Frontend: /api/auth/giris) ---
    // DUZELTME 2: Endpoint adini "login" yerine "giris" yaptik.
    @PostMapping("/giris")
    public ResponseEntity<?> girisYap(@RequestBody GirisIstegi istek) {

        // Frontend'den gelen veri: { "girilenBilgi": "admin" veya "ali@gmail.com", "sifre": "1234" }
        String girilenBilgi = istek.getGirilenBilgi();
        String sifre = istek.getSifre();

        // 1. ADIM: YONETICI TABLOSUNA BAK (Kullanici Adi ile)
        // Yoneticiler hala "admin", "memur1" gibi kullanici adiyla girer.
        Optional<Yonetici> yoneticiKutusu = yoneticiServisi.kullaniciAdiIleGetir(girilenBilgi);

        if (yoneticiKutusu.isPresent()) {
            Yonetici yonetici = yoneticiKutusu.get();
            if (sifreleyici.matches(sifre, yonetici.getSifre())) {

                UserDetails yoneticiDetay = User.builder()
                        .username(yonetici.getKullaniciAdi())
                        .password(yonetici.getSifre())
                        .roles(yonetici.getRol().name())
                        .build();

                return tokenCevabiOlustur(yoneticiDetay, yonetici.getRol().name(), yonetici.getYoneticiId());
            }
        }

        // 2. ADIM: YONETICI DEGILSE, UYE TABLOSUNA BAK (Email ile)
        // DUZELTME 3: Uyeler "Kullanici Adi" ile degil "EMAIL" ile aranir.
        // Onceki kodda burada hata vardi (kullaniciAdiIleGetir cagiriliyordu).
        Optional<Uye> uyeKutusu = uyeServisi.emailIleGetir(girilenBilgi);

        if (uyeKutusu.isPresent()) {
            Uye uye = uyeKutusu.get();
            if (sifreleyici.matches(sifre, uye.getSifre())) {

                UserDetails uyeDetay = User.builder()
                        .username(uye.getEmail()) // Username yerine Email koyuyoruz
                        .password(uye.getSifre())
                        .roles("UYE")
                        .build();

                return tokenCevabiOlustur(uyeDetay, "UYE", uye.getUyeId());
            }
        }

        // 3. ADIM: HICBIRI DEGILSE
        return ResponseEntity.status(401).body("Giris basarisiz: Bilgiler hatali.");
    }

    private ResponseEntity<?> tokenCevabiOlustur(UserDetails kullanici, String rol, Integer id) {
        String token = jwtServisi.tokenUret(kullanici);

        Map<String, String> cevap = new HashMap<>();
        cevap.put("token", token);
        cevap.put("rol", rol);
        cevap.put("id", id.toString());

        return ResponseEntity.ok(cevap);
    }
}

// --- VERI TASIYICI (DTO) ---
// Frontend'den gelen JSON artik soyle: { "girilenBilgi": "...", "sifre": "..." }
// Cunku girilen sey Kullanici Adi da olabilir, Email de olabilir.
class GirisIstegi {
    private String girilenBilgi; // Eski adi: kullaniciAdi
    private String sifre;

    public String getGirilenBilgi() { return girilenBilgi; }
    public void setGirilenBilgi(String girilenBilgi) { this.girilenBilgi = girilenBilgi; }
    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }
}