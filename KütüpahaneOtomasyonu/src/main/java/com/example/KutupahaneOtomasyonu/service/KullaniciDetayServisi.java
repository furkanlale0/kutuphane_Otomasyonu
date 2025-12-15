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

// @Service: Spring'e "Bu sinif is mantigi yurutur" diyoruz.
@Service
public class KullaniciDetayServisi implements UserDetailsService { // AdminDetailsService -> KullaniciDetayServisi

    private final YoneticiServisi yoneticiServisi;
    private final UyeServisi uyeServisi;

    @Autowired
    public KullaniciDetayServisi(YoneticiServisi yoneticiServisi, UyeServisi uyeServisi) {
        this.yoneticiServisi = yoneticiServisi;
        this.uyeServisi = uyeServisi;
    }

    // Spring Security login sirasinda bu metodu otomatik cagirir.
    // Parametre olan 'girilenBilgi': Giris ekranindaki kutuya yazilan seydir (Admin icin "admin", Uye icin "ali@gmail.com").
    @Override
    public UserDetails loadUserByUsername(String girilenBilgi) throws UsernameNotFoundException {

        // 1. ADIM: ONCE YONETICI TABLOSUNA BAK
        // Girilen bilgiyi "Kullanici Adi" kabul edip ariyoruz.
        Optional<Yonetici> yoneticiKutusu = yoneticiServisi.kullaniciAdiIleGetir(girilenBilgi);

        if (yoneticiKutusu.isPresent()) {
            Yonetici yonetici = yoneticiKutusu.get();
            // Spring Security'nin anlayacagi dilde bir User nesnesi olusturup donuyoruz.
            return User.builder()
                    .username(yonetici.getKullaniciAdi())
                    .password(yonetici.getSifre())
                    .roles(yonetici.getRol().name()) // Rol: SUPERADMIN veya PERSONEL
                    .build();
        }

        // 2. ADIM: YONETICI DEGILSE, UYE TABLOSUNA BAK
        // Yonetici degilse, girilen bilgi muhtemelen bir "E-posta" adresidir.
        // Uye tablosunda "Email" ile arama yapiyoruz.
        Optional<Uye> uyeKutusu = uyeServisi.emailIleGetir(girilenBilgi);

        if (uyeKutusu.isPresent()) {
            Uye uye = uyeKutusu.get();
            return User.builder()
                    // Spring Security arka planda "username" ister, biz ona email veriyoruz.
                    .username(uye.getEmail())
                    .password(uye.getSifre())
                    .roles("UYE") // Rol: Sabit UYE
                    .build();
        }

        // 3. ADIM: KIMSE BULUNAMADI
        throw new UsernameNotFoundException("Kullanici bulunamadi: " + girilenBilgi);
    }
}