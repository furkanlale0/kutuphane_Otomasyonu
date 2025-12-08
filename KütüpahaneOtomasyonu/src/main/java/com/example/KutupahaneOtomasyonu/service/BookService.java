package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Author;
import com.example.KutupahaneOtomasyonu.entity.Book;
import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import com.example.KutupahaneOtomasyonu.repository.AuthorRepository;
import com.example.KutupahaneOtomasyonu.repository.BookRepository;
import com.example.KutupahaneOtomasyonu.repository.BorrowingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BorrowingRepository borrowingRepository;

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, BorrowingRepository borrowingRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.borrowingRepository = borrowingRepository;
    }

    public List<Book> findAll() { return bookRepository.findAll(); }

    public Optional<Book> findById(Integer id) { return bookRepository.findById(id); }

    @Transactional
    public Book save(Book book) {
        Author incomingAuthor = book.getAuthor();
        if (incomingAuthor != null && incomingAuthor.getName() != null) {
            String name = incomingAuthor.getName().trim();
            String surname = incomingAuthor.getSurname().trim();
            Optional<Author> existingAuthor = authorRepository.findByNameAndSurname(name, surname);
            if (existingAuthor.isPresent()) {
                book.setAuthor(existingAuthor.get());
            } else {
                incomingAuthor.setName(name);
                incomingAuthor.setSurname(surname);
                Author savedAuthor = authorRepository.save(incomingAuthor);
                book.setAuthor(savedAuthor);
            }
        } else {
            throw new RuntimeException("Yazar adı boş olamaz!");
        }
        return bookRepository.save(book);
    }

    // --- KRİTİK DEĞİŞİKLİK BURADA ---
    @Transactional
    public void deleteById(Integer id) {
        // 1. Önce: Kitap ŞU AN birinde mi? (İade edilmemiş)
        boolean isBorrowed = borrowingRepository.existsByBook_BookIdAndReturnDateIsNull(id);

        if (isBorrowed) {
            // Hala üyedeyse SİLME, hata ver.
            throw new IllegalStateException("Bu kitap şu an bir üyede! Teslim almadan silemezsiniz.");
        }

        // 2. Eğer kitap üyede değilse (raftaysa), onun eski kayıtlarını temizle
        // (Veritabanı hatasını önlemek için geçmişi siliyoruz)
        List<Borrowing> history = borrowingRepository.findByBook_BookId(id);
        if (!history.isEmpty()) {
            borrowingRepository.deleteAll(history);
        }

        // 3. Geçmiş temizlendiğine göre artık kitabı silebiliriz
        bookRepository.deleteById(id);
    }

    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
}