package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// @Entity: Spring ve Hibernate'e "Bu sınıfı al, veritabanında tablo yap" diyoruz.
@Entity
// @Table: Veritabanındaki tablonun adı "books" olsun.
@Table(name = "books")
// @Data: Lombok. Getter, Setter, toString gibi metodları bizim yerimize yazar.
@Data
@NoArgsConstructor // Boş constructor (Hibernate'in çalışması için şarttır).
@AllArgsConstructor // Dolu constructor.
public class Book {

    // @Id: Bu alanın tablonun kimliği (Primary Key) olduğunu belirtir.
    // @GeneratedValue: ID'yi biz vermeyiz, veritabanı 1, 2, 3 diye otomatik arttırır.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;

    // nullable = false: Kitap başlığı asla boş bırakılamaz.
    @Column(nullable = false)
    private String title;

    private String isbn; // Kitabın barkod numarası

    // Java'da değişken adı "year" ama veritabanında "publication_year" olarak kaydedilsin.
    @Column(name = "publication_year")
    private Integer year;

    private Integer copies; // Stoktaki kopya sayısı

    // --- ÖZEL ALAN: ÖZET ---
    // columnDefinition = "TEXT": Normalde String veritabanında 255 karakterlik yer kaplar.
    // Ancak özet uzun olabilir (1000 karakter vs). O yüzden tipini "TEXT" yapıyoruz.
    @Column(columnDefinition = "TEXT")
    private String summary;

    // --- İLİŞKİ 1: KİTAP -> YAZAR ---
    // @ManyToOne: Çok Kitap -> Tek Yazar.
    // fetch = FetchType.EAGER: "Hemen Getir".
    // Bir kitabı veritabanından sorguladığında, Hibernate otomatik olarak gidip Yazar bilgisini de getirir.
    // Çünkü kullanıcı kitabı listelerken yazarını da hemen görmek ister.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false) // Yazar boş olamaz (nullable = false).
    private Author author;

    // --- İLİŞKİ 2: KİTAP -> EKLEYEN ADMİN ---
    // @ManyToOne: Çok Kitap -> Tek Admin (Bir admin çok kitap ekleyebilir).
    // fetch = FetchType.LAZY: "Tembel Getir".
    // Kitap listesini çekerken, kitabı kimin eklediği genelde önemsizdir.
    // O yüzden "Admin bilgisini hemen getirme, ben özel olarak istersem (getAddedByAdmin) getir" diyoruz.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by")

    // @JsonIgnoreProperties: Admin bilgisini JSON'a çevirirken şu alanları görmezden gel:
    // 1. "books": Adminin eklediği diğer kitaplar (Sonsuz döngüyü önler).
    // 2. "password": Güvenlik için adminin şifresini gizle.
    // 3. "hibernateLazyInitializer": LAZY yükleme yaparken oluşan teknik bir hatayı önler.
    @JsonIgnoreProperties({"books", "password", "hibernateLazyInitializer"})
    private Admin addedByAdmin;
}