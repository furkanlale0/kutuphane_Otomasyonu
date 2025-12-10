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
import java.time.temporal.ChronoUnit;
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

    // --- 1. ÖDÜNÇ ALMA ---
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody LoanRequest request) {
        Integer memberId = Integer.parseInt(request.getMemberId());
        Integer bookId = request.getBookId();

        boolean alreadyHas = borrowingRepository.existsByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(memberId, bookId);
        if (alreadyHas) return ResponseEntity.badRequest().body("Bu kitabı zaten ödünç almışsınız!");

        Optional<Member> member = memberRepository.findById(memberId);
        Optional<Book> book = bookRepository.findById(bookId);

        if (member.isEmpty() || book.isEmpty()) return ResponseEntity.badRequest().body("Hata.");
        Book b = book.get();
        if (b.getCopies() <= 0) return ResponseEntity.badRequest().body("Stok yok!");

        b.setCopies(b.getCopies() - 1);
        bookRepository.save(b);

        Borrowing borrowing = new Borrowing();
        borrowing.setMember(member.get());
        borrowing.setBook(b);
        borrowing.setBorrowDate(LocalDateTime.now());
        borrowing.setDueDate(LocalDateTime.now().plusDays(14));
        borrowing.setStatus(BorrowingStatus.BORROWED);
        borrowing.setFinePaid(false);
        borrowingRepository.save(borrowing);
        return ResponseEntity.ok("Ödünç alındı.");
    }

    // --- 2. İADE ETME ---
    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestBody LoanRequest request) {
        List<Borrowing> activeLoans = borrowingRepository.findByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(
                Integer.parseInt(request.getMemberId()), request.getBookId());

        if (activeLoans.isEmpty()) return ResponseEntity.badRequest().body("Kitap sizde görünmüyor.");

        Borrowing b = activeLoans.get(0);
        b.setReturnDate(LocalDateTime.now());
        b.setStatus(BorrowingStatus.RETURNED);
        borrowingRepository.save(b);

        Book book = b.getBook();
        book.setCopies(book.getCopies() + 1);
        bookRepository.save(book);
        return ResponseEntity.ok("İade edildi.");
    }

    // --- 3. GEÇMİŞ ---
    @GetMapping("/history")
    public List<Map<String, Object>> getMemberHistory(@RequestParam Integer memberId) {
        List<Borrowing> history = borrowingRepository.findByMember_MemberId(memberId);
        return mapBorrowings(history);
    }

    // --- 4. AKTİF ÖDÜNÇLER ---
    @GetMapping("/my-loans")
    public List<Map<String, Object>> getMyLoans(@RequestParam Integer memberId) {
        List<Borrowing> borrowings = borrowingRepository.findByMember_MemberIdAndReturnDateIsNull(memberId);
        return mapBorrowings(borrowings);
    }

    // --- 5. CEZALAR (HATA BURADAYDI, DÜZELTİLDİ) ---
    @GetMapping("/admin/fines")
    public List<Map<String, Object>> getAllFines() {
        List<Borrowing> allLoans = borrowingRepository.findAll();
        List<Map<String, Object>> fines = new ArrayList<>();

        for (Borrowing b : allLoans) {
            // DÜZELTME: isFinePaid() yerine getFinePaid() kullandık ve Null kontrolü yaptık.
            if (Boolean.TRUE.equals(b.getFinePaid())) continue;

            LocalDateTime effectiveReturnDate = (b.getReturnDate() != null) ? b.getReturnDate() : LocalDateTime.now();

            if (effectiveReturnDate.isAfter(b.getDueDate())) {
                long overdueDays = ChronoUnit.DAYS.between(b.getDueDate(), effectiveReturnDate);
                if (overdueDays > 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("borrowId", b.getBorrowId());
                    item.put("memberName", b.getMember().getName() + " " + b.getMember().getSurname());
                    item.put("bookTitle", b.getBook().getTitle());
                    item.put("days", overdueDays);
                    item.put("amount", overdueDays * 5.0);
                    item.put("isReturned", b.getReturnDate() != null);
                    fines.add(item);
                }
            }
        }
        return fines;
    }

    // --- 6. CEZA TAHSİL ET ---
    @PostMapping("/pay-fine/{id}")
    public ResponseEntity<?> payFine(@PathVariable Integer id) {
        Optional<Borrowing> borrowing = borrowingRepository.findById(id);
        if (borrowing.isPresent()) {
            Borrowing b = borrowing.get();
            b.setFinePaid(true);
            borrowingRepository.save(b);
            return ResponseEntity.ok("Tahsil edildi.");
        }
        return ResponseEntity.badRequest().body("Kayıt yok.");
    }

    private List<Map<String, Object>> mapBorrowings(List<Borrowing> list) {
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Borrowing b : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("bookId", b.getBook().getBookId());
            item.put("bookTitle", b.getBook().getTitle());
            item.put("borrowDate", b.getBorrowDate().toString());
            item.put("dueDate", b.getDueDate().toString());
            item.put("returnDate", (b.getReturnDate() != null) ? b.getReturnDate().toString() : null);
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