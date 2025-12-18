package com.example.KutupahaneOtomasyonu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class GuvenlikYapilandirmasi {

    private final JwtKimlikDogrulamaFiltresi jwtFiltresi;
    private final AuthenticationProvider kimlikSaglayici;

    public GuvenlikYapilandirmasi(JwtKimlikDogrulamaFiltresi jwtFiltresi, AuthenticationProvider kimlikSaglayici) {
        this.jwtFiltresi = jwtFiltresi;
        this.kimlikSaglayici = kimlikSaglayici;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. HERKESE ACIK OLANLAR
                        .requestMatchers(
                                "/", "/index.html", "/dashboard.html", "/app.js",
                                "/api/auth/**",  // Giris ve Kayit
                                "/api/kitaplar"  // Kitap listeleme (GET) herkese acik olsun dersen
                        ).permitAll()

                        // 2. SADECE UYE VE ADMINLER (Giris yapmis herkes)
                        // HATA BURADAYDI: /api/loans yerine /api/odunc yazdik!
                        .requestMatchers("/api/odunc/**").authenticated()
                        .requestMatchers("/api/uyeler/**").authenticated()

                        // 3. ADMIN OZEL ISLEMLERI (Kitap silme/ekleme)
                        .requestMatchers("/api/kitaplar/**").authenticated() // Detayli yetki sonra ayarlanabilir

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(kimlikSaglayici)
                .addFilterBefore(jwtFiltresi, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}