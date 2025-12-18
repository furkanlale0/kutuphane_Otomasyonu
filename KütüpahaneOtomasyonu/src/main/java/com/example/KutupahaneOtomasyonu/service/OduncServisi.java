package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.*;
import com.example.KutupahaneOtomasyonu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/*
 * BU SINIF NE İŞE YARAR?
 * Kütüphanedeki "Hareket" mekanizmasını yöneten ana servistir.
 * Kitap verme, iade alma, ceza hesaplama ve ödeme onayı gibi
 * tüm operasyonel iş kuralları (Business Logic) burada işlenir.
 */
@Service
public class OduncServisi {

    @Autowired private OduncRepository oduncRepository;
    @Autowired private KitapRepository kitapRepository;
    @Autowired private UyeRepository uyeRepository;

    /*
     * 1. KİTAP ÖDÜNÇ VERME İŞLEMİ
     * Üyenin kitabı alıp alamayacağını kontrol eder ve işlemi başlatır.
     *
     * KONTROLLER:
     * - Kitap ve Üye var mı?
     * - Kitabın stoğu var mı?
     * - Üyenin ödenmemiş eski bir borcu var mı? (Varsa engeller)
     */
    public String kitapOduncVer(Integer uyeId, Integer kitapId) {
        Optional<Kitap> k = kitapRepository.findById(kitapId);
        Optional<Uye> u = uyeRepository.findById(uyeId);

        if (k.isEmpty() || u.isEmpty()) return "Hata: Kitap veya Üye bulunamadı.";

        Kitap kitap = k.get();
        if (kitap.getStokSayisi() <= 0) return "Stok Yok";

        // BORÇ KONTROLÜ: "Borcu olan yeni kredi çekemez" mantığı.
        boolean borcluMu = oduncRepository.findAll().stream()
                .anyMatch(o -> o.getUye().getUyeId().equals(uyeId) &&
                        ( "ODENMEDI".equals(o.getOdemeDurumu()) || "ONAY_BEKLIYOR".equals(o.getOdemeDurumu()) ));

        if(borcluMu) return "Ödenmemiş cezanız varken yeni kitap alamazsınız!";

        // İşlemin Başlatılması
        OduncIslemi islem = new OduncIslemi();
        islem.setUye(u.get());
        islem.setKitap(kitap);
        islem.setAlisTarihi(LocalDateTime.now());

        /*
         * DEMO AYARI:
         * Sunum sırasında gecikme senaryosunu hemen gösterebilmek için
         * teslim süresi 1 dakika olarak ayarlanmıştır.
         * (Gerçek hayatta burası .plusDays(15) olurdu).
         */
        islem.setSonTeslimTarihi(LocalDateTime.now().plusMinutes(1));

        islem.setDurum(OduncDurumu.ODUNC_ALINDI);
        islem.setOdemeDurumu("YOK");

        oduncRepository.save(islem);
        return "Islem Basarili";
    }

    /*
     * 2. KİTAP İADE ALMA İŞLEMİ
     * Kitabı geri alır, tarihi kontrol eder ve gerekirse ceza keser.
     */
    public String kitapIadeAl(Integer uyeId, Integer kitapId) {
        // İlgili üyenin elindeki ilgili kitabı buluyoruz.
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

        /*
         * CEZA HESAPLAMA ALGORİTMASI (Demo Modu)
         * Geciken her dakika için 5 TL ceza uygulanır.
         */
        if (islem.getSonTeslimTarihi().isBefore(bugun)) {
            long gecikenDakika = ChronoUnit.MINUTES.between(islem.getSonTeslimTarihi(), bugun);

            if (gecikenDakika > 0) {
                double ceza = gecikenDakika * 5.0;
                islem.setCezaMiktari(ceza);
                islem.setOdemeDurumu("ODENMEDI"); // Borç oluştu
            }
        }
        oduncRepository.save(islem);
        return "Islem Basarili";
    }

    /*
     * 3. ÜYE ÖDEME BİLDİRİMİ
     * Üye borcunu ödediğini beyan eder ("Ödedim" butonu).
     * Borcu silmez, sadece durumu "ONAY_BEKLIYOR" yapar.
     */
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

    /*
     * 4. ADMIN ÖDEME ONAYI
     * Yönetici paranın hesaba geçtiğini onaylar ("Tahsil Et" butonu).
     * Borcu kalıcı olarak kapatır ("ODENDI").
     */
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

    /*
     * YARDIMCI METOD: ANLIK CEZA HESAPLAYICI
     * Veritabanına kaydetmeden, o anki duruma göre ekranda görünecek cezayı hesaplar.
     * İki senaryoyu yönetir:
     * A) Aktif Gecikme: Kitap hala üyede, ceza her dakika artıyor.
     * B) Kesinleşmiş Ceza: Kitap iade edilmiş, ceza tutarı sabitlenmiş.
     */
    private Map<String, Object> cezaVerisiHazirla(OduncIslemi o) {
        LocalDateTime bugun = LocalDateTime.now();
        double hesaplananCeza = 0;
        long gecikenSure = 0;
        String durumMetni = o.getOdemeDurumu();

        // Senaryo A: Aktif Gecikme
        if (o.getDurum() == OduncDurumu.ODUNC_ALINDI && o.getSonTeslimTarihi().isBefore(bugun)) {
            gecikenSure = ChronoUnit.MINUTES.between(o.getSonTeslimTarihi(), bugun);
            hesaplananCeza = gecikenSure * 5.0;
            durumMetni = "AKTIF_GECIKME";
        }
        // Senaryo B: Kesinleşmiş Ceza
        else if (o.getCezaMiktari() > 0) {
            hesaplananCeza = o.getCezaMiktari();
            // Göstermelik gecikme süresi hesaplama (Tutar / 5)
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

    // --- RAPORLAMA VE LİSTELEME METODLARI ---

    // Admin Paneli: Tüm Cezalar
    public List<Map<String, Object>> tumCezalariGetir() {
        List<Map<String, Object>> liste = new ArrayList<>();
        List<OduncIslemi> hepsi = oduncRepository.findAll();

        for (OduncIslemi o : hepsi) {
            // "ODENDI" durumundakiler listede kalabalık yapmasın diye gizleniyor.
            Map<String, Object> veri = cezaVerisiHazirla(o);
            if (veri != null && !o.getOdemeDurumu().equals("ODENDI")) {
                liste.add(veri);
            }
        }
        return liste;
    }

    // Üye Paneli: Kendi Cezaları
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

    // Üye Paneli: Aktif (Okumakta olduğu) Kitaplar
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

    // Üye Paneli: Geçmiş İşlemler
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