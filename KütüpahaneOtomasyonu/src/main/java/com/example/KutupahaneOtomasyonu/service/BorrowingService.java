package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Book;
import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import com.example.KutupahaneOtomasyonu.entity.BorrowingStatus;
import com.example.KutupahaneOtomasyonu.repository.BookRepository;
import com.example.KutupahaneOtomasyonu.repository.BorrowingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;

    @Autowired
    public BorrowingService(BorrowingRepository borrowingRepository, BookRepository bookRepository) {
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
    }

    public List<Borrowing> findAll() {
        return borrowingRepository.findAll();
    }

    @Transactional
    public Borrowing save(Borrowing borrowing) {
        if (borrowing.getBorrowId() == null) {
            Book book = borrowing.getBook();
            if (book.getCopies() > 0) {
                book.setCopies(book.getCopies() - 1);
                bookRepository.save(book);

                borrowing.setBorrowDate(LocalDateTime.now());
                borrowing.setDueDate(LocalDateTime.now().plusDays(14));
                borrowing.setStatus(BorrowingStatus.BORROWED);
            } else {
                throw new RuntimeException("Stokta kitap yok!");
            }
        }
        return borrowingRepository.save(borrowing);
    }
}