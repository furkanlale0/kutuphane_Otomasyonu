package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.OduncIslemi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// @Repository: Spring'e "Bu dosya OduncIslemi tablosunu (OduncIslemleri) yoneten depodur" diyoruz.
@Repository
// JpaRepository<OduncIslemi, Integer>:
// "Ben OduncIslemi tablosunu yonetiyorum ve ID'si Integer turunde."
public interface OduncRepository extends JpaRepository<OduncIslemi, Integer> { // BorrowingRepository -> OduncRepository

    // --- AKTIF ODUNCLERI BUL (Uye Bazli) ---
    // Entity degiskenleri: Uye (uye) -> UyeId (uyeId) ve IadeTarihi (iadeTarihi)

    // Meali: "Bu uyenin aldigi ama henuz geri getirmedigi (iade tarihi NULL olan) kitaplari listele."
    // SQL: SELECT * FROM OduncIslemleri WHERE uye_id = ? AND iade_tarihi IS NULL
    List<OduncIslemi> findByUye_UyeIdAndIadeTarihiIsNull(Integer uyeId);
    // Eski adi: findByMember_MemberIdAndReturnDateIsNull

    // --- SPESIFIK KITAP KONTROLU (Hata Onleyici) ---
    // Meali: "Bu uye, su an bu kitabi elinde tutuyor mu?"
    // List donuyoruz ki veritabaninda yanlislikla cift kayit varsa sistem patlamasin.
    List<OduncIslemi> findByUye_UyeIdAndKitap_KitapIdAndIadeTarihiIsNull(Integer uyeId, Integer kitapId);
    // Eski adi: findByMember_MemberIdAndBook_BookIdAndReturnDateIsNull

    // --- KITAP AKTIF MI? ---
    // Meali: "Bu kitap ID'sine sahip olup da henuz iade edilmemis bir kayit var mi?"
    // Kitap silinirken ise yarar. Eger true donerse, kitap birinin elindedir ve silinemez.
    boolean existsByKitap_KitapIdAndIadeTarihiIsNull(Integer kitapId);
    // Eski adi: existsByBook_BookIdAndReturnDateIsNull

    // --- KITAP GECMISI ---
    // Meali: "Bu kitap bugune kadar kimler tarafindan ne zaman alinmis? Hepsini getir."
    List<OduncIslemi> findByKitap_KitapId(Integer kitapId);
    // Eski adi: findByBook_BookId

    // --- UYE GECMISI ---
    // Meali: "Bu uye bugune kadar hangi kitaplari almis/vermis? Hepsini getir."
    // (Profil sayfasindaki 'Gecmis Islemler' tablosu icin kullanilir).
    List<OduncIslemi> findByUye_UyeId(Integer uyeId);
    // Eski adi: findByMember_MemberId

    // --- AYNI KITABI TEKRAR ALMA KONTROLU ---
    // Meali: "Bu uyenin elinde bu kitap zaten var mi?" (True/False)
    // Bir uye ayni kitabi iade etmeden ikinci kez alamasin diye bu kontrolu yapariz.
    boolean existsByUye_UyeIdAndKitap_KitapIdAndIadeTarihiIsNull(Integer uyeId, Integer kitapId);
    // Eski adi: existsByMember_MemberIdAndBook_BookIdAndReturnDateIsNull

    // --- GECIKMIS KITAPLARI BULMA (Otomatik Mail Icin) ---
    // Entity degiskenleri: SonTeslimTarihi (sonTeslimTarihi) ve IadeTarihi (iadeTarihi)

    // Meali: "Teslim tarihi su andan eski olan (Before) ama hala geri gelmemis (IsNull) kitaplari bul."
    List<OduncIslemi> findBySonTeslimTarihiBeforeAndIadeTarihiIsNull(java.time.LocalDateTime tarih);
    // Eski adi: findByDueDateBeforeAndReturnDateIsNull
}