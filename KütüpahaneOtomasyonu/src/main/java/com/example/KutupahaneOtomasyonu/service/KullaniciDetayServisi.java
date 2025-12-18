package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Yonetici;
import com.example.KutupahaneOtomasyonu.entity.Uye;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/*
 * BU SINIF NE İŞE YARAR?
 * Spring Security entegrasyonu için özelleştirilmiş "Kullanıcı Detay Servisi"dir.
 * Standart Spring giriş yapısını, kendi veritabanı yapımıza (Yönetici ve Üye tabloları)
 * uyarlamak için kullanılır (Adapter Pattern).
 *
 * GÖREVİ:
 * Login ekranından gelen veriyi alır, önce Yönetici tablosunda, bulamazsa Üye tablosunda arar.
 */
@Service
public class KullaniciDetayServisi implements UserDetailsService {

    private final YoneticiServisi yoneticiServisi;
    private final UyeServisi uyeServisi;

    @Autowired
    public KullaniciDetayServisi(YoneticiServisi yoneticiServisi, UyeServisi uyeServisi) {
        this.yoneticiServisi = yoneticiServisi;
        this.uyeServisi = uyeServisi;
    }

    /*
     * KULLANICI YÜKLEME METODU
     * Spring Security, login işlemi tetiklendiğinde otomatik olarak bu metodu çağırır.
     *
     * @param girilenBilgi: Login formuna yazılan Kullanıcı Adı veya E-posta.
     * @return UserDetails: Spring'in anlayacağı formatta hazırlanmış kullanıcı nesnesi.
     */
    @Override
    public UserDetails loadUserByUsername(String girilenBilgi) throws UsernameNotFoundException {

        // 1. ÖNCELİKLİ KONTROL: YÖNETİCİ GİRİŞİ
        // Girilen bilginin "Kullanıcı Adı" olduğunu varsayarak Yönetici tablosunu sorgular.
        Optional<Yonetici> yoneticiKutusu = yoneticiServisi.kullaniciAdiIleGetir(girilenBilgi);

        if (yoneticiKutusu.isPresent()) {
            Yonetici yonetici = yoneticiKutusu.get();
            // Veritabanındaki yöneticiyi, Spring Security User nesnesine dönüştürür.
            return User.builder()
                    .username(yonetici.getKullaniciAdi())
                    .password(yonetici.getSifre())
                    .roles(yonetici.getRol().name()) // Rol: ADMIN veya PERSONEL
                    .build();
        }

        // 2. İKİNCİL KONTROL: ÜYE GİRİŞİ
        // Yönetici bulunamadıysa, girilen bilginin "E-posta" olduğunu varsayarak Üye tablosunu sorgular.
        Optional<Uye> uyeKutusu = uyeServisi.emailIleGetir(girilenBilgi);

        if (uyeKutusu.isPresent()) {
            Uye uye = uyeKutusu.get();
            return User.builder()
                    .username(uye.getEmail()) // Üyeler için E-posta, kullanıcı adı yerine geçer.
                    .password(uye.getSifre())
                    .roles("UYE") // Standart üye rolü atanır.
                    .build();
        }

        // 3. HATA DURUMU
        // Ne yönetici ne de üye tablosunda eşleşme bulunamazsa hata fırlatılır.
        throw new UsernameNotFoundException("Kullanıcı bulunamadı: " + girilenBilgi);
    }
}