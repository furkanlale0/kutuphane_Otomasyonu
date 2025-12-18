package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.OduncDurumu;
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

/*
 * BU SINIF NE İŞE YARAR?
 * Üye işlemlerini yöneten servis katmanıdır.
 * Yeni üye kaydı, şifreleme işlemleri ve üyenin profil sayfasında göreceği
 * anlık borç/ceza bilgilerinin hesaplanmasından sorumludur.
 */
@Service
public class UyeServisi {

    private final UyeRepository uyeRepository;
    private final OduncRepository oduncRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UyeServisi(UyeRepository uyeRepository, OduncRepository oduncRepository, PasswordEncoder passwordEncoder) {
        this.uyeRepository = uyeRepository;
        this.oduncRepository = oduncRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * YENİ ÜYE KAYDI
     * Kullanıcının şifresini güvenli bir şekilde hashleyerek veritabanına kaydeder.
     * Mükerrer e-posta kontrolü yapar.
     */
    public String uyeKaydet(Uye uye) {
        // E-posta kontrolü (Business Rule)
        if (uyeRepository.existsByEmail(uye.getEmail())) {
            return "Bu e-posta adresi zaten kayıtlı.";
        }

        // Şifre Güvenliği (BCrypt Hashing)
        // Kullanıcı "1234" girse bile veritabanında okunamaz bir formatta saklanır.
        uye.setSifre(passwordEncoder.encode(uye.getSifre()));
        uye.setKayitTarihi(LocalDateTime.now());

        uyeRepository.save(uye);
        return "Islem Basarili";
    }

    // Login işlemi için kullanıcıyı E-posta ile bulur.
    public Optional<Uye> emailIleGetir(String email) {
        return uyeRepository.findByEmail(email);
    }

    /*
     * ÜYE PROFİL VERİLERİ VE BORÇ HESAPLAMA
     * Üye profiline girdiğinde çalışır.
     * Hem kesinleşmiş borçları hem de o an işlemeye devam eden (teslim edilmemiş)
     * gecikme cezalarını anlık olarak hesaplar.
     */
    public Map<String, Object> uyeProfiliniGetir(Integer uyeId) {
        Uye uye = uyeRepository.findById(uyeId)
                .orElseThrow(() -> new RuntimeException("Üye bulunamadı"));

        List<OduncIslemi> gecmis = oduncRepository.findByUye_UyeId(uyeId);

        double toplamBorc = 0;
        List<Map<String, Object>> cezaDetaylari = new ArrayList<>();
        LocalDateTime bugun = LocalDateTime.now();

        for (OduncIslemi islem : gecmis) {

            // Eğer borç zaten ödenmişse ("ODENDI") hesaplamaya katma.
            if ("ODENDI".equals(islem.getOdemeDurumu())) continue;

            double islemBorcu = 0.0;
            long gecikmeSuresi = 0;

            // DURUM 1: Kitap hala üyede ve süresi geçmiş (AKTİF GECİKME)
            // Demo için dakika hesabı yapıyoruz.
            if (islem.getDurum() == OduncDurumu.ODUNC_ALINDI && islem.getSonTeslimTarihi().isBefore(bugun)) {
                gecikmeSuresi = ChronoUnit.MINUTES.between(islem.getSonTeslimTarihi(), bugun);
                islemBorcu = gecikmeSuresi * 5.0; // Dakika başı 5 TL
            }
            // DURUM 2: Kitap iade edilmiş ama borç ödenmemiş (KESİNLEŞMİŞ BORÇ)
            else if ("ODENMEDI".equals(islem.getOdemeDurumu()) || "ONAY_BEKLIYOR".equals(islem.getOdemeDurumu())) {
                islemBorcu = islem.getCezaMiktari();
                // Detayda göstermek için tahmini süre (Tutar / 5)
                gecikmeSuresi = (long) (islemBorcu / 5.0);
            }

            // Eğer borç varsa listeye ekle
            if (islemBorcu > 0) {
                toplamBorc += islemBorcu;

                Map<String, Object> detay = new HashMap<>();
                detay.put("kitapAdi", islem.getKitap().getKitapAdi());
                detay.put("gecikenSure", gecikmeSuresi + " Dakika");
                detay.put("tutar", islemBorcu);
                // Onay bekliyorsa parantez içinde belirtelim
                String durumNotu = "ONAY_BEKLIYOR".equals(islem.getOdemeDurumu()) ? " (Onay Bekliyor)" : "";
                detay.put("durum", durumNotu);

                cezaDetaylari.add(detay);
            }
        }

        // Frontend için JSON paketi hazırlama
        Map<String, Object> cevap = new HashMap<>();
        cevap.put("ad", uye.getAd());
        cevap.put("soyad", uye.getSoyad());
        cevap.put("email", uye.getEmail());
        cevap.put("toplamBorc", toplamBorc);
        cevap.put("cezaDetaylari", cezaDetaylari);

        return cevap;
    }

    // --- YARDIMCI METODLAR ---
    public List<Uye> tumunuGetir() {
        return uyeRepository.findAll();
    }

    public Optional<Uye> idIleGetir(Integer id) {
        return uyeRepository.findById(id);
    }

    public void sil(Integer id) {
        // Güvenlik: İlerde buraya "Borcu var mı?" kontrolü eklenebilir.
        uyeRepository.deleteById(id);
    }
}