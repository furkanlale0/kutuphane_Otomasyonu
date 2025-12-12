package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Book;
import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import com.example.KutupahaneOtomasyonu.entity.BorrowingStatus;
import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.repository.BookRepository;
import com.example.KutupahaneOtomasyonu.repository.BorrowingRepository;
import com.example.KutupahaneOtomasyonu.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LoanService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public LoanService(BorrowingRepository borrowingRepository, BookRepository bookRepository, MemberRepository memberRepository) {
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    public String borrowBook(Integer memberId, Integer bookId) {
        boolean alreadyHas = borrowingRepository.existsByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(memberId, bookId);
        if (alreadyHas) return "Bu kitabı zaten ödünç almışsınız!";

        Optional<Member> member = memberRepository.findById(memberId);
        Optional<Book> book = bookRepository.findById(bookId);

        if (member.isEmpty() || book.isEmpty()) return "Hata: Üye veya kitap bulunamadı.";

        Book b = book.get();
        if (b.getCopies() <= 0) return "Stokta kitap kalmadı!";

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
        return "OK";
    }

    public String returnBook(Integer memberId, Integer bookId) {
        List<Borrowing> activeLoans = borrowingRepository.findByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(memberId, bookId);
        if (activeLoans.isEmpty()) return "Kitap sizde görünmüyor.";

        Borrowing b = activeLoans.get(0);
        b.setReturnDate(LocalDateTime.now());
        b.setStatus(BorrowingStatus.RETURNED);
        borrowingRepository.save(b);

        Book book = b.getBook();
        book.setCopies(book.getCopies() + 1);
        bookRepository.save(book);
        return "OK";
    }

    public List<Map<String, Object>> getMemberHistory(Integer memberId) {
        List<Borrowing> history = borrowingRepository.findByMember_MemberId(memberId);
        return mapBorrowings(history);
    }

    public List<Map<String, Object>> getActiveLoans(Integer memberId) {
        List<Borrowing> loans = borrowingRepository.findByMember_MemberIdAndReturnDateIsNull(memberId);
        return mapBorrowings(loans);
    }

    public List<Map<String, Object>> getAllFines() {
        List<Borrowing> allLoans = borrowingRepository.findAll();
        List<Map<String, Object>> fines = new ArrayList<>();

        for (Borrowing b : allLoans) {
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

    public boolean payFine(Integer borrowId) {
        Optional<Borrowing> borrowing = borrowingRepository.findById(borrowId);
        if (borrowing.isPresent()) {
            Borrowing b = borrowing.get();
            b.setFinePaid(true);
            borrowingRepository.save(b);
            return true;
        }
        return false;
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