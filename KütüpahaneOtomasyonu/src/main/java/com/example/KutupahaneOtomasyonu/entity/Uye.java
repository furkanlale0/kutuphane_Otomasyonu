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

// @Entity ve @Table: Bu sinifin veritabanindaki karsiligi "Uyeler" tablosudur.
@Entity
@Table(name = "Uyeler")
// @Data: Getter, Setter, toString metodlarini otomatik olusturur.
@Data
@NoArgsConstructor
@AllArgsConstructor
// --- COK ONEMLI: UserDetails Arayuzu ---
// Spring Security'ye diyoruz ki: "Bu sinif, senin guvenlik sistemine uyumlu bir kullanici modelidir."
// Bunu dedigimiz icin asagida Spring'in zorunlu kildigi bazi metodlari (@Override) eklemek zorundayiz.
public class Uye implements UserDetails { // Member -> Uye

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uye_id")
    private Integer uyeId;

    // Veritabanindaki 'ad' sutunu
    private String ad;

    // Veritabanindaki 'soyad' sutunu
    private String soyad;

    // Veritabanindaki 'email' sutunu (Benzersizdir)
    // SQL tablonda "kullanici_adi" sutunu olmadigi icin, giris yaparken Email kullanacagiz.
    @Column(nullable = false, unique = true)
    private String email;

    // Sifre burada saklanir (Sifrelenmis/Hashlenmis olarak).
    @Column(nullable = false)
    private String sifre;

    // Veritabanindaki 'telefon' sutunu
    private String telefon;

    // Uyenin sisteme kayit oldugu tarih.
    @Column(name = "kayit_tarihi")
    private LocalDateTime kayitTarihi;

    // --- USERDETAILS ICIN ZORUNLU METOTLAR ---
    // Spring Security soruyor: "Bu adamin kullanici adi (username) nedir?"
    @Override
    public String getUsername() {
        // Bizim tabloda ayri bir "username" yok, email'i kullanici adi sayiyoruz.
        return email;
    }

    // Spring Security soruyor: "Sifresi nedir?"
    @Override
    public String getPassword() {
        return sifre;
    }

    // Spring Security soruyor: "Yetkisi (rolu) nedir?"
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Biz de diyoruz ki: "Bu bir Uyedir, yetkisi 'UYE' olsun."
        return List.of(new SimpleGrantedAuthority("UYE"));
    }

    // Hesap suresi doldu mu? (true = Hayir, suresi dolmadi, gecerli)
    @Override
    public boolean isAccountNonExpired() { return true; }

    // Hesap kilitli mi? (true = Hayir, kilitli degil, acik)
    @Override
    public boolean isAccountNonLocked() { return true; }

    // Sifrenin suresi doldu mu? (true = Hayir, hala gecerli)
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // Hesap aktif mi? (true = Evet, aktif)
    @Override
    public boolean isEnabled() { return true; }
}