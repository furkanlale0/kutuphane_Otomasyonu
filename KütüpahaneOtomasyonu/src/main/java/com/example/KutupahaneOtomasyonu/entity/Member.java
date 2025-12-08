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

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member implements UserDetails { // <-- ARTIK GÜVENLİK SİSTEMİNE UYUMLU

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Integer memberId;

    @Column(unique = true, nullable = false)
    private String username;

    private String name;
    private String surname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    // --- USERDETAILS ZORUNLU METOTLARI ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Üyelerin rolü sabit olarak "MEMBER" olsun
        return List.of(new SimpleGrantedAuthority("MEMBER"));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}