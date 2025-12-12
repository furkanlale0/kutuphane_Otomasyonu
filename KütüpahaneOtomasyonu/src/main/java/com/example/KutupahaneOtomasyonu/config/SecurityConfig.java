package com.example.KutupahaneOtomasyonu.config;

import com.example.KutupahaneOtomasyonu.service.AdminDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration: Spring'e "Bu bir ayar dosyasıdır" diyoruz.
// @EnableWebSecurity: Spring'in kendi varsayılan basit güvenliğini kapatıp,
// bizim aşağıda yazdığımız özel kuralları devreye sokar.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // Bizim yazdığımız Token kontrol filtresi
    private final AdminDetailsService adminDetailsService; // Kullanıcıları veritabanından bulan servis

    // Constructor Injection: Gerekli parçaları Spring'den istiyoruz.
    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AdminDetailsService adminDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.adminDetailsService = adminDetailsService;
    }

    // --- EN ÖNEMLİ KISIM: GÜVENLİK ZİNCİRİ ---
    // HttpSecurity: Web güvenliğini yapılandırdığımız ana nesne.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF (Cross-Site Request Forgery): Tarayıcı oturumlarına saldırı tipidir.
                // Biz JWT (Token) kullandığımız için ve Token'lar her istekte elle taşındığı için
                // bu korumaya ihtiyacımız yok. Kapatıyoruz (disable) ki POST istekleri engellenmesin.
                .csrf(AbstractHttpConfigurer::disable)

                // --- URL ERİŞİM KURALLARI (Kim nereye girebilir?) ---
                .authorizeHttpRequests(auth -> auth
                        // 1. HERKESE AÇIK OLANLAR (PermitAll)
                        // Ana sayfa, javascript dosyaları, giriş ve kayıt olma ekranları şifresiz erişilebilir olmalı.
                        .requestMatchers("/", "/index.html", "/register.html", "/app.js", "/dashboard.html", "/api/auth/**").permitAll()

                        // 2. KİTAPLARI GÖRMEK HERKESE AÇIK
                        // Sadece GET isteği (Veri okuma) herkese açıktır. Üye olmayan da kitap listesine bakabilir.
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()

                        // 3. ADMİN İŞLERİ (Sadece yetkililer)
                        // Kitap EKLEME (POST) ve SİLME (DELETE) işlemlerini sadece ADMIN veya SUPERADMIN yapabilir.
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // Admin özel paneli ve işlemleri için de yetki şart.
                        .requestMatchers("/api/loans/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")

                        // 4. ÖDÜNÇ ALMA / ÜYE İŞLEMLERİ
                        // "authenticated()": Rolü ne olursa olsun, sisteme giriş yapmış (Token'ı olan) herkes girebilir.
                        .requestMatchers("/api/loans/**").authenticated()
                        .requestMatchers("/api/members/**").authenticated()

                        // 5. GERİ KALAN HER ŞEY
                        // Yukarıda saymadığımız herhangi bir sayfa varsa, oraya girmek için kesinlikle giriş yapılmış olmalı.
                        .anyRequest().authenticated()
                )

                // --- OTURUM YÖNETİMİ (Session Management) ---
                // SessionCreationPolicy.STATELESS: Bu çok kritik!
                // Sunucuya diyoruz ki: "Kullanıcıyı hafızanda tutma (Session açma)."
                // Çünkü biz REST API yapıyoruz. Her istekte kimlik kartını (Token) tekrar gösterecekler.
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Kimlik doğrulama sağlayıcısını (Provider) tanımlıyoruz (Aşağıdaki metod).
                .authenticationProvider(authenticationProvider(null))

                // --- FİLTRE EKLEME ---
                // Standart "Kullanıcı Adı / Şifre" kontrolünden ÖNCE, bizim yazdığımız "JWT Token Kontrolü"nü çalıştır.
                // Önce Token'a bak, eğer Token geçerliyse şifre sormana gerek kalmaz.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- KİMLİK DOĞRULAYICI (Provider) ---
    // Bu metod, veritabanındaki kullanıcı ile girilen şifreyi kıyaslayan mekanizmadır.
    // PasswordEncoder parametresi ApplicationConfig dosyasından gelir.
    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 1. Kullanıcıyı nerede arayayım? -> adminDetailsService (Veritabanı)
        authProvider.setUserDetailsService(adminDetailsService);
        // 2. Şifreyi nasıl kıyaslayayım? -> passwordEncoder (BCrypt)
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // --- KİMLİK DOĞRULAMA YÖNETİCİSİ (Manager) ---
    // Login işleminde "authenticate" metodunu çağıran ana yöneticidir.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}