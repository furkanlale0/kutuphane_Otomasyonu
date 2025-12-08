package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Member;
import com.example.KutupahaneOtomasyonu.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // CRUD: Yeni üye kaydet
    public Member save(Member member) {
        // NOT: Normalde burada şifre hash'leme, validasyon ve kullanıcıya hoş geldin e-postası gönderme işlemleri yapılır.
        return memberRepository.save(member);
    }

    // CRUD: Tüm üyeleri getir (Admin yetkisi gerekir)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // CRUD: ID ile üye getir (LoanController tarafından kullanılıyor)
    public Optional<Member> findById(Integer id) {
        return memberRepository.findById(id);
    }

    // CRUD: Üye sil (Admin yetkisi gerekir)
    public void deleteById(Integer id) {
        memberRepository.deleteById(id);
    }

    // NOT: Eğer Members tablosuna username ve password eklersek, findByUsername metodu da buraya gelmelidir.
}