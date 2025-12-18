package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.*;
import com.example.KutupahaneOtomasyonu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class OduncServisi {

    @Autowired private OduncRepository oduncRepository;
    @Autowired private KitapRepository kitapRepository;
    @Autowired private UyeRepository uyeRepository;

    // --- 1. KITAP ODUNC VER (Demo: 1 Dakika Süre) ---
    public String kitapOduncVer(Integer uyeId, Integer kitapId) {
        Optional<Kitap> k = kitapRepository.findById(kitapId);
        Optional<Uye> u = uyeRepository.findById(uyeId);
        if (k.isEmpty() || u.isEmpty()) return "Hata";

        Kitap kitap = k.get();
        if (kitap.getStokSayisi() <= 0) return "Stok Yok";

        // Aktif borcu (ödenmemiş veya onay bekleyen) var mı?
        boolean borcluMu = oduncRepository.findAll().stream()
                .anyMatch(o -> o.getUye().getUyeId().equals(uyeId) &&
                        ( "ODENMEDI".equals(o.getOdemeDurumu()) || "ONAY_BEKLIYOR".equals(o.getOdemeDurumu()) ));
        if(borcluMu) return "Ödenmemiş cezanız varken yeni kitap alamazsınız!";

        OduncIslemi islem = new OduncIslemi();
        islem.setUye(u.get());
        islem.setKitap(kitap);
        islem.setAlisTarihi(LocalDateTime.now());
        islem.setSonTeslimTarihi(LocalDateTime.now().plusMinutes(1)); // 1 Dakika Süre
        islem.setDurum(OduncDurumu.ODUNC_ALINDI);
        islem.setOdemeDurumu("YOK");

        oduncRepository.save(islem);
        return "Islem Basarili";
    }

    // --- 2. KITAP IADE AL (Cezayı Hesapla ve Kaydet) ---
    public String kitapIadeAl(Integer uyeId, Integer kitapId) {
        List<OduncIslemi> kayitlar = oduncRepository.findAll();
        OduncIslemi islem = kayitlar.stream()
                .filter(o -> o.getUye().getUyeId().equals(uyeId) && o.getKitap().getKitapId().equals(kitapId) && o.getDurum() == OduncDurumu.ODUNC_ALINDI)
                .findFirst().orElse(null);

        if (islem == null) return "Kayit Yok";

        LocalDateTime bugun = LocalDateTime.now();
        islem.setIadeTarihi(bugun);
        islem.setDurum(OduncDurumu.IADE_EDILDI);

        // CEZA HESAPLAMA (Demo: Dakikası 5 TL)
        if (islem.getSonTeslimTarihi().isBefore(bugun)) {
            long gecikenDakika = ChronoUnit.MINUTES.between(islem.getSonTeslimTarihi(), bugun);
            if (gecikenDakika > 0) {
                double ceza = gecikenDakika * 5.0;
                islem.setCezaMiktari(ceza);
                islem.setOdemeDurumu("ODENMEDI"); // Ceza var ve henüz ödenmedi
            }
        }
        oduncRepository.save(islem);
        return "Islem Basarili";
    }

    // --- 3. UYE: ÖDEME BILDIRIMI YAP ("Ödedim" butonu) ---
    public boolean odemeBildirimiYap(Integer uyeId) {
        List<OduncIslemi> hepsi = oduncRepository.findAll();
        boolean islemYapildi = false;

        for (OduncIslemi o : hepsi) {
            // Sadece bu üyenin ÖDENMEMİŞ cezalarını bul
            if (o.getUye().getUyeId().equals(uyeId) && "ODENMEDI".equals(o.getOdemeDurumu())) {
                o.setOdemeDurumu("ONAY_BEKLIYOR"); // Durumu değiştir
                oduncRepository.save(o);
                islemYapildi = true;
            }
        }
        return islemYapildi;
    }

    // --- 4. ADMIN: ÖDEMEYI ONAYLA ("Tahsil Et" butonu) ---
    public boolean odemeyiOnayla(Integer oduncId) { // uyeId yerine oduncId ile işlem yapıyoruz ki spesifik olsun
        Optional<OduncIslemi> islemOp = oduncRepository.findById(oduncId);
        if(islemOp.isPresent()) {
            OduncIslemi islem = islemOp.get();
            // Onay bekleyen veya ödenmemiş cezayı kapat
            islem.setOdemeDurumu("ODENDI");
            oduncRepository.save(islem);
            return true;
        }
        return false;
    }

    // --- YARDIMCI METOD: ANLIK CEZA HESAPLA ---
    private Map<String, Object> cezaVerisiHazirla(OduncIslemi o) {
        LocalDateTime bugun = LocalDateTime.now();
        double hesaplananCeza = 0;
        long gecikenSure = 0;
        String durumMetni = o.getOdemeDurumu(); // Varsayılan durum

        // A) Kitap hala üyede ve süresi geçmiş (AKTİF CEZA)
        if (o.getDurum() == OduncDurumu.ODUNC_ALINDI && o.getSonTeslimTarihi().isBefore(bugun)) {
            gecikenSure = ChronoUnit.MINUTES.between(o.getSonTeslimTarihi(), bugun);
            hesaplananCeza = gecikenSure * 5.0; // Dakika başı 5 TL
            durumMetni = "AKTIF_GECIKME"; // Özel durum kodu
        }
        // B) Kitap iade edilmiş ve cezası var (KESİNLEŞMİŞ CEZA)
        else if (o.getCezaMiktari() > 0) {
            hesaplananCeza = o.getCezaMiktari();
            // Gecikme süresi iade tarihine göre hesaplanmış olmalı ama burada göstermelik 0 geçebiliriz
            // veya veritabanında tutuyorsak onu çekeriz. Şimdilik admin görsün diye:
            gecikenSure = (long) (hesaplananCeza / 5.0);
        }

        // Eğer ceza yoksa boş dön
        if (hesaplananCeza <= 0) return null;

        Map<String, Object> m = new HashMap<>();
        m.put("oduncId", o.getOduncId());
        m.put("uye", o.getUye().getAd() + " " + o.getUye().getSoyad());
        m.put("kitap", o.getKitap().getKitapAdi());
        m.put("gecikme", gecikenSure + " Dakika");
        m.put("tutar", hesaplananCeza);
        m.put("durum", durumMetni); // AKTIF_GECIKME, ODENMEDI, ONAY_BEKLIYOR, ODENDI
        return m;
    }

    // --- 5. ADMIN TABLOSU İÇİN CEZALAR (GÜNCELLENDİ) ---
    public List<Map<String, Object>> tumCezalariGetir() {
        List<Map<String, Object>> liste = new ArrayList<>();
        List<OduncIslemi> hepsi = oduncRepository.findAll();

        for (OduncIslemi o : hepsi) {
            // Sadece (Aktif Gecikme) veya (Kesinleşmiş Borç) veya (Onay Bekleyen) olanları getir
            // "ODENDI" olanları admin listesinde kalabalık yapmasın diye gizleyebiliriz veya gösterebiliriz.
            // Şimdilik sadece sorunu olanları gösterelim:
            Map<String, Object> veri = cezaVerisiHazirla(o);
            if (veri != null && !o.getOdemeDurumu().equals("ODENDI")) {
                liste.add(veri);
            }
        }
        return liste;
    }

    // --- 6. UYE PROFILI İÇİN CEZA DETAYLARI (GÜNCELLENDİ) ---
    public List<Map<String, Object>> uyeCezaDetaylari(Integer uyeId) {
        List<Map<String, Object>> liste = new ArrayList<>();
        List<OduncIslemi> hepsi = oduncRepository.findAll();

        for (OduncIslemi o : hepsi) {
            if (o.getUye().getUyeId().equals(uyeId)) {
                Map<String, Object> veri = cezaVerisiHazirla(o);
                if (veri != null) {
                    liste.add(veri);
                }
            }
        }
        return liste;
    }

    // --- Diğer metodlar (aktifOduncler vb.) aynı kalabilir ---
    public List<Map<String, Object>> aktifOduncleriGetir(Integer uyeId) {
        // ... (Eski kodun aynısı)
        List<Map<String, Object>> liste = new ArrayList<>();
        for (OduncIslemi o : oduncRepository.findAll()) {
            if (o.getUye().getUyeId().equals(uyeId) && o.getDurum() == OduncDurumu.ODUNC_ALINDI) {
                Map<String, Object> veri = new HashMap<>();
                veri.put("kitapAdi", o.getKitap().getKitapAdi());
                veri.put("kitapId", o.getKitap().getKitapId());
                veri.put("sonTeslimTarihi", o.getSonTeslimTarihi().toString());
                liste.add(veri);
            }
        }
        return liste;
    }

    public List<Map<String, Object>> uyeGecmisiniGetir(Integer uyeId) {
        // ... (Eski kodun aynısı)
        List<Map<String, Object>> liste = new ArrayList<>();
        for (OduncIslemi o : oduncRepository.findAll()) {
            if (o.getUye().getUyeId().equals(uyeId)) {
                Map<String, Object> veri = new HashMap<>();
                veri.put("kitapAdi", o.getKitap().getKitapAdi());
                veri.put("alisTarihi", o.getAlisTarihi().toString());
                veri.put("iadeTarihi", o.getIadeTarihi() != null ? o.getIadeTarihi().toString() : "-");
                veri.put("durum", o.getDurum().name());
                liste.add(veri);
            }
        }
        return liste;
    }
}