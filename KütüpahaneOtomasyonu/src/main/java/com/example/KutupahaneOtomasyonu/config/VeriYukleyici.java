package com.example.KutupahaneOtomasyonu.config;

import com.example.KutupahaneOtomasyonu.entity.*;
import com.example.KutupahaneOtomasyonu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/*
 * BU SINIF NE İŞE YARAR?
 * "Veri Tohumlama" (Data Seeding) sınıfıdır.
 * Uygulama çalıştırıldığında devreye girer.
 * Normalde veritabanı boşsa otomatik Admin ekler.
 *
 * ŞU ANKİ DURUM:
 * Sunum kendi bilgisayarımdan yapılacağı ve Admin zaten kayıtlı olduğu için
 * tüm otomatik veri ekleme kodları devre dışı bırakılmıştır.
 */
@Component
public class VeriYukleyici implements CommandLineRunner {

    @Autowired private YoneticiRepository yoneticiRepository;
    @Autowired private PasswordEncoder sifreleyici;

    @Override
    public void run(String... args) throws Exception {

        /*
          --- ADMIN OLUŞTURMA KODU ---
          Zaten veritabanımızda admin kayıtlı olduğu için bu kodun çalışmasına gerek yok.
          EĞER yeni bir bilgisayara geçilirse veya veritabanı silinirse,
          aşağıdaki yorum satırlarını (slah ve yıldızları) kaldırıp kodu aktif etmelisiniz!
         */

        /*
        if (yoneticiRepository.count() == 0) {
            Yonetici yonetici = new Yonetici();
            yonetici.setKullaniciAdi("admin");
            yonetici.setSifre(sifreleyici.encode("123")); // Şifre: 123
            yonetici.setAd("Sistem");
            yonetici.setSoyad("Yöneticisi");
            yonetici.setEmail("admin@library.com");
            yonetici.setRol(Rol.ADMIN);
            yonetici.setOlusturulmaTarihi(LocalDateTime.now());

            yoneticiRepository.save(yonetici);
            System.out.println(" Varsayılan Admin oluşturuldu.");
        }
        */

    }
}