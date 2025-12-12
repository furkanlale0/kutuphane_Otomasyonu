package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Book;
import com.example.KutupahaneOtomasyonu.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController: Bu sınıfın bir Web API denetleyicisi olduğunu belirtir.
// Yani burası bir websitesi (HTML) değil, saf veri (JSON) döndürür.
@RestController
// @RequestMapping: "localhost:8080/api/books" adresine gelen tüm istekler bu sınıfa yönlendirilir.
@RequestMapping("/api/books")
public class BookController {

    // Kitaplarla ilgili asıl işi yapan Servis katmanını çağırıyoruz.
    private final BookService bookService;

    // Constructor Injection: Spring'e "Bana çalışan bir BookService ver" diyoruz.
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // --- TÜM KİTAPLARI LİSTELE ---
    // GET İsteği: Veri tabanındaki bütün kitapları liste halinde döner.
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.findAll();
    }

    // --- KİTAP EKLEME (HATA KONTROLLÜ) ---
    // POST İsteği: Yeni kitap eklemek için kullanılır.
    // @RequestBody: Gelen JSON verisini 'Book' nesnesine çevirir.
    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        try {
            // Servise "Bunu kaydet" diyoruz.
            // Eğer stok sayısı eksi girilmişse veya yazar yoksa Servis hata fırlatabilir.
            Book savedBook = bookService.save(book);

            // Başarılı olursa 200 OK koduyla kitabı geri dön.
            return ResponseEntity.ok(savedBook);

        } catch (IllegalArgumentException e) {
            // Servis "Stok 0'dan küçük olamaz" gibi bir mantık hatası fırlatırsa burası çalışır.
            // 400 Bad Request hatası döneriz.
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            // Beklenmedik başka bir hata olursa (Veritabanı koptu vs.) burası çalışır.
            return ResponseEntity.badRequest().body("Bir hata oluştu: " + e.getMessage());
        }
    }

    // --- ID İLE KİTAP BULMA ---
    // URL örneği: /api/books/5 (ID'si 5 olan kitabı getir)
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        // Servisten kitabı istiyoruz (Optional döner, yani boş olabilir).
        return bookService.findById(id)
                .map(ResponseEntity::ok) // Kitap varsa 200 OK ile dön.
                .orElse(ResponseEntity.notFound().build()); // Kitap yoksa 404 Not Found dön.
    }

    // --- ARAMA YAPMA ---
    // URL örneği: /api/books/search?title=harry
    // @RequestParam: URL'deki soru işaretinden sonraki 'title' parametresini okur.
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String title) {
        return bookService.searchByTitle(title);
    }

    // --- KİTAP SİLME ---
    // URL örneği: /api/books/10 (ID'si 10 olanı sil)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Integer id) {
        try {
            // Servise "Bunu sil" diyoruz.
            // DİKKAT: Eğer bu kitap şu an bir üyede ödünçteyse, Servis silmeye izin vermez ve hata fırlatır.
            bookService.deleteById(id);
            return ResponseEntity.ok("Kitap silindi.");
        } catch (IllegalStateException e) {
            // "Bu kitap ödünç verilmiş, silemezsin" hatası gelirse kullanıcıya 400 ile bildiriyoruz.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}