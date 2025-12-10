package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Admin;
import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.entity.Role;
import com.example.KutupahaneOtomasyonu.repository.MemberRepository;
import com.example.KutupahaneOtomasyonu.service.AdminService;
import com.example.KutupahaneOtomasyonu.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminService adminService; // Artık Service kullanıyoruz
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthController(AdminService adminService, MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminService = adminService;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member) {
        if (memberRepository.findByUsername(member.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Kullanıcı adı zaten var.");
        }
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setRegistrationDate(LocalDateTime.now());
        memberRepository.save(member);
        return ResponseEntity.ok("Kayıt başarılı.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. ADMIN GİRİŞ KONTROLÜ
        Optional<Admin> adminOptional = adminService.getByUsername(request.getUsername());

        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            if (passwordEncoder.matches(request.getPassword(), admin.getPassword())) {

                // --- İŞTE DÜZELTME BURADA ---
                // Admin nesnesini, Token üreticisinin anlayacağı UserDetails formatına çeviriyoruz.
                UserDetails adminDetails = User.builder()
                        .username(admin.getUsername())
                        .password(admin.getPassword())
                        .roles(admin.getRole().name())
                        .build();

                String token = jwtService.generateToken(adminDetails);

                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("role", "ADMIN");
                response.put("id", admin.getAdminId().toString());
                return ResponseEntity.ok(response);
            }
        }

        // 2. ÜYE GİRİŞ KONTROLÜ
        Optional<Member> memberOptional = memberRepository.findByUsername(request.getUsername());

        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            if (passwordEncoder.matches(request.getPassword(), member.getPassword())) {

                // Üyeyi de UserDetails'e çeviriyoruz
                UserDetails memberDetails = User.builder()
                        .username(member.getUsername())
                        .password(member.getPassword())
                        .roles("MEMBER")
                        .build();

                String token = jwtService.generateToken(memberDetails);

                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("role", "MEMBER");
                response.put("id", member.getMemberId().toString());
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(401).body("Giriş başarısız. Kullanıcı adı veya şifre hatalı.");
    }
}

class LoginRequest {
    private String username;
    private String password;
    // Getter-Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}