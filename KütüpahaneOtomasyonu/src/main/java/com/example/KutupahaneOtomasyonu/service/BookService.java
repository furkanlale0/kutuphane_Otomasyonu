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
        // --- YENİ EKLENEN KONTROL ---
        if (book.getCopies() == null || book.getCopies() < 1) {
            throw new IllegalArgumentException("Stok adedi en az 1 olmalıdır!");
        }

        // Yazar kontrolü ve ekleme mantığı
        Author incomingAuthor = book.getAuthor();
        if (incomingAuthor != null && incomingAuthor.getName() != null) {
            String name = incomingAuthor.getName().trim();
            String surname = incomingAuthor.getSurname().trim();

            // Yazar veritabanında var mı?
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

    @Transactional
    public void deleteById(Integer id) {
        // Kitap bir üyede mi?
        boolean isBorrowed = borrowingRepository.existsByBook_BookIdAndReturnDateIsNull(id);

        if (isBorrowed) {
            throw new IllegalStateException("Bu kitap şu an bir üyede! Teslim almadan silemezsiniz.");
        }

        // Geçmiş kayıtlarını temizle
        List<Borrowing> history = borrowingRepository.findByBook_BookId(id);
        if (!history.isEmpty()) {
            borrowingRepository.deleteAll(history);
        }

        bookRepository.deleteById(id);
    }

    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
}