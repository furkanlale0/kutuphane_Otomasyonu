package com.example.KutupahaneOtomasyonu.config;

import com.example.KutupahaneOtomasyonu.entity.Admin;
import com.example.KutupahaneOtomasyonu.entity.Author;
import com.example.KutupahaneOtomasyonu.repository.AdminRepository;
import com.example.KutupahaneOtomasyonu.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final AuthorRepository authorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(AdminRepository adminRepository, AuthorRepository authorRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.authorRepository = authorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. ADMIN KONTROLÜ VE GÜNCELLEME
        Optional<Admin> existingAdmin = adminRepository.findByUsername("admin");
        if (existingAdmin.isPresent()) {
            Admin admin = existingAdmin.get();
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Admin.Role.SUPERADMIN);
            adminRepository.save(admin);
            System.out.println("--- ADMIN GÜNCELLENDİ (SUPERADMIN) ---");
        } else {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("Sistem");
            admin.setSurname("Yöneticisi");
            admin.setEmail("admin@library.com");
            admin.setRole(Admin.Role.SUPERADMIN);
            adminRepository.save(admin);
            System.out.println("--- YENİ SÜPER ADMIN OLUŞTURULDU ---");
        }

        // 2. ÖRNEK YAZARLAR (Listede görünsün diye)
        if (authorRepository.count() == 0) {
            Author a1 = new Author(null, "Ömer", "Seyfettin", 1884, null);
            authorRepository.save(a1);
            
            Author a2 = new Author(null, "Victor", "Hugo", 1802, null);
            authorRepository.save(a2);
            
            System.out.println("--- ÖRNEK YAZARLAR OLUŞTURULDU ---");
        }
    }
}