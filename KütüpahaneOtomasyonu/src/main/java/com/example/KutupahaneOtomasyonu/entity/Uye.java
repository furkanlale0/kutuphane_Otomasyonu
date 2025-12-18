package com.example.KutupahaneOtomasyonu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/*
 * BU SINIF NE İŞE YARAR?
 * Kütüphane üyelerinin veritabanı karşılığıdır ("Uyeler" tablosu).
 *
 * KRİTİK ÖZELLİK: 'UserDetails' Arayüzü
 * Bu sınıf sadece veri tutmaz, aynı zamanda Spring Security'nin
 * kimlik doğrulama sistemine entegre çalışır. Login işlemlerinde
 * kullanıcı doğrulaması bu sınıf üzerinden yapılır.
 */
@Entity
@Table(name = "Uyeler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Uye implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uye_id")
    private Integer uyeId;

    private String ad;

    private String soyad;

    /*
     * KULLANICI ADI YERİNE EMAIL
     * Sistemde giriş yaparken kullanıcı adı yerine E-posta adresi kullanılır.
     * Bu yüzden unique (benzersiz) olması zorunludur.
     */
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String sifre;

    private String telefon;

    @Column(name = "kayit_tarihi")
    private LocalDateTime kayitTarihi;

    // --- SPRING SECURITY ZORUNLU METODLARI ---

    /*
     * YETKİ TANIMLAMA
     * Sisteme giren bu kişinin rolü nedir?
     * Bu sınıf normal üyeleri temsil ettiği için sabit olarak "UYE" yetkisi verilir.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("UYE"));
    }

    /*
     * KİMLİK BİLGİSİ
     * Spring Security "Username" istediğinde biz Email adresini döneriz.
     */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return sifre;
    }

    /*
     * HESAP DURUM KONTROLLERİ
     * Hesabın süresi doldu mu? Kilitli mi? Şifre süresi bitti mi?
     * Şimdilik basitlik adına hepsine "true" (Sorun Yok/Aktif) dönüyoruz.
     */
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}