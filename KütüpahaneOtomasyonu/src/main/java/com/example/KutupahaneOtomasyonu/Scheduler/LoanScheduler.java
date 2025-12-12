package com.example.KutupahaneOtomasyonu.scheduler;

import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import com.example.KutupahaneOtomasyonu.repository.BorrowingRepository;
import com.example.KutupahaneOtomasyonu.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// @Component: Spring'e "Bu sınıfı yönet, bu sistemin çalışan bir parçasıdır" diyoruz.
// Eğer bunu yazmazsak, zamanlayıcı asla devreye girmez.
@Component
public class LoanScheduler {

    // Veritabanı sorguları için Depo Sorumlusu
    private final BorrowingRepository borrowingRepository;
    // Mail göndermek için Postacı Servisi
    private final EmailService emailService;

    // Constructor Injection: Gerekli araçları Spring'den istiyoruz.
    @Autowired
    public LoanScheduler(BorrowingRepository borrowingRepository, EmailService emailService) {
        this.borrowingRepository = borrowingRepository;
        this.emailService = emailService;
    }

    // --- ZAMANLAYICI METODU ---
    // @Scheduled: Bu metodun belirli aralıklarla otomatik çalışacağını belirtir.
    // cron = "*/10 * * * * ?" -> "Her 10 saniyede bir çalıştır" demektir.
    // (Gerçek hayatta genelde günde 1 kere çalıştırılır: "0 0 9 * * ?")
    @Scheduled(cron = "*/10 * * * * ?")
    public void checkOverdueLoans() {
        System.out.println("Gecikme kontrolü başladı...");

        // 1. GECİKENLERİ BUL
        // findByDueDateBefore: Son teslim tarihi "Şu an"dan (LocalDateTime.now) önce olanlar.
        // AndReturnDateIsNull: Ve henüz iade edilmemiş (Hala üyede) olanlar.
        List<Borrowing> overdueLoans = borrowingRepository.findByDueDateBeforeAndReturnDateIsNull(LocalDateTime.now());

        // Eğer hiç geciken kitap yoksa, boşuna işlem yapma, metodu bitir.
        if (overdueLoans.isEmpty()) {
            return;
        }

        // 2. DÖNGÜ (HER BİR GECİKEN KİTAP İÇİN)
        for (Borrowing borrowing : overdueLoans) {

            // --- SPAM KORUMASI ---
            // Eğer bu kitap için daha önce mail atılmışsa (notificationSent == true),
            // tekrar mail atma! "continue" diyerek döngüdeki bir sonraki kitaba geç.
            if (borrowing.isNotificationSent()) {
                continue;
            }

            // Gerekli bilgileri al (Kime atılacak? Hangi kitap?)
            String memberEmail = borrowing.getMember().getEmail();
            String bookTitle = borrowing.getBook().getTitle();

            // 3. MAİL GÖNDERME (Hata Yönetimli)
            try {
                // Mail servisini çağır.
                emailService.sendSimpleEmail(memberEmail, "Gecikme", "Kitabı getir.");
                System.out.println("Mail gitti: " + memberEmail);
            } catch (Exception e) {
                // Eğer internet yoksa veya mail adresi hatalıysa sistem çökmesin.
                // Sadece hatayı konsola yaz ve devam et.
                System.err.println("Mail hatası (" + memberEmail + "): " + e.getMessage());
            }

            // --- İŞARETLEME ---
            // Mail gitse de gitmese de (hata olsa bile), bu kaydı "İşlem Yapıldı" olarak işaretle.
            // Neden? Çünkü işaretlemezsek, 10 saniye sonra sistem tekrar mail atmayı dener.
            // Sonsuz döngüye girmemek için burayı 'true' yapıp kaydediyoruz.
            borrowing.setNotificationSent(true);
            borrowingRepository.save(borrowing);
        }
    }
}