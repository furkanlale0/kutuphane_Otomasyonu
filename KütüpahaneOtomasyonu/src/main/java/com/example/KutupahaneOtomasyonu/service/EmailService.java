package com.example.KutupahaneOtomasyonu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// @Service: Spring'e "Bu sınıf bir iş mantığı yürütür (Postacıdır), bunu hafızaya al" diyoruz.
@Service
public class EmailService {

    // Spring Boot'un hazır e-posta gönderme aracı.
    // Tıpkı gerçek hayattaki PTT veya Kargo şirketi gibi, altyapıyı bu sağlar.
    @Autowired
    private JavaMailSender mailSender;

    /**
     * Basit bir e-posta gönderme metodu.
     *
     * @param to      Kime gönderilecek? (Alıcı e-posta adresi)
     * @param subject E-postanın konusu (Başlık)
     * @param body    E-postanın içeriği (Mesaj metni)
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        // Yeni bir boş mektup kağıdı oluşturuyoruz.
        SimpleMailMessage message = new SimpleMailMessage();

        // Mektubun üzerindeki bilgileri dolduruyoruz:
        message.setFrom("seninmailin@gmail.com"); // Kimden gidiyor? (Ayarlardaki mail ile aynı olmalı)
        message.setTo(to);                        // Kime gidiyor?
        message.setSubject(subject);              // Konusu ne?
        message.setText(body);                    // İçeriği ne?

        // Ve postacıyı çağırıp "Bunu gönder!" diyoruz.
        mailSender.send(message);

        // Konsola da bilgi verelim ki çalıştığını görelim.
        System.out.println("📨 E-posta gönderildi: " + to);
    }
}