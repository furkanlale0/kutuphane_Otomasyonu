package com.example.KutupahaneOtomasyonu.config;

import com.example.KutupahaneOtomasyonu.service.KullaniciDetayServisi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UygulamaYapilandirmasi {

    private final KullaniciDetayServisi kullaniciDetayServisi;
    private final PasswordEncoder passwordEncoder; // Yeni dosyadan otomatik gelecek

    // Constructor Injection ile PasswordEncoder'i istiyoruz
    public UygulamaYapilandirmasi(KullaniciDetayServisi kullaniciDetayServisi, PasswordEncoder passwordEncoder) {
        this.kullaniciDetayServisi = kullaniciDetayServisi;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(kullaniciDetayServisi);
        // Artik passwordEncoder'i parametre olarak aldigimiz degiskenden kullaniyoruz
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // NOT: passwordEncoder() bean tanimini buradan kaldirdik!
}