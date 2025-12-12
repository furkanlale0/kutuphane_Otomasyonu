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

// @Entity ve @Table: Bu sınıfın veritabanındaki karşılığı "members" tablosudur.
@Entity
@Table(name = "members")
// @Data: Getter, Setter, toString metodlarını otomatik oluşturur.
@Data
@NoArgsConstructor
@AllArgsConstructor
// --- ÇOK ÖNEMLİ: UserDetails Arayüzü ---
// Spring Security'ye diyoruz ki: "Bu sınıf, senin güvenlik sistemine uyumlu bir kullanıcı modelidir."
// Bunu dediğimiz için aşağıda Spring'in zorunlu kıldığı bazı metodları (@Override) eklemek zorundayız.
public class Member implements UserDetails { // <-- ARTIK GÜVENLİK SİSTEMİNE UYUMLU

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Integer memberId;

    // unique = true: Veritabanında aynı kullanıcı adından bir tane daha olamaz.
    @Column(unique = true, nullable = false)
    private String username;

    private String name;
    private String surname;

    // unique = true: Aynı e-posta ile ikinci kez kayıt olunamaz.
    @Column(unique = true, nullable = false)
    private String email;

    // Şifre burada saklanır (Şifrelenmiş/Hashlenmiş olarak).
    @Column(nullable = false)
    private String password;

    private String phone;

    // Üyenin sisteme kayıt olduğu tarih.
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    // --- USERDETAILS İÇİN ZORUNLU METOTLAR ---
    // Spring Security soruyor: "Bu adamın yetkisi (rolü) nedir?"
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Biz de diyoruz ki: "Bu bir Üyedir, yetkisi 'MEMBER' olsun."
        // (Admin tablosunda bu farklıydı, burada sabit MEMBER döndürüyoruz).
        return List.of(new SimpleGrantedAuthority("MEMBER"));
    }

    // Hesap süresi doldu mu? (true = Hayır, süresi dolmadı, geçerli)
    @Override
    public boolean isAccountNonExpired() { return true; }

    // Hesap kilitli mi? (true = Hayır, kilitli değil, açık)
    @Override
    public boolean isAccountNonLocked() { return true; }

    // Şifrenin süresi doldu mu? (true = Hayır, hala geçerli)
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // Hesap aktif mi? (true = Evet, aktif)
    // Eğer ileride "Banlama" sistemi yaparsan burayı kontrol edebilirsin.
    @Override
    public boolean isEnabled() { return true; }
}