package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Admin;
import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.service.AdminService;
import com.example.KutupahaneOtomasyonu.service.JwtService;
import com.example.KutupahaneOtomasyonu.service.MemberService; // YENİ
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// @RestController: Bu sınıfın bir web denetleyicisi olduğunu ve JSON cevabı döndüreceğini belirtir.
// @RequestMapping: Tarayıcıdan "/api/auth" adresine gelen tüm istekleri bu sınıf karşılar.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Giriş işlemleri için gerekli servisleri çağırıyoruz.
    private final AdminService adminService;   // Adminleri bulmak için
    private final MemberService memberService; // Üyeleri bulmak ve kaydetmek için
    private final PasswordEncoder passwordEncoder; // Şifreleri kontrol etmek (eşleştirmek) için
    private final JwtService jwtService; // Token üretmek için

    @Autowired
    public AuthController(AdminService adminService, MemberService memberService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminService = adminService;
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // --- KAYIT OLMA (REGISTER) ---
    // Kullanıcı form doldurup "Kayıt Ol"a basınca burası çalışır.
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member) {
        // Tüm zor işi MemberService'e yaptırıyoruz (Şifreleme, kontrol vs.)
        String result = memberService.registerMember(member);

        // Eğer servis "OK" dediyse kayıt başarılıdır.
        if ("OK".equals(result)) {
            return ResponseEntity.ok("Kayıt başarılı.");
        }
        // Değilse hatayı (örn: "Bu kullanıcı adı dolu") ekrana basar.
        return ResponseEntity.badRequest().body(result);
    }

    // --- GİRİŞ YAPMA (LOGIN) ---
    // En kritik metod burasıdır. Hem Admin hem Üye girişini yönetir.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // 1. ÖNCE ADMIN TABLOSUNA BAK
        // Girilen kullanıcı adı Admin tablosunda var mı?
        Optional<Admin> adminOptional = adminService.getByUsername(request.getUsername());

        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            // Şifre doğru mu? (matches metodu: Girilen "123" ile veritabanındaki "$2a$..."yı kıyaslar)
            if (passwordEncoder.matches(request.getPassword(), admin.getPassword())) {

                // Admin bulundu ve şifre doğru!
                // Spring Security için geçici bir kullanıcı nesnesi oluşturuyoruz.
                UserDetails adminDetails = User.builder()
                        .username(admin.getUsername())
                        .password(admin.getPassword())
                        .roles(admin.getRole().name()) // Rolünü (ADMIN/SUPERADMIN) ekle
                        .build();

                // Ona özel token üretip gönderiyoruz.
                return createTokenResponse(adminDetails, "ADMIN", admin.getAdminId());
            }
        }

        // 2. ADMIN DEĞİLSE, ÜYE TABLOSUNA BAK
        // Girilen kullanıcı adı Member tablosunda var mı?
        Optional<Member> memberOptional = memberService.findByUsername(request.getUsername());

        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            // Üyenin şifresi doğru mu?
            if (passwordEncoder.matches(request.getPassword(), member.getPassword())) {

                // Üye bulundu ve şifre doğru!
                UserDetails memberDetails = User.builder()
                        .username(member.getUsername())
                        .password(member.getPassword())
                        .roles("MEMBER") // Rolü sabit MEMBER
                        .build();

                // Ona özel token üretip gönderiyoruz.
                return createTokenResponse(memberDetails, "MEMBER", member.getMemberId());
            }
        }

        // 3. HİÇBİRİ DEĞİLSE (veya şifre yanlışsa)
        return ResponseEntity.status(401).body("Giriş başarısız.");
    }

    // --- YARDIMCI METOD: TOKEN PAKETLEME ---
    // Token oluşturup, yanına rol ve ID bilgisini koyup paketleyen küçük yardımcı.
    private ResponseEntity<?> createTokenResponse(UserDetails user, String role, Integer id) {
        // JwtService'i çağır ve Token üret
        String token = jwtService.generateToken(user);

        // Bilgileri bir kutuya (Map) koy: Token, Rol, ID
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", role);       // Frontend buna bakarak Admin/Üye paneli açacak
        response.put("id", id.toString()); // Profil bilgileri için ID lazım olacak

        return ResponseEntity.ok(response);
    }
}

// Basit bir veri taşıyıcı (DTO).
// Login isteğinden gelen "username" ve "password" bilgisini tutar.
class LoginRequest {
    private String username; private String password;
    public String getUsername() { return username; } void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; } void setPassword(String password) { this.password = password; }
}