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
                .csrf(AbstractHttpConfigurer::disable) // Form guvenligini kapatiyoruz (API oldugu icin)
                .authorizeHttpRequests(auth -> auth
                        // 1. HERKESE ACIK OLAN YERLER (Login gerekmez)
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/dashboard.html", // HTML dosyasina erisime izin ver (Veriyi zaten API koruyor)
                                "/app.js",         // JavaScript dosyasina izin ver
                                "/api/auth/**",    // Giris ve Kayit endpointlerine izin ver
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // 2. DIGER TUM ISTEKLER ICIN GIRIS SART
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sunucuda oturum tutma (Token kullaniyoruz)
                )
                .authenticationProvider(kimlikSaglayici)
                .addFilterBefore(jwtFiltresi, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}