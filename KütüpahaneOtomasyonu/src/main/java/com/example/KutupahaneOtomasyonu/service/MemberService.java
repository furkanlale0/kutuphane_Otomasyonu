package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.repository.BorrowingRepository;
import com.example.KutupahaneOtomasyonu.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BorrowingRepository borrowingRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, BorrowingRepository borrowingRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.borrowingRepository = borrowingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 1. YENİ ÜYE KAYDI (Şifreleme Dahil) ---
    public String registerMember(Member member) {
        // Kullanıcı adı kontrolü
        if (memberRepository.findByUsername(member.getUsername()).isPresent()) {
            return "Kullanıcı adı zaten var.";
        }

        // Şifreyi şifrele (Hash)
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setRegistrationDate(LocalDateTime.now());

        memberRepository.save(member);
        return "OK";
    }

    // --- 2. KULLANICI BULMA (Login İçin) ---
    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    // --- 3. PROFİL VE CEZA HESAPLAMA (Zeka Burada) ---
    public Map<String, Object> getMemberProfile(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Üye bulunamadı"));

        // Üyenin geçmişini çek
        List<Borrowing> history = borrowingRepository.findByMember_MemberId(memberId);

        double totalFine = 0;
        List<Map<String, Object>> fineDetails = new ArrayList<>();

        for (Borrowing b : history) {
            // Eğer cezası zaten ödenmişse hesaplamaya katma
            if (Boolean.TRUE.equals(b.getFinePaid())) continue;

            long overdueDays = 0;
            // İade tarihi varsa onu kullan, yoksa bugünü kullan
            LocalDateTime effectiveReturnDate = (b.getReturnDate() != null) ? b.getReturnDate() : LocalDateTime.now();

            // Günü geçmiş mi?
            if (effectiveReturnDate.isAfter(b.getDueDate())) {
                overdueDays = ChronoUnit.DAYS.between(b.getDueDate(), effectiveReturnDate);
            }

            // Gecikme varsa ceza ekle (Günlük 5 TL)
            if (overdueDays > 0) {
                double fineAmount = overdueDays * 5.0;
                totalFine += fineAmount;

                Map<String, Object> detail = new HashMap<>();
                detail.put("bookTitle", b.getBook().getTitle());
                detail.put("days", overdueDays);
                detail.put("amount", fineAmount);
                fineDetails.add(detail);
            }
        }

        // Sonuçları paketle
        Map<String, Object> response = new HashMap<>();
        response.put("name", member.getName());
        response.put("surname", member.getSurname());
        response.put("username", member.getUsername());
        response.put("email", member.getEmail());
        response.put("totalFine", totalFine);
        response.put("fineDetails", fineDetails);

        return response;
    }

    // --- 4. YARDIMCI METOTLAR (İhtiyaç Olursa Diye) ---
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Optional<Member> findById(Integer id) {
        return memberRepository.findById(id);
    }

    public void deleteById(Integer id) {
        memberRepository.deleteById(id);
    }
}