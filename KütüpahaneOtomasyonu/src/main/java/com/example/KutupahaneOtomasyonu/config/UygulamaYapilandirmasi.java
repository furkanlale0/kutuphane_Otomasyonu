package com.example.KutupahaneOtomasyonu.config;

import com.example.KutupahaneOtomasyonu.service.KullaniciDetayServisi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
 * BU SINIF NE İŞE YARAR?
 * Uygulamanın "Kimlik Doğrulama" (Authentication) altyapısını kurar.
 * Spring Security'ye; kullanıcı verilerinin hangi servisten çekileceğini ve
 * şifrelerin hangi yöntemle kontrol edileceğini söyler.
 */
@Configuration
public class UygulamaYapilandirmasi {

    private final KullaniciDetayServisi kullaniciDetayServisi;
    private final PasswordEncoder passwordEncoder;

    /*
     * Bağımlılık Enjeksiyonu (Dependency Injection)
     * Kullanıcı detay servisini ve (başka dosyada tanımladığımız) şifreleyiciyi buraya alıyoruz.
     */
    public UygulamaYapilandirmasi(KullaniciDetayServisi kullaniciDetayServisi, PasswordEncoder passwordEncoder) {
        this.kullaniciDetayServisi = kullaniciDetayServisi;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * KİMLİK DOĞRULAMA SAĞLAYICISI (AuthenticationProvider)
     * Veritabanındaki kullanıcı bilgisi ile giriş ekranından gelen şifreyi
     * karşılaştıran mekanizmadır. Köprü görevi görür.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(kullaniciDetayServisi);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /*
     * KİMLİK YÖNETİCİSİ (AuthenticationManager)
     * Login Controller'da "Kullanıcı adı ve şifreyi al, kontrol et" komutunu
     * çalıştırmamızı sağlayan ana yöneticidir.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}