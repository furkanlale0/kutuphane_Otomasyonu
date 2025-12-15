package com.example.KutupahaneOtomasyonu;

import com.example.KutupahaneOtomasyonu.entity.Rol;
import com.example.KutupahaneOtomasyonu.entity.Yonetici;
import com.example.KutupahaneOtomasyonu.repository.YoneticiRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling // Zamanlayıcıyı (Gecikme Takip) aktif eder
public class KutupahaneOtomasyonuApplication {

    public static void main(String[] args) {
        SpringApplication.run(KutupahaneOtomasyonuApplication.class, args);
    }

    // PROGRAM ILK CALISTIGINDA BU METOD DEVREYE GIRER
    @Bean
    CommandLineRunner baslangicVerileri(YoneticiRepository yoneticiRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Eger veritabaninda hic yonetici yoksa, varsayilan bir tane olustur.
            if (yoneticiRepository.count() == 0) {
                Yonetici admin = new Yonetici();
                admin.setKullaniciAdi("admin");
                // Sifreyi "1234" olarak ayarla ama veritabanina sifreli kaydet
                admin.setSifre(passwordEncoder.encode("1234"));
                admin.setRol(Rol.ADMIN);

                yoneticiRepository.save(admin);
                System.out.println("✅ Varsayılan Admin oluşturuldu -> Kullanıcı Adı: admin | Şifre: 1234");
            }
        };
    }
}