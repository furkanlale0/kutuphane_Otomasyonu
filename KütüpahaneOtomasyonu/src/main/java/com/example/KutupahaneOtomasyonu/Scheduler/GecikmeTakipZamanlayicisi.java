package com.example.KutupahaneOtomasyonu.scheduler;

import com.example.KutupahaneOtomasyonu.entity.OduncIslemi;
import com.example.KutupahaneOtomasyonu.repository.OduncRepository;
import com.example.KutupahaneOtomasyonu.service.EpostaServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/*
 * BU SINIF NE İŞE YARAR?
 * "Zamanlanmış Görev" (Scheduler) sınıfıdır.
 * Arka planda sürekli çalışarak belirli aralıklarla sistem taraması yapar.
 * Teslim tarihi geçen kitapları tespit eder ve üyelere otomatik e-posta gönderir.
 */
@Component
public class GecikmeTakipZamanlayicisi {

    private final OduncRepository oduncRepository;
    private final EpostaServisi epostaServisi;

    @Autowired
    public GecikmeTakipZamanlayicisi(OduncRepository oduncRepository, EpostaServisi epostaServisi) {
        this.oduncRepository = oduncRepository;
        this.epostaServisi = epostaServisi;
    }

    /*
     //OTOMATİK KONTROL VE BİLDİRİM METODU
     //@Scheduled(cron = "*///30 * * * * ?"): Bu metodun her 30 saniyede bir çalışmasını sağlar.
      //       (Demo amaçlı süre kısa tutulmuştur, normalde günde 1 kez çalışması yeterlidir).
       //     *
    //        * İŞLEYİŞ:
    //        * 1. Veritabanından gecikmiş ve henüz iade edilmemiş kitapları bulur.
     //       * 2. Daha önce bildirim gönderilip gönderilmediğini kontrol eder (Spam Koruması).
    //        * 3. Eğer bildirim gitmediyse, üyeye uyarı e-postası atar.
//     */
    @Scheduled(cron = "*/30 * * * * ?")
    public void gecikmeleriKontrolEt() {
        System.out.println("Otomatik Gecikme Kontrolu Basladi...");

        List<OduncIslemi> gecikenler = oduncRepository.findBySonTeslimTarihiBeforeAndIadeTarihiIsNull(LocalDateTime.now());

        if (gecikenler.isEmpty()) {
            return;
        }

        for (OduncIslemi islem : gecikenler) {

            // Eğer üyeye daha önce mail atıldıysa tekrar atma (Tekrar Bildirim Kontrolü)
            if (islem.isBildirimGonderildi()) {
                continue;
            }

            String uyeEmail = islem.getUye().getEmail();
            String kitapAdi = islem.getKitap().getKitapAdi();

            try {
                String mesaj = "Sayın üyemiz, '" + kitapAdi + "' isimli kitabın teslim süresi dolmuştur. Lütfen iade ediniz.";
                epostaServisi.mailGonder(uyeEmail, "Kütüphane - Gecikme Uyarısı", mesaj);

                System.out.println("Mail gitti: " + uyeEmail);
            } catch (Exception e) {
                // Mail gönderiminde hata olsa bile sistem çalışmaya devam etmeli (Fault Tolerance).
                System.err.println("Mail hatasi (" + uyeEmail + "): " + e.getMessage());
            }

            /*
             * Hata olsa bile bayrağı işaretliyoruz ki sistem sürekli aynı kişiye
             * mail atmayı deneyip döngüye girmesin.
             */
            islem.setBildirimGonderildi(true);
            oduncRepository.save(islem);
        }
    }
}