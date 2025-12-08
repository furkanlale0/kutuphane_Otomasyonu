package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Admin;
import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.repository.AdminRepository;
import com.example.KutupahaneOtomasyonu.repository.MemberRepository;
import com.example.KutupahaneOtomasyonu.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthController(AdminRepository adminRepository, MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // --- GİRİŞ YAPMA (LOGIN) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. ADMIN KONTROLÜ
        Optional<Admin> admin = adminRepository.findByUsername(request.getUsername());
        if (admin.isPresent() && passwordEncoder.matches(request.getPassword(), admin.get().getPassword())) {
            String token = jwtService.generateToken(admin.get());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", "ADMIN");
            response.put("id", admin.get().getAdminId());
            return ResponseEntity.ok(response);
        }

        // 2. ÜYE KONTROLÜ (USERNAME İLE)
        Optional<Member> member = memberRepository.findByUsername(request.getUsername());

        // Eğer username ile bulunamazsa EMAIL ile dene
        if (member.isEmpty()) {
            member = memberRepository.findByEmail(request.getUsername());
        }

        if (member.isPresent() && passwordEncoder.matches(request.getPassword(), member.get().getPassword())) {
            // --- DÜZELTME BURADA ---
            // Üye için de geçerli bir JWT Token üretmemiz lazım.
            // JwtService genelde UserDetails ister. Member sınıfımız UserDetails değil.
            // Bu yüzden geçici bir "Admin" nesnesi gibi davranıp token alıyoruz.

            Admin tempUser = new Admin();
            tempUser.setUsername(member.get().getUsername());
            tempUser.setRole(Admin.Role.STAFF); // Yetkisi düşük olsun

            // Gerçek JWT üret
            String token = jwtService.generateToken(tempUser);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", "MEMBER");
            response.put("id", member.get().getMemberId());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body("Kullanıcı adı veya şifre hatalı");
    }

    // --- YENİ ÜYE KAYDI ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member) {
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Bu email zaten kayıtlı!");
        }
        if (memberRepository.findByUsername(member.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Bu kullanıcı adı alınmış!");
        }

        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setRegistrationDate(java.time.LocalDateTime.now());

        memberRepository.save(member);
        return ResponseEntity.ok("Üye kaydı başarılı! Giriş yapabilirsiniz.");
    }
}

class LoginRequest {
    private String username;
    private String password;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}