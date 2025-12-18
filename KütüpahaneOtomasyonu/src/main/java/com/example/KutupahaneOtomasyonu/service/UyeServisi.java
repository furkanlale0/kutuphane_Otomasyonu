package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.OduncIslemi;
import com.example.KutupahaneOtomasyonu.entity.Uye;
import com.example.KutupahaneOtomasyonu.repository.OduncRepository;
import com.example.KutupahaneOtomasyonu.repository.UyeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class UyeServisi { // MemberService -> UyeServisi

    private final UyeRepository uyeRepository;
    private final OduncRepository oduncRepository;

    // Sifreleri veritabanina "1234" diye acik kaydetmemek icin sifreleyici.
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UyeServisi(UyeRepository uyeRepository, OduncRepository oduncRepository, PasswordEncoder passwordEncoder) {
        this.uyeRepository = uyeRepository;
        this.oduncRepository = oduncRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 1. YENI UYE KAYDI ---
    public String uyeKaydet(Uye uye) {
        // MANTIK DEGISIKLIGI: Kullanici adi yok, E-posta kontrolu yapiyoruz.
        // Repository'ye ekledigimiz "existsByEmail" metodunu kullaniyoruz.
        if (uyeRepository.existsByEmail(uye.getEmail())) {
            return "Bu e-posta adresi zaten kayitli.";
        }

        // Sifreyi sifrele (Hash'le). "1234" girerse veritabanina "$2a$10$..." olarak kaydolur.
        uye.setSifre(passwordEncoder.encode(uye.getSifre()));

        // Kayit tarihini su an olarak ayarla.
        uye.setKayitTarihi(LocalDateTime.now());

        uyeRepository.save(uye);
        return "Islem Basarili";
    }

    // --- 2. KULLANICI BULMA (Login Icin) ---
    // Giris yaparken kullanici adini degil, E-postayi kullaniyoruz.
    public Optional<Uye> emailIleGetir(String email) {
        return uyeRepository.findByEmail(email);
    }

    // --- 3. PROFIL VE CEZA HESAPLAMA (Dinamik Hesaplama) ---
    // Uyenin profil sayfasini actigi anda calisir.
    // Veritabanindaki "Cezalar" tablosuna bakmaz, o anki gecikmeleri canli hesaplar.
    public Map<String, Object> uyeProfiliniGetir(Integer uyeId) {
        Uye uye = uyeRepository.findById(uyeId)
                .orElseThrow(() -> new RuntimeException("Uye bulunamadi"));

        // Uyenin butun odunc gecmisini cek.
        List<OduncIslemi> gecmis = oduncRepository.findByUye_UyeId(uyeId);

        double toplamBorc = 0;
        List<Map<String, Object>> cezaDetaylari = new ArrayList<>();

        for (OduncIslemi islem : gecmis) {
            // Eger cezasi zaten odenmisse hesaplamaya katma, pas gec.
            if (Boolean.TRUE.equals(islem.isCezaOdendiMi())) continue;

            long gecikenGun = 0;

            // Hesaplama Tarihi: Iade ettiyse "Iade Tarihi", etmediyse "Bugun".
            LocalDateTime hesaplamaTarihi = (islem.getIadeTarihi() != null) ? islem.getIadeTarihi() : LocalDateTime.now();

            // Gun sinirini gecti mi? (Son Teslim Tarihinden sonra mi?)
            if (hesaplamaTarihi.isAfter(islem.getSonTeslimTarihi())) {
                gecikenGun = ChronoUnit.DAYS.between(islem.getSonTeslimTarihi(), hesaplamaTarihi);
            }

            // Gecikme varsa listeye ekle (Gunluk 5 TL)
            if (gecikenGun > 0) {
                double cezaTutari = gecikenGun * 5.0;
                toplamBorc += cezaTutari;

                Map<String, Object> detay = new HashMap<>();
                detay.put("kitapAdi", islem.getKitap().getKitapAdi());
                detay.put("gecikenGun", gecikenGun);
                detay.put("tutar", cezaTutari);

                cezaDetaylari.add(detay);
            }
        }

        // Sonuclari paketle ve on yuze (Frontend) gonder.
        Map<String, Object> cevap = new HashMap<>();
        cevap.put("ad", uye.getAd());
        cevap.put("soyad", uye.getSoyad());
        cevap.put("email", uye.getEmail()); // Kullanici adi yerine Email gosteriyoruz
        cevap.put("toplamBorc", toplamBorc);
        cevap.put("cezaDetaylari", cezaDetaylari);

        return cevap;
    }

    // --- 4. YARDIMCI METOTLAR ---
    public List<Uye> tumunuGetir() {
        return uyeRepository.findAll();
    }

    public Optional<Uye> idIleGetir(Integer id) {
        return uyeRepository.findById(id);
    }

    public void sil(Integer id) {
        // Gercek projede once "Uyenin elinde kitap var mi?" diye kontrol edilmeli.
        // KitapServisi'nde yaptigimiz gibi burada da kontrol eklenebilir.
        uyeRepository.deleteById(id);
    }
}