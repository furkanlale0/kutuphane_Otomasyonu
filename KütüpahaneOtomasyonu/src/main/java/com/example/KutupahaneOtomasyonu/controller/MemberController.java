package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.entity.Borrowing;
import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.repository.BorrowingRepository;
import com.example.KutupahaneOtomasyonu.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @GetMapping("/{id}/profile")
    public Map<String, Object> getProfile(@PathVariable Integer id) {
        Member member = memberRepository.findById(id).orElseThrow();

        // Üyenin TÜM geçmişini (İade edilmiş veya edilmemiş) getir
        // Not: BorrowingRepository'de "findByMember_MemberId" metodunun olması lazım.
        // Yoksa repository'e ekle: List<Borrowing> findByMember_MemberId(Integer memberId);
        List<Borrowing> history = borrowingRepository.findByMember_MemberId(id);

        double totalFine = 0;
        List<Map<String, Object>> fineDetails = new ArrayList<>();

        for (Borrowing b : history) {
            long overdueDays = 0;
            LocalDateTime effectiveReturnDate = (b.getReturnDate() != null) ? b.getReturnDate() : LocalDateTime.now();

            // Eğer son teslim tarihi geçmişse
            if (effectiveReturnDate.isAfter(b.getDueDate())) {
                overdueDays = ChronoUnit.DAYS.between(b.getDueDate(), effectiveReturnDate);
            }

            if (overdueDays > 0) {
                double fineAmount = overdueDays * 5.0; // GÜNLÜK 5 TL CEZA
                totalFine += fineAmount;

                Map<String, Object> detail = new HashMap<>();
                detail.put("bookTitle", b.getBook().getTitle());
                detail.put("days", overdueDays);
                detail.put("amount", fineAmount);
                fineDetails.add(detail);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("name", member.getName());
        response.put("surname", member.getSurname());
        response.put("username", member.getUsername());
        response.put("email", member.getEmail());
        response.put("totalFine", totalFine);
        response.put("fineDetails", fineDetails);

        return response;
    }
}