package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Admin;
import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminDetailsService implements UserDetailsService {

    private final AdminService adminService;
    private final MemberRepository memberRepository; // Üyeleri de arayabilmek için

    @Autowired
    public AdminDetailsService(AdminService adminService, MemberRepository memberRepository) {
        this.adminService = adminService;
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Önce ADMIN tablosuna bak
        Optional<Admin> adminOptional = adminService.getByUsername(username);

        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            return User.builder()
                    .username(admin.getUsername())
                    .password(admin.getPassword())
                    .roles(admin.getRole().name())
                    .build();
        }

        // 2. Admin değilse ÜYE tablosuna bak
        Optional<Member> memberOptional = memberRepository.findByUsername(username);

        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            return User.builder()
                    .username(member.getUsername())
                    .password(member.getPassword())
                    .roles("MEMBER") // Rolünü MEMBER olarak belirle
                    .build();
        }

        // 3. İkisinde de yoksa hata fırlat
        throw new UsernameNotFoundException("Kullanıcı bulunamadı: " + username);
    }
}