package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Book;
import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import com.example.KutupahaneOtomasyonu.entity.BorrowingStatus;
import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.repository.BookRepository;
import com.example.KutupahaneOtomasyonu.repository.BorrowingRepository;
import com.example.KutupahaneOtomasyonu.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public LoanController(BorrowingRepository borrowingRepository, BookRepository bookRepository, MemberRepository memberRepository) {
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody LoanRequest request) {
        Optional<Member> member = memberRepository.findById(Integer.parseInt(request.getMemberId()));
        Optional<Book> book = bookRepository.findById(request.getBookId());

        if (member.isEmpty() || book.isEmpty()) return ResponseEntity.badRequest().body("Üye veya kitap bulunamadı.");

        Book b = book.get();
        if (b.getCopies() <= 0) return ResponseEntity.badRequest().body("Stokta kitap kalmadı!");

        b.setCopies(b.getCopies() - 1);
        bookRepository.save(b);

        Borrowing borrowing = new Borrowing();
        borrowing.setMember(member.get());
        borrowing.setBook(b);
        borrowing.setBorrowDate(LocalDateTime.now());
        borrowing.setDueDate(LocalDateTime.now().plusDays(14));
        borrowing.setStatus(BorrowingStatus.BORROWED);

        borrowingRepository.save(borrowing);
        return ResponseEntity.ok("Kitap ödünç alındı.");
    }

    // --- YENİ EKLENEN İADE METODU (GÜVENLİ VERSİYON) ---
    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestBody LoanRequest request) {
        // Üyenin bu kitaba ait AKTİF (iade tarihi null) kaydını bul
        Optional<Borrowing> borrowing = borrowingRepository.findByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(
                Integer.parseInt(request.getMemberId()),
                request.getBookId()
        );

        if (borrowing.isEmpty()) return ResponseEntity.badRequest().body("Bu kitap zaten iade edilmiş veya sizde yok.");

        Borrowing b = borrowing.get();

        // 1. İade tarihini bas
        b.setReturnDate(LocalDateTime.now());

        // 2. Durumu değiştir
        b.setStatus(BorrowingStatus.RETURNED);
        borrowingRepository.save(b);

        // 3. Kitap stoğunu artır
        Book book = b.getBook();
        book.setCopies(book.getCopies() + 1);
        bookRepository.save(book);

        return ResponseEntity.ok("Kitap iade edildi.");
    }

    @GetMapping("/my-loans")
    public List<Map<String, Object>> getMyLoans(@RequestParam Integer memberId) {
        // Hem aktif hem geçmiş ödünçleri getirebiliriz veya sadece aktifleri.
        // Şimdilik sadece iade edilmemişleri (Aktifleri) getirelim ki tablo şişmesin.
        List<Borrowing> borrowings = borrowingRepository.findByMember_MemberIdAndReturnDateIsNull(memberId);

        List<Map<String, Object>> responseList = new ArrayList<>();

        for (Borrowing b : borrowings) {
            Map<String, Object> item = new HashMap<>();
            // Frontend'in kitabı tanıması için ID şart!
            item.put("bookId", b.getBook().getBookId());
            item.put("bookTitle", b.getBook().getTitle());
            item.put("borrowDate", b.getBorrowDate().toString());
            item.put("dueDate", b.getDueDate().toString());
            item.put("status", b.getStatus());

            responseList.add(item);
        }
        return responseList;
    }
}

class LoanRequest {
    private String memberId;
    private Integer bookId;
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
}