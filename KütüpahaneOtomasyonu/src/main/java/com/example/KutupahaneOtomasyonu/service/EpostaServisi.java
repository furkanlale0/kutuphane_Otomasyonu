package com.example.KutupahaneOtomasyonu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/*
 * BU SINIF NE İŞE YARAR?
 * E-posta gönderim işlemlerini yöneten servis sınıfıdır.
 * Spring Boot'un 'JavaMailSender' arayüzünü kullanarak, SMTP protokolü üzerinden
 * üyelere otomatik bildirim (gecikme uyarısı vb.) gönderilmesini sağlar.
 */
@Service
public class EpostaServisi {

    /*
     * JavaMailSender
     * Spring Framework'ün sağladığı e-posta gönderim aracıdır.
     * 'application.properties' dosyasındaki Gmail yapılandırma ayarlarını
     * (host, port, username, password) otomatik olarak kullanır.
     */
    @Autowired
    private JavaMailSender mailSender;

    /**
     * E-POSTA GÖNDERME METODU
     * Belirtilen alıcıya, konu ve içerik bilgileriyle basit metin formatında e-posta gönderir.
     * Genellikle 'GecikmeTakipZamanlayicisi' tarafından tetiklenir.
     *
     * @param alici   Alıcının e-posta adresi
     * @param konu    E-postanın başlığı
     * @param icerik  E-postanın gövde metni
     */
    public void mailGonder(String alici, String konu, String icerik) {
        // 1. E-posta nesnesinin (SimpleMailMessage) oluşturulması
        SimpleMailMessage mesaj = new SimpleMailMessage();

        // 2. Gönderim detaylarının ayarlanması
        // NOT: 'setFrom' kısmındaki adres, application.properties ayarlarındaki ile aynı olmalıdır.
        mesaj.setFrom("furkanlale408@gmail.com");
        mesaj.setTo(alici);
        mesaj.setSubject(konu);
        mesaj.setText(icerik);

        // 3. Gönderim işleminin gerçekleştirilmesi
        try {
            mailSender.send(mesaj);
            System.out.println("E-posta başarıyla gönderildi: " + alici);
        } catch (Exception e) {
            // Ağ hatası veya hatalı e-posta durumunda sistemin çökmemesi için hata yakalanır.
            System.err.println("E-posta gönderim hatası: " + e.getMessage());
        }
    }
}