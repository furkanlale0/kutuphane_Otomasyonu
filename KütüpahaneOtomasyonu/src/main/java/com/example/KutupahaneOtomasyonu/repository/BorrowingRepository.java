package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Integer> {
    List<Borrowing> findByMember_MemberIdAndReturnDateIsNull(Integer memberId);
    Optional<Borrowing> findByMember_MemberIdAndBook_BookIdAndReturnDateIsNull(Integer memberId, Integer bookId);
    boolean existsByBook_BookIdAndReturnDateIsNull(Integer bookId);

    // YENİ EKLENEN: Bir kitaba ait TÜM kayıtları (Geçmiş dahil) bul
    List<Borrowing> findByBook_BookId(Integer bookId);
}