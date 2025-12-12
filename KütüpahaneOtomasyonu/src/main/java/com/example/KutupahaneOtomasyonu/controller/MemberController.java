package com.example.KutupahaneOtomasyonu.controller;

import com.example.KutupahaneOtomasyonu.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// @RestController: Bu sınıfın bir Web API olduğunu ve geriye veri (JSON) döndüreceğini belirtir.
// @RequestMapping: "localhost:8080/api/members" adresine gelen istekleri bu sınıf dinler.
@RestController
@RequestMapping("/api/members")
public class MemberController {

    // --- MİMARİ DÜZELTME ---
    // Artık Repository (Veritabanı) yok, sadece Service (İş Mantığı) var.
    // Controller -> Service -> Repository zincirini kurduk.
    private final MemberService memberService;

    // Constructor Injection: Spring'e "Çalışan bir MemberService örneğini buraya bağla" diyoruz.
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // --- PROFİL VE CEZA DETAYLARINI GETİRME ---
    // URL Örneği: /api/members/10/profile (ID'si 10 olan üye)
    // @GetMapping: Veri okuma isteği.
    // @PathVariable: URL'deki {id} kısmını (örn: 10) alıp 'Integer id' değişkenine atar.
    @GetMapping("/{id}/profile")
    public Map<String, Object> getProfile(@PathVariable Integer id) {
        // Controller olarak biz hesap kitap (ceza hesaplama, borç toplama) yapmayız.
        // Sadece Service'e "Bu ID'li üyenin profilini hazırla" deriz.
        // Service bize içinde İsim, E-posta, Toplam Borç ve Ceza Detayları olan hazır bir paket (Map) döner.
        return memberService.getMemberProfile(id);
    }
}