package com.example.KutupahaneOtomasyonu.scheduler;

import com.example.KutupahaneOtomasyonu.entity.OduncIslemi;
import com.example.KutupahaneOtomasyonu.repository.OduncRepository;
import com.example.KutupahaneOtomasyonu.service.EpostaServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// @Component: Spring'e "Bu sinifi yonet, bu sistemin surekli calisan bir parcasidir" diyoruz.
// Eger bunu yazmazsak, zamanlayici asla devreye girmez, sus pus oturur.
@Component
public class GecikmeTakipZamanlayicisi { // LoanScheduler -> GecikmeTakipZamanlayicisi

    // Veritabani sorgulari icin Depo Sorumlusu
    private final OduncRepository oduncRepository;

    // Mail gondermek icin Postaci Servisi
    private final EpostaServisi epostaServisi;

    // Constructor Injection: Gerekli araclari Spring'den istiyoruz.
    @Autowired
    public GecikmeTakipZamanlayicisi(OduncRepository oduncRepository, EpostaServisi epostaServisi) {
        this.oduncRepository = oduncRepository;
        this.epostaServisi = epostaServisi;
    }

    // --- ZAMANLAYICI METODU ---
    // @Scheduled: Bu metodun belirli araliklarla otomatik calisacagini belirtir.
    // cron = "*/30 * * * * ?" -> "Her 30 saniyede bir calistir" demektir.
    // (Sunumda 30 saniye iyidir, hemen sonucu gorursunuz. Gercek hayatta gunde 1 kere yapilir).
    @Scheduled(cron = "*/30 * * * * ?")
    public void gecikmeleriKontrolEt() {
        System.out.println("⏰ Otomatik Gecikme Kontrolu Basladi...");

        // 1. GECIKENLERI BUL
        // Repository'de yazdigimiz o uzun isimli metodu cagiriyoruz.
        // Meali: Son teslim tarihi "Su an"dan (LocalDateTime.now) once olan VE hala iade edilmemisleri getir.
        List<OduncIslemi> gecikenler = oduncRepository.findBySonTeslimTarihiBeforeAndIadeTarihiIsNull(LocalDateTime.now());

        // Eger hic geciken kitap yoksa, bosuna islem yapma, metodu bitir.
        if (gecikenler.isEmpty()) {
            return;
        }

        // 2. DONGU (HER BIR GECIKEN KITAP ICIN)
        for (OduncIslemi islem : gecikenler) {

            // --- SPAM KORUMASI (CRITICAL) ---
            // Eger bu kitap icin daha once mail atilmissa (bildirimGonderildi == true),
            // tekrar tekrar mail atip adami taciz etme! "continue" diyerek siradaki kitaba gec.
            if (islem.isBildirimGonderildi()) {
                continue;
            }

            // Gerekli bilgileri al (Kime atilacak? Hangi kitap?)
            // Entity icindeki iliskilerden (islem -> uye -> email) veriyi cekiyoruz.
            String uyeEmail = islem.getUye().getEmail();
            String kitapAdi = islem.getKitap().getKitapAdi();

            // 3. MAIL GONDERME (Hata Yonetimli)
            try {
                // Eposta servisini cagir.
                String mesaj = "Sayın üyemiz, '" + kitapAdi + "' isimli kitabın teslim süresi dolmuştur. Lütfen iade ediniz.";
                epostaServisi.mailGonder(uyeEmail, "Kütüphane - Gecikme Uyarısı", mesaj);

                System.out.println("✅ Mail gitti: " + uyeEmail);
            } catch (Exception e) {
                // Eger internet yoksa veya mail adresi hataliysa sistem cokmesin.
                // Sadece hatayi konsola yaz ve diger kullanicilara gecmeye devam et.
                System.err.println("❌ Mail hatasi (" + uyeEmail + "): " + e.getMessage());
            }

            // --- ISARETLEME ---
            // Mail gitse de gitmese de (hata olsa bile), bu kaydi "Islem Yapildi" olarak isaretle.
            // Neden? Cunku isaretlemezsek, 30 saniye sonra sistem tekrar mail atmayi dener.
            // Sonsuz donguye girmemek icin burayi 'true' yapip kaydediyoruz.
            islem.setBildirimGonderildi(true);
            oduncRepository.save(islem);
        }
    }
}