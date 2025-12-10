package com.example.KutupahaneOtomasyonu.config;
//fweıufuw
import com.example.KutupahaneOtomasyonu.entity.*;
import com.example.KutupahaneOtomasyonu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private AdminRepository adminRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private BorrowingRepository borrowingRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Admin Ekle (Eğer yoksa)
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123")); // Şifre: 123
            admin.setName("Süper");
            admin.setSurname("Yönetici");
            admin.setEmail("admin@library.com");
            admin.setRole(Role.SUPERADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            adminRepository.save(admin);
            System.out.println("✅ Admin oluşturuldu: admin / 123");
        }

        // 2. Yazar Ekle
        if (authorRepository.count() == 0) {
            Author author = new Author();
            author.setName("J.K.");
            author.setSurname("Rowling");
            author.setBirthYear(1965);
            authorRepository.save(author);

            // 3. Kitap Ekle
            Book book = new Book();
            book.setTitle("Harry Potter ve Felsefe Taşı");
            book.setIsbn("978-3-16-148410-0");
            book.setCopies(10);
            book.setYear(1997);
            book.setSummary("Büyücülük okuluna giden bir çocuğun hikayesi.");
            book.setAuthor(author);
            bookRepository.save(book);
            System.out.println("✅ Örnek Kitap eklendi.");
        }

        // NOT: Otomatik ödünç kaydı eklemiyoruz ki "null" hatası riski olmasın.
        // Sistem açılınca kendin ödünç alabilirsin.
    }
}