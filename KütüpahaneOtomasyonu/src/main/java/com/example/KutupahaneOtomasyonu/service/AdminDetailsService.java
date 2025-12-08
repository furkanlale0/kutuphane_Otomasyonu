package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Admin;
import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.repository.AdminRepository;
import com.example.KutupahaneOtomasyonu.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public AdminDetailsService(AdminRepository adminRepository, MemberRepository memberRepository) {
        this.adminRepository = adminRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Önce Admin tablosuna bak
        Optional<Admin> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            return admin.get();
        }

        // 2. Bulamazsan Üye tablosuna bak (Username ile)
        Optional<Member> member = memberRepository.findByUsername(username);
        if (member.isPresent()) {
            return member.get();
        }

        // 3. Hala bulamazsan Email ile Üye tablosuna bak (Belki email ile giriş yapmıştır)
        Optional<Member> memberByEmail = memberRepository.findByEmail(username);
        if (memberByEmail.isPresent()) {
            return memberByEmail.get();
        }

        throw new UsernameNotFoundException("Kullanıcı bulunamadı: " + username);
    }
}