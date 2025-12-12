package com.example.KutupahaneOtomasyonu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Configuration: Spring Boot'a "Bu sınıf normal bir kod değil, bir AYAR dosyasıdır" diyoruz.
// Uygulama çalıştırıldığında Spring ilk olarak bu etikete sahip sınıfları okur.
@Configuration
public class ApplicationConfig {

    // @Bean: Bu metodun ürettiği nesneyi Spring'in "Alet Çantasına" (ApplicationContext) koyar.
    // Böylece projenin başka bir yerinde (mesela Service katmanında) "Bana şifreleyici ver" dediğimizde,
    // Spring gelip buradaki hazır nesneyi bize verir (Dependency Injection).
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder: Şifreleri "Hash"lemek (karmaşıklaştırmak) için kullanılan en popüler güvenlik aracıdır.
        // Görevi şudur: Kullanıcı "123456" şifresini girdiğinde, bunu veritabanına "$2a$10$EixZA..." gibi
        // geri döndürülemez karmaşık bir metin olarak kaydetmemizi sağlar.
        // Böylece veritabanı çalınsa bile saldırganlar kullanıcıların gerçek şifrelerini göremez.
        return new BCryptPasswordEncoder();
    }
}