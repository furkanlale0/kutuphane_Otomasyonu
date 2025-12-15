package com.example.KutupahaneOtomasyonu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// @Service: Spring'e "Bu sinif bir is mantigi yurutur (Postacidir), bunu hafizaya al" diyoruz.
@Service
public class EpostaServisi { // EmailService -> EpostaServisi

    // Spring Boot'un hazir e-posta gonderme araci.
    // Tipki gercek hayattaki PTT veya Kargo sirketi gibi, altyapiyi bu saglar.
    // (Bunun calismasi icin application.properties dosyasinda Gmail ayarlari yapilmalidir).
    @Autowired
    private JavaMailSender mailSender;

    /**
     * Basit bir e-posta gonderme metodu.
     * Scheduler (Zamanlayici) sinifi buradan cagirip mail attirir.
     *
     * @param alici   Kime gonderilecek? (Uyenin e-posta adresi)
     * @param konu    E-postanin konusu (Baslik)
     * @param icerik  E-postanin icerigi (Mesaj metni)
     */
    public void mailGonder(String alici, String konu, String icerik) {
        // 1. Yeni bir bos mektup kagidi (zarf) olusturuyoruz.
        SimpleMailMessage mesaj = new SimpleMailMessage();

        // 2. Mektubun uzerindeki bilgileri dolduruyoruz:
        // DIKKAT: Buradaki mail adresi, application.properties ayarlarindaki mail ile ayni olmali!
        mesaj.setFrom("furkanlale408@gmail.com"); // Kimden gidiyor?

        mesaj.setTo(alici);      // Kime gidiyor?
        mesaj.setSubject(konu);  // Konusu ne?
        mesaj.setText(icerik);   // Icerigi ne? ("Kitabiniz gecikti getiriniz" vb.)

        // 3. Ve postaciyi cagirip "Bunu gonder!" diyoruz.
        try {
            mailSender.send(mesaj);
            // Konsola da bilgi verelim ki calistigini gorelim.
            System.out.println("📨 E-posta basariyla gonderildi: " + alici);
        } catch (Exception e) {
            System.err.println("❌ E-posta gonderilirken hata olustu: " + e.getMessage());
        }
    }
}