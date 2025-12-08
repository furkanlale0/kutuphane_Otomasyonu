package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin implements UserDetails { // <-- ARTIK USERDETAILS UYUMLU!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Integer adminId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String name;
    private String surname;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "addedByAdmin", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Book> addedBooks;

    public enum Role {
        SUPERADMIN, STAFF
    }

    // --- USERDETAILS ZORUNLU METOTLARI ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Rolü Spring Security'nin anlayacağı dile çeviriyoruz
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Hesap süresi dolmadı
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Hesap kilitli değil
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Şifre süresi dolmadı
    }

    @Override
    public boolean isEnabled() {
        return true; // Hesap aktif
    }
}