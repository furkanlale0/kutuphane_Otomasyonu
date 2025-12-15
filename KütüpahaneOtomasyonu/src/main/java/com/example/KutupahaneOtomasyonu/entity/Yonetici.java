package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

// @Entity: Bu sinifin siradan bir kod degil, veritabanindaki bir TABLO oldugunu belirtir.
@Entity
// @Table: Veritabaninda "Yoneticiler" tablosuna karsilik gelir.
@Table(name = "Yoneticiler")
// @Data: Lombok kutuphanesi; Getter, Setter ve toString metodlarini bizim yerimize otomatik yazar.
@Data
@NoArgsConstructor // Bos constructor (Hibernate veri cekerken nesne olusturmak icin kullanir).
@AllArgsConstructor // Dolu constructor (Testlerde veya elle nesne olustururken isimize yarar).
public class Yonetici { // Admin -> Yonetici

    // @Id: Tablonun benzersiz kimlik numarasidir (Primary Key).
    // @GeneratedValue: Biz elle 1,2 diye sayi vermeyiz; veritabani otomatik sirayla arttirir.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yonetici_id")
    private Integer yoneticiId;

    // nullable=false: Bu alan bos birakilamaz.
    // unique=true: Ayni kullanici adiyla ikinci bir kisi kaydolamaz (Benzersiz olmalidir).
    @Column(name = "kullanici_adi", nullable = false, unique = true)
    private String kullaniciAdi;

    // Sifre burada guvenlik geregi "1234" gibi acik degil, kriptolanmis ($2a$...) haliyle tutulur.
    @Column(nullable = false)
    private String sifre;

    private String ad;

    private String soyad;

    private String email;

    // --- ROL YONETIMI ---
    // @Enumerated: Veritabanina rolu 0,1 gibi sayiyla degil; "SUPERADMIN", "PERSONEL" diye yaziyla kaydeder.
    // Boylece veritabanina bakinca kimin ne yetkisi oldugunu rahatca anlariz.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol; // Role -> Rol (Enum sinifi)

    // Kaydin ne zaman olusturuldugunu tutar.
    @Column(name = "olusturulma_tarihi")
    private LocalDateTime olusturulmaTarihi;

    // --- ILISKI: BIR YONETICI -> COK KITAP ---
    // Bir yonetici sisteme birden fazla kitap eklemis olabilir.

    // mappedBy: Iliskinin yoneticisi biz degiliz, Kitap sinifindaki "ekleyenYonetici" alanidir.
    // fetch=LAZY: Yoneticiyi cektigimizde, ekledigi 1000 kitabi hemen hafizaya yukleme.
    // Sadece biz ozel olarak "getEkledigiKitaplar()" dersek getir (Performans ayari).
    @OneToMany(mappedBy = "ekleyenYonetici", fetch = FetchType.LAZY)

    // @JsonIgnore: SONSUZ DONGU ENGELI.
    // Yonetici verisini JSON olarak gonderirken kitap listesini dahil etme.
    // Yoksa: Yonetici -> Kitap -> Yonetici -> Kitap... seklinde sonsuz dongu olur ve program coker.
    @JsonIgnore
    private List<Kitap> ekledigiKitaplar;
}