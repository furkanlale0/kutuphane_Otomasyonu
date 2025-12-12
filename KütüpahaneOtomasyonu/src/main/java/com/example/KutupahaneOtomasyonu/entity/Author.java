package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

// @Entity: Spring'e "Bu sınıf bir veritabanı tablosudur" diyoruz.
// @Table(name = "authors"): Veritabanındaki tablonun adı "authors" olacak.
@Entity
@Table(name = "authors")
// @Data: Lombok sayesinde Getter, Setter, toString metodlarını elle yazmaktan kurtuluyoruz.
@Data
@NoArgsConstructor // Boş constructor (Hibernate için şart)
@AllArgsConstructor // Dolu constructor (Bizim testlerde kullanmamız için)
public class Author {

    // @Id: Bu alan tablonun benzersiz kimlik numarasıdır (Primary Key).
    // @GeneratedValue(...): ID'yi biz vermiyoruz, veritabanı 1, 2, 3 diye otomatik arttırıyor.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Integer authorId;

    private String name;    // Yazarın Adı
    private String surname; // Yazarın Soyadı
    private Integer birthYear; // Doğum Yılı

    // --- KRİTİK NOKTA: Bir Yazarın Çok Kitabı Olur (1-N İlişkisi) ---
    // @OneToMany: Bir Yazar -> Çok Kitap.
    // mappedBy = "author": Bu ilişkinin asıl sahibi Book sınıfındaki 'author' değişkenidir diyoruz.

    // cascade = CascadeType.ALL: BU ÇOK ÖNEMLİ! Eğer veritabanından bu yazarı silersen,
    // Hibernate gider bu yazarın yazdığı TÜM KİTAPLARI da otomatik olarak siler.
    // (Yazarsız kitap olmaz mantığıyla temizlik yapar).

    // fetch = FetchType.LAZY: Yazarı veritabanından çektiğinde (SELECT * FROM authors),
    // hemen gidip kitaplarını da çekme (Sistem yorulmasın). Sadece biz "getBooks()" dersek o zaman git getir.
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    // @JsonIgnore: Yazarı JSON'a çevirirken (Frontend'e gönderirken) kitap listesini dahil ETME.
    // Neden? Çünkü Kitap -> Yazarı tutuyor, Yazar -> Kitabı tutuyor.
    // JSON çevirici bunu görünce sonsuz döngüye girer (Yazar->Kitap->Yazar->Kitap...) ve program patlar.
    @JsonIgnore
    private List<Book> books;
}