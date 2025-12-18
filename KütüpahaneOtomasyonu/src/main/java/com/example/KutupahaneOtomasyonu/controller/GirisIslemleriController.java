package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.service.JwtServisi;
import com.example.KutupahaneOtomasyonu.service.KullaniciDetayServisi;
import com.example.KutupahaneOtomasyonu.entity.Yonetici;
import com.example.KutupahaneOtomasyonu.entity.Uye;
import com.example.KutupahaneOtomasyonu.repository.YoneticiRepository;
import com.example.KutupahaneOtomasyonu.repository.UyeRepository;
import com.example.KutupahaneOtomasyonu.entity.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class GirisIslemleriController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private KullaniciDetayServisi kullaniciDetayServisi;
    @Autowired private JwtServisi jwtServisi;
    @Autowired private YoneticiRepository yoneticiRepository;
    @Autowired private UyeRepository uyeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- 1. GIRIS YAPMA (LOGIN) ---
    @PostMapping("/giris")
    public ResponseEntity<?> girisYap(@RequestBody GirisIstegi istek) {
        try {
            // 1. Kullanici adi ve sifreyi kontrol et
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(istek.getGirilenBilgi(), istek.getSifre())
            );

            // 2. Kullaniciyi bul (Isim bilgisini almak icin)
            UserDetails userDetails = kullaniciDetayServisi.loadUserByUsername(istek.getGirilenBilgi());
            String token = jwtServisi.tokenUret(userDetails);

            // 3. Rol ve Isim Bilgisini Bul
            String rol = "UYE";
            Integer id = 0;
            String adSoyad = "Kullanıcı"; // Varsayılan

            Optional<Yonetici> yonetici = yoneticiRepository.findByKullaniciAdi(istek.getGirilenBilgi());
            if (yonetici.isPresent()) {
                rol = "ADMIN";
                id = yonetici.get().getYoneticiId();
                adSoyad = yonetici.get().getAd() + " " + yonetici.get().getSoyad();
            } else {
                Optional<Uye> uye = uyeRepository.findByEmail(istek.getGirilenBilgi());
                if (uye.isPresent()) {
                    rol = "UYE";
                    id = uye.get().getUyeId();
                    adSoyad = uye.get().getAd() + " " + uye.get().getSoyad();
                }
            }

            // 4. Cevabi gonder (Token + Rol + ID + AD SOYAD)
            return ResponseEntity.ok(new GirisCevabi(token, rol, id, adSoyad));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Giriş başarısız: " + e.getMessage());
        }
    }

    // --- 2. KAYIT OLMA (REGISTER) ---
    @PostMapping("/kayit")
    public ResponseEntity<?> kayitOl(@RequestBody UyeKayitIstegi istek) {
        if (uyeRepository.findByEmail(istek.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Bu email zaten kayıtlı!");
        }
        Uye yeniUye = new Uye();
        yeniUye.setAd(istek.getAd());
        yeniUye.setSoyad(istek.getSoyad());
        yeniUye.setEmail(istek.getEmail());
        yeniUye.setSifre(passwordEncoder.encode(istek.getSifre()));
        yeniUye.setKayitTarihi(LocalDateTime.now());
        uyeRepository.save(yeniUye);
        return ResponseEntity.ok("Kayıt başarılı");
    }
}

// --- YARDIMCI SINIFLAR (Dosyanin altina ekli kalsinlar) ---

class GirisIstegi {
    private String girilenBilgi;
    private String sifre;
    public String getGirilenBilgi() { return girilenBilgi; }
    public void setGirilenBilgi(String girilenBilgi) { this.girilenBilgi = girilenBilgi; }
    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }
}

class GirisCevabi { // AuthenticationResponse yerine bunu kullaniyoruz
    private String token;
    private String rol;
    private Integer id;
    private String adSoyad; // Yeni ekledigimiz alan

    public GirisCevabi(String token, String rol, Integer id, String adSoyad) {
        this.token = token;
        this.rol = rol;
        this.id = id;
        this.adSoyad = adSoyad;
    }

    public String getToken() { return token; }
    public String getRol() { return rol; }
    public Integer getId() { return id; }
    public String getAdSoyad() { return adSoyad; }
}

class UyeKayitIstegi {
    private String ad;
    private String soyad;
    private String email;
    private String sifre;
    // Getter-Setter
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }
    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }
}