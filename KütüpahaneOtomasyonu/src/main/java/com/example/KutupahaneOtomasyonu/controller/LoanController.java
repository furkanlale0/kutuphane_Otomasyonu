package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// @RestController: Bu sınıfın bir Web API olduğunu ve JSON cevabı döneceğini belirtir.
// @RequestMapping: "localhost:8080/api/loans" adresine gelen istekler buraya düşer.
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    // ARTIK REPOSITORY YOK, SERVICE VAR
    // Veritabanı ile direkt konuşmuyoruz, kuralları bilen Servis ile konuşuyoruz.
    private final LoanService loanService;

    // Constructor Injection: Spring'e "Bana çalışan bir LoanService ver" diyoruz.
    @Autowired
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // --- KİTAP ÖDÜNÇ ALMA ---
    // POST İsteği: /api/loans/borrow
    // @RequestBody: Frontend'den gelen JSON verisini (memberId, bookId) alır.
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody LoanRequest request) {
        // Servise emrediyoruz: "Bu üyeye bu kitabı ver."
        // Not: request.getMemberId() String geldiği için Integer'a çeviriyoruz.
        String result = loanService.borrowBook(Integer.parseInt(request.getMemberId()), request.getBookId());

        // Servis bize işlem sonucunu String olarak döner ("OK" veya hata mesajı).
        if ("OK".equals(result)) {
            // Başarılıysa 200 OK
            return ResponseEntity.ok("Ödünç alındı.");
        }
        // Başarısızsa (Stok yok, limit dolu vs.) 400 Bad Request ve hata mesajı.
        return ResponseEntity.badRequest().body(result);
    }

    // --- KİTAP İADE ETME ---
    // POST İsteği: /api/loans/return
    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestBody LoanRequest request) {
        // Servise emrediyoruz: "Bu üye bu kitabı iade ediyor."
        String result = loanService.returnBook(Integer.parseInt(request.getMemberId()), request.getBookId());

        if ("OK".equals(result)) {
            return ResponseEntity.ok("İade edildi.");
        }
        return ResponseEntity.badRequest().body(result);
    }

    // --- GEÇMİŞ İŞLEMLERİ GÖRME ---
    // GET İsteği: /api/loans/history?memberId=5
    // @RequestParam: URL'deki '?memberId=...' kısmını okur.
    @GetMapping("/history")
    public List<Map<String, Object>> getMemberHistory(@RequestParam Integer memberId) {
        // Servisten bu üyenin tüm eski kayıtlarını istiyoruz.
        // Map<String, Object> yapısı, özel bir veri formatı oluşturmak içindir (Kitap adı, tarih vs.).
        return loanService.getMemberHistory(memberId);
    }

    // --- AKTİF ÖDÜNÇLERİ GÖRME ---
    // GET İsteği: /api/loans/my-loans?memberId=5
    // Üyenin şu an elinde olan (henüz iade etmediği) kitapları getirir.
    @GetMapping("/my-loans")
    public List<Map<String, Object>> getMyLoans(@RequestParam Integer memberId) {
        return loanService.getActiveLoans(memberId);
    }

    // --- (ADMIN) GECİKMİŞ CEZALARI GÖRME ---
    // GET İsteği: /api/loans/admin/fines
    // Sadece Admin'in görebileceği, cezası olan tüm işlemleri listeler.
    @GetMapping("/admin/fines")
    public List<Map<String, Object>> getAllFines() {
        return loanService.getAllFines();
    }

    // --- (ADMIN) CEZA TAHSİL ETME ---
    // POST İsteği: /api/loans/pay-fine/105 (105 burada işlem ID'sidir)
    // @PathVariable: URL yolundaki ID'yi (105) alır.
    @PostMapping("/pay-fine/{id}")
    public ResponseEntity<?> payFine(@PathVariable Integer id) {
        // Servise "Bu borç ödendi, sil" diyoruz.
        boolean success = loanService.payFine(id);

        if (success) {
            return ResponseEntity.ok("Tahsil edildi.");
        }
        return ResponseEntity.badRequest().body("Kayıt bulunamadı.");
    }
}

// DTO Sınıfı (Data Transfer Object)
// Frontend'den gelen "Hangi üye? Hangi kitap?" verisini taşıyan basit bir kutudur.
class LoanRequest {
    private String memberId; // Frontend bazen ID'leri tırnak içinde String yollayabilir, o yüzden String tutuyoruz.
    private Integer bookId;

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
}