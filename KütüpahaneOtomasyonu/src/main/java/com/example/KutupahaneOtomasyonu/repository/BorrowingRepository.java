package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// @Repository: Spring'e "Bu dosya Ödünç alma tablosunu (borrowings) yöneten depodur" diyoruz.
@Repository
// JpaRepository<Borrowing, Integer>:
// "Ben Borrowing tablosunu yönetiyorum ve ID'si Integer türünde."
public interface BorrowingRepository extends JpaRepository<Borrowing, Integer> {

    // --- AKTİF ÖDÜNÇLERİ BUL (Üye Bazlı) ---
    // İngilizcesi: find (Bul) ByMember_MemberId (Üye ID'sine göre) And (Ve) ReturnDateIsNull (İade tarihi boş olanları)
    // Meali: "Bu üyenin aldığı ama henüz geri getirmediği (elinde olan) kitapları listele."
    List<Borrowing> findByMember_MemberIdAndReturnDateIsNull(Integer memberId);

    // --- SPESİFİK KİTAP KONTROLÜ (Hata Önleyici) ---
    // KRİTİK DEĞİŞİKLİK: Optional Yerine List yaptık.
    // Meali: "Bu üye, şu an bu kitabı elinde tutuyor mu?"
    // Neden List? Eğer veritabanında bir hata sonucu aynı kitaptan 2 tane görünüyorsa program çökmesin, liste dönsün diye.
    List<Borrowing> findByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(Integer memberId, Integer bookId);

    // --- KİTAP AKTİF Mİ? ---
    // Meali: "Bu kitap ID'sine sahip olup da henüz iade edilmemiş bir kayıt var mı?"
    // Kitap silinirken işe yarar. Eğer true dönerse, kitap birinin elindedir ve silinemez.
    boolean existsByBook_BookIdAndReturnDateIsNull(Integer bookId);

    // --- KİTAP GEÇMİŞİ ---
    // Meali: "Bu kitap bugüne kadar kimler tarafından ne zaman alınmış? Hepsini getir."
    List<Borrowing> findByBook_BookId(Integer bookId);

    // --- ÜYE GEÇMİŞİ ---
    // Meali: "Bu üye bugüne kadar hangi kitapları almış/vermiş? Hepsini getir."
    // (Profil sayfasındaki 'Geçmiş İşlemler' tablosu için kullanılır).
    List<Borrowing> findByMember_MemberId(Integer memberId);

    // --- AYNI KİTABI TEKRAR ALMA KONTROLÜ ---
    // Meali: "Bu üyenin elinde bu kitap zaten var mı?" (True/False)
    // Bir üye aynı kitabı iade etmeden ikinci kez alamasın diye bu kontrolü yaparız.
    boolean existsByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(Integer memberId, Integer bookId);

    // --- GECİKMİŞ KİTAPLARI BULMA (Otomatik Mail İçin) ---
    // İngilizce meali: find (Bul) By (Göre) DueDateBefore (Son teslim tarihi geçmişte kalmış) And (Ve) ReturnDateIsNull (Henüz iade edilmemiş)
    // Parametre olarak "LocalDateTime.now()" (Şu an) verilir.
    // Meali: "Teslim tarihi şu andan eski olan ama hala geri gelmemiş kitapları bul."
    List<Borrowing> findByDueDateBeforeAndReturnDateIsNull(java.time.LocalDateTime date);
}