package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// @Entity: Bu sinifin bir veritabani tablosu oldugunu belirtir.
@Entity
// @Table: Veritabanindaki adi "OduncIslemleri" olsun.
@Table(name = "OduncIslemleri")
// @Data: Lombok (Getter, Setter, toString metodlarini otomatik yazar).
@Data
@NoArgsConstructor // Bos constructor (Hibernate icin zorunlu).
@AllArgsConstructor // Dolu constructor.
public class OduncIslemi { // Borrowing -> OduncIslemi

    // @Id: Tablonun benzersiz kimlik numarasi (Primary Key).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "odunc_id")
    private Integer oduncId;

    // --- OZEL KONTROL: SPAM ENGELLEYICI ---
    // Mail gonderildi mi kontrolu icin yeni kutucuk.
    // Otomatik sistem (Scheduler) her calistiginda tekrar tekrar mail atmasin diye,
    // mail attiktan sonra burayi 'true' yapiyoruz.
    @Column(name = "bildirim_gonderildi")
    private boolean bildirimGonderildi = false;

    // --- ILISKI 1: ODUNC ISLEMI -> UYE ---
    // @ManyToOne: Bir uye birden fazla kitap alabilir (Ama bir islem tek uyeye aittir).

    // fetch = FetchType.EAGER: "Hemen Getir".
    // Bir odunc islemini sorguladigimizda, kitabi alan uyenin kim oldugunu da hemen bilmek isteriz.
    // Ekranda "Kitabi alan: Ahmet" yazabilmek icin bu gereklidir.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uye_id", nullable = false) // Uye olmadan odunc islemi olamaz.

    // @JsonIgnoreProperties: COK ONEMLI! Sonsuz donguyu ve guvenlik acigini onler.
    // 1. "oduncIslemleri": Uyenin icindeki eski oduncleri getirme (Sonsuz dongu: Odunc->Uye->Odunc...).
    // 2. "sifre": Uyenin sifresini JSON icinde gosterme (Guvenlik).
    // 3. "hibernateLazyInitializer": Teknik hatalari onler.
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "oduncIslemleri", "sifre"})
    private Uye uye;

    // --- ILISKI 2: ODUNC ISLEMI -> KITAP ---
    // @ManyToOne: Bir kitap defalarca odunc alinabilir (Farkli zamanlarda).
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "kitap_id", nullable = false)

    // Kitap objesini JSON'a cevirirken, kitabi ekleyen yoneticiyi ve kitabin eski odunc gecmisini gizle.
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "oduncIslemleri", "ekleyenYonetici"})
    private Kitap kitap;

    // Kitabin alindigi tarih ve saat.
    @Column(name = "alis_tarihi", nullable = false)
    private LocalDateTime alisTarihi; // borrowDate -> alisTarihi

    // Kitabin geri getirildigi tarih. (Henuz getirilmediyse NULL olur).
    @Column(name = "iade_tarihi")
    private LocalDateTime iadeTarihi; // returnDate -> iadeTarihi

    // Son teslim tarihi (Alis tarihi + 15 gun gibi).
    @Column(name = "son_teslim_tarihi")
    private LocalDateTime sonTeslimTarihi; // dueDate -> sonTeslimTarihi

    // Durum: ODUNC_ALINDI, IADE_EDILDI, GECIKTI vs. (Enum olarak tutuyoruz).
    @Enumerated(EnumType.STRING)
    @Column(name = "durum", nullable = false)
    private OduncDurumu durum; // BorrowingStatus -> OduncDurumu

    // --- CEZA KONTROLÜ ---
    // Duzeltilen Kisim: Kucuk 'boolean' yerine buyuk 'Boolean' kullandik.
    // Neden? Veritabaninda eski kayitlarda bu alan NULL olabilir.
    // Kucuk 'boolean' null kabul etmez, patlar. Buyuk 'Boolean' null kabul eder.
    @Column(name = "ceza_odendi_mi")
    private Boolean cezaOdendiMi = false; // finePaid -> cezaOdendiMi

    // Getter ve Setter'lari Lombok (@Data) otomatik halleder ama
    // manuel eklenen bu metodlar, ozel durumlarda (ornegin boolean isimlendirme kurallari) garanti saglar.
    public boolean isBildirimGonderildi() { return bildirimGonderildi; }
    public void setBildirimGonderildi(boolean bildirimGonderildi) { this.bildirimGonderildi = bildirimGonderildi; }
}