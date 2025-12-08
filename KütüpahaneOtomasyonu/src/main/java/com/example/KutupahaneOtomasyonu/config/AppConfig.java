package com.example.KutupahaneOtomasyonu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    // Şifreleme aletini (Bean) burada oluşturuyoruz.
    // Artık hem SecurityConfig hem de DataSeeder bunu kullanabilir.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}