package com.example.KutupahaneOtomasyonu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SifrelemeYapilandirmasi {

    //bu sınıf şifreleri hashler yani kullanıcı 1234 gibi kolay bir şifre girse bile veri tabanına karmaşık şekilde kaydeder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}