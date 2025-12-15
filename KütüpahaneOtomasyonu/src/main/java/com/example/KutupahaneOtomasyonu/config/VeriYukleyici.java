package com.example.KutupahaneOtomasyonu.config;

import com.example.KutupahaneOtomasyonu.entity.*;
import com.example.KutupahaneOtomasyonu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

// @Component: Spring Boot'a "Bu sinifi yonet, bu sistemin calisan bir parcasidir" diyoruz.
// CommandLineRunner: Uygulama "Baslat" tusuna basildigi an devreye giren ozel bir arayuzdur.
// Icerisindeki 'run' metodu uygulama ayaga kalkar kalkmaz otomatik calisir.
@Component
public class VeriYukleyici implements CommandLineRunner { // DataSeeder -> VeriYukleyici

    // Veritabani tablolariyla konusmak icin gerekli "Depo" (Repository) araclarini cagiriyoruz.
    // Yeni veritabani yapimiza uygun repository isimlerini kullaniyoruz.
    @Autowired private YoneticiRepository yoneticiRepository;
    @Autowired private UyeRepository uyeRepository;
    @Autowired private YazarRepository yazarRepository;
    @Autowired private KitapRepository kitapRepository;
    @Autowired private OduncRepository oduncRepository;

    // Sifreleri veritabanina "123" diye duz yazmamak, guvenli sekilde kaydetmek icin gerekli arac.
    @Autowired private PasswordEncoder sifreleyici;

    @Override
    public void run(String... args) throws Exception {

        // 1. YONETICI (ADMIN) EKLEME KONTROLU
        // count() == 0 kontrolu HAYAT KURTARIR.
        // Eger bunu yapmazsak, uygulamayi her durdurup baslattiginda tekrar tekrar ayni admini eklemeye calisir ve hata verir.
        if (yoneticiRepository.count() == 0) {
            Yonetici yonetici = new Yonetici();
            yonetici.setKullaniciAdi("admin");

            // encode("123"): Sifreyi veritabanina "$2a$10$..." gibi anlasilmaz bir formatta kaydeder.
            // Sunumda: "Hocam guvenlik geregi sifreleri acik metin olarak tutmuyoruz" diyebilirsin.
            yonetici.setSifre(sifreleyici.encode("123"));

            yonetici.setAd("Süper");
            yonetici.setSoyad("Yönetici");
            yonetici.setEmail("admin@library.com");
            yonetici.setRol(Rol.ADMIN); // En yetkili rol (Rol enum'ini daha sonra olusturacagiz)
            yonetici.setOlusturulmaTarihi(LocalDateTime.now());

            yoneticiRepository.save(yonetici); // Veritabanina kaydet.
            System.out.println("✅ Baslangic Yoneticisi olusturuldu: admin / 123");
        }

        // 2. YAZAR VE KITAP EKLEME (Eger hic yazar yoksa ornek veri ekle)
        if (yazarRepository.count() == 0) {

            // Once yazari olusturuyoruz cunku kitap yazara baglidir.
            Yazar yazar = new Yazar();
            yazar.setAd("J.K.");
            yazar.setSoyad("Rowling");
            yazar.setDogumYili(1965);
            yazarRepository.save(yazar); // Yazari kaydet.

            // 3. KITAP EKLE
            Kitap kitap = new Kitap();
            kitap.setKitapAdi("Harry Potter ve Felsefe Taşı");
            kitap.setIsbn("978-3-16-148410-0");
            kitap.setStokSayisi(10);
            kitap.setYayinYili(1997);
            kitap.setOzet("Büyücülük okuluna giden bir çocuğun hikayesi.");

            // Iliski kurma: Bu kitabin yazari yukaridaki 'yazar'dir diyoruz.
            kitap.setYazar(yazar);

            kitapRepository.save(kitap); // Kitabi kaydet.
            System.out.println("✅ Ornek Kitap ve Yazar eklendi.");
        }
    }
}