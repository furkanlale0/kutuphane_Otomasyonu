package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

// @Entity: Spring ve Hibernate'e "Bu sınıf bir veritabanı tablosudur" diyoruz.
// @Table(name = "admins"): Bu sınıfın veritabanındaki karşılığı "admins" isimli tablodur.
@Entity
@Table(name = "admins")
// @Data: Lombok kütüphanesi sayesinde Getter, Setter, toString gibi metodları
// bizim yerimize arka planda otomatik yazar. Kod kalabalığını önler.
@Data
@NoArgsConstructor // Parametresiz boş constructor oluşturur (Hibernate için şarttır).
@AllArgsConstructor // Tüm alanları (id, isim, şifre vs.) içeren dolu constructor oluşturur.
public class Admin {

    // @Id: Bu alanın tablonun "Birincil Anahtarı" (Primary Key) olduğunu belirtir.
    // @GeneratedValue(...IDENTITY): ID'yi biz elle vermeyiz (1, 2, 3...),
    // veritabanı otomatik olarak sırayla arttırır (Auto Increment).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Integer adminId;

    // nullable = false: Bu alan boş bırakılamaz.
    // unique = true: Aynı kullanıcı adından iki tane olamaz (Benzersiz olmalı).
    @Column(nullable = false, unique = true)
    private String username;

    // Şifre alanı boş olamaz. (Burada şifreli/hashli hali tutulur).
    @Column(nullable = false)
    private String password;

    private String name;

    private String surname;

    private String email;

    // --- KRİTİK KISIM: Rol Yönetimi ---
    // @Enumerated(EnumType.STRING): Veritabanına rolü sayı olarak değil (0, 1),
    // yazı olarak ("ADMIN", "SUPERADMIN") kaydetmemizi sağlar. Okunabilirliği artırır.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Role enum sınıfından (ADMIN, SUPERADMIN vs.) değer alır.

    // Kayıt oluşturulma tarihi.
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // --- İLİŞKİ: Bir Admin birden fazla Kitap ekleyebilir ---
    // @OneToMany: Bir Admin -> Çok Kitap (1-N İlişkisi).
    // mappedBy = "addedByAdmin": İlişkinin sahibi burası değil, Book sınıfındaki 'addedByAdmin' alanıdır.
    // fetch = FetchType.LAZY: Admin'i veritabanından çektiğimizde, eklediği 1000 tane kitabı
    // hemen peşinden getirme (Sistem yorulmasın). Sadece biz özellikle istersek getir.
    @OneToMany(mappedBy = "addedByAdmin", fetch = FetchType.LAZY)
    // @JsonIgnore: Admin bilgisini JSON'a çevirirken kitapları dahil etme.
    // Yoksa Admin -> Kitap -> Admin -> Kitap şeklinde sonsuz döngüye girer ve program patlar.
    @JsonIgnore
    private List<Book> books;
}