package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// @Entity: Spring ve Hibernate'e "Bu sinifi al, veritabaninda tablo yap" diyoruz.
@Entity
// @Table: Veritabanindaki tablonun adi "Kitaplar" olsun.
@Table(name = "Kitaplar")
// @Data: Lombok. Getter, Setter, toString gibi metodlari bizim yerimize yazar.
@Data
@NoArgsConstructor // Bos constructor (Hibernate'in calismasi icin sarttir).
@AllArgsConstructor // Dolu constructor.
public class Kitap { // Book -> Kitap

    // @Id: Bu alanin tablonun kimligi (Primary Key) oldugunu belirtir.
    // @GeneratedValue: ID'yi biz vermeyiz, veritabani 1, 2, 3 diye otomatik arttirir.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kitap_id")
    private Integer kitapId;

    // nullable = false: Kitap basligi asla bos birakilamaz.
    @Column(name = "kitap_adi", nullable = false)
    private String kitapAdi; // title -> kitapAdi

    private String isbn; // Kitabin barkod numarasi

    // Veritabaninda "yayin_yili" sutununa karsilik gelir.
    @Column(name = "yayin_yili")
    private Integer yayinYili;

    @Column(name = "stok_sayisi")
    private Integer stokSayisi; // copies -> stokSayisi

    // --- OZEL ALAN: OZET ---
    // columnDefinition = "TEXT": Normalde String veritabaninda 255 karakterlik yer kaplar.
    // Ancak ozet uzun olabilir (1000 karakter vs). O yuzden tipini "TEXT" yapiyoruz.
    @Column(columnDefinition = "TEXT")
    private String ozet;

    // --- ILISKI 1: KITAP -> YAZAR ---
    // @ManyToOne: Cok Kitap -> Tek Yazar.

    // fetch = FetchType.EAGER: "Hemen Getir".
    // Bir kitabi veritabanindan sorguladiginda, Hibernate otomatik olarak gidip Yazar bilgisini de getirir.
    // Cunku kullanici kitabi listelerken yazarini da hemen gormek ister.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "yazar_id", nullable = false) // Yazar bos olamaz.
    private Yazar yazar;

    // --- ILISKI 2: KITAP -> EKLEYEN YONETICI ---
    // @ManyToOne: Cok Kitap -> Tek Yonetici (Bir yonetici cok kitap ekleyebilir).

    // fetch = FetchType.LAZY: "Tembel Getir".
    // Kitap listesini cekerken, kitabi hangi personelin ekledigi genelde onemsizdir.
    // O yuzden "Yonetici bilgisini hemen getirme, ben ozel olarak istersem getir" diyoruz. Performans saglar.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ekleyen_yonetici_id")

    // @JsonIgnoreProperties: Yonetici bilgisini JSON'a cevirirken su alanlari gormezden gel:
    // 1. "ekledigiKitaplar": Yoneticinin ekledigi diger kitaplar (Sonsuz donguyu onler).
    // 2. "sifre": Guvenlik icin yoneticinin sifresini gizle.
    // 3. "hibernateLazyInitializer": LAZY yukleme yaparken olusan teknik bir hatayi onler.
    @JsonIgnoreProperties({"ekledigiKitaplar", "sifre", "hibernateLazyInitializer"})
    private Yonetici ekleyenYonetici;
}