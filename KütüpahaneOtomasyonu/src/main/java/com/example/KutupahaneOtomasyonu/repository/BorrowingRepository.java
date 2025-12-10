package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Integer> {

    // Üyenin elindeki tüm kitaplar
    List<Borrowing> findByMember_MemberIdAndReturnDateIsNull(Integer memberId);

    // KRİTİK DEĞİŞİKLİK: Optional Yerine List yaptık.
    // (Aynı kitaptan yanlışlıkla 2 tane almışsa bile patlamasın diye)
    List<Borrowing> findByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(Integer memberId, Integer bookId);

    boolean existsByBook_BookIdAndReturnDateIsNull(Integer bookId);
    List<Borrowing> findByBook_BookId(Integer bookId);

    // Üye geçmişi için
    List<Borrowing> findByMember_MemberId(Integer memberId);

    // Aynı kitabı zaten almış mı kontrolü için
    boolean existsByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(Integer memberId, Integer bookId);
}