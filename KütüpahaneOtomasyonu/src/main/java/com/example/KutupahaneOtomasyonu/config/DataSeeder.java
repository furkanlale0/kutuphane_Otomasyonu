package com.example.KutupahaneOtomasyonu.config;

import com.example.KutupahaneOtomasyonu.entity.*;
import com.example.KutupahaneOtomasyonu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// @Component: Spring Boot'a "Bu sınıfı yönet, bu sistemin bir parçasıdır" diyoruz.
// CommandLineRunner: Uygulama "Run" edildiği an (ayağa kalkar kalkmaz) çalışacak özel bir arayüzdür.
// İçindeki 'run' metodunu otomatik tetikler.
@Component
public class DataSeeder implements CommandLineRunner {

    // Veritabanı tablolarıyla konuşmak için gerekli "Depo" (Repository) araçlarını çağırıyoruz.
    @Autowired private AdminRepository adminRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private BorrowingRepository borrowingRepository;

    // Şifreleri veritabanına "123" diye düz yazmamak, şifreleyerek kaydetmek için gerekli araç.
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Admin Ekleme Kontrolü (Eğer veritabanında hiç admin yoksa)
        // adminRepository.count() == 0 kontrolü ÇOK ÖNEMLİDİR.
        // Eğer bunu yapmazsak, uygulamayı her durdurup başlattığında tekrar tekrar admin eklemeye çalışır ve hata verir.
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            // encode("123"): Şifreyi "$2a$10$..." gibi karmaşık bir formata çevirip kaydeder.
            admin.setPassword(passwordEncoder.encode("123")); // Şifre: 123
            admin.setName("Süper");
            admin.setSurname("Yönetici");
            admin.setEmail("admin@library.com");
            admin.setRole(Role.SUPERADMIN); // En yetkili rolü veriyoruz.
            admin.setCreatedAt(LocalDateTime.now()); // Oluşturulma tarihini şu an yapıyoruz.
            adminRepository.save(admin); // Veritabanına kaydet.
            System.out.println("✅ Admin oluşturuldu: admin / 123");
        }

        // 2. Yazar ve Kitap Ekleme (Eğer hiç yazar yoksa test verisi ekle)
        if (authorRepository.count() == 0) {
            // Önce yazarı oluşturuyoruz çünkü kitap yazara bağlıdır.
            Author author = new Author();
            author.setName("J.K.");
            author.setSurname("Rowling");
            author.setBirthYear(1965);
            authorRepository.save(author); // Yazarı kaydet.

            // 3. Kitap Ekle
            Book book = new Book();
            book.setTitle("Harry Potter ve Felsefe Taşı");
            book.setIsbn("978-3-16-148410-0");
            book.setCopies(10); // Stok adedi
            book.setYear(1997);
            book.setSummary("Büyücülük okuluna giden bir çocuğun hikayesi.");
            // İlişki kurma: Bu kitabın yazarı yukarıdaki 'author'dur diyoruz.
            book.setAuthor(author);
            bookRepository.save(book); // Kitabı kaydet.
            System.out.println("✅ Örnek Kitap eklendi.");
        }

        // NOT: Otomatik ödünç kaydı eklemiyoruz ki "null" hatası riski olmasın.
        // Sistem açılınca kendin ödünç alabilirsin.
    }
}