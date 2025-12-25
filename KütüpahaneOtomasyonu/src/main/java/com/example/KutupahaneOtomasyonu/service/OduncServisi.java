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

    // --- 1. KITAP ODUNC VER ---
    public String kitapOduncVer(Integer uyeId, Integer kitapId) {
        Optional<Kitap> k = kitapRepository.findById(kitapId);
        Optional<Uye> u = uyeRepository.findById(uyeId);

        if (k.isEmpty() || u.isEmpty()) return "Hata: Kitap veya Üye bulunamadı.";

        Kitap kitap = k.get();

        // --- KONTROL 1: BORÇ VAR MI? ---
        boolean borcluMu = oduncRepository.findAll().stream()
                .anyMatch(o -> o.getUye().getUyeId().equals(uyeId) &&
                        ( "ODENMEDI".equals(o.getOdemeDurumu()) || "ONAY_BEKLIYOR".equals(o.getOdemeDurumu()) ));

        if(borcluMu) return "Ödenmemiş cezanız varken yeni kitap alamazsınız!";

        // --- KONTROL 2 (YENİ EKLENDİ): AYNI KİTAP ZATEN ELİNDE Mİ? ---
        boolean ayniKitapSendeMi = oduncRepository.findAll().stream()
                .anyMatch(o -> o.getUye().getUyeId().equals(uyeId) &&      // Bu üye
                        o.getKitap().getKitapId().equals(kitapId) && // Bu kitap
                        o.getDurum() == OduncDurumu.ODUNC_ALINDI);   // Hala elinde mi? (İade etmemiş)

        if (ayniKitapSendeMi) {
            return "Bu kitabı zaten ödünç aldınız! İade etmeden tekrar alamazsınız.";
        }

        // --- KONTROL 3: STOK VAR MI? ---
        if (kitap.getStokSayisi() <= 0) return "Stok Yok";

        // --- İŞLEM BAŞLIYOR ---
        OduncIslemi islem = new OduncIslemi();
        islem.setUye(u.get());
        islem.setKitap(kitap);
        islem.setAlisTarihi(LocalDateTime.now());

        // Demo: 1 Dakika süre
        islem.setSonTeslimTarihi(LocalDateTime.now().plusMinutes(1));

        islem.setDurum(OduncDurumu.ODUNC_ALINDI);
        islem.setOdemeDurumu("YOK");

        oduncRepository.save(islem);
        return "Islem Basarili";
    }

    // --- 2. KITAP IADE AL ---
    public String kitapIadeAl(Integer uyeId, Integer kitapId) {
        List<OduncIslemi> kayitlar = oduncRepository.findAll();
        OduncIslemi islem = kayitlar.stream()
                .filter(o -> o.getUye().getUyeId().equals(uyeId) &&
                        o.getKitap().getKitapId().equals(kitapId) &&
                        o.getDurum() == OduncDurumu.ODUNC_ALINDI)
                .findFirst().orElse(null);

        if (islem == null) return "Kayit Yok";

        LocalDateTime bugun = LocalDateTime.now();
        islem.setIadeTarihi(bugun);
        islem.setDurum(OduncDurumu.IADE_EDILDI);

        // CEZA HESAPLAMA (Dakikası 5 TL)
        if (islem.getSonTeslimTarihi().isBefore(bugun)) {
            long gecikenDakika = ChronoUnit.MINUTES.between(islem.getSonTeslimTarihi(), bugun);
            if (gecikenDakika > 0) {
                double ceza = gecikenDakika * 5.0;
                islem.setCezaMiktari(ceza);
                islem.setOdemeDurumu("ODENMEDI");
            }
        }
        oduncRepository.save(islem);
        return "Islem Basarili";
    }

    // --- 3. ODEME BILDIRIMI YAP ---
    public boolean odemeBildirimiYap(Integer uyeId) {
        List<OduncIslemi> hepsi = oduncRepository.findAll();
        boolean islemYapildi = false;

        for (OduncIslemi o : hepsi) {
            if (o.getUye().getUyeId().equals(uyeId) && "ODENMEDI".equals(o.getOdemeDurumu())) {
                o.setOdemeDurumu("ONAY_BEKLIYOR");
                oduncRepository.save(o);
                islemYapildi = true;
            }
        }
        return islemYapildi;
    }

    // --- 4. ODEMEYI ONAYLA ---
    public boolean odemeyiOnayla(Integer oduncId) {
        Optional<OduncIslemi> islemOp = oduncRepository.findById(oduncId);
        if(islemOp.isPresent()) {
            OduncIslemi islem = islemOp.get();
            islem.setOdemeDurumu("ODENDI");
            oduncRepository.save(islem);
            return true;
        }
        return false;
    }

    // --- YARDIMCI METODLAR ---
    private Map<String, Object> cezaVerisiHazirla(OduncIslemi o) {
        LocalDateTime bugun = LocalDateTime.now();
        double hesaplananCeza = 0;
        long gecikenSure = 0;
        String durumMetni = o.getOdemeDurumu();

        if (o.getDurum() == OduncDurumu.ODUNC_ALINDI && o.getSonTeslimTarihi().isBefore(bugun)) {
            gecikenSure = ChronoUnit.MINUTES.between(o.getSonTeslimTarihi(), bugun);
            hesaplananCeza = gecikenSure * 5.0;
            durumMetni = "AKTIF_GECIKME";
        }
        else if (o.getCezaMiktari() > 0) {
            hesaplananCeza = o.getCezaMiktari();
            gecikenSure = (long) (hesaplananCeza / 5.0);
        }

        if (hesaplananCeza <= 0) return null;

        Map<String, Object> m = new HashMap<>();
        m.put("oduncId", o.getOduncId());
        m.put("uye", o.getUye().getAd() + " " + o.getUye().getSoyad());
        m.put("kitap", o.getKitap().getKitapAdi());
        m.put("gecikme", gecikenSure + " Dakika");
        m.put("tutar", hesaplananCeza);
        m.put("durum", durumMetni);
        return m;
    }

    public List<Map<String, Object>> tumCezalariGetir() {
        List<Map<String, Object>> liste = new ArrayList<>();
        List<OduncIslemi> hepsi = oduncRepository.findAll();
        for (OduncIslemi o : hepsi) {
            Map<String, Object> veri = cezaVerisiHazirla(o);
            if (veri != null && !o.getOdemeDurumu().equals("ODENDI")) {
                liste.add(veri);
            }
        }
        return liste;
    }

    public List<Map<String, Object>> uyeCezaDetaylari(Integer uyeId) {
        List<Map<String, Object>> liste = new ArrayList<>();
        List<OduncIslemi> hepsi = oduncRepository.findAll();
        for (OduncIslemi o : hepsi) {
            if (o.getUye().getUyeId().equals(uyeId)) {
                Map<String, Object> veri = cezaVerisiHazirla(o);
                if (veri != null) liste.add(veri);
            }
        }
        return liste;
    }

    public List<Map<String, Object>> aktifOduncleriGetir(Integer uyeId) {
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