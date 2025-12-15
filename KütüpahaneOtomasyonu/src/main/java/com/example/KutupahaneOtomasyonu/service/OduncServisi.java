package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.*;
import com.example.KutupahaneOtomasyonu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OduncServisi { // LoanService -> OduncServisi

    private final OduncRepository oduncRepository;
    private final KitapRepository kitapRepository;
    private final UyeRepository uyeRepository;

    @Autowired
    public OduncServisi(OduncRepository oduncRepository, KitapRepository kitapRepository, UyeRepository uyeRepository) {
        this.oduncRepository = oduncRepository;
        this.kitapRepository = kitapRepository;
        this.uyeRepository = uyeRepository;
    }

    // --- KITAP ODUNC ALMA ---
    @Transactional // Stok duserken hata olursa islemi geri alir.
    public String kitapOduncVer(Integer uyeId, Integer kitapId) {
        // 1. Kontrol: Bu uye bu kitabi daha once almis ve iade etmemis mi?
        boolean zatenVar = oduncRepository.existsByUye_UyeIdAndKitap_KitapIdAndIadeTarihiIsNull(uyeId, kitapId);
        if (zatenVar) return "Bu kitabi zaten odunc almissiniz, once eskisini getirin!";

        Optional<Uye> uyeKutusu = uyeRepository.findById(uyeId);
        Optional<Kitap> kitapKutusu = kitapRepository.findById(kitapId);

        if (uyeKutusu.isEmpty() || kitapKutusu.isEmpty()) return "Hata: Uye veya kitap bulunamadi.";

        Kitap kitap = kitapKutusu.get();

        // 2. Kontrol: Stokta kitap kaldi mi?
        if (kitap.getStokSayisi() <= 0) return "Stokta kitap kalmadi!";

        // 3. Islem: Stok dusur
        kitap.setStokSayisi(kitap.getStokSayisi() - 1);
        kitapRepository.save(kitap);

        // 4. Islem: Odunc kaydi olustur
        OduncIslemi islem = new OduncIslemi();
        islem.setUye(uyeKutusu.get());
        islem.setKitap(kitap);
        islem.setAlisTarihi(LocalDateTime.now());
        // 14 gun (2 hafta) sure veriyoruz.
        islem.setSonTeslimTarihi(LocalDateTime.now().plusDays(14));
        islem.setDurum(OduncDurumu.ODUNC_ALINDI);
        islem.setCezaOdendiMi(false);

        oduncRepository.save(islem);

        return "Islem Basarili";
    }

    // --- KITAP IADE ETME ---
    @Transactional
    public String kitapIadeAl(Integer uyeId, Integer kitapId) {
        // Uyenin elindeki o kitabi bul (List donuyor ama biz ilkini alacagiz).
        List<OduncIslemi> aktifIslemler = oduncRepository.findByUye_UyeIdAndKitap_KitapIdAndIadeTarihiIsNull(uyeId, kitapId);

        if (aktifIslemler.isEmpty()) return "Bu kitap sizde gorunmuyor.";

        // Kaydi bul
        OduncIslemi islem = aktifIslemler.get(0);

        // Iade tarihini bugun yap ve durumu guncelle
        islem.setIadeTarihi(LocalDateTime.now());
        islem.setDurum(OduncDurumu.IADE_EDILDI);

        // GECIKME KONTROLU: Eger gecikmisse durumu "GECIKTI" yapalim mi?
        // Kod karmasiklasmasin diye burada sadece IADE_EDILDI yapiyoruz, ceza hesaplamada tarihe bakacagiz zaten.

        oduncRepository.save(islem);

        // Stok arttir
        Kitap kitap = islem.getKitap();
        kitap.setStokSayisi(kitap.getStokSayisi() + 1);
        kitapRepository.save(kitap);

        return "Islem Basarili";
    }

    // --- UYENIN GECMISI ---
    public List<Map<String, Object>> uyeGecmisiniGetir(Integer uyeId) {
        List<OduncIslemi> gecmis = oduncRepository.findByUye_UyeId(uyeId);
        return listeyiFormatla(gecmis);
    }

    // --- UYENIN ELINDEKI AKTIF KITAPLAR ---
    public List<Map<String, Object>> aktifOduncleriGetir(Integer uyeId) {
        List<OduncIslemi> oduncler = oduncRepository.findByUye_UyeIdAndIadeTarihiIsNull(uyeId);
        return listeyiFormatla(oduncler);
    }

    // --- CEZA HESAPLAMA MOTORU ---
    // Burasi biraz matematiksel, butun kayitlari tarayip borclu olanlari bulur.
    public List<Map<String, Object>> tumCezalariHesapla() {
        List<OduncIslemi> tumIslemler = oduncRepository.findAll();
        List<Map<String, Object>> cezalar = new ArrayList<>();

        for (OduncIslemi islem : tumIslemler) {
            // Eger ceza zaten odenmisse (veya borcu yoksa) gec.
            if (Boolean.TRUE.equals(islem.getCezaOdendiMi())) continue;

            // Karsilastirma Tarihi: Kitap iade edildiyse "Iade Tarihi", edilmediyse "Bugun".
            LocalDateTime karsilastirmaTarihi = (islem.getIadeTarihi() != null) ? islem.getIadeTarihi() : LocalDateTime.now();

            // Eger (Iade/Bugun) tarihi, Son Teslim Tarihinden sonraysa -> GECIKME VAR
            if (karsilastirmaTarihi.isAfter(islem.getSonTeslimTarihi())) {

                // Kac gun gecikti?
                long gecikenGun = ChronoUnit.DAYS.between(islem.getSonTeslimTarihi(), karsilastirmaTarihi);

                if (gecikenGun > 0) {
                    Map<String, Object> satir = new HashMap<>();
                    satir.put("oduncId", islem.getOduncId());
                    satir.put("uyeAdSoyad", islem.getUye().getAd() + " " + islem.getUye().getSoyad());
                    satir.put("kitapAdi", islem.getKitap().getKitapAdi());
                    satir.put("gecikenGun", gecikenGun);
                    // Gunluk ceza bedeli: 5.0 TL
                    satir.put("toplamTutar", gecikenGun * 5.0);
                    satir.put("iadeEdildiMi", islem.getIadeTarihi() != null);

                    cezalar.add(satir);
                }
            }
        }
        return cezalar;
    }

    // --- CEZA ODEME ---
    public boolean cezaOde(Integer oduncId) {
        Optional<OduncIslemi> islemKutusu = oduncRepository.findById(oduncId);
        if (islemKutusu.isPresent()) {
            OduncIslemi islem = islemKutusu.get();
            islem.setCezaOdendiMi(true);
            oduncRepository.save(islem);
            return true;
        }
        return false;
    }

    // --- YARDIMCI METOT: Listeyi JSON Formatina Cevirir ---
    private List<Map<String, Object>> listeyiFormatla(List<OduncIslemi> liste) {
        List<Map<String, Object>> sonucListesi = new ArrayList<>();
        for (OduncIslemi b : liste) {
            Map<String, Object> satir = new HashMap<>();
            satir.put("kitapId", b.getKitap().getKitapId());
            satir.put("kitapAdi", b.getKitap().getKitapAdi());
            satir.put("alisTarihi", b.getAlisTarihi().toString());
            satir.put("sonTeslimTarihi", b.getSonTeslimTarihi().toString());
            // Iade tarihi varsa string yap, yoksa null don
            satir.put("iadeTarihi", (b.getIadeTarihi() != null) ? b.getIadeTarihi().toString() : null);
            satir.put("durum", b.getDurum());
            sonucListesi.add(satir);
        }
        return sonucListesi;
    }
}