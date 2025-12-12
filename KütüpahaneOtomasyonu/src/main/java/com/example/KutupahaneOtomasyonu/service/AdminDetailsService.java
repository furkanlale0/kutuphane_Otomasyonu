package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Admin;
import com.example.KutupahaneOtomasyonu.entity.Member;
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
    private final MemberService memberService; // ARTIK REPOSITORY YOK, SERVICE VAR

    @Autowired
    public AdminDetailsService(AdminService adminService, MemberService memberService) {
        this.adminService = adminService;
        this.memberService = memberService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Önce ADMIN servisine sor
        Optional<Admin> adminOptional = adminService.getByUsername(username);

        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            return User.builder()
                    .username(admin.getUsername())
                    .password(admin.getPassword())
                    .roles(admin.getRole().name())
                    .build();
        }

        // 2. Yoksa ÜYE servisine sor (Repository yerine Service kullandık)
        Optional<Member> memberOptional = memberService.findByUsername(username);

        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            return User.builder()
                    .username(member.getUsername())
                    .password(member.getPassword())
                    .roles("MEMBER")
                    .build();
        }

        throw new UsernameNotFoundException("Kullanıcı bulunamadı: " + username);
    }
}